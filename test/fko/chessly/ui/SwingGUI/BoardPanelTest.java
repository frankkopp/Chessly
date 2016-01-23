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
package fko.chessly.ui.SwingGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.junit.Test;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;

public class BoardPanelTest {

    @Test
    public void testBoardPanel() {

        JFrame frame = new JFrame("FrameDemo");
        frame.setMinimumSize(new Dimension(900,900));
        frame.setSize(new Dimension(900,900));
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        GameBoard b = new GameBoardImpl("2r1k2r/1pq2ppp/3b4/p2ppb2/3n4/P2P4/1P3PPP/1RBKQBNR w k -");

        BoardPanel bp = new BoardPanel(null);

        bp.drawBoard(b);

        frame.getContentPane().add(bp, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.requestFocus();
        while (frame.isShowing()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("test");
    }


}
