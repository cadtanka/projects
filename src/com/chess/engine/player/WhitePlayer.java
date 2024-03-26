package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.ChessTile;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Piece.PieceType;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;
import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Move.QueenSideCastleMove;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.BoardUtils.*;

public class WhitePlayer extends Player {
    public WhitePlayer(final Board board,
                       final Collection<Move> whiteLegalMoves,
                       final Collection<Move> blackLegalMoves) {
        super(board, whiteLegalMoves, blackLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    public String toString() {
        return "White";
    }

    @Override
    public Collection<Move> calculateKingCastles(Collection<Move> opponentLegals) {
        final List<Move> kingCastles = new ArrayList<>();

        if(this.playerKing.isFirstMove() && !this.inCheck()) {
            //Specific to white king side castle
            if(this.board.getTile(61).getPiece() == null && this.board.getTile(62).getPiece() == null) {
                final Piece rookPiece = this.board.getTile(63).getPiece();

                if(rookPiece != null && rookPiece.isFirstMove()) {
                    if(Player.calculateAttackOnTile(61, opponentLegals).isEmpty() &&
                        Player.calculateAttackOnTile(62, opponentLegals).isEmpty() &&
                        rookPiece.getPieceType() == PieceType.ROOK) {
                        if (isKingPawnTrap(this.board, this.playerKing, 52)) {
                            //Adds castling moves
                            kingCastles.add(new KingSideCastleMove(this.board, this.playerKing, 62,
                                    (Rook) rookPiece, rookPiece.getPiecePosition(), 61));
                        }
                    }
                }
            }

            //Check to see if castle spaces are empty
            if(!this.board.getTile(59).isOccupied() &&
                    !this.board.getTile(58).isOccupied() &&
                    !this.board.getTile(57).isOccupied()) {
                final ChessTile rookTile = this.board.getTile(56);

                if(rookTile.isOccupied() && rookTile.getPiece().isFirstMove() &&
                        Player.calculateAttackOnTile(58, opponentLegals).isEmpty() &&
                        Player.calculateAttackOnTile(59, opponentLegals).isEmpty() &&
                        rookTile.getPiece().getPieceType().isRook()) {
                    if (isKingPawnTrap(this.board, this.playerKing, 52)) {
                        kingCastles.add(new QueenSideCastleMove(this.board, this.playerKing, 58,
                                (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 59));

                    }
                }
            }
        }
        return ImmutableList.copyOf(kingCastles);
    }
}
