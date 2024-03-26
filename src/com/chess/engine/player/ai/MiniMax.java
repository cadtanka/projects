package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

public class MiniMax implements MoveStrategy {

    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;

    public MiniMax(final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
    }

    @Override
    public String toString() {
        return "MiniMax";
    }

    @Override
    public Move execute(Board board) {
        final long startTime = System.currentTimeMillis();

        Move bestMove = null;

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + searchDepth);

        int numMoves = board.getCurrentPlayer().getLegalMoves().size();

        System.out.println("Choosing between  " + numMoves + " moves");

        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

            if(moveTransition.moveStatus().isDone()) {
                currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.transitionBoard(), searchDepth - 1) :
                        max(moveTransition.transitionBoard(), searchDepth - 1);

                if(board.getCurrentPlayer().getAlliance().isWhite() && currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                } else if(board.getCurrentPlayer().getAlliance().isBlack() && currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;

        System.out.println("Time Elapsed:  " + executionTime);


        return bestMove;
    }

    public int min(final Board board, final int depth) {
        if(depth == 0) {
            return this.boardEvaluator.evaluate(board,depth);
        }

        int lowestSeenValue = Integer.MAX_VALUE;
        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.moveStatus().isDone()) {
                final int currentValue = max(moveTransition.transitionBoard(), depth - 1);
                if(currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
            }
        }
        return lowestSeenValue;
    }

    public int max(final Board board, final int depth) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board,depth);
        }

        int highestSeenValue = Integer.MIN_VALUE;
        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.moveStatus().isDone()) {
                final int currentValue = min(moveTransition.transitionBoard(), depth - 1);
                if(currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
            }
        }
        return highestSeenValue;
    }

    private static boolean isEndGameScenario(final Board board) {
        return board.getCurrentPlayer().inCheckMate() ||
                board.getCurrentPlayer().inStaleMate();
    }
}
