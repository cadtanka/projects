package com.tests.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.*;
import com.chess.engine.player.MoveTransition;
import org.junit.jupiter.api.Test;

import static com.chess.engine.board.Move.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    @Test
    public void inCheck() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Queen(16, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        assertFalse(board.getCurrentPlayer().getOpponent().inCheck());
        assertFalse(board.getCurrentPlayer().getOpponent().inCheckMate());
        assertFalse(board.getCurrentPlayer().getOpponent().inStaleMate());
        assertFalse(board.getCurrentPlayer().inCheck());

        final Move move = MoveFactory.createMove(board,16,20);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertTrue(moveTransition.getTransitionBoard().getCurrentPlayer().inCheck());
        assertFalse(moveTransition.getTransitionBoard().getCurrentPlayer().getOpponent().inCheck());

        final Move move1 = MoveFactory.createMove(moveTransition.getTransitionBoard(), 20, 8);
        MoveTransition moveTransition1 = board.getCurrentPlayer().makeMove(move1);

        assertFalse(moveTransition1.getTransitionBoard().getCurrentPlayer().inCheck());
        assertFalse(moveTransition1.getTransitionBoard().getCurrentPlayer().getOpponent().inCheck());
    }

    @Test
    public void inStaleMate() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Knight(20, Alliance.WHITE));
        builder.setPiece(new Rook(8, Alliance.WHITE));

        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 8, 9);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertTrue(moveTransition.getTransitionBoard().getCurrentPlayer().inStaleMate());
    }

    @Test
    public void notInStaleMate() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(56, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Queen(40, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        assertFalse(board.getCurrentPlayer().getOpponent().inStaleMate());
    }

    @Test
    public void inCheckMate() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(56, Alliance.BLACK, false,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, false, false, false));
        builder.setPiece(new Rook(1, Alliance.WHITE));
        builder.setPiece(new Queen(50, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        assertFalse(board.getCurrentPlayer().getOpponent().inCheckMate());

        Move move = MoveFactory.createMove(board, 50, 49);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertTrue(moveTransition.getTransitionBoard().getCurrentPlayer().inCheckMate());
    }

    @Test
    public void revealedCheck() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(56, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Bishop(52, Alliance.WHITE));
        builder.setPiece(new Queen(12, Alliance.BLACK));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 52, 43);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertFalse(moveTransition.getMoveStatus().isDone());
    }

    @Test
    public void moveIntoCheck() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(56, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Bishop(52, Alliance.WHITE));
        builder.setPiece(new Queen(11, Alliance.BLACK));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 60, 59);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertFalse(moveTransition.getMoveStatus().isDone());
    }

    @Test
    public void invalidQueenSideCastlePieceInWay() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Bishop(57, Alliance.WHITE));
        builder.setPiece(new Rook(56, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 60, 58);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertFalse(moveTransition.getMoveStatus().isDone());
        assertTrue(board.getCurrentPlayer().calculateKingCastles(board.getCurrentPlayer().getOpponent().getLegalMoves()).isEmpty());
    }

    @Test
    public void invalidQueenSideCastlePlaceUnderAttack() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Rook(3, Alliance.BLACK));
        builder.setPiece(new Rook(56, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 60, 58);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertFalse(moveTransition.getMoveStatus().isDone());
        assertTrue(board.getCurrentPlayer().calculateKingCastles(board.getCurrentPlayer().getOpponent().getLegalMoves()).isEmpty());
    }

    @Test
    public void invalidKingSideCastlePieceInWay() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Bishop(61, Alliance.WHITE));
        builder.setPiece(new Rook(63, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 60, 62);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertFalse(moveTransition.getMoveStatus().isDone());
        assertTrue(board.getCurrentPlayer().calculateKingCastles(board.getCurrentPlayer().getOpponent().getLegalMoves()).isEmpty());
    }

    @Test
    public void invalidKingSideCastleUnderAttack() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Rook(6, Alliance.BLACK));
        builder.setPiece(new Rook(63, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 60, 62);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertFalse(moveTransition.getMoveStatus().isDone());
        assertTrue(board.getCurrentPlayer().calculateKingCastles(board.getCurrentPlayer().getOpponent().getLegalMoves()).isEmpty());
    }

    @Test
    public void validKingSideCastle() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Rook(7, Alliance.BLACK));
        builder.setPiece(new Rook(63, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 60, 62);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertTrue(moveTransition.getMoveStatus().isDone());
        assertFalse(board.getCurrentPlayer().calculateKingCastles(board.getCurrentPlayer().getOpponent().getLegalMoves()).isEmpty());
    }

    @Test
    public void validQueenSideCastle() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Rook(1, Alliance.BLACK));
        builder.setPiece(new Rook(56, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 60, 58);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        assertTrue(moveTransition.getMoveStatus().isDone());
        assertFalse(board.getCurrentPlayer().calculateKingCastles(board.getCurrentPlayer().getOpponent().getLegalMoves()).isEmpty());
    }

    @Test
    public void pawnEnPassant9Steps() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Pawn(32, Alliance.BLACK));
        builder.setPiece(new Pawn(49, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 49, 33);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        Move move1 = MoveFactory.createMove(moveTransition.getTransitionBoard(), 32, 41);
        MoveTransition moveTransition1 = moveTransition.getTransitionBoard().getCurrentPlayer().makeMove(move1);
        assertTrue(moveTransition1.getMoveStatus().isDone());
    }

    @Test
    public void pawnEnPassant7Steps() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Pawn(37, Alliance.BLACK));
        builder.setPiece(new Pawn(52, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 52, 36);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);

        Move move1 = MoveFactory.createMove(moveTransition.getTransitionBoard(), 37, 44);
        MoveTransition moveTransition1 = moveTransition.getTransitionBoard().getCurrentPlayer().makeMove(move1);
        assertTrue(moveTransition1.getMoveStatus().isDone());
    }


    @Test
    public void pawnJump() {
        Board.Builder builder = new Board.Builder();

        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Pawn(48, Alliance.WHITE));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        Move move = MoveFactory.createMove(board, 48, 32);
        MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
        assertTrue(moveTransition.getMoveStatus().isDone());

        Move move1 = MoveFactory.createMove(moveTransition.getTransitionBoard(), 32, 16);
        MoveTransition moveTransition1 = board.getCurrentPlayer().getOpponent().makeMove(move1);
        assertFalse(moveTransition1.getMoveStatus().isDone());
    }

    @Test
    public void pawnEdgeCase1() {
        Board.Builder builder = new Board.Builder();
        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Pawn(24, Alliance.BLACK, false));
        builder.setPiece(new Pawn(31, Alliance.WHITE, false));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        assertEquals(6, board.getCurrentPlayer().getLegalMoves().size());
        assertEquals(6, board.getCurrentPlayer().getOpponent().getLegalMoves().size());
    }

    @Test
    public void pawnEdgeCase2() {
        Board.Builder builder = new Board.Builder();
        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Pawn(7, Alliance.BLACK, false));
        builder.setPiece(new Pawn(16, Alliance.WHITE, false));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        assertEquals(6, board.getCurrentPlayer().getLegalMoves().size());
        assertEquals(6, board.getCurrentPlayer().getOpponent().getLegalMoves().size());

    }

    @Test
    public void pawnEdgeCase3() {
        Board.Builder builder = new Board.Builder();
        builder.setPiece(new King(4, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Pawn(24, Alliance.BLACK, false));
        builder.setPiece(new Pawn(31, Alliance.WHITE, false));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        assertEquals(6,board.getCurrentPlayer().getLegalMoves().size());
        assertEquals(6,board.getCurrentPlayer().getOpponent().getLegalMoves().size());

    }

    @Test
    public void kingEdgeCase() {
        Board.Builder builder = new Board.Builder();
        builder.setPiece(new King(16, Alliance.BLACK, true,false, false));
        builder.setPiece(new King(39, Alliance.WHITE, true, false, false));
        builder.setPiece(new Pawn(23, Alliance.BLACK));
        builder.setMoveMaker(Alliance.WHITE);

        Board board = builder.build();

        assertEquals(5, board.getCurrentPlayer().getLegalMoves().size());
        assertEquals(6,board.getCurrentPlayer().getOpponent().getLegalMoves().size());

    }

}