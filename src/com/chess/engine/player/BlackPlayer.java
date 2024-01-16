package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.ChessTile;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Move.QueenSideCastleMove;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlackPlayer extends Player {
    public BlackPlayer(final Board board,
                       final Collection<Move> whiteLegalMoves,
                       final Collection<Move> blackLegalMoves) {
        super(board, blackLegalMoves, whiteLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals, final Collection<Move> opponentLegals) {
        final List<Move> kingCastles = new ArrayList<>();
        if(this.playerKing.isFirstMove() && !this.inCheck()) {
            //Specific to black king side castle
            if(!this.board.getTile(5).isOccupied() && !this.board.getTile(6).isOccupied()) {
                final ChessTile rookTile = this.board.getTile(7);

                if(rookTile.isOccupied() && rookTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttackOnTile(5, opponentLegals).isEmpty() &&
                            Player.calculateAttackOnTile(6, opponentLegals).isEmpty() &&
                            rookTile.getPiece().getPieceType().isRook()) {
                        //Adds castling moves
                        kingCastles.add(new KingSideCastleMove(this.board, this.playerKing, 6,
                                        (Rook)rookTile.getPiece(), rookTile.getTileCoordinate(), 5));
                    }
                }
            }

            //Check to see if castle spaces are empty
            if(!this.board.getTile(1).isOccupied() &&
                    !this.board.getTile(2).isOccupied() &&
                    !this.board.getTile(3).isOccupied()) {
                final ChessTile rookTile = this.board.getTile(0);

                if(rookTile.isOccupied() && rookTile.getPiece().isFirstMove() &&
                        Player.calculateAttackOnTile(2, opponentLegals).isEmpty() &&
                        Player.calculateAttackOnTile(3, opponentLegals).isEmpty() &&
                        rookTile.getPiece().getPieceType().isRook()) {
                    kingCastles.add(new QueenSideCastleMove(this.board, this.playerKing, 2,
                                    (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 3));
                }
            }
        }

        return ImmutableList.copyOf(kingCastles);
    }
}
