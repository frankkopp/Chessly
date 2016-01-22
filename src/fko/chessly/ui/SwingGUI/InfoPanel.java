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

import java.awt.Color;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;

/**
 * A panel with a textarea to show information like move list etc.
 */
public class InfoPanel extends JScrollPane {

    private static final long serialVersionUID = -7702061974667253593L;

    private JTextArea textArea = null;

    /**
     * Who many lines will this InfoPanel show at maximum.
     */
    public int SCROLL_BUFFER_SIZE = 20000;

    /**
     * Constructs a new TextArea.  A default model is set, the initial string
     * is null, and rows/columns are set to 0.
     */
    public InfoPanel() {
        super();
        textArea = new JTextArea();
        textArea.setBorder(new LineBorder(Color.darkGray, 1, true));
        textArea.setEditable(false);
        textArea.setTabSize(8);
        textArea.setColumns(40);
        textArea.setFont(new Font("Lucida Console", Font.PLAIN, 12));
        textArea.setDoubleBuffered(true);
        textArea.setAutoscrolls(true);
        ((AbstractDocument) textArea.getDocument()).setDocumentFilter(
                new TextAreaDocLengthFilter(textArea, SCROLL_BUFFER_SIZE));
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.setViewportView(textArea);
    }

    /**
     * Constructs a new TextArea.  A default model is set, the initial string
     * is null, and rows/columns are set to 0.
     * @param textSize
     */
    public InfoPanel(int textSize) {
        this();
        textArea.setFont(new Font("Lucida Console", Font.PLAIN, textSize));
    }

    /**
     * print a text into the info pane
     * @param text
     */
    public synchronized void printInfo(String text) {
        textArea.append(text);
        //textArea.setCaretPosition(textArea.getText().length());
        //SwingUtilities.invokeLater(_autoScroller);
    }

    /**
     * print a text plus newline into the info pane
     * @param text
     */
    public synchronized void printInfoln(String text) {
        printInfo(text+ '\n');
    }

    /**
     /**
     * Thread to scroll infoPanel to the last position
     */
    private final Runnable _autoScroller = new Runnable() {
        @Override
        public void run() {
            textArea.setCaretPosition(textArea.getText().length());
        }
    };

    /**
     * Makes sure the textArea does not grow until out of memory
     * @author Frank
     */
    public class TextAreaDocLengthFilter extends DocumentFilter {

        private JTextArea area;
        private int max;

        /**
         * @param area
         * @param max
         */
        public TextAreaDocLengthFilter(JTextArea area, int max) {
            this.area = area;
            this.max = max;
        }

        /* (non-Javadoc)
         * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass, int, java.lang.String, javax.swing.text.AttributeSet)
         */
        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
            super.insertString(fb, offset, text, attrs);

            int lines = area.getLineCount();
            if (lines > max) {
                int linesToRemove = lines - max -1;
                int lengthToRemove = area.getLineStartOffset(linesToRemove);
                remove(fb, 0, lengthToRemove);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass, int, int, java.lang.String, javax.swing.text.AttributeSet)
         */
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

            super.replace(fb, offset, length, text, attrs);

            int lines = area.getLineCount();
            if (lines > max) {
                int linesToRemove = lines - max -1;
                int lengthToRemove = area.getLineStartOffset(linesToRemove);
                remove(fb, 0, lengthToRemove);
            }

        }
    }




}