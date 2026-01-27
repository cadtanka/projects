import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import json
import chess
import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path

class ChessDataset(Dataset):
  def __init__(self, data_path):
    self.data = []
    with open(data_path, 'r') as f:
      for line in f:
        self.data.append(json.loads(line))

  def __len__(self):
    return len(self.data)

  def __getitem__(self, idx):
    item = self.data[idx]
    board = chess.Board(item['fen'])

    legal_moves = list(board.legal_moves)
    eval_before = item["value"]

    try:
      uci_move = item['uci_move']
      move = chess.Move.from_uci(uci_move)
      move_idx = legal_moves.index(move)
    except Exception as e:
      return self.__getitem__((idx + 1) % len(self.data))

    if idx + 1 < len(self.data):
      next_item = self.data[idx + 1]
      if (next_item["game_id"] == item["game_id"] and
         next_item["ply"] == item["ply"] + 1):
         eval_after = next_item["value"]
      else:
        eval_after = eval_before
    else:
      eval_after = eval_before

    # Convert FEN to input features
    features = self.fen_to_tensor(board)

    move_encoding = self.encode_move(move)

    legal_moves_mask = self.create_legal_moves_mask(legal_moves)

    # Target value (position eval)
    board.push(move)
    eval_before = torch.tensor([eval_before], dtype=torch.float32)
    eval_after = torch.tensor([eval_after], dtype=torch.float32)

    return features, eval_before, eval_after, move_encoding, legal_moves_mask

  def encode_move(self, move):
        """
        Encode move as single integer: from_square * 64 + to_square
        This gives us 4096 possible moves (64 * 64)
        This covers all non-promotion moves properly
        """
        from_square = move.from_square
        to_square = move.to_square

        # Simple encoding: from * 64 + to
        move_index = from_square * 64 + to_square

        return torch.tensor(move_index, dtype=torch.long)

  def create_legal_moves_mask(self, legal_moves):
      """Create mask of legal moves for this position"""
      mask = torch.zeros(4096, dtype=torch.float32)  # 64*64 possible moves

      for move in legal_moves:
          from_square = move.from_square
          to_square = move.to_square
          move_idx = from_square * 64 + to_square
          mask[move_idx] = 1.0

      return mask


  def fen_to_tensor(self, board):
    """ Convert chess board to tensor representation
      Create a 12x8x8 where:
      12 channels: One for each piece type
      8x8: For the chess board
    """

    tensor = torch.zeros(12, 8, 8, dtype=torch.float32)

    piece_to_channel = {
        chess.PAWN: 0, chess.KNIGHT: 1, chess.BISHOP: 2,
        chess.ROOK: 3, chess.QUEEN: 4, chess.KING: 5
    }

    for square in chess.SQUARES:
      piece = board.piece_at(square)
      if piece:
        rank = chess.square_rank(square)
        file = chess.square_file(square)
        channel = piece_to_channel[piece.piece_type]
        if not piece.color:
          channel += 6
        tensor[channel, rank, file] = 1

    return tensor

class ChessNet(nn.Module):
  """Simple CNN for chess position evaluation"""

  def __init__(self, num_moves=4096):
    super().__init__()

    self.conv_layers = nn.Sequential(
        nn.Conv2d(12, 64, kernel_size=3, padding=1),
        nn.BatchNorm2d(64),
        nn.ReLU(),

        nn.Conv2d(64, 128, kernel_size=3, padding=1),
        nn.BatchNorm2d(128),
        nn.ReLU(),

        nn.Conv2d(128, 256, kernel_size=3, padding=1),
        nn.BatchNorm2d(256),
        nn.ReLU()
    )

    self.flatten = nn.Flatten()

    self.shared_fc = nn.Sequential(
        nn.Linear(256 * 8 * 8, 512),
        nn.ReLU(),
        nn.Dropout(0.3)
    )

    # Policy head
    self.policy_head = nn.Sequential(
        nn.Linear(512, 512),
        nn.ReLU(),
        nn.Dropout(0.2),
        nn.Linear(512, num_moves)
    )

    # Value head
    self.value_head = nn.Sequential(
        nn.Linear(512, 128),
        nn.ReLU(),
        nn.Linear(128, 1),
        nn.Tanh()
    )

  def forward(self, x):
    x = self.conv_layers(x)
    x = self.flatten(x)
    x = self.shared_fc(x)

    policy_logits = self.policy_head(x)
    value = self.value_head(x)

    return policy_logits, value
  
