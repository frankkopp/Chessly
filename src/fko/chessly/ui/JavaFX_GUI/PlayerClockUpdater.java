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


import fko.chessly.Chessly;
import fko.chessly.game.GameColor;
import fko.chessly.player.PlayerType;
import fko.chessly.util.HelperTools;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * @author fkopp
 *
 */
public class PlayerClockUpdater {

    // -- a thread to update the clock --
    private Thread updater = new updateThread();
    private Label whitePlayer_name;
    private Label white_clock;
    private Label white_playertype;
    private ProgressBar white_progressbar;
    private Label blackPlayer_name;
    private Label black_clock;
    private Label black_playertype;
    private ProgressBar black_progressbar;

    /**
     * @param whitePlayer_name
     * @param white_clock
     * @param white_playertype
     * @param white_progressbar
     * @param blackPlayer_name
     * @param black_clock
     * @param black_playertype
     * @param black_progressbar
     */
    public PlayerClockUpdater(Label whitePlayer_name, Label white_clock, Label white_playertype, ProgressBar white_progressbar,
            Label blackPlayer_name, Label black_clock, Label black_playertype, ProgressBar black_progressbar) {

        this.whitePlayer_name = whitePlayer_name;
        this.white_clock = white_clock;
        this.white_playertype = white_playertype;
        this.white_progressbar = white_progressbar;
        this.blackPlayer_name = blackPlayer_name;
        this.black_clock = black_clock;
        this.black_playertype = black_playertype;
        this.black_progressbar = black_progressbar;

        clear();
        startUpdate();

    }

    /**
     *
     */
    public void clear() {

        this.whitePlayer_name.setText(Chessly.getPlayroom().getNameWhitePlayer());
        this.white_clock.setText("00:00:00");
        this.white_progressbar.setDisable(true);
        this.white_progressbar.setProgress(0);
        this.blackPlayer_name.setText(Chessly.getPlayroom().getNameBlackPlayer());
        this.black_clock.setText("00:00:00");
        this.black_progressbar.setDisable(true);
        this.black_progressbar.setProgress(0);

        PlayerType playerTypeWhite;
        PlayerType playerTypeBlack;
        playerTypeBlack=Chessly.getPlayroom().getPlayerTypeBlack();
        playerTypeWhite=Chessly.getPlayroom().getPlayerTypeWhite();

        if (playerTypeWhite == PlayerType.HUMAN) {
            this.white_playertype.setText("Human");
        } else {
            this.white_playertype.setText("Engine Level " + Chessly.getPlayroom().getCurrentEngineLevelWhite());
        }

        if (playerTypeBlack == PlayerType.HUMAN) {
            this.black_playertype.setText("Human");
        }  else {
            this.black_playertype.setText("Engine Level " + Chessly.getPlayroom().getCurrentEngineLevelBlack());
        }

    }

    /**
     *
     */
    public void update() {

        if (Chessly.getPlayroom().getCurrentGame() != null) {

            this.whitePlayer_name.setText(Chessly.getPlayroom().getNameWhitePlayer());
            this.blackPlayer_name.setText(Chessly.getPlayroom().getNameBlackPlayer());

            PlayerType playerTypeWhite;
            PlayerType playerTypeBlack;
            if (Chessly.getPlayroom().getCurrentGame() != null) {
                playerTypeBlack=Chessly.getPlayroom().getCurrentGame().getPlayerBlack().getPlayerType();
                playerTypeWhite=Chessly.getPlayroom().getCurrentGame().getPlayerWhite().getPlayerType();
            } else {
                playerTypeBlack=Chessly.getPlayroom().getPlayerTypeBlack();
                playerTypeWhite=Chessly.getPlayroom().getPlayerTypeWhite();
            }
            if (playerTypeWhite == PlayerType.HUMAN) {
                this.white_playertype.setText("Human");
            } else {
                this.white_playertype.setText("Engine Level " + Chessly.getPlayroom().getCurrentEngineLevelWhite());
            }

            if (playerTypeBlack == PlayerType.HUMAN) {
                this.black_playertype.setText("Human");
            }  else {
                this.black_playertype.setText("Engine Level " + Chessly.getPlayroom().getCurrentEngineLevelBlack());
            }

            if (Chessly.getPlayroom().isTimedGame()) {
                long whiteTotalTime = Chessly.getPlayroom().getTimeWhite();
                long blackTotalTime = Chessly.getPlayroom().getTimeBlack();
                long whiteCurrentTime = Chessly.getPlayroom().getCurrentGame().getWhiteClock().getTime();
                long blackCurrentTime = Chessly.getPlayroom().getCurrentGame().getBlackClock().getTime();

                this.white_clock.setText(HelperTools.formatTime(whiteTotalTime-whiteCurrentTime, false));
                this.black_clock.setText(HelperTools.formatTime(blackTotalTime-blackCurrentTime, false));

            } else {
                this.white_clock.setText(Chessly.getPlayroom().getCurrentGame().getWhiteClock().getFormattedTime());
                this.black_clock.setText(Chessly.getPlayroom().getCurrentGame().getBlackClock().getFormattedTime());
            }

            if (Chessly.getPlayroom().getCurrentGame()!= null && Chessly.getPlayroom().getCurrentGame().getCurBoard().getNextPlayerColor() == GameColor.WHITE) {
                this.white_progressbar.setProgress(-1);
                this.white_progressbar.setDisable(false);
                this.black_progressbar.setProgress(0);
                this.black_progressbar.setDisable(true);
                this.white_clock.setUnderline(true);
                this.black_clock.setUnderline(false);
            } else {
                this.white_progressbar.setProgress(0);
                this.white_progressbar.setDisable(true);
                this.black_progressbar.setProgress(-1);
                this.black_progressbar.setDisable(false);
                this.white_clock.setUnderline(false);
                this.black_clock.setUnderline(true);
            }
            if (Chessly.getPlayroom().getCurrentGame()!= null && Chessly.getPlayroom().getCurrentGame().isPaused()) {
                this.white_progressbar.setProgress(0);
                this.white_progressbar.setDisable(true);
                this.black_progressbar.setProgress(0);
                this.black_progressbar.setDisable(true);
            }
        } else {
            this.white_progressbar.setProgress(0);
            this.white_progressbar.setDisable(true);
            this.black_progressbar.setProgress(0);
            this.black_progressbar.setDisable(true);
            this.white_clock.setUnderline(false);
            this.black_clock.setUnderline(false);
        }

    }

    /**
     * Starts the updater thread
     */
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
                Platform.runLater(aUpdateRunnable);
                try {
                    // update every 200 ms
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
            try {
                update();
            } catch (NullPointerException e) {
                // ignore
            }
        }
    }

}
