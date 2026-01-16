package com.chess.gui;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.gui.Table.MoveLog;
import com.chess.pgn.FenUtilities;
import com.chess.engine.player.ai.StandardBoardEvaluator;
import com.chess.gui.Table;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.chess.gui.Table.*;

public class GameHistoryPanel extends JPanel {

    private final DataModel model;
    private final JScrollPane scrollPane;
    private static final Dimension HISTORY_PANEL_DIMENSION = new Dimension(100,40);
    private final StandardBoardEvaluator boardEvaluator = new StandardBoardEvaluator();
    private Set<String> loggedPositions = new HashSet<>();

    public GameHistoryPanel() {
        this.setLayout(new BorderLayout());
        this.model = new DataModel();
        final JTable table = new JTable(model);
        table.setRowHeight(15);
        //Creates a scroll panel when number of moves goes off-screen
        this.scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        scrollPane.setPreferredSize(HISTORY_PANEL_DIMENSION);
        this.add(scrollPane, BorderLayout.CENTER);
        this.setVisible(true);
    }

    void redo(final Board board, final MoveLog moveHistory) {
        int currentRow = 0;
        this.model.clear();
        for(final Move move : moveHistory.getMoves()) {
            final String moveText = move.toString();

            if(move.getMovedPiece().getAlliance().isWhite()) {
                this.model.setValueAt(moveText, currentRow, 0);
            } else if(move.getMovedPiece().getAlliance().isBlack()) {
                this.model.setValueAt(moveText, currentRow, 1);
                currentRow++;
            }
        }
        if(moveHistory.getMoves().size() > 0) {
            final Move lastMove = moveHistory.getMoves().get(moveHistory.size() - 1);
            
            String positionKey = FenUtilities.createFENFromGame(board) + lastMove.toString();

            if(!loggedPositions.contains(positionKey)) {
                int moveNumber = moveHistory.getMoves().size();
                logMoveForML(board, lastMove, boardEvaluator.evaluate(board, 3), moveNumber);
                loggedPositions.add(positionKey);
            }
            final String moveText = lastMove.toString();
            if(lastMove.getMovedPiece().getAlliance().isWhite()) {
                this.model.setValueAt(moveText + calculateCheckAndCheckMateHash(board), currentRow, 0);
            } else if (lastMove.getMovedPiece().getAlliance().isBlack()) {
                this.model.setValueAt(moveText + calculateCheckAndCheckMateHash(board), currentRow - 1, 1);
            }
        }

        final JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    public void logMoveForML(Board board, Move move, double eval, int moveNumber) {
        String fen = FenUtilities.createFENFromGame(board);
        String moveUCI = move.toString();
        System.out.println(moveNumber);

        String record = String.format("{\"fen\": \"%s\", \"move\": \"%s\", \"eval\": %.2f, \"move_num\": %d}\n",
                          fen, moveUCI, eval, moveNumber);

        try (FileWriter fw = new FileWriter("selfplay_dataset.jsonl", true)) {
            if (calculateCheckAndCheckMateHash(board).equals("#")) {
                String winner = board.getCurrentPlayer().getOpponent().toString(); // Player who just moved
                record = String.format("{\"fen\": \"%s\", \"move\": \"%s\", \"eval\": %.2f, \"result\": \"checkmate\", \"winner\": \"%s\"}\n",
                              fen, moveUCI, eval, winner);
            } else if (moveNumber >= 299) {
                record = String.format("{\"fen\": \"%s\", \"move\": \"%s\", \"eval\": %.2f, \"result\": \"draw\"}\n",
                              fen, moveUCI, eval);
            }
            fw.write(record);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String calculateCheckAndCheckMateHash(Board board) {
        if(board.getCurrentPlayer().inCheckMate()) {
            return "#";
        } else if (board.getCurrentPlayer().inCheck()) {
            return "+";
        }
        return "";
    }

    private static class DataModel extends DefaultTableModel {
        private final List<Row> values;
        private static final String[] NAMES = {"White", "Black"};

        DataModel() {
            this.values = new ArrayList<>();
        }

        public void clear() {
            this.values.clear();
            setRowCount(0);
        }

        @Override
        public int getRowCount() {
            if(this.values == null) {
                return 0;
            }

            return this.values.size();
        }

        @Override
        public int getColumnCount() {
            return NAMES.length;
        }

        @Override
        public Object getValueAt(final int row, final int column) {
            final Row currentRow = this.values.get(row);
            if(column == 0) {
                return currentRow.getWhiteMove();
            } else if (column == 1) {
                return currentRow.getBlackMove();
            }
            return null;
        }

        @Override
        public void setValueAt(final Object aValue,
                               final int row,
                               final int column) {
            final Row currentRow;
            if(this.values.size() <= row) {
                currentRow = new Row();
                this.values.add(currentRow);
            } else {
                currentRow = this.values.get(row);
            }

            if(column == 0) {
                currentRow.setWhiteMove((String) aValue);
                fireTableRowsInserted(row, row);
            } else if (column == 1) {
                currentRow.setBlackMove((String) aValue);
                fireTableCellUpdated(row, column);
            }
        }

        @Override
        public Class<?> getColumnClass(final int column) {
            return Move.class;
        }

        @Override
        public String getColumnName(final int column) {
            return NAMES[column];
        }
    }

    //One set of moves (White then black go)
    private static class Row {
        private String whiteMove;
        private String blackMove;

        Row() {

        }

        public String getWhiteMove() {
            return this.whiteMove;
        }

        public String getBlackMove() {
            return this.blackMove;
        }

        public void setWhiteMove(final String move) {
            this.whiteMove = move;
        }

        public void setBlackMove(final String move) {
            this.blackMove = move;
        }
    }
}