class TrainingVisualizer:
  def __init__(self):
      self.train_losses = []
      self.val_losses = []
      self.train_maes = []
      self.val_maes = []
      self.epochs = []
      self.predictions = []
      self.targets = []

  def update(self, epoch, train_loss, val_loss, train_mae, val_mae):
      self.epochs.append(epoch)
      self.train_losses.append(train_loss)
      self.val_losses.append(val_loss)
      self.train_maes.append(train_mae)
      self.val_maes.append(val_mae)

  def add_predictions(self, preds, targets):
      self.predictions = preds
      self.targets = targets

  def plot_loss_curves(self):
      fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

      ax1.plot(self.epochs, self.train_losses, 'b-', label='Train Loss')
      ax1.plot(self.epochs, self.val_losses, 'r-', label='Val Loss')
      ax1.set_xlabel('Epoch')
      ax1.set_ylabel('Loss')
      ax1.set_title('Training and Validation Loss')
      ax1.legend()
      ax1.grid(True)

      ax2.plot(self.epochs, self.train_maes, 'b-', label='Train MAE')
      ax2.plot(self.epochs, self.val_maes, 'r-', label='Val MAE')
      ax2.set_xlabel('Epoch')
      ax2.set_ylabel('MAE')
      ax2.set_title('Training and Validation MAE')
      ax2.legend()
      ax2.grid(True)

      plt.tight_layout()
      Path('visualizations').mkdir(exist_ok=True)
      plt.savefig('visualizations/loss_curves.png')
      print("Saved loss curves to visualizations/loss_curves.png")

  def plot_predictions(self):
      plt.figure(figsize=(10, 10))
      plt.scatter(self.targets, self.predictions, alpha=0.3, s=10)

      min_val = min(min(self.targets), min(self.predictions))
      max_val = max(max(self.targets), max(self.predictions))
      plt.plot([min_val, max_val], [min_val, max_val], 'r--', label='Perfect Prediction')

      plt.xlabel('True Evaluation')
      plt.ylabel('Predicted Evaluation')
      plt.title('Predictions vs True Values')
      plt.legend()
      plt.grid(True)
      plt.savefig('visualizations/predictions.png')
      print("Saved predictions plot to visualizations/predictions.png")

  def plot_error_distribution(self):
      errors = np.array(self.predictions) - np.array(self.targets)

      plt.figure(figsize=(10, 6))
      plt.hist(errors, bins=50, edgecolor='black')
      plt.xlabel('Prediction Error')
      plt.ylabel('Frequency')
      plt.title(f'Error Distribution (Mean: {errors.mean():.4f}, Std: {errors.std():.4f})')
      plt.grid(True)
      plt.savefig('visualizations/error_distribution.png')
      print("Saved error distribution to visualizations/error_distribution.png")

  def print_summary(self):
      errors = np.array(self.predictions) - np.array(self.targets)

      print("\n" + "="*50)
      print("TRAINING SUMMARY")
      print("="*50)
      print(f"Final Train Loss: {self.train_losses[-1]:.6f}")
      print(f"Final Val Loss:   {self.val_losses[-1]:.6f}")
      print(f"Final Train MAE:  {self.train_maes[-1]:.6f}")
      print(f"Final Val MAE:    {self.val_maes[-1]:.6f}")
      print(f"\nPrediction Error Statistics:")
      print(f"  Mean Error:     {errors.mean():.6f}")
      print(f"  Std Dev:        {errors.std():.6f}")
      print(f"  Min Error:      {errors.min():.6f}")
      print(f"  Max Error:      {errors.max():.6f}")
      print(f"  Median Error:   {np.median(errors):.6f}")
      print("="*50)

