from flask import Flask, request, jsonify
import torch
import chess
import numpy as np

from training_model import ChessNet, ChessDataset

app = Flask(__name__)

# Load our trained model
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = ChessNet(num_moves=4096).to(device)
model.load_state_dict(torch.load("chess_model_pth", map_location=device))
model.eval()

# Helpers
def fen_to_tensor(board):
    """Use your dataset's fen_to_tensor method"""
    from training_model import ChessDataset
    return ChessDataset.fen_to_tensor(None, board).unsqueeze(0).to(device)

def move_to_index(move, board):
    return move.from_square * 64 + move.to_square

def index_to_move(idx, board):
    """Convert integer index back to legal move"""
    from_square = idx // 64
    to_square = idx % 64
    move = chess.Move(from_square, to_square)
    
    if move in board.legal_moves:
        return move
    else:
        return list(board.legal_moves)[0]
    
def ml_select_move(board: chess.Board):
    features = fen_to_tensor(board)
    with torch.no_grad():
        policy_logits, _ = model(features)
        legal_mask = torch.zeros(1, 4096, device=device)
        for move in board.legal_moves:
            legal_mask[0, move_to_index(move, board)] = 1.0
        masked_logits = policy_logits + (legal_mask - 1.0) * 1e9
        move_idx = torch.argmax(masked_logits, dim=1).item()
    return index_to_move(move_idx, board)

# REST endpoint
@app.route("/ml-move", methods=["POST"])
def ml_move():
    data = request.json
    fen = data["fen"]
    board = chess.Board(fen)
    move = ml_select_move(board)
    return jsonify({"move": move.uci()})

if __name__ == "__main__":
    app.run(port=5000)