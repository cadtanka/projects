package com.chess.engine.board;

import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

//Immutable class of a single chess tile
public abstract class ChessTile {
    protected final int cord;

    private static final Map<Integer, EmptyTile> EMPTY_TILES_CACHE = createAllPossibleEmptyTiles();

    //Creates a map with 64 spaces, each with one tile within it
    private static Map<Integer, EmptyTile> createAllPossibleEmptyTiles() {
        final Map<Integer, EmptyTile> emptyTileMap = new HashMap<>();

        for(int i = 0; i < 64; i++) {
            emptyTileMap.put(i, new EmptyTile(i));
        }

        //Could download Guava dependency to make immutable (someone could change this tileMap))
        return  ImmutableMap.copyOf(emptyTileMap);
    }

    public static ChessTile createTile(final int tileCoordinate, final Piece piece) {
        return piece != null ? new Occupied(tileCoordinate, piece) : EMPTY_TILES_CACHE.get(tileCoordinate);
    }
    private ChessTile(int cord) {
        this.cord = cord;
    }

    public abstract boolean isOccupied();

    public int getTileCoordinate() {
        return this.cord;
    }
    public abstract Piece getPiece();

    //If there is no piece on the tile
    public static final class EmptyTile extends ChessTile  {
        private EmptyTile(final int coordinate) {
            super(coordinate);
        }

        @Override
        public String toString() {
            return "-";
        }
        @Override
        public boolean isOccupied() {
            return false;
        }

        @Override
        public Piece getPiece() {
            return null;
        }
    }

    //If there IS a piece on the tile
    public static final class Occupied extends ChessTile {
        private final Piece pieceOn;

        private Occupied(int cord, Piece pieceOn) {
            super(cord);
            this.pieceOn = pieceOn;
        }

        @Override
        public String toString() {
            return getPiece().getAlliance().isBlack() ? getPiece().toString().toLowerCase() : getPiece().toString();
        }
        @Override
        public boolean isOccupied() {
            return true;
        }

        @Override
        public Piece getPiece() {
            return this.pieceOn;
        }

    }

}
