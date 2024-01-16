package com.chess.gui;

import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.google.common.primitives.Ints;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.chess.gui.Table.*;

//Creates the sidebars and pieces that the captured pieces will reside on during game
public class TakenPiecesPanel extends JPanel {

    private final JPanel northPanel;
    private final JPanel southPanel;
    private static final Dimension TAKEN_PIECES_DIMENSION = new Dimension(40,80);
    private static final Color PANEL_COLOR = Color.decode("0xFDF5E6");
    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);

    public TakenPiecesPanel() {
        super(new BorderLayout());
        this.setBackground(PANEL_COLOR);
        this.setBorder(PANEL_BORDER);
        this.northPanel = new JPanel(new GridLayout(8, 2));
        this.southPanel = new JPanel(new GridLayout(8, 2));
        this.northPanel.setBackground(PANEL_COLOR);
        this.southPanel.setBackground(PANEL_COLOR);

        add(this.northPanel, BorderLayout.NORTH);
        add(this.southPanel, BorderLayout.SOUTH);
        setPreferredSize(TAKEN_PIECES_DIMENSION);
    }

    public void redo(final MoveLog moveLog) {
        this.southPanel.removeAll();
        this.northPanel.removeAll();

        final List<Piece> whiteTakenPieces = new ArrayList<>();
        final List<Piece> blackTakenPieces = new ArrayList<>();

        //Gets all the black and white taken pieces and adds them
        //to their corresponding list
        for(final Move move : moveLog.getMoves()) {
            if(move.isAttack()) {
                final Piece takenPiece = move.getAttackedPiece();
                if(takenPiece.getAlliance().isWhite()) {
                    whiteTakenPieces.add(takenPiece);
                } else if(takenPiece.getAlliance().isBlack()){
                    blackTakenPieces.add(takenPiece);
                } else {
                    System.out.print("Should not reach here!");
                }
            }
        }

        //Organizes the pieces into ranks
        //TODO: Put in separate methods
        whiteTakenPieces.sort((o1, o2) -> Ints.compare(o1.getPieceValue(), o2.getPieceValue()));

        blackTakenPieces.sort((o1, o2) -> Ints.compare(o1.getPieceValue(), o2.getPieceValue()));

        //TODO: Put in separate methods
        String defaultPieceImagesPath = "art/pieces/plain/";
        for(final Piece takenPiece : whiteTakenPieces) {
            try {
                final BufferedImage image = ImageIO.read(new File(defaultPieceImagesPath
                        + takenPiece.getAlliance().toString().charAt(0) + "" + takenPiece + ".gif"));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(
                        icon.getIconWidth() - 15, icon.getIconWidth() - 15, Image.SCALE_SMOOTH)));
                this.southPanel.add(imageLabel);
            } catch(final IOException e) {
                e.printStackTrace();
            }
        }

        for(final Piece takenPiece : blackTakenPieces) {
            try {
                final BufferedImage image = ImageIO.read(new File(defaultPieceImagesPath
                        + takenPiece.getAlliance().toString().charAt(0) + "" + takenPiece + ".gif"));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(
                        icon.getIconWidth() - 15, icon.getIconWidth() - 15, Image.SCALE_SMOOTH)));
                this.northPanel.add(imageLabel);
            } catch(final IOException e) {
                e.printStackTrace();
            }
        }
        validate();
    }
}
