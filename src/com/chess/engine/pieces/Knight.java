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

//The knight piece of a chess game
public class Knight extends Piece {

    private static final int[] MOVE_COORDINATES = {-6, -10, -15, -17, 6, 10, 15, 17};

    public Knight(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.KNIGHT, piecePosition, pieceAlliance, true);
    }

    public Knight(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.KNIGHT, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {

        List<Move> legalMoves = new ArrayList<>();

        for (final int currSpot : MOVE_COORDINATES) {
            int move_Coordinate = this.piecePosition + currSpot;

            if (BoardUtils.isValidTileCoordinate(move_Coordinate)) {

                if (firstColumnEdgeCase(this.piecePosition, currSpot) || secondColumnEdgeCase(this.piecePosition, currSpot)||
                        seventhColumnEdgeCase(this.piecePosition, currSpot) || eighthColumnEdgeCase(this.piecePosition, currSpot)) {
                    continue;
                }

                final ChessTile move_Tile = board.getTile(move_Coordinate);

                if (!move_Tile.isOccupied()) {
                    legalMoves.add(new MajorMove(board, this, move_Coordinate));
                } else {
                    final Piece pieceAtCoordinate = move_Tile.getPiece();
                    final Alliance pieceAlliance = pieceAtCoordinate.getAlliance();

                    if (this.pieceAlliance != pieceAlliance) {
                        legalMoves.add(new MajorAttackMove(board, this, move_Coordinate, pieceAtCoordinate));
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    private static boolean firstColumnEdgeCase(final int currPosition, final int candidateOffset) {
        return BoardUtils.FIRST_COLUMN[currPosition] && ((candidateOffset == -17) || (candidateOffset == -10)
                || (candidateOffset == 6) || (candidateOffset == 15));
    }

    private static boolean secondColumnEdgeCase(final int currPosition, final int candidateOffset) {
        return BoardUtils.SECOND_COLUMN[currPosition] && ((candidateOffset == -10 || candidateOffset == 6));
    }

    private static boolean seventhColumnEdgeCase(final int currPosition, final int candidateOffset) {
        return BoardUtils.SEVENTH_COLUMN[currPosition] && ((candidateOffset == -6 || candidateOffset == 10));
    }

    private static boolean eighthColumnEdgeCase(final int currPosition, final int candidateOffset) {
        return BoardUtils.EIGHTH_COLUMN[currPosition] && ((candidateOffset == -15) || candidateOffset == -6
                || candidateOffset == 10 || candidateOffset == 17);
    }

    @Override
    public String toString() {
        return PieceType.KNIGHT.toString();
    }

    //Could be optimized
    @Override
    public Knight movePiece(Move move) {
        return new Knight(move.getDestinationCoordinate(), move.getMovedPiece().getAlliance());
    }
}
