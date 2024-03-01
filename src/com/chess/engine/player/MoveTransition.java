package com.chess.engine.player;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

public class MoveTransition {

    private final Board transitionBoard;

    private final Board fromBoard;
    private final MoveStatus moveStatus;

    public MoveTransition(final Board transitionBoard,
                          final Board fromBoard,
                          final Move move,
                          final MoveStatus moveStatus) {
        this.fromBoard = fromBoard;
        this.transitionBoard = transitionBoard;
        this.moveStatus = moveStatus;
    }

    public MoveStatus getMoveStatus() {
        return this.moveStatus;
    }

    public Board getTransitionBoard() {
        return this.transitionBoard;
    }

    public Board getFromBoard() {
        return this.fromBoard;
    }


}