def train_model(jsonl_path, epochs=5, batch_size=32, learning_rate=0.001):
  device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
  print(f"Current device: {device}")

  # Train visualizer
  viz = TrainingVisualizer()
  dataset = ChessDataset(jsonl_path)
  topk_history = []

  train_size = int(0.8 * len(dataset))
  val_size = len(dataset) - train_size
  train_dataset, val_dataset = torch.utils.data.random_split(dataset, [train_size, val_size])

  train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
  val_loader = DataLoader(val_dataset, batch_size=batch_size)

  model = ChessNet(num_moves=4096).to(device)
  optimizer = optim.Adam(model.parameters(), lr = learning_rate, weight_decay=1e-5)

  scheduler = optim.lr_scheduler.ReduceLROnPlateau(
    optimizer, mode='min', factor=0.5, patience=3
  )

  for epoch in range(epochs):
    model.train()
    train_loss = 0.0
    train_mae = 0.0
    train_value_loss = 0.0
    train_policy_loss = 0.0

    for features, eval_before, eval_after, move_encoding, legal_mask in train_loader:
      features = features.to(device)
      eval_before = eval_before.to(device)
      eval_after = eval_after.to(device)
      move_encoding = move_encoding.to(device)
      legal_mask = legal_mask.to(device)

      optimizer.zero_grad()

      eval_drop = eval_after - eval_before
      weights = torch.where(
          eval_drop < -0.5,
          torch.tensor(2.0, device=device),
          torch.tensor(1.0, device=device)
      )

      policy_logits, value_pred = model(features)

      masked_logits = policy_logits + (legal_mask - 1.0) * 1e9

      ce = nn.CrossEntropyLoss(reduction="none")
      loss_policy = ce(masked_logits, move_encoding)
      loss_policy = (loss_policy * weights).mean()
      loss_policy = (loss_policy * weights).mean()

      loss_value = nn.MSELoss()(value_pred, eval_before)

      loss = loss_value + 0.2 * loss_policy
      loss.backward()

      torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)

      optimizer.step()

      train_loss += loss.item()
      train_value_loss += loss_value.item()
      train_policy_loss += loss_policy.item()
      train_mae += torch.mean(torch.abs(value_pred - eval_before)).item()

    model.eval()
    val_loss = 0.0
    val_mae = 0.0
    val_preds = []
    val_targets = []
    epoch_topk_acc = 0.0
    epoch_policy_acc = 0.0
    K = 3

    with torch.no_grad():
      for features, eval_before, eval_after, move_encoding, legal_mask in val_loader:
        features = features.to(device)
        eval_before = eval_before.to(device)
        move_encoding = move_encoding.to(device)
        legal_mask = legal_mask.to(device)

        policy_logits, value_pred = model(features)

        masked_logits = policy_logits + (legal_mask - 1.0) * 1e9 # Added this line

        # Top-k accuracy
        topk = torch.topk(masked_logits, k=K, dim=1).indices
        topk_correct = (topk == move_encoding.view(-1, 1)).any(dim=1).float().mean().item()
        epoch_topk_acc += topk_correct

        # Top-1 policy accuracy
        pred_moves = torch.argmax(masked_logits, dim=1)
        policy_acc = (pred_moves == move_encoding).float().mean().item()
        epoch_policy_acc += policy_acc

        ce = nn.CrossEntropyLoss()
        loss_policy = ce(masked_logits, move_encoding)
        loss_value = nn.MSELoss()(value_pred, eval_before)

        val_loss += (loss_value.item() + 0.3 * loss_policy.item())
        val_mae += torch.mean(torch.abs(value_pred - eval_before)).item()

        val_preds.extend(value_pred.cpu().numpy().flatten())
        val_targets.extend(eval_before.cpu().numpy().flatten())

    # Update visualizer
    avg_train_loss = train_loss / len(train_loader)
    avg_val_loss = val_loss / len(val_loader)
    avg_train_mae = train_mae / len(train_loader)
    avg_val_mae = val_mae / len(val_loader)
    avg_topk_acc = epoch_topk_acc / len(val_loader)
    avg_policy_acc = epoch_policy_acc / len(val_loader)

    viz.update(epoch + 1, avg_train_loss, avg_val_loss, avg_train_mae, avg_val_mae)

    print(f"Epoch {epoch + 1}/{epochs}")
    print(f"  Train Loss: {avg_train_loss:.6f}   Val Loss: {avg_val_loss:.6f}")
    print(f"  Val MAE: {avg_val_mae:.6f}")
    print(f"  Top-{K} Acc: {avg_topk_acc:.4f}   Top-1 Acc: {avg_policy_acc:.4f}")

    topk_history.append(avg_topk_acc)

    scheduler.step(avg_val_loss)

  # Final predictions for all validation data
  viz.add_predictions(val_preds, val_targets)

  # Final visualizations
  viz.add_predictions(val_preds, val_targets)
  viz.plot_loss_curves()
  viz.plot_predictions()
  viz.plot_error_distribution()
  viz.print_summary()

  plt.figure(figsize=(10, 6))
  plt.plot(topk_history)
  plt.title("Top-K Accuracy Over Epochs")
  plt.xlabel("Epoch")
  plt.ylabel(f"Top-{K} Accuracy")
  plt.grid(True)
  plt.savefig('visualizations/topk_accuracy.png')
  plt.show()

  return model

if __name__ == "__main__":
  model = train_model('/content/selfplay_dataset.jsonl', epochs=10, batch_size=16, learning_rate=0.001)
  torch.save(model.state_dict(), 'chess_model_pth')
  print("Model saved to chess_model.pth")