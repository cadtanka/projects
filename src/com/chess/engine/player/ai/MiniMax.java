package com.chess.engine.player.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    private final Map<Integer, Integer> transpositionTable;
    private static final int MAX_CAHE_SIZE = 100000;

    public MiniMax(final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
        this.transpositionTable = new HashMap<>();
    }

    @Override
    public String toString() {
        return "MiniMax (Alpha-Beta)";
    }

    @Override
    public Move execute(Board board) {
        // final long startTime = System.currentTimeMillis();
        int pieces = board.getCurrentPlayer().getActivePieces().size();
        double epsilon;

        double currentValue;
        List<MoveScore> scoredMoves = new ArrayList<>();

        // System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + searchDepth);

        // int numMoves = board.getCurrentPlayer().getLegalMoves().size();
        // System.out.println("Choosing between  " + numMoves + " moves");

        if (transpositionTable.size() > MAX_CAHE_SIZE) {
            transpositionTable.clear();
        }

        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

            if(moveTransition.moveStatus().isDone()) {
                currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.transitionBoard(), searchDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE) :
                        max(moveTransition.transitionBoard(), searchDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);

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
            return null;
        }
        int K = board.getCurrentPlayer().getActivePieces().size() > 14 ? 3 : 1;
        int limit = Math.min(K, scoredMoves.size());

        // final long executionTime = System.currentTimeMillis() - startTime;

        // System.out.println("Time Elapsed:  " + executionTime);

        return scoredMoves.get(rand.nextInt(limit)).move;
    }

    public int min(final Board board, final int depth, int alpha, int beta) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board,depth);
        }

        int boardHash = board.hashCode();
        if (transpositionTable.containsKey(boardHash)) {
            return transpositionTable.get(boardHash);
        }

        int lowestSeenValue = Integer.MAX_VALUE;

        List<Move> orderedMove = orderMoves(board.getCurrentPlayer().getLegalMoves());

        for(final Move move : orderedMove) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.moveStatus().isDone()) {
                final int currentValue = max(moveTransition.transitionBoard(), depth - 1, alpha, beta);

                if(currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }

                if (lowestSeenValue <= alpha) {
                    break;
                }

                if( lowestSeenValue < beta ) {
                    beta = lowestSeenValue;
                }
            }
        }

        transpositionTable.put(boardHash, lowestSeenValue);

        return lowestSeenValue;
    }

    public int max(final Board board, final int depth, int alpha, int beta) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }

        int boardHash = board.hashCode();
        if (transpositionTable.containsKey(boardHash)) {
            return transpositionTable.get(boardHash);
        }

        int highestSeenValue = Integer.MIN_VALUE;
        List<Move> orderedMove = orderMoves(board.getCurrentPlayer().getLegalMoves());

        for(final Move move : orderedMove) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.moveStatus().isDone()) {
                final int currentValue = min(moveTransition.transitionBoard(), depth - 1, alpha, beta);

                if(currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }

                if(highestSeenValue >= beta) {
                    break;
                }

                if(highestSeenValue > alpha) {
                    alpha = highestSeenValue;
                }
            }
        }

        transpositionTable.put(boardHash, highestSeenValue);

        return highestSeenValue;
    }

    private List<Move> orderMoves(final Iterable<Move> moves) {
        List<Move> captures = new ArrayList<>();
        List<Move> quietMoves = new ArrayList<>();

        for (Move move : moves) {
            if (move.isAttack()) {
                captures.add(move);
            } else {
                quietMoves.add(move);
            }
        }

        captures.addAll(quietMoves);
        return captures;
    }

    private static boolean isEndGameScenario(final Board board) {
        return board.getCurrentPlayer().inCheckMate() ||
                board.getCurrentPlayer().inStaleMate();
    }
}
