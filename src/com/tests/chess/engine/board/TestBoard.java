package com.tests.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.MiniMax;
import com.chess.engine.player.ai.MoveStrategy;
import org.junit.jupiter.api.Test;

import static com.chess.engine.board.Board.*;
import static org.junit.jupiter.api.Assertions.*;

class TestBoard {

    @Test
    public void initialBoard() {
        final Board board = createStandardBoard();
        assertEquals(board.getCurrentPlayer().getLegalMoves().size(), 20);
        assertEquals(board.getCurrentPlayer().getOpponent().getLegalMoves().size(), 20);
        assertEquals(board.getCurrentPlayer(), board.whitePlayer());
        assertEquals(board.getCurrentPlayer().getOpponent(), board.blackPlayer());

        assertFalse(board.getCurrentPlayer().inCheck());
        assertFalse(board.getCurrentPlayer().inCheckMate());
        assertFalse(board.getCurrentPlayer().inStaleMate());

        assertFalse(board.getCurrentPlayer().getOpponent().inCheck());
        assertFalse(board.getCurrentPlayer().getOpponent().inCheckMate());
        assertFalse(board.getCurrentPlayer().getOpponent().inStaleMate());

        assertTrue(board.getCurrentPlayer().calculateKingCastles(board.getCurrentPlayer().getLegalMoves()).isEmpty());
        assertTrue(board.getCurrentPlayer().getOpponent().calculateKingCastles(board.getCurrentPlayer().getOpponent().
                getLegalMoves()).isEmpty());
        assertEquals(board.whitePlayer().toString(), "White");
        assertEquals(board.blackPlayer().toString(), "Black");
    }

    @Test
    public void testKing() {
        Builder builder = new Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        assertEquals(board.getCurrentPlayer().getLegalMoves().size(), 5);
        assertEquals(board.getCurrentPlayer().getOpponent().getLegalMoves().size(), 5);

        assertFalse(board.getCurrentPlayer().inCheck());
        assertFalse(board.getCurrentPlayer().getOpponent().inCheck());

        assertFalse(board.getCurrentPlayer().inStaleMate());
        assertFalse(board.getCurrentPlayer().getOpponent().inStaleMate());

        assertFalse(board.getCurrentPlayer().inCheckMate());
        assertFalse(board.getCurrentPlayer().getOpponent().inCheckMate());

        final Move move = Move.MoveFactory.createMove(board, 60, 61);

        final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertTrue(moveTransition.getMoveStatus().isDone());
        assertEquals(moveTransition.getTransitionBoard().getCurrentPlayer().getAlliance(), Alliance.BLACK);
        assertNotEquals(moveTransition.getTransitionBoard().getCurrentPlayer().getAlliance(), Alliance.WHITE);
        assertEquals(moveTransition.getTransitionBoard().getCurrentPlayer().getActivePieces().size(), 1);
        assertEquals(moveTransition.getTransitionBoard().getCurrentPlayer().getOpponent().getActivePieces().size(), 1);

        final Move move1 = Move.MoveFactory.createMove(board, 60, 64);

        final MoveTransition moveTransition1 = board.getCurrentPlayer().makeMove(move1);

        assertFalse(moveTransition1.getMoveStatus().isDone());
        assertNotEquals(moveTransition1.getTransitionBoard().getCurrentPlayer().getAlliance(), Alliance.BLACK);
        assertEquals(moveTransition1.getTransitionBoard().getCurrentPlayer().getAlliance(), Alliance.WHITE);
    }

    @Test
    public void testAlgebraicNotation() {
        assertEquals(BoardUtils.getPositionAtCoordinate(0), "a8");
        assertEquals(BoardUtils.getPositionAtCoordinate(1), "b8");
        assertEquals(BoardUtils.getPositionAtCoordinate(2), "c8");
        assertEquals(BoardUtils.getPositionAtCoordinate(3), "d8");
        assertEquals(BoardUtils.getPositionAtCoordinate(4), "e8");
        assertEquals(BoardUtils.getPositionAtCoordinate(5), "f8");
        assertEquals(BoardUtils.getPositionAtCoordinate(6), "g8");
        assertEquals(BoardUtils.getPositionAtCoordinate(7), "h8");
    }

    @Test
    public void testFoolsMate() {
        final Board board = Board.createStandardBoard();
        Move move1 = Move.MoveFactory.createMove(board, BoardUtils.getCoordinateAtPosition("f2"),
                BoardUtils.getCoordinateAtPosition("f3"));

        final MoveTransition t1 = board.getCurrentPlayer().makeMove(move1);

        assertTrue(t1.getMoveStatus().isDone());

        Move move2 = Move.MoveFactory.createMove(t1.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("e7"),
                BoardUtils.getCoordinateAtPosition("e5"));

        final MoveTransition t2 = t1.getTransitionBoard().getCurrentPlayer().makeMove(move2);

        assertTrue(t2.getMoveStatus().isDone());

        Move move3 = Move.MoveFactory.createMove(t2.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("g2"),
                BoardUtils.getCoordinateAtPosition("g4"));

        final MoveTransition t3 = t2.getTransitionBoard().getCurrentPlayer().makeMove(move3);

        assertTrue(t3.getMoveStatus().isDone());

        final MoveStrategy strategy = new MiniMax(4);

        final Move aiMove = strategy.execute(t3.getTransitionBoard());

        final Move bestMove = Move.MoveFactory.createMove(t3.getTransitionBoard(), BoardUtils.getCoordinateAtPosition("d8"),
                BoardUtils.getCoordinateAtPosition("h4"));

        assertEquals(aiMove, bestMove);

    }
}