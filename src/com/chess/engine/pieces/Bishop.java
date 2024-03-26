package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.ChessTile;
import com.chess.engine.board.Move;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.Move.*;

//The Bishop Piece
public class Bishop extends Piece {
    public Bishop(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.BISHOP, piecePosition, pieceAlliance, true);
    }

// --Commented out by Inspection START (3/2/24, 11:45 AM):
//    public Bishop(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
//        super(PieceType.BISHOP, piecePosition, pieceAlliance, isFirstMove);
//    }
// --Commented out by Inspection STOP (3/2/24, 11:45 AM)

    @Override
    public Collection<Move> calculateLegals(final Board board) {
        List<Move> legalMoves = new ArrayList<>();

        List<Integer> moveCoordinates = new ArrayList<>();

        //Finds all possible moves of a bishop, regardless of other pieces
        BoardUtils.getDiagonalFromPiece(this.piecePosition, moveCoordinates, board);

        for(final int currSpot : moveCoordinates) {

            if(BoardUtils.isValidTileCoordinate(currSpot)) {
                final ChessTile move_Tile = board.getTile(currSpot);

                if(!move_Tile.isOccupied()) {
                    legalMoves.add(new MajorMove(board, this, currSpot));
                } else {
                    final Piece pieceAtCoordinate = move_Tile.getPiece();
                    final Alliance pieceAlliance = pieceAtCoordinate.getAlliance();

                    if(this.pieceAlliance != pieceAlliance) {
                        legalMoves.add(new MajorAttackMove(board, this, currSpot, pieceAtCoordinate));
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    //Could be optimized
    @Override
    public Bishop movePiece(Move move) {
        return new Bishop(move.getDestinationCoordinate(), move.getMovedPiece().getAlliance());
    }

    @Override
    public String toString() {
        return PieceType.BISHOP.toString();
    }
}
