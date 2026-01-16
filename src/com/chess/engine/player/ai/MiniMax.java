package com.chess.engine.player.ai;

import java.util.ArrayList;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import java.util.List;
import java.util.Random;

class MoveScore {
    final Move move;
    final double score;

    MoveScore(Move move, double score) {
        this.move = move;
        this.score = score;
    }
}

public class MiniMax implements MoveStrategy {

    private final Random rand = new Random();
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
        int pieces = board.getCurrentPlayer().getActivePieces().size();
        double epsilon;

        double currentValue;
        List<MoveScore> scoredMoves = new ArrayList<>();

        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + searchDepth);

        int numMoves = board.getCurrentPlayer().getLegalMoves().size();

        System.out.println("Choosing between  " + numMoves + " moves");

        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

            if(moveTransition.moveStatus().isDone()) {
                currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.transitionBoard(), searchDepth - 1) :
                        max(moveTransition.transitionBoard(), searchDepth - 1);

                // Add in some noise for more diverse games
                if (pieces > 14) {
                    epsilon = 30.0;
                } else if (pieces > 8) {
                    epsilon = 10.0;
                } else {
                    epsilon = 2.0;
                }

                currentValue += rand.nextGaussian() * epsilon;
                scoredMoves.add(new MoveScore(move, currentValue));
            }
        }

        scoredMoves.sort((a, b) ->
                    board.getCurrentPlayer().getAlliance().isWhite()
                        ? Double.compare(b.score, a.score)
                        : Double.compare(a.score, b.score)
            );

        if (scoredMoves.isEmpty()) {
            System.out.println("Scored Moves empty");
            // No scored moves, fallback to first legal move
            System.out.println(board.getCurrentPlayer().getLegalMoves().size());
            return null;
        }
        int K = board.getCurrentPlayer().getActivePieces().size() > 14 ? 3 : 1;
        int limit = Math.min(K, scoredMoves.size());

        final long executionTime = System.currentTimeMillis() - startTime;

        System.out.println("Time Elapsed:  " + executionTime);

        return scoredMoves.get(rand.nextInt(limit)).move;
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
