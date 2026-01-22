package com.chess.selfplay;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.player.ai.MiniMax;
import com.chess.engine.player.ai.MoveStrategy;
import com.chess.engine.player.ai.StandardBoardEvaluator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelfPlayRunner {
    private static final int MAX_PLIES = 300;
    private static final StandardBoardEvaluator boardEvaluator = new StandardBoardEvaluator();

    public static void main(String[] args) {
        Random rand = new Random();
        int gamesToPlay = 256;

        for(int g = 1; g <= gamesToPlay; g++) {
            System.out.println("Starting game " + g);
            int searchDepth = rand.nextInt(4, 6);
            runSingleGame(searchDepth, g, boardEvaluator, g);
            System.out.println("Depth " + searchDepth);
        }

        System.out.println("Self-play complete");
    }

    private static void runSingleGame(int depth, int gameNumber, StandardBoardEvaluator evalutator, int game_id) {
        Board board = Board.createStandardBoard();
        MoveStrategy ai = new MiniMax(depth);
        List<String> gameList = new ArrayList<>();

        int ply = 0;

        while (!isGameOver(board, ply)) {
            Move move = ai.execute(board);

            if (move == null) {
                System.out.println("AI returned null move at ply " + ply);
                System.out.println("Game state - Checkmate: " + board.getCurrentPlayer().inCheckMate() 
                                + ", Stalemate: " + board.getCurrentPlayer().inStaleMate());
                System.out.println(board.getCurrentPlayer().getLegalMoves());
                break; // End the game
            }

            String uci_Move = String.valueOf(BoardUtils.getPositionAtCoordinate(move.getCurrentCoordinate())) + String.valueOf(BoardUtils.getPositionAtCoordinate(move.getDestinationCoordinate()));
            SelfPlayLogger.logMoveForML(
                board, move, uci_Move,
                evalutator.evaluate(board, 3),
                ply, game_id,
                gameList
            );

            board = board.getCurrentPlayer().makeMove(move).transitionBoard();
            ply++;
        }

        SelfPlayLogger.flushGame(gameList, board, MAX_PLIES);
        System.out.println("Game " + gameNumber + " finished in " + ply + " plies.");
    }

    private static boolean isGameOver(Board board, int ply) {
        return board.getCurrentPlayer().inCheckMate()
                || board.getCurrentPlayer().inStaleMate()
                || ply >= MAX_PLIES;
    }
}


