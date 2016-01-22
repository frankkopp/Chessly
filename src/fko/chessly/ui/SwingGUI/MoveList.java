/*
 * <p>GPL Dislaimer</p>
 * <p>
 * "Chessly by Frank Kopp"
 * Copyright (c) 2003-2015 Frank Kopp
 * mail-to:frank@familie-kopp.de
 *
 * This file is part of "Chessly by Frank Kopp".
 *
 * "Chessly by Frank Kopp" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * "Chessly by Frank Kopp" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Chessly by Frank Kopp"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </p>
 *
 *
 */

package fko.chessly.ui.SwingGUI;

import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameMove;

/**
 * This class shows a list of moves in a table within a JScrollpane.
 * The list is updated when the board of a game is changed that is when a move is made
 */
public class MoveList extends JScrollPane {

	private static final long serialVersionUID = -397845607941445463L;

	// -- _moves --
    private DefaultTableModel _moves;

    // -- the table --
    private JTable _movelist;
    private final String[] nullRow = new String[] {"", "", "", "", "", ""};

    /**
     * Creates an empty (no viewport view) <code>JScrollPane</code>
     * where both horizontal and vertical scrollbars appear when needed.
     */
    public MoveList() {
        String[] columnNames = { "Move Number", "White Moves", "Move Values", "Black Moves", "Move Values" };

        _moves = new DefaultTableModel(columnNames,0);

        _movelist = new JTable(_moves);
        _movelist.setShowGrid(true);
        _movelist.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        _movelist.setEnabled(false);
        _movelist.setDefaultRenderer(Object.class, centerRenderer);
        

        this.setViewportView(_movelist);
        this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.setDoubleBuffered(true);
    }

    /**
     * clears the list
     */
    protected void clear() {
        // -- removes all rows --
        _moves.setNumRows(0);
    }

    /**
     * builds the list of move of the given board
     * @param game
     */
    protected void drawMove(Game game) {

        // -- reference the current board --
        GameBoard board = game.getCurBoard();

        // -- reference to the move list of the board --
        List<GameMove> moves = board.getMoveHistory();
        
        // -- empty list to handle undo 
        _moves.setNumRows(0);
        _movelist.validate();

        // -- cycle through all moves and copy them to the movelist --
        int counter = 0;
        for (Iterator<GameMove> i = moves.listIterator();i.hasNext();) {
            counter++;
            GameMove move = i.next();

            // -- calculate the last row we need --
            int lastRow = ((counter + 1) >> 1) - 1;

            // -- do we have enough rows defined? --
            if (lastRow >= _movelist.getRowCount()) {
                _moves.addRow( nullRow );
            }

            // -- prepare some Strings we need to format the output in the list/table --
            String moveNumberString = Integer.toString(counter/2+1) + '.';
            String moveValueString = getMoveValueString(move.getValue());

            String moveString;
            if (move.getMovedPiece().isWhite()) {
                moveString = new StringBuilder(10)
                        .append(move.toLongAlgebraicNotationString())
                        .toString();
                _moves.setValueAt(moveNumberString, lastRow, 0);
                _moves.setValueAt(moveString      , lastRow, 1);
                _moves.setValueAt(moveValueString , lastRow, 2);
            } else if (move.getMovedPiece().isBlack()) {
                 // -- format the moveString --
                 moveString = new StringBuilder(10)
                         .append(move.toLongAlgebraicNotationString())
                         .toString();
                 _moves.setValueAt(moveString      , lastRow, 3);
                 _moves.setValueAt(moveValueString , lastRow, 4);
             
             }
        }

        // -- autoscroll to the last entry --
        this.getVerticalScrollBar().setValue(this.getVerticalScrollBar().getMaximum());

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
            moveValueString = "+inf";
        }
        if (value == Integer.MIN_VALUE) {
            moveValueString = "-inf";
        }
        if (value == GameMove.VALUE_UNKNOWN) {
            moveValueString = "only";
        }
        return moveValueString;
    }

}
