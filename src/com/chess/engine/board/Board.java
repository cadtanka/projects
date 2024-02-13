package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.*;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.*;

import static com.chess.engine.pieces.Piece.PieceType.KING;

public class Board {

    private final List<ChessTile> gameBoard;
    private final Collection<Piece> whitePieces;
    private final Collection<Piece> blackPieces;
    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Pawn enPassantPawn;
    private final Player currentPlayer;
   // private final Collection<Move> kingMoves;

    private Board(final Builder builder) {
        this.gameBoard = createGameBoard(builder);
        this.whitePieces = calculateActivePieces(this.gameBoard, Alliance.WHITE);
        this.blackPieces = calculateActivePieces(this.gameBoard, Alliance.BLACK);
        this.enPassantPawn = builder.enPassantPawn;

        final Collection<Move> whiteLegalMoves = calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackLegalMoves = calculateLegalMoves(this.blackPieces);

        this.whitePlayer = new WhitePlayer(this, whiteLegalMoves, blackLegalMoves);
        this.blackPlayer = new BlackPlayer(this, blackLegalMoves, whiteLegalMoves);
        this.currentPlayer = builder.nextMoveMaker.choosePlayer(this.whitePlayer, this.blackPlayer);

        //this.kingMoves = this.currentPlayer.calculateKingCastles(this.currentPlayer.getOpponent().getLegalMoves());
        //System.out.println("King at constructor" + kingMoves);
        //System.out.println(currentPlayer);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < BoardUtils.NUM_TILES; i++) {
            final String tileText = this.gameBoard.get(i).toString();
            builder.append(String.format("%3s", tileText));
            if((i + 1) % BoardUtils.NUM_TILES_ROW == 0) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public Player whitePlayer() {
        return this.whitePlayer;
    }

    public Player blackPlayer() {
        return this.blackPlayer;
    }

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    public Pawn getEnPassantPawn() {
        return this.enPassantPawn;
    }

    public Collection<Piece> getBlackPieces() {
        return this.blackPieces;
    }

    public Collection<Piece> getWhitePieces() {
        return this.whitePieces;
    }

    private Collection<Move> calculateLegalMoves(Collection<Piece> pieces) {
        //TODO: Need to add the kings move to this list
        //System.out.println("At calculate legal moves:" + currentPlayer);
        List<Move> legalMoveList = new ArrayList<>();
        for(Piece piece : pieces) {
//            if(this.currentPlayer.getAlliance().isBlack() || this.currentPlayer.getAlliance().isWhite()) {
//                Collection<Move> kingMoves = this.getCurrentPlayer().calculateKingCastles(this.getCurrentPlayer().getOpponent().getLegalMoves());
//                legalMoveList.addAll(kingMoves);
//            }
            for(Move move : piece.calculateLegals(this)) {
                //if(piece.getPieceType().isKing() && move.get)
                legalMoveList.add(move);
            }
        }

//        if(this.kingMoves != null) {
//            legalMoveList.addAll(this.kingMoves);
//        }

        //System.out.println("King moves at calculateLegalMoves" + this.kingMoves);

        return legalMoveList;
    }

    private static Collection<Piece> calculateActivePieces(final List<ChessTile> gameBoard, final Alliance alliance) {
        final List<Piece> activePieces = new ArrayList<>();
        for(final ChessTile tile: gameBoard) {
            if(tile.isOccupied()) {
                final Piece piece = tile.getPiece();
                if(piece.getAlliance() == alliance) {
                    activePieces.add(piece);
                }
            }
        }
        return ImmutableList.copyOf(activePieces);
    }

    public ChessTile getTile(int move_coordinate) {
        return gameBoard.get(move_coordinate);
    }

    //Creates a board of 64 tiles
    private static List<ChessTile> createGameBoard(final Builder builder) {
        final ChessTile[] tiles = new ChessTile[BoardUtils.NUM_TILES];
        for(int i = 0; i < BoardUtils.NUM_TILES; i++) {
            tiles[i] = ChessTile.createTile(i, builder.boardConfig.get(i));
        }
        return ImmutableList.copyOf(tiles);
    }

    //Creates a standard board of Chess
    public static Board createStandardBoard() {
        final Builder builder = new Builder();
        //Black
        buildBlack(builder);
        //White
        buildWhite(builder);
        //White moves first
        builder.setMoveMaker(Alliance.WHITE);

        return builder.build();
    }

    private static void buildBlack(Builder builder) {
        builder.setPiece(new Rook(0, Alliance.BLACK));
        builder.setPiece(new Knight(1, Alliance.BLACK));
        builder.setPiece(new Bishop(2, Alliance.BLACK));
        builder.setPiece(new Queen(3, Alliance.BLACK));
        builder.setPiece(new King(4, Alliance.BLACK, true, false, false));
        builder.setPiece(new Bishop(5, Alliance.BLACK));
        builder.setPiece(new Knight(6, Alliance.BLACK));
        builder.setPiece(new Rook(7, Alliance.BLACK));
        for(int i = 0; i < BoardUtils.NUM_TILES_ROW; i++) {
            builder.setPiece(new Pawn(i + BoardUtils.NUM_TILES_ROW, Alliance.BLACK));
        }
    }

    private static void buildWhite(Builder builder) {
        builder.setPiece(new Rook(56, Alliance.WHITE));
        builder.setPiece(new Knight(57, Alliance.WHITE));
        builder.setPiece(new Bishop(58, Alliance.WHITE ));
        builder.setPiece(new Queen(59, Alliance.WHITE));
        builder.setPiece(new King(60, Alliance.WHITE, true, false, false));
        builder.setPiece(new Bishop(61, Alliance.WHITE));
        builder.setPiece(new Knight(62, Alliance.WHITE));
        builder.setPiece(new Rook(63, Alliance.WHITE));
        for(int i = 0; i < BoardUtils.NUM_TILES_ROW; i++) {
            builder.setPiece(new Pawn(i + (BoardUtils.NUM_TILES_ROW * 6), Alliance.WHITE));
        }
    }

    public Iterable<Move> getAllLegalMoves() {
        return Iterables.unmodifiableIterable(Iterables.concat(this.whitePlayer.getLegalMoves(), this.blackPlayer.getLegalMoves()));
    }


    public static class Builder {
        final Map<Integer, Piece> boardConfig;
        Alliance nextMoveMaker;
        Pawn enPassantPawn;

        public Builder() {
            this.boardConfig = new HashMap<>();
        }

        public void setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPiecePosition(), piece);
        }

        public Builder setMoveMaker(final Alliance nextMoveMaker) {
            this.nextMoveMaker = nextMoveMaker;
            return this;
        }

        public Board build() {
            return new Board(this);
        }

        public void setEnPassantPawn(Pawn enPassantPawn) {
            this.enPassantPawn = enPassantPawn;
        }
    }
}
