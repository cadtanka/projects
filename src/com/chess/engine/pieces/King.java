package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.ChessTile;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.MajorAttackMove;
import com.chess.engine.board.Move.MajorMove;
import com.google.common.collect.ImmutableList;

import java.util.*;

import static com.chess.engine.player.Player.calculateAttackOnTile;

public class King extends Piece {

    private static final int[] MOVE_COORDINATES = {7,8,9,1,-1,-7,-8,-9};
//    private final boolean isCastled;
//    private final boolean KingSideCastleCapapable;
//    private final boolean queenSideCastleCapable;

    public King(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.KING, piecePosition, pieceAlliance, true);
    }

    public King(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.KING, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();
        Collection<Move> opponentLegals = new ArrayList<>();
        //MoveStatus moveTransition = MoveStatus.ILLEGAL_MOVE;

        if(board.currentPlayer() != null) {
            opponentLegals = board.currentPlayer().getOpponent().getLegalMoves();
            //final Move move = new MajorMove(board, this, move_Coordinate);
            //moveTransition = board.currentPlayer().makeMove(move).getMoveStatus();
        }

        /*
        TODO: Pawns attacks are still seen as valid move spaces for king
        */
        for (int currSpot : MOVE_COORDINATES) {
            final int move_Coordinate = this.piecePosition + currSpot;

            if (BoardUtils.isValidTileCoordinate(move_Coordinate)) {

                //final Move move = new MajorMove(board, this, move_Coordinate);
                //MoveStatus moveTransition = board.currentPlayer().makeMove(move).getMoveStatus();
                //&&moveTransition.isDone()

                final ChessTile move_Tile = board.getTile(move_Coordinate);

                if (calculateAttackOnTile(move_Coordinate, opponentLegals).isEmpty()) {
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

    private static boolean isFirstColumnExclusion(final int currentCandidate,
                                                  final int candidateDestinationCoordinate) {
        return BoardUtils.FIRST_COLUMN[currentCandidate]
                && ((candidateDestinationCoordinate == -9) || (candidateDestinationCoordinate == -1) ||
                (candidateDestinationCoordinate == 7));
    }

    private static boolean isEighthColumnExclusion(final int currentCandidate,
                                                   final int candidateDestinationCoordinate) {
        return BoardUtils.EIGHTH_COLUMN[currentCandidate]
                && ((candidateDestinationCoordinate == -7) || (candidateDestinationCoordinate == 1) ||
                (candidateDestinationCoordinate == 9));
    }
}
