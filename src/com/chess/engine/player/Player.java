package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Player {

    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean inCheck;
    protected final Collection<Move> opponentLegals;

    Player(final Board board,
           final Collection<Move> playerLegals,
           final Collection<Move> opponentMoves) {

        this.board = board;
        this.playerKing = establishKing();
        playerLegals.addAll(calculateKingCastles(opponentMoves));
        this.inCheck = !Player.calculateAttackOnTile(this.playerKing.getPiecePosition(), opponentMoves).isEmpty();
        this.legalMoves = Collections.unmodifiableCollection(playerLegals);
        this.opponentLegals = opponentMoves;
    }

    //Calculates the players pieces attacks on a given tile
    public static Collection<Move> calculateAttackOnTile(int piecePosition, Collection<Move> moves) {
        final List<Move> attackMoves = new ArrayList<>();
        for(final Move move : moves) {
            if(piecePosition == move.getDestinationCoordinate()) {
                if((move.getMovedPiece().getPieceType().isPawn()) && !move.isAttack()) {
                    continue;
                }
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }

    public King getPlayerKing() {
        return this.playerKing;
    }

    public Collection<Move> getLegalMoves() {
        return this.legalMoves;
    }

    private King establishKing() {
        for (final Piece piece : getActivePieces()) {
            if (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }
        throw new RuntimeException("No king on the board! Not a valid board");
    }

    public boolean isMoveLegal(final Move move) {
        return this.legalMoves.contains(move);
    }

    public boolean inCheck() {
        return this.inCheck;
    }

    public boolean inCheckMate() {
        return this.inCheck && !hasEscape();
    }

    //Makes the move on a hypothetical board, and checks whether it's in check
    //If all spots are not in check, then there is a possible escape route
    private boolean hasEscape() {
        for(final Move move : this.legalMoves) {
            final MoveTransition transition = makeMove(move);
            if(transition.getMoveStatus().isDone()) {
                return true;
            }
        }
        return false;
    }

    private Collection<Move> blockKing(Collection<Move> moves) {
        Collection<Move> retMoves = new ArrayList<>();

        if(moves.isEmpty()) {
            return retMoves;
        }

        for(Move move : moves) {
            final MoveTransition transition = makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                moves.add(move);
            }
        }
        if(retMoves.isEmpty()) {
            System.out.println("Empty!");
        }
        return retMoves;
    }

    public boolean inStaleMate() {
        return !this.inCheck() && !this.hasEscape();
    }

    public boolean isCastled() {
        return false;
    }

    public MoveTransition makeMove(final Move move) {
        if(!isMoveLegal(move)) {
            return new MoveTransition(this.board, this.board, move, MoveStatus.ILLEGAL_MOVE);
        }

        final Board transitionBoard = move.execute();

        List<Move> oppLegals = new ArrayList<>(this.opponentLegals);

        final Collection<Move> kingAttacks = Player.calculateAttackOnTile(transitionBoard.getCurrentPlayer().getOpponent()
                .getPlayerKing().getPiecePosition(), transitionBoard.getCurrentPlayer().getLegalMoves());

        if(!kingAttacks.isEmpty()) {
            return new MoveTransition(this.board, this.board, move, MoveStatus.PLAYER_IN_CHECK);
        }

        return new MoveTransition(transitionBoard, this.board, move, MoveStatus.DONE);
    }

    public abstract Collection<Piece> getActivePieces();
    public abstract Alliance getAlliance();
    public abstract Player getOpponent();
    public abstract Collection<Move> calculateKingCastles(Collection<Move> opponentLegals);
}
