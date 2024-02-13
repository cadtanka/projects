package com.tests.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.player.MoveTransition;
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
}