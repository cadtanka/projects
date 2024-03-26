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

public class Queen extends Piece {


    public Queen(int piecePosition, Alliance pieceAlliance) {
        super(PieceType.QUEEN, piecePosition, pieceAlliance, true);
    }

    public Queen(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.KING, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegals(final Board board) {
        List<Move> legalMoves = new ArrayList<>();
        List<Integer> moveCoordinates = new ArrayList<>();

        BoardUtils.getColumn(piecePosition, moveCoordinates, board);
        BoardUtils.getRow(piecePosition, moveCoordinates, board);
        BoardUtils.getDiagonalFromPiece(piecePosition, moveCoordinates, board);

        for(final int currSpot : moveCoordinates) {
            if(BoardUtils.isValidTileCoordinate(currSpot)) {
                final ChessTile move_Tile = board.getTile(currSpot);

                if(!move_Tile.isOccupied()) {
                    legalMoves.add(new MajorMove(board, this, currSpot));
                } else {
                    final Piece pieceAtCoordinate = move_Tile.getPiece();
                    final Alliance pieceAlliance = move_Tile.getPiece().getAlliance();

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
        return PieceType.QUEEN.toString();
    }

    //Could be optimized
    @Override
    public Queen movePiece(Move move) {
        return new Queen(move.getDestinationCoordinate(), move.getMovedPiece().getAlliance());
    }
}
