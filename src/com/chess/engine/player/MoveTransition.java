package com.chess.engine.player;

import com.chess.engine.board.Board;

public record MoveTransition(Board transitionBoard, Board fromBoard, MoveStatus moveStatus) {


}
