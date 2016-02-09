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

import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.player.ComputerPlayer;
import fko.chessly.player.Player;
import fko.chessly.player.computer.ObservableEngine;
import fko.chessly.ui.JavaFX_GUI.JavaFX_GUI_Controller.EngineInfoLabels;
import fko.chessly.util.HelperTools;
import javafx.application.Platform;

/**
 * This class updates the engine information.
 *
 * @author fkopp
 */
public class EngineInfoUpdater {

    private final Playroom _model;

    private final Thread _updater;

    private final EngineInfoLabels _engineLabels;

    private GameColor _color;

    private static final Format numberFormat = new DecimalFormat();

    /**
     * @param color
     * @param engineInfoLabels
     */
    public EngineInfoUpdater(GameColor color, EngineInfoLabels engineInfoLabels) {
        this._color = color;
        this._engineLabels = engineInfoLabels;
        this._model = Chessly.getPlayroom();

        clearAll();
        updateUI();
        _updater = new updateThread();
        _updater.start();
    }

    /**
     * Clears all field including tab label
     */
    private void clearAll() {
        _engineLabels.engineTab.setText("Engine Info: "+_color.toString());
        clear();

    }

    /**
     * clears all fields but not tab label
     */
    private void clear() {
        _engineLabels.pv_label.setText("");
        _engineLabels.currentMove_label.setText("");
        _engineLabels.bestMove_label.setText("");
        _engineLabels.depth_label.setText("");
        _engineLabels.time_label.setText("");
        _engineLabels.nodes_label.setText("");
        _engineLabels.speed_label.setText("");
        _engineLabels.boards_label.setText("");
        _engineLabels.nonQuiet_label.setText("");
        _engineLabels.ncSize_label.setText("");
        _engineLabels.ncUse_label.setText("");
        _engineLabels.ncHits_label.setText("");
        _engineLabels.ncMisses_label.setText("");
        _engineLabels.bcSize_label.setText("");
        _engineLabels.bcUse_label.setText("");
        _engineLabels.bcHits_label.setText("");
        _engineLabels.bcMisses_label.setText("");
        _engineLabels.config_label.setText("Configuration: ");
        _engineLabels.status_label.setText("Status: ");
    }

    /**
     * updates all fields with values from the engine
     */
    private void updateUI() {
        Game game = _model.getCurrentGame();

        if (game != null) {
            if (game.isRunning()) {

                String tabTitel1 = "Player: "+game.getPlayerBlack().getName();
                String tabTitel3 = "Player: "+game.getPlayerBlack().getName()+" <no info available>";
                String tabTitel4 = "Player: "+game.getPlayerWhite().getName();
                String tabTitel6 = "Player: "+game.getPlayerWhite().getName()+" <no info available>";

                if (_color.isWhite()) {
                    _engineLabels.engineTab.setText(tabTitel4);
                    // -- only update the details when it is a COMPUTER player with an engine --
                    if (game.getPlayerWhite() instanceof ComputerPlayer) {
                        if (game.getPlayerWhite().isWaiting()) {
                            _engineLabels.engineTab.setStyle("-fx-font-weight: normal");
                            updateUI(game, game.getPlayerWhite());
                        } else {
                            _engineLabels.engineTab.setStyle("-fx-font-weight: bold");
                            updateUI(game, game.getPlayerWhite());
                        }
                    } else {
                        clear();
                        _engineLabels.engineTab.setText(tabTitel6);
                    }
                } else if (_color.isBlack()){
                    _engineLabels.engineTab.setText(tabTitel1);
                    // -- only update the details when it is a COMPUTER player with an engine --
                    if (game.getPlayerBlack() instanceof ComputerPlayer) {
                        if (game.getPlayerBlack().isWaiting()) {
                            _engineLabels.engineTab.setStyle("-fx-font-weight: normal");
                            updateUI(game, game.getPlayerBlack());
                        } else {
                            _engineLabels.engineTab.setStyle("-fx-font-weight: bold");
                            updateUI(game, game.getPlayerBlack());
                        }
                    } else {
                        clear();
                        _engineLabels.engineTab.setText(tabTitel3);
                    }
                }
            }
        } else {
            clearAll();
        }
        game = null;
    }

