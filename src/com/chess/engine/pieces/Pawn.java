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

public class Pawn extends Piece {
    private static final int[] MOVE_COORDINATES = {16,9,8,7};

    private static final int[] ghost_Attack_Moves = {9,7};

    public Pawn(int piecePosition, Alliance pieceAlliance) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, true);
    }

    public Pawn(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegals(final Board board) {
        List<Move> legalMoves = new ArrayList<>();

        for(final int currSpot: MOVE_COORDINATES) {
            int move_Coordinate = this.piecePosition + (this.pieceAlliance.getDirection() * currSpot);

            if (!BoardUtils.isValidTileCoordinate(move_Coordinate)) {
                continue;
            }

            final ChessTile move_Tile = board.getTile(move_Coordinate);

            //Regular move for a pawn
            if (currSpot == 8 && !move_Tile.isOccupied()) {
                if(this.pieceAlliance.isPawnPromotionSquare(move_Coordinate)) {
                    legalMoves.add(new PawnMove(board, this, move_Coordinate));
                } else {
                    legalMoves.add(new PawnMove(board, this, move_Coordinate));
                }

            } else if(this.isFirstMove() && currSpot == 16 && !move_Tile.isOccupied()) {
                final int behindPawnJump = this.piecePosition + (this.pieceAlliance.getDirection() * 8);
                if(!board.getTile(behindPawnJump).isOccupied()) {
                    legalMoves.add(new PawnJump(board, this, move_Coordinate));
                }

            } else if (currSpot == 7) {
                if(move_Tile.isOccupied()) {
                    final Piece pieceAtCoordinate = move_Tile.getPiece();
                    if (this.pieceAlliance != pieceAtCoordinate.pieceAlliance) {
                        if (this.pieceAlliance.isPawnPromotionSquare(move_Coordinate)) {
                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, move_Coordinate, pieceAtCoordinate)));
                        } else {
                            legalMoves.add(new PawnAttackMove(board, this, move_Coordinate, pieceAtCoordinate));
                        }
                    }
                } else if (board.getEnPassantPawn() != null) {
                    if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition + (this.pieceAlliance.getOppositeDirection()))) {
                        final Piece enPassantPawn = board.getEnPassantPawn();
                        if (this.pieceAlliance != enPassantPawn.getAlliance()) {
                            legalMoves.add(new PawnEnPassantAttackMove(board, this, move_Coordinate, enPassantPawn));
                        }
                    }
                }
            //TODO: Put in new method
            } else if (currSpot == 9) {
                if(move_Tile.isOccupied()) {
                    final Piece pieceAtCoordinate = move_Tile.getPiece();
                    if (this.pieceAlliance != pieceAtCoordinate.pieceAlliance && move_Tile.isOccupied()) {
                        if (this.pieceAlliance.isPawnPromotionSquare(move_Coordinate)) {
                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, move_Coordinate, pieceAtCoordinate)));
                        } else {
                            legalMoves.add(new PawnAttackMove(board, this, move_Coordinate, pieceAtCoordinate));
                        }
                    } else if (board.getEnPassantPawn() != null) {
                        if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition - (this.pieceAlliance.getOppositeDirection()))) {
                            final Piece enPassantPawn = board.getEnPassantPawn();
                            if (this.pieceAlliance != enPassantPawn.getAlliance()) {
                                legalMoves.add(new PawnEnPassantAttackMove(board, this, move_Coordinate, enPassantPawn));
                            }
                        }
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    //TODO: To fix bug that GUI displays a king can move into a pawn's attack square
    public Collection<Integer> ghostAttack(final Board board) {
        List<Integer> ghostReturn = new ArrayList<>();
        for(final int currSpot : ghost_Attack_Moves) {
            int move_Coordinate = this.piecePosition + (this.pieceAlliance.getDirection() * currSpot);
            ghostReturn.add(move_Coordinate);
        }
        return ImmutableList.copyOf(ghostReturn);
    }

    @Override
    public String toString() {
        return PieceType.PAWN.toString();
    }

    //Could be optimized
    @Override
    public Pawn movePiece(Move move) {
        return new Pawn(move.getDestinationCoordinate(), move.getMovedPiece().getAlliance(), false);
    }

    public Piece getPromotionPiece() {
        return new Queen(this.piecePosition, this.pieceAlliance, false);
    }
}
