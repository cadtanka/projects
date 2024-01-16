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

//The Rook Piece
public class Rook extends Piece {

    public Rook(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.ROOK, piecePosition, pieceAlliance, true);
    }

    public Rook (final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.ROOK, piecePosition, pieceAlliance, isFirstMove);
    }
    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {

        List<Move> legalMoves = new ArrayList<>();
        List<Integer> moveCoordinates = new ArrayList<>();

        BoardUtils.getRow(piecePosition, moveCoordinates, board);
        BoardUtils.getColumn(piecePosition, moveCoordinates, board);

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

    @Override
    public String toString() {
        return PieceType.ROOK.toString();
    }

    //Could be optimized
    @Override
    public Rook movePiece(Move move) {
        return new Rook(move.getDestinationCoordinate(), move.getMovedPiece().getAlliance());
    }
}
