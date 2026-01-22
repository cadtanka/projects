package com.chess.selfplay;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.pgn.FenUtilities;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SelfPlayLogger {

    private static final String OUTPUT_FILE = "selfplay_dataset.jsonl";

    // Clamp + tanh normalization for eval stability
    private static double normalizeEval(double eval) {
        return Math.tanh(eval / 300.0);
    }

    public static void logMoveForML(
            Board board,
            Move move,
            String uci_move,
            double eval,
            int ply,
            int gameId,
            List<String> gameBuffer
    ) {
        String fen = FenUtilities.createFENFromGame(board);
        String alg_move = move.toString();
        String sideToMove = board.getCurrentPlayer().getAlliance().isWhite() ? "w" : "b";
        double normEval = normalizeEval(eval);
        
        if (board.getCurrentPlayer().inStaleMate()) {
            normEval = 0;
        }

        String record = String.format(
                "{\"game_id\": %d, \"ply\": %d, \"fen\": \"%s\", \"side\": \"%s\", \"alg_move\": \"%s\", \"uci_move\": \"%s\", \"value\": %.4f}",
                gameId, ply, fen, sideToMove, alg_move, uci_move, normEval
        );

        gameBuffer.add(record);
    }

    public static void flushGame(
            List<String> gameBuffer,
            Board finalBoard,
            int MAX_PLIES
    ) {
        boolean isCheckmate = finalBoard.getCurrentPlayer().inCheckMate();
        boolean isDraw = finalBoard.getCurrentPlayer().inStaleMate() || gameBuffer.size() >= MAX_PLIES - 1;

        // Winner detection
        // If current player is checkmated → opponent won
        boolean whiteWon = isCheckmate &&
                finalBoard.getCurrentPlayer().getAlliance().isBlack();

        try (FileWriter fw = new FileWriter(OUTPUT_FILE, true)) {

            for (int i = 0; i < gameBuffer.size(); i++) {
                String record = gameBuffer.get(i);

                boolean isWhiteSide = record.contains("\"side\": \"w\"");
                boolean terminal = (i == gameBuffer.size() - 1);

                int outcome;
                if (isDraw) {
                    outcome = 0;
                } else if (whiteWon) {
                    outcome = isWhiteSide ? 1 : -1;
                } else {
                    outcome = isWhiteSide ? -1 : 1;
                }

                StringBuilder sb = new StringBuilder(record);
                sb.deleteCharAt(sb.length() - 1); // remove }

                sb.append(String.format(
                        ", \"outcome\": %d, \"terminal\": %s",
                        outcome,
                        terminal
                ));

                if (terminal) {
                    if (isCheckmate) {
                        sb.append(", \"result\": \"checkmate\"");
                    } else if (isDraw) {
                        sb.append(", \"result\": \"draw\"");
                    }
                }

                sb.append("}\n");
                fw.write(sb.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        gameBuffer.clear();
    }
}
