package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.ChessTile;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.MajorAttackMove;
import com.chess.engine.board.Move.MajorMove;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class King extends Piece {
    public King(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.KING, piecePosition, pieceAlliance, true);
    }

    public King(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.KING, piecePosition, pieceAlliance, isFirstMove);
    }
    private static final int[] MOVE_COORDINATES = {7,8,9,1,2,-7,-8,-9};

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();
        /*
        TODO: In check not coded yet
        TODO: Castling moves
        */
        for(int currSpot: MOVE_COORDINATES) {
            final int move_Coordinate = this.piecePosition + currSpot;

            if(BoardUtils.isValidTileCoordinate(move_Coordinate)) {
                final ChessTile move_Tile = board.getTile(move_Coordinate);

                if(!move_Tile.isOccupied()) {
                    legalMoves.add(new MajorMove(board, this, move_Coordinate));
                } else {
                    final Piece pieceAtCoordinate = move_Tile.getPiece();
                    final Alliance pieceAlliance = pieceAtCoordinate.getAlliance();

                    if(this.pieceAlliance != pieceAlliance) {
                        legalMoves.add(new MajorAttackMove(board, this, move_Coordinate, pieceAtCoordinate));
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public String toString() {
        return PieceType.KING.toString();
    }

    //Could be optimized
    @Override
    public King movePiece(Move move) {
        return new King(move.getDestinationCoordinate(), move.getMovedPiece().getAlliance());
    }
}
