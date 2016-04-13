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

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveList;
import fko.chessly.player.ComputerPlayer;
import fko.chessly.player.Player;
import fko.chessly.player.computer.ObservableEngine;
import fko.chessly.ui.JavaFX_GUI.JavaFX_GUI_Controller.EngineInfoLabels;
import fko.chessly.util.HelperTools;
import javafx.application.Platform;

/**
 * This class updates the engine information by using a thread that call <code>updateUI()</code>
 * regularly. <code>updateUI()</code> then determines which player is active and calls
 * <code>updateUI(Game, Player)</code> which reads informations from the model to update the view.
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

            _engineLabels.status_label.setText("Status: " + engine.getStatusText());

            // Print verbose or debug info into the info text panel
            String newInfoText = engine.getInfoText();
            if (!newInfoText.isEmpty() && _engineLabels.infoArea.isVisible()) _engineLabels.infoArea.printInfo(newInfoText);

            // PV label
            _engineLabels.pv_label.setText(printCurPV(game, player));

            // -- current move in calculation --
            if (engine.getCurrentMove() != null) {
                engineShowCurMove(
                        game.getCurBoard().getNextHalfMoveNumber(),
                        engine.getCurrentMove(),
                        engine.getCurrentMoveNumber(),
                        engine.getNumberOfMoves()
                        );
            }

            // -- current calculated value for the best move so far --
            if (engine.getCurrentMaxValueMove() != null) {
                engineShowCurValue(engine.getCurrentMaxValueMove());
            }

            // -- current search depth --
            _engineLabels.depth_label.setText(engine.getCurrentSearchDepth()+"/"+engine.getCurrentMaxSearchDepth());

            // -- current time used for the move --
            engineShowCurTime(engine.getCurrentUsedTime());

            // -- current number of checked nodes --
            _engineLabels.nodes_label.setText(numberFormat.format(engine.getTotalNodes()) + " N");

            // -- current number of nodes per second --
            _engineLabels.speed_label.setText(numberFormat.format(engine.getCurrentNodesPerSecond()) + " N/s");

            // -- show the number of boards analyzed so far --
            _engineLabels.boards_label.setText(numberFormat.format(engine.getTotalBoards()) + " B");

            // -- show the number of non-quiet boards found so far --
            _engineLabels.nonQuiet_label.setText(numberFormat.format(engine.getTotalNonQuietBoards()) + " NB");

            // -- show the current capacity of the node cache --
            final int curNodeCacheSize = engine.getCurrentNodeCacheSize();
            _engineLabels.ncSize_label.setText(numberFormat.format(curNodeCacheSize));

            // -- show the number of nodes in the cache --
            final int curNodesInCache = engine.getCurrentNodesInCache();
            int percent = (int)(100.F * curNodesInCache / curNodeCacheSize);
            _engineLabels.ncUse_label.setText(numberFormat.format(curNodesInCache)+" ("+percent+"%)");

            // -- show the number of cache hits and misses so far --
            long cachehits = engine.getNodeCacheHits();
            long cachemisses = engine.getNodeCacheMisses();
            percent = (int) (100.0F * ((float) cachehits / (float) (cachehits + cachemisses)));
            _engineLabels.ncHits_label.setText(numberFormat.format(cachehits) + " (" + percent + "%)");
            _engineLabels.ncMisses_label.setText(numberFormat.format(cachemisses));

            // -- show the current capacity of the board cache --
            final int curBoardCacheSize2 = engine.getCurrentBoardCacheSize();
            _engineLabels.bcSize_label.setText(numberFormat.format(curBoardCacheSize2));

            // -- show the number of boards in the cache --
            final int curBoardsInCache = engine.getCurrentBoardsInCache();
            percent = (int)(100.F * curBoardsInCache / curBoardCacheSize2);
            _engineLabels.bcUse_label.setText(numberFormat.format(curBoardsInCache)+ " (" + percent + "%)");

            // -- show the number of cache hits and misses so far --
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
     * Creates a correctly formatted PV string.
     * @param game
     * @param player
     * @return String with formatted PV
     *
     * FIXME: fix the numbering
     */
    public String printCurPV(Game game, Player player ) {

        // get the engine and the PV list
        ObservableEngine engine = (ObservableEngine) ((ComputerPlayer) player).getEngine();

        // retrieve copy of PV list
        GameMoveList list = engine.getCurrentPV();
        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder s = new StringBuilder();

        // get some info directly from the engine - doesn't matter if they change afterwards
        GameMove ponderMove = engine.getPonderMove();
        int engineState = engine.getState();

        // last move made
        int halfMoveNumber = game.getCurBoard().getNextHalfMoveNumber();
        GameColor nextColor = _color.getInverseColor();

        // are we watching white or black
        if (_color.isWhite()) { // we are white
            switch (engineState) {
                case ObservableEngine.IDLE:
                case ObservableEngine.THINKING:
                    // here white has next move

                    // white is always full= half/2 +1
                    s.append(((halfMoveNumber/2)+1) +". "+list.get(0)+" ");
                    // used up first move from list
                    list.remove(0);

                    // increase halfmovenumber and change side
                    halfMoveNumber += 1;
                    nextColor = GameColor.BLACK;

                    break;

                case ObservableEngine.PONDERING:
                    // here black has next move

                    // black is always full= half/2
                    s.append("Pondering: "+((halfMoveNumber)/2) +". ..."+ponderMove+" ");

                    // increase halfmovenumber and change side
                    halfMoveNumber += 1;
                    nextColor = GameColor.WHITE;

                    break;

                default:
                    break;
            }

        } else { // we are black
            switch (engineState) {
                case ObservableEngine.THINKING:
                    // here black has next move

                    // black is always full= half/2
                    s.append((halfMoveNumber/2) +". ... "+list.get(0)+" ");
                    list.remove(0);

                    // increase halfmovenumber and change side
                    halfMoveNumber += 1;
                    nextColor = GameColor.WHITE;

                    break;

                case ObservableEngine.PONDERING:
                    // here white has next move

                    // white is always full= half/2 +1
                    s.append("Pondering: "+((halfMoveNumber/2)+1) +". "+ponderMove+" ");

                    // increase halfmovenumber and change side
                    halfMoveNumber += 1;
                    nextColor = GameColor.BLACK;

                    break;

                default:
                    break;
            }
        }

        for (int i = 0; i < list.size(); ++i) {
            if (nextColor.equals(GameColor.WHITE)) {
                // white is always full= half/2 +1
                s.append(((halfMoveNumber+i)/2)+1 + ". ");
            }
            s.append(list.get(i).toString()+" ");
            nextColor = nextColor.getInverseColor();
        }

        s.append(printCurValue(engine.getCurrentMaxValueMove()));

        return s.toString();
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

                try {
                    updateUI();
                } catch (NullPointerException e) {
                    // ignore
                    // easier and cleaner that to avoid all timing issues by checking
                    // for != null

                }
            }

        }
    }

}