    /**
     * @param game
     * @param playerBlack
     */
    private void updateUI(Game game, Player player) {
        // -- we can only watch the engine when the Interface "TreeSearchEngineWatcher" is implemented --
        if (((ComputerPlayer)player).getEngine() instanceof ObservableEngine) {
            ObservableEngine engine = (ObservableEngine)((ComputerPlayer)player).getEngine();

            _engineLabels.status_label.setText("Status: " + engine.getStatusText() +" "+engine.getState()+" "+engine.getPonderMove());

            // Print verbose or debug info into the info text panel
            String newInfoText = engine.getInfoText();
            if (!newInfoText.isEmpty() && _engineLabels.infoArea.isVisible()) _engineLabels.infoArea.printInfo(newInfoText);

            // PV label
            _engineLabels.pv_label.setText(printCurPV(game, engine.getPV(), engine.getMaxValueMove()));

            // -- current move in calculation --
            if (engine.getCurMove() != null) {
                engineShowCurMove(
                        game.getCurBoard().getNextHalfMoveNumber(),
                        engine.getCurMove(),
                        engine.getCurMoveNumber(),
                        engine.getNumberOfMoves()
                        );
            }

            // -- current calculated value for the best move so far --
            if (engine.getMaxValueMove() != null) {
                engineShowCurValue(
                        engine.getMaxValueMove()
                        );
            }

            // -- current search depth --
            _engineLabels.depth_label.setText(engine.getCurSearchDepth()+"/"+engine.getCurExtraSearchDepth());

            // -- current time used for the move --
            engineShowCurTime(engine.getCurUsedTime());

            // -- current number of checked nodes --
            _engineLabels.nodes_label.setText(numberFormat.format(engine.getNodesChecked()) + " N");

            // -- current number of nodes per second --
            _engineLabels.speed_label.setText(numberFormat.format(engine.getCurNodesPerSecond()) + " N/s");

            // -- show the number of boards analyzed so far --
            _engineLabels.boards_label.setText(numberFormat.format(engine.getBoardsChecked()) + " B");

            // -- show the number of non-quiet boards found so far --
            _engineLabels.nonQuiet_label.setText(numberFormat.format(engine.getBoardsNonQuiet()) + " NB");

            // -- show the current capacity of the node cache --
            final int curNodeCacheSize = engine.getCurNodeCacheSize();
            _engineLabels.ncSize_label.setText(numberFormat.format(curNodeCacheSize));

            // -- show the numer of nodes in the cache --
            final int curNodesInCache = engine.getCurNodesInCache();
            int percent = (int)(100.F * curNodesInCache / curNodeCacheSize);
            _engineLabels.ncUse_label.setText(numberFormat.format(curNodesInCache)+" ("+percent+"%)");

            // -- show the number of cache hits ans misses so far --
            long cachehits = engine.getNodeCacheHits();
            long cachemisses = engine.getNodeCacheMisses();
            percent = (int) (100.0F * ((float) cachehits / (float) (cachehits + cachemisses)));
            _engineLabels.ncHits_label.setText(numberFormat.format(cachehits) + " (" + percent + "%)");
            _engineLabels.ncMisses_label.setText(numberFormat.format(cachemisses));

            // -- show the current capacity of the board cache --
            final int curBoardCacheSize2 = engine.getCurBoardCacheSize();
            _engineLabels.bcSize_label.setText(numberFormat.format(curBoardCacheSize2));

            // -- show the number of boards in the cache --
            final int curBoardsInCache = engine.getCurBoardsInCache();
            percent = (int)(100.F * curBoardsInCache / curBoardCacheSize2);
            _engineLabels.bcUse_label.setText(numberFormat.format(curBoardsInCache)+ " (" + percent + "%)");

            // -- show the number of cache hits ans misses so far --
            cachehits = engine.getBoardCacheHits();
            cachemisses = engine.getBoardCacheMisses();
            percent = (int) (100.0F * ((float) cachehits / (float) (cachehits + cachemisses)));
            _engineLabels.bcHits_label.setText(numberFormat.format(cachehits) + " (" + percent + "%)");
            _engineLabels.bcMisses_label.setText(numberFormat.format(cachemisses));

            // -- show the current config as string
            _engineLabels.config_label.setText("Configuration: "+engine.getCurConfig());

        } else {
            clearAll();
        }
    }

