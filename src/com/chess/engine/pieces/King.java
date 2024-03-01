package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.ChessTile;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.MajorAttackMove;
import com.chess.engine.board.Move.MajorMove;
import com.chess.engine.player.MoveStatus;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.Player;
import com.google.common.collect.ImmutableList;

import java.util.*;

import static com.chess.engine.board.BoardUtils.isKingPawnTrap;
import static com.chess.engine.player.Player.calculateAttackOnTile;

public class King extends Piece {

    private static final int[] MOVE_COORDINATES = {7,8,9,1,-1,-7,-8,-9};
    private final boolean isCastled;
    private final boolean kingSideCastleCapable;
    private final boolean queenSideCastleCapable;

    private final Alliance pieceAlliance;

    private final int piecePosition;

    public King(final int piecePosition, final Alliance pieceAlliance,
                final boolean kingSideCastleCapable, final boolean queenSideCastleCapable) {
        super(PieceType.KING, piecePosition, pieceAlliance, true);
        this.isCastled = false;
        this.kingSideCastleCapable = kingSideCastleCapable;
        this.queenSideCastleCapable = queenSideCastleCapable;
        this.pieceAlliance = pieceAlliance;
        this.piecePosition = piecePosition;
    }

    public King(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove,
                final boolean kingSideCastleCapable, final boolean queenSideCastleCapable) {
        super(PieceType.KING, piecePosition, pieceAlliance, isFirstMove);
        this.isCastled = false;
        this.kingSideCastleCapable = kingSideCastleCapable;
        this.queenSideCastleCapable = queenSideCastleCapable;
        this.pieceAlliance = pieceAlliance;
        this.piecePosition = piecePosition;
    }

