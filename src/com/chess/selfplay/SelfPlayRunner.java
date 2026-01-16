package com.chess.selfplay;

import com.chess.engine.board.Board;
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
        int searchDepth = rand.nextInt(1, 4);
        int gamesToPlay = 1;

        for(int g = 1; g <= gamesToPlay; g++) {
            searchDepth = rand.nextInt(1, 4);
            runSingleGame(3, g, boardEvaluator, g);
        }

        System.out.println("Self-play complete.");
    }

    private static void runSingleGame(int depth, int gameNumber, StandardBoardEvaluator evalutator, int game_id) {
        Board board= Board.createStandardBoard();
        MoveStrategy ai = new MiniMax(depth);
        List<String> gameList = new ArrayList<>();

        int ply = 0;

        while (!isGameOver(board, ply)) {
            Move move = ai.execute(board);
            if (move == null) {
                break;
            }

            SelfPlayLogger.logMoveForML(
                board, move,
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


