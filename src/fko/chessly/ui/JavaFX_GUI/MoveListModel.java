/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fko.chessly.ui.JavaFX_GUI;

import java.util.Iterator;

import com.sun.javafx.scene.control.Logging;

import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sun.util.logging.PlatformLogger;
import sun.util.logging.PlatformLogger.Level;

/**
 * This class shows a list of moves in a table within a JScrollpane.
 * The list is updated when the board of a game is changed that is when a move is made
 */
public class MoveListModel {

    private ObservableList<FullMove> moveList = FXCollections.observableArrayList();

    /**
     * Creates a model of moves for TableView
     */
    public MoveListModel() {
    }

    /**
     * Creates a model of moves for TableView
     * @param gameMoveList
     */
    public MoveListModel(GameMoveList gameMoveList) {
        updateList(gameMoveList);
    }

    /**
     * Clears the list
     */
    public void clear() {
        moveList.clear();
    }

    /**
     * Builds the list of moves of the given board.
     * @param moves
     */
    public void updateList(GameMoveList moves) {

        /*
         * FIXME hack to avoid a INFO message int VirtualFlow.class - bug in JavaFX ?
         */
        final PlatformLogger logger = Logging.getControlsLogger();
        //Level old = logger.level();
        logger.setLevel(Level.OFF);

        // clear the old list
        moveList.clear();

        // add all moves to the observable list
        for (Iterator<GameMove> i = moves.listIterator();i.hasNext();) {
            GameMove moveWhite;
            GameMove moveBlack;
            GameMove move = i.next();
            if (move.getMovedPiece().isWhite()) {
                moveWhite = move;
                if (i.hasNext()){
                    moveBlack = i.next();
                } else {
                    moveBlack = null;
                }
            } else {
                moveWhite = null;
                moveBlack = move;
            }


            moveList.add(new FullMove(moveWhite, moveBlack));
        }
        //logger.setLevel(old);
    }

    /**
     * @return a reference to the move list as observable list.
     */
    public ObservableList<FullMove> getMoveList() {
        return this.moveList;
    }

    /**
     * formats a string based on the integer value of the move
     * @param value integer value of a move
     * @return String representing the value of the move
     */
    private static String getMoveValueString(int value) {
        String moveValueString = "";
        if (value > 0) {
            moveValueString += "+";
        } else if (value == 0) {
            moveValueString += " ";
        }
        moveValueString += value;
        if (value ==  Integer.MAX_VALUE) {
            moveValueString = "++";
        }
        if (value == Integer.MIN_VALUE) {
            moveValueString = "--";
        }
        if (value == GameMove.VALUE_UNKNOWN) {
            moveValueString = "-";
        }
        return moveValueString;
    }

    /**
     * A full move has three Strings: move number, white move and black move.
     * @author fkopp
     */
    public class FullMove {
        private final StringProperty number;
        private final StringProperty white;
        private final StringProperty black;

        /**
         * @param moveWhite
         * @param moveBlack
         */
        public FullMove (GameMove moveWhite, GameMove moveBlack) {

            this.number = new SimpleStringProperty("");

            if (moveWhite==null) {
                white = new SimpleStringProperty("");
                if (moveBlack!=null) {
                    this.number.set(String.format("%d.",moveBlack.getHalfMoveNumber()/2));
                }
            } else {
                this.number.set(String.format("%d.",(moveWhite.getHalfMoveNumber()/2)+1));
                String string = String
                        .format("%s (%s)",
                                moveWhite.toLongAlgebraicNotationString(),
                                getMoveValueString(moveWhite.getValue())
                                );
                white = new SimpleStringProperty(string);
            }

            if (moveBlack==null) {
                black = new SimpleStringProperty("");
            } else {
                String string = String
                        .format("%s (%s)",
                                moveBlack.toLongAlgebraicNotationString(),
                                getMoveValueString(moveBlack.getValue())
                                );
                black = new SimpleStringProperty(string);
            }
        }

        /**
         * @return number as String
         */
        public String getNumber() {
            return this.number.get();
        }

        /**
         * @return white String
         */
        public String getWhite() {
            return this.white.get();
        }

        /**
         * @return black String
         */
        public String getBlack() {
            return this.black.get();
        }

        /**
         * @return number as StringPortperty
         */
        public StringProperty numberProperty() {
            return this.number;
        }

        /**
         * @return white as StringPortperty
         */
        public StringProperty whiteProperty() {
            return this.white;
        }

        /**
         * @return black as StringPortperty
         */
        public StringProperty blackProperty() {
            return this.black;
        }

    }

}