    @Override
    public Collection<Move> calculateLegals(final Board board) {

        List<Move> legalMoves = new ArrayList<>();
        Collection<Move> opponentLegals = new ArrayList<>();

        if(board.getCurrentPlayer() != null) {
            opponentLegals = board.getCurrentPlayer().getOpponent().getLegalMoves();
        }

        for (int currSpot : MOVE_COORDINATES) {

            if(isFirstColumnExclusion(this.piecePosition, currSpot) || isEighthColumnExclusion(this.piecePosition, currSpot)) {
                continue;
            }

            final int move_Coordinate = this.piecePosition + currSpot;
            Move move = new MajorMove(board, this, move_Coordinate);

            if(board.getPawnGhostMoves() != null && board.getPawnGhostMoves().contains(move_Coordinate)) {
                continue;
            }

            if (BoardUtils.isValidTileCoordinate(move_Coordinate)) {

                final ChessTile move_Tile = board.getTile(move_Coordinate);

                if (calculateAttackOnTile(move_Coordinate, opponentLegals).isEmpty()) {
                    if (!move_Tile.isOccupied()) {
                        legalMoves.add(move);
                    } else {
                        final Piece pieceAtCoordinate = move_Tile.getPiece();
                        final Alliance pieceAllianceAtSpot = pieceAtCoordinate.getAlliance();

                        if (this.pieceAlliance != pieceAllianceAtSpot) {
                            legalMoves.add(new MajorAttackMove(board, this, move_Coordinate, pieceAtCoordinate));
                        }
                    }
                }
            }
        }

        if(board.getCurrentPlayer() != null) {
            if (board.getCurrentPlayer().getAlliance().isWhite()) {
                legalMoves.addAll(calculateKingCastlesWhite(opponentLegals, board));
            } else if (board.getCurrentPlayer().getAlliance().isBlack()) {
                legalMoves.addAll(calculateKingCastlesBlack(opponentLegals, board));
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    private boolean moveIntoCheck(Move move, Board board) {
        Move moveBack = new MajorMove(board, move.getMovedPiece(), move.getDestinationCoordinate());

        if(board.getCurrentPlayer() != null) {
            final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
            if(transition.getMoveStatus().isDone()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public Collection<Move> calculateKingCastlesWhite(Collection<Move> opponentLegals, Board board) {
        final List<Move> kingCastles = new ArrayList<>();
        opponentLegals = board.getCurrentPlayer().getOpponent().getLegalMoves();

        if (this.isFirstMove && calculateAttackOnTile(this.piecePosition, opponentLegals).isEmpty()) {
            //Specific to white king side castle
            if (board.getTile(61).getPiece() == null && board.getTile(62).getPiece() == null) {
                final Piece rookPiece = board.getTile(63).getPiece();

                if (rookPiece != null && rookPiece.isFirstMove()) {
                    if (Player.calculateAttackOnTile(61, opponentLegals).isEmpty() &&
                            Player.calculateAttackOnTile(62, opponentLegals).isEmpty() &&
                            rookPiece.getPieceType() == PieceType.ROOK) {
                        if (!isKingPawnTrap(board, this, 52)) {
                            //Adds castling moves
                            kingCastles.add(new Move.KingSideCastleMove(board, this, 62,
                                    (Rook) rookPiece, rookPiece.getPiecePosition(), 61));
                        }
                    }
                }
            }

            //Check to see if castle spaces are empty
            if (!board.getTile(59).isOccupied() &&
                    !board.getTile(58).isOccupied() &&
                    !board.getTile(57).isOccupied()) {
                final ChessTile rookTile = board.getTile(56);

                if (rookTile.isOccupied() && rookTile.getPiece().isFirstMove() &&
                        Player.calculateAttackOnTile(58, opponentLegals).isEmpty() &&
                        Player.calculateAttackOnTile(59, opponentLegals).isEmpty() &&
                        rookTile.getPiece().getPieceType().isRook()) {
                    if (!isKingPawnTrap(board, this, 52)) {
                        kingCastles.add(new Move.QueenSideCastleMove(board, this, 58,
                                (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 59));

                    }
                }
            }
        }
        return ImmutableList.copyOf(kingCastles);
    }

    //TODO: GUI DOESNT SHOW KING CAN MOVE INFRONT OF PAWNS

    public Collection<Move> calculateKingCastlesBlack(Collection<Move> opponentLegals, Board board) {
        final List<Move> kingCastles = new ArrayList<>();
        opponentLegals = board.getCurrentPlayer().getOpponent().getLegalMoves();

        if (this.isFirstMove && calculateAttackOnTile(this.piecePosition, opponentLegals).isEmpty()) {
            //Specific to white king side castle
            if (board.getTile(5).getPiece() == null && board.getTile(6).getPiece() == null) {
                final Piece rookPiece = board.getTile(7).getPiece();

                if (rookPiece != null && rookPiece.isFirstMove()) {
                    if (Player.calculateAttackOnTile(5, opponentLegals).isEmpty() &&
                            Player.calculateAttackOnTile(6, opponentLegals).isEmpty() &&
                            rookPiece.getPieceType() == PieceType.ROOK) {
                        if (!isKingPawnTrap(board, this, 12)) {
                            //Adds castling moves
                            kingCastles.add(new Move.KingSideCastleMove(board, this, 6,
                                    (Rook) rookPiece, rookPiece.getPiecePosition(), 5));
                        }
                    }
                }
            }

            //Check to see if castle spaces are empty
            if (!board.getTile(1).isOccupied() &&
                    !board.getTile(2).isOccupied() &&
                    !board.getTile(3).isOccupied()) {
                final ChessTile rookTile = board.getTile(0);

                if (rookTile.isOccupied() && rookTile.getPiece().isFirstMove() &&
                        Player.calculateAttackOnTile(2, opponentLegals).isEmpty() &&
                        Player.calculateAttackOnTile(3, opponentLegals).isEmpty() &&
                        rookTile.getPiece().getPieceType().isRook()) {
                    if (!isKingPawnTrap(board, this, 12)) {
                        kingCastles.add(new Move.QueenSideCastleMove(board, this, 2,
                                (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 3));

                    }
                }
            }
        }
        return ImmutableList.copyOf(kingCastles);
    }

    public boolean checkForPawn(Board board, int move_Coordinate) {
        final Move move = new MajorMove(board, this, move_Coordinate);
        if(board.getCurrentPlayer() != null) {
            MoveStatus moveTransition = board.getCurrentPlayer().makeMove(move).getMoveStatus();
            return moveTransition.isDone();
        }
        return true;
    }

    @Override
    public String toString() {
        return PieceType.KING.toString();
    }

    //Could be optimized
    @Override
    public King movePiece(Move move) {
        return new King(move.getDestinationCoordinate(), move.getMovedPiece().getAlliance(), false, false, false);
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
