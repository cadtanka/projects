package com.chess.engine.board;

import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardUtils {

    public static final boolean[] FIRST_COLUMN = initColumn(0);
    public static final boolean[] SECOND_COLUMN = initColumn(1);
    public static final boolean[] SEVENTH_COLUMN = initColumn(6);
    public static final boolean[] EIGHTH_COLUMN = initColumn(7);
    public static final boolean[] FIRST_RANK = initRow(56);
    public static final boolean[] EIGHTH_RANK = initRow(0);
    public static final String[] ALGEBRAIC_NOTATION = initializeAlgebraicNotation();
    public static final Map<String, Integer> POSITION_TO_COORDINATE = initializePositionToCoordinateMap();

    public static final int NUM_TILES = 64;
    public static final int NUM_TILES_ROW = 8;

    private static String[] initializeAlgebraicNotation() {
        return new String[] {
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"
        };
    }

    private static Map<String, Integer> initializePositionToCoordinateMap() {
        final Map<String, Integer> positionToCoordinate = new HashMap<>();
        for(int i = 0; i < NUM_TILES; i++) {
            positionToCoordinate.put(ALGEBRAIC_NOTATION[i],i);
        }

        return ImmutableMap.copyOf(positionToCoordinate);
    }

    public static boolean[] initRow(int rowNumber) {
        final boolean[] row = new boolean[NUM_TILES];

        do {
            row[rowNumber] = true;
            rowNumber ++;
        } while (rowNumber % NUM_TILES_ROW != 0);

        return row;
    }

    private static boolean[] initColumn(int columnNumber) {
        final boolean[] column = new boolean[NUM_TILES];

        do {
            column[columnNumber] = true;
            columnNumber += NUM_TILES_ROW;
        } while(columnNumber < NUM_TILES);

        return column;
    }

    private BoardUtils () {
        throw new RuntimeException("You cannot instantiate this");
    }
    public static boolean isValidTileCoordinate(int move_coordinate) {
        return (move_coordinate >= 0 && move_coordinate < NUM_TILES);
    }

    //Finds all possible diagonal squares, regardless of other pieces. Will add some negative coordinates
    //that will later be factored out
    public static void getDiagonalFromPiece(int piecePos, List<Integer> moveCoordinates, Board board) {
        boolean botLeftDone = false;
        boolean botRightDone = false;
        boolean topLeftDone = false;
        boolean topRightDone = false;

        int botLeft = piecePos;
        int botRight = piecePos;
        int topLeft = piecePos;
        int topRight = piecePos;

        if(FIRST_COLUMN[piecePos]) {
            topLeftDone = true;
            botLeftDone = true;
        }

        if(EIGHTH_COLUMN[piecePos]) {
            topRightDone = true;
            botRightDone = true;
        }

        while(!(botRightDone && botLeftDone && topLeftDone && topRightDone)) {
            if(!botLeftDone) {
                botLeft -= 9;

                if(botLeft % 8 == 0 || botLeft <= 0 || board.getTile(botLeft).isOccupied()) {
                    botLeftDone = true;
                }

                moveCoordinates.add(botLeft);
            }

            if(!botRightDone) {
                botRight -= 7;

                if(botRight % 8 == 7 || botRight <= 0 || board.getTile(botRight).isOccupied()) {
                    botRightDone = true;
                }
                moveCoordinates.add(botRight);
            }

            if(!topLeftDone) {
                topLeft += 7;

                if(topLeft % 8 == 0 || topLeft >= NUM_TILES || board.getTile(topLeft).isOccupied()) {
                    topLeftDone = true;
                }
                moveCoordinates.add(topLeft);
            }

            if(!topRightDone) {
                topRight += 9;

                if(topRight % 8 == 7 || topRight >= NUM_TILES || board.getTile(topRight).isOccupied()) {
                    topRightDone = true;
                }
                moveCoordinates.add(topRight);
            }
        }
    }

    //Gets all coordinates of the tiles that are in the same column as a given spot
    public static void getColumn (int piecePosition, List<Integer> moveCoordinates, Board board) {
        int topPosition = piecePosition + NUM_TILES_ROW;
        int botPosition = piecePosition - NUM_TILES_ROW;

        boolean topDone = false;
        boolean botDone = false;

        while(!topDone || !botDone) {
            if(!botDone) {
                if(botPosition <= 0 || board.getTile(botPosition).isOccupied()) {
                    botDone = true;
                }
                moveCoordinates.add(botPosition);
                botPosition -= 8;
            }

            if(!topDone) {
                if(topPosition >= NUM_TILES || board.getTile(topPosition).isOccupied()) {
                    topDone = true;
                }
                moveCoordinates.add(topPosition);
                topPosition += 8;
            }
        }
    }

    public static boolean isFirstColumn(int piecePosition) {
        return piecePosition % 8 == 0;
    }

    public static boolean isEighthColumn(int piecePosition) {
        return piecePosition % 8 == 7;
    }
    public static boolean isKingPawnTrap(final Board board, final King king, final int frontTile) {
        final Piece piece = board.getTile(frontTile).getPiece();
        return piece != null && piece.getPieceType() == Piece.PieceType.PAWN &&
                piece.getAlliance() != king.getAlliance();
    }

    //TODO: Make method to check that king does not move into pawn attack path

    //Gets all coordinates of the tiles that are in the same row as a given spot
    public static void getRow(int piecePosition, List<Integer> moveCoordinates, Board board) {

        int rightPosition = piecePosition + 1;
        int leftPosition = piecePosition - 1;

        boolean leftDone = false;
        boolean rightDone = false;

        if(piecePosition % 8 == 0) {
            leftDone = true;
        }

        if(piecePosition % 8 == 7) {
            rightDone = true;
        }

        while(!leftDone || !rightDone) {
            if(!leftDone) {
                if(leftPosition % 8 == 0 || leftPosition <= 0 || board.getTile(leftPosition).isOccupied()) {
                    leftDone = true;
                }

                moveCoordinates.add(leftPosition);

                leftPosition -= 1;
            }

            if(!rightDone) {
                if(rightPosition % 8 == 7 || rightPosition >= NUM_TILES || board.getTile(rightPosition).isOccupied()) {
                    rightDone = true;
                }

                moveCoordinates.add(rightPosition);

                rightPosition += 1;
            }
        }
    }

    public static int getCoordinateAtPosition(final String position) {
        return POSITION_TO_COORDINATE.get(position);
    }

    public static String getPositionAtCoordinate(final int coordinate) {
        return ALGEBRAIC_NOTATION[coordinate];
    }
}