    /**
     * shows the move the engine is currently working on
     * @param move
     */
    private void engineShowCurMove(int nextMoveNumber, GameMove move, int moveNumber, int numberOfMoves) {
        _engineLabels.currentMove_label.setText(move.toString() + " (" + moveNumber + '/' + numberOfMoves+")");
    }

    /**
     * shows the highest value and the move so far
     * @param move
     */
    private void engineShowCurValue(GameMove move) {
        _engineLabels.bestMove_label.setText(printCurValue(move));
    }

    /**
     * shows the time elapsed so far
     */
    private void engineShowCurTime(long time) {
        final String timeString = HelperTools.formatTime(time, true);
        _engineLabels.time_label.setText(timeString);
    }

    /**
     * @param move
     * @return String with formatted value
     */
    private static String printCurValue(GameMove move) {
        String text = "";
        if (move==null) return text;

        text += move + " (";

        int value = move.getValue();
        if (value > 0) {
            text += "+";
        }
        if (value == Integer.MAX_VALUE) {
            text += "max ";
        } else if (value == -Integer.MAX_VALUE) {
            text += "min ";
        } else {
            text += value + ")";
        }
        return text;
    }

    /**
     * @param game
     * @param list
     * @param maxMove
     * @return String with formatted PV
     * FIXME - wrong when pondering
     */
    public String printCurPV(Game game, List<GameMove> list, GameMove maxMove) {

        String s = "";

        if (list == null || list.size() == 0) return s;

        int halfMoveNumber = game.getCurBoard().getLastHalfMoveNumber();
        GameColor nextColor = game.getNextPlayer().getColor();

        if (_color.equals(nextColor)) {
            //System.out.println("thinking");
        } else {
            //System.out.println("pondering");
            halfMoveNumber++;
            nextColor = nextColor.getInverseColor();
        }

        // engine color
        int whiteOffset = _color.isWhite() ? 1 : 0;

        // First move in list needs extra attention
        if (nextColor.isBlack()) {
            // 3. ... e7-e5
            s += ((halfMoveNumber+1)>>1)+whiteOffset +". ... "+list.get(0)+" ";
        } else {
            // 4. e2-e4
            s += ((halfMoveNumber+1)>>1)+whiteOffset +". "+list.get(0)+" ";
        }

        // increase half move number for next move
        halfMoveNumber++;
        nextColor = nextColor.getInverseColor();

        // nextColor = _
        for (int i = 1; i < list.size(); ++i) {
            // show number before each white move
            if (nextColor.equals(GameColor.WHITE)) {
                s += ((halfMoveNumber+i+1)>>1) +". ";
            }
            s += list.get(i)+" ";
            nextColor = nextColor.getInverseColor();
        }

        int value = maxMove.getValue();
        s += "(";
        if (value >= 0) {
            s += "+";
        }
        s += value + ")";
        return s;
    }

    /**
     *
     */
    private class updateThread extends Thread {

        private updateThread() {
            super("EngineInfoUpdater"+_engineLabels.engineTab.getText());
            setPriority(Thread.MIN_PRIORITY);
            setDaemon(true);
        }

        @Override
        public void run() {
            updateRunnable aUpdateRunnable = new updateRunnable();
            while (true) {
                Platform.runLater(aUpdateRunnable);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // -- ignore --
                }
            }
        }

        private class updateRunnable implements Runnable {
            @Override
            public void run() {
                updateUI();
            }

        }
    }

}
