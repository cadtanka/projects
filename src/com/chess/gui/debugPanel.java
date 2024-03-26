package com.chess.gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import static java.net.http.WebSocket.*;

class DebugPanel extends JPanel implements Listener {

    private static final Dimension CHAT_PANEL_DIMENSION = new Dimension(600, 150);

    public DebugPanel() {
        super(new BorderLayout());
        JTextArea jTextArea = new JTextArea("");
        add(jTextArea);
        setPreferredSize(CHAT_PANEL_DIMENSION);
        validate();
        setVisible(true);
    }

    public void redo() {
        validate();
    }
}
