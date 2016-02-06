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
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import fko.chessly.Chessly;
import fko.chessly.player.PlayerType;
import fko.chessly.util.HelperTools;

/**
 * Displays the player's clocks
 */
public class ClockPanel extends JPanel {

    private static final long serialVersionUID = 1637869402371655930L;

    // -- back reference to ui --
    private SwingGUI _ui;

    // -- a thread to update the clock --
    private Thread updater = new updateThread();

    private boolean abort = false;

    // -- some components --
    private JLabel blackTime, blackInfo, whiteTime, whiteInfo;
    private Font timeFont, timeFontSmall, timeFontBold, timeFontSmallBold ,nameFont, nameFontBold;
    private JPanel blackClock, whiteClock;
    private TitledBorder blackClockBorder, whiteClockBorder;

    public ClockPanel(SwingGUI backReference) {
        super();
        setName("Clock Panel");
        // -- back reference to ui --
        this._ui = backReference;
        setupPanel();
        clear();
        startUpdate();
    }

    private void setupPanel() {
        // -- two columns --
        whiteClock = new JPanel(new BorderLayout());
        whiteClockBorder = new TitledBorder(new EtchedBorder(), "White");
        whiteClock.setBorder(whiteClockBorder);
        blackClock = new JPanel(new BorderLayout());
        blackClockBorder = new TitledBorder(new EtchedBorder(), "Black");
        blackClock.setBorder(blackClockBorder);

        // -- set font --
        timeFont = new Font("Arial", Font.PLAIN, 36);
        timeFontSmall = new Font("Arial", Font.PLAIN, 16);
        timeFontBold = new Font("Arial", Font.BOLD, 36);
        timeFontSmallBold = new Font("Arial", Font.BOLD, 16);
        nameFont = new Font("Arial", Font.PLAIN, 12);
        nameFontBold = new Font("Arial", Font.BOLD, 12);

        // -- clock panel --
        whiteTime = new JLabel("", SwingConstants.CENTER);
        whiteTime.setFont(timeFont);
        whiteClock.add(whiteTime, BorderLayout.CENTER);
        blackTime = new JLabel("", SwingConstants.CENTER);
        blackTime.setFont(timeFont);
        blackClock.add(blackTime, BorderLayout.CENTER);

        // -- level info --
        whiteInfo = new JLabel("", SwingConstants.CENTER);
        whiteInfo.setFont(timeFontSmall);
        whiteClock.add(whiteInfo, BorderLayout.SOUTH);
        blackInfo = new JLabel("", SwingConstants.CENTER);
        blackInfo.setFont(timeFontSmall);
        blackClock.add(blackInfo, BorderLayout.SOUTH);

        // create gui
        setLayout(new GridLayout(0, 2, 0, 0));
        this.add(whiteClock);
        this.add(blackClock);
    }

    public void clear() {
        blackTime.setText("00:00:00");
        blackClockBorder.setTitle("Black");
        whiteTime.setText("00:00:00");
        whiteClockBorder.setTitle("White");
        blackTime.setFont(timeFont);
        blackClockBorder.setTitleFont(nameFont);
        whiteTime.setFont(timeFont);
        whiteClockBorder.setTitleFont(nameFont);
    }

    public void updateGUI() {
        if (Chessly.getPlayroom().getCurrentGame() != null) {
            if (Chessly.getPlayroom().getCurrentGame().getCurBoard().getNextPlayerColor() == fko.chessly.game.GameColor.BLACK) {
                blackTime.setFont(timeFontBold);
                blackInfo.setFont(timeFontSmallBold);
                blackClockBorder.setTitleFont(nameFontBold);
                whiteTime.setFont(timeFont);
                whiteInfo.setFont(timeFontSmall);
                whiteClockBorder.setTitleFont(nameFont);
            } else {
                blackTime.setFont(timeFont);
                blackInfo.setFont(timeFontSmall);
                blackClockBorder.setTitleFont(nameFont);
                whiteTime.setFont(timeFontBold);
                whiteInfo.setFont(timeFontSmallBold);
                whiteClockBorder.setTitleFont(nameFontBold);
            }
            blackClockBorder.setTitle("Black: " + Chessly.getPlayroom().getCurrentGame().getPlayerBlack().getName());
            whiteClockBorder.setTitle("White: " + Chessly.getPlayroom().getCurrentGame().getPlayerWhite().getName());
            blackClock.setBorder(blackClockBorder);
            whiteClock.setBorder(whiteClockBorder);
            whiteTime.setText(Chessly.getPlayroom().getCurrentGame().getWhiteClock().getFormattedTime());
            blackTime.setText(Chessly.getPlayroom().getCurrentGame().getBlackClock().getFormattedTime());
        }
        if (Chessly.getPlayroom().isTimedGame()) {
            blackInfo.setText(HelperTools.formatTime(Chessly.getPlayroom().getTimeBlack(), false));
            whiteInfo.setText(HelperTools.formatTime(Chessly.getPlayroom().getTimeWhite(), false));
        } else {
            PlayerType playerTypeBlack;
            PlayerType playerTypeWhite;
            if (Chessly.getPlayroom().getCurrentGame() != null) {
                playerTypeBlack=Chessly.getPlayroom().getCurrentGame().getPlayerBlack().getPlayerType();
                playerTypeWhite=Chessly.getPlayroom().getCurrentGame().getPlayerWhite().getPlayerType();
            } else {
                playerTypeBlack=Chessly.getPlayroom().getPlayerTypeBlack();
                playerTypeWhite=Chessly.getPlayroom().getPlayerTypeWhite();
            }
            if (playerTypeBlack == PlayerType.HUMAN) {
                blackInfo.setText("Human");
            }  else {
                blackInfo.setText("Computer Level " + Chessly.getPlayroom().getCurrentEngineLevelBlack());
            }
            if (playerTypeWhite == PlayerType.HUMAN) {
                whiteInfo.setText("Human");
            } else {
                whiteInfo.setText("Computer Level " + Chessly.getPlayroom().getCurrentEngineLevelWhite());
            }

        }
        repaint();
    }

    public void startUpdate() {
        updater.start();
    }

    private class updateThread extends Thread {
        private updateThread() {
            super("ClockInfoUpdater");
            setPriority(Thread.MIN_PRIORITY);
            setDaemon(true);
        }
        @Override
        public void run() {
            updateRunnable aUpdateRunnable = new updateRunnable();
            while (true) {
                if (abort) {
                    return;
                }
                SwingUtilities.invokeLater(aUpdateRunnable);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // -- ignore --
                }
            }
        }
    }

    private class updateRunnable implements Runnable {
        @Override
        public void run() {
            updateGUI();
        }
    }

}
