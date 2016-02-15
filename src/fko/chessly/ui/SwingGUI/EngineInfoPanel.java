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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.player.ComputerPlayer;
import fko.chessly.player.Player;
import fko.chessly.player.computer.ObservableEngine;
import fko.chessly.util.HelperTools;

/**
 * Shows details of the engines internal calculation.
 *
 * @author Frank
 */
public class EngineInfoPanel extends JPanel {

    private static final long serialVersionUID = 6146971638142440871L;

    private final Playroom _model;

    private Thread updater = new updateThread();

    // -- if an engine color is set then the panel only shows this engine --
    private final GameColor _engineColor;

    private boolean _abort = false;

    private Game _updatedGame;

    private TitledBorder _titledBorder;
    private TitledBorder _titledBorder2;
    private TitledBorder _titledBorder3;
    private TitledBorder _titledBorder4;
    private TitledBorder _titledBorder5;
    private TitledBorder _titledBorder6;

    private static final Format numberFormat = new DecimalFormat();

    private int _fontSize = 10;

    private JPanel _infoPanel0;
    private JLabel curPVLabel,  curPV;
    private JPanel _infoPanel1,             _infoPanel2,                   _infoPanel3,                         _infoPanel4;
    private JLabel curMoveLabel,  curMove,  curNodesLabel,    curNodes,    curCacheSizeLabel,   curCacheSize,   curBoardCacheSizeLabel,   curBoardCacheSize;
    private JLabel curBestLabel,  curBest,  curSpeedLabel,    curSpeed,    curCacheUseLabel,    curCacheUse,    curBoardCacheUseLabel,    curBoardCacheUse;
    private JLabel curDepthLabel, curDepth, curBoardsLabel,   curBoards,   curCacheHitsLabel,   curCacheHits,   curBoardCacheHitsLabel,   curBoardCacheHits;
    private JLabel curTimeLabel,  curTime,  curNonQuietLabel, curNonQuiet, curCacheMissesLabel, curCacheMisses, curBoardCacheMissesLabel, curBoardCacheMisses;
    private JLabel                          curThreadsLabel,  curThreads,  curConfigLabel,      curConfig;
    // -- a panel displaying text (engine output) --
    private InfoPanel _infoPanel5;


    /**
     * Constructor
     * @param engineColor
     */
    public EngineInfoPanel(GameColor engineColor) {
        super();
        if (!engineColor.isBlack() && !engineColor.isWhite()) {
            throw new IllegalArgumentException(
                    "Parameter engineColor must be either ChesslyColor.BLACK or ChesslyColor.WHITE. Was " + engineColor);
        }
        _model = Chessly.getPlayroom();
        _engineColor=engineColor;
        this.buildPanel();
    }

    private void buildPanel() {
        setName("Engine Info Panel");

        final FlowLayout layout0 = new FlowLayout(FlowLayout.LEFT);
        _infoPanel0 = new JPanel(layout0);
        _infoPanel0.setOpaque(false);

        // -- 4 panels --
        final GridBagLayout layout1 = new GridBagLayout();
        _infoPanel1 = new JPanel(layout1);
        _infoPanel1.setOpaque(false);

        final GridBagLayout layout2 = new GridBagLayout();
        _infoPanel2 = new JPanel(layout2);
        _infoPanel2.setOpaque(false);

        final GridBagLayout layout3 = new GridBagLayout();
        _infoPanel3 = new JPanel(layout3);
        _infoPanel3.setOpaque(false);

        final GridBagLayout layout4 = new GridBagLayout();
        _infoPanel4 = new JPanel(layout4);
        _infoPanel4.setOpaque(false);

        // -- info panel --
        _infoPanel5 = new InfoPanel(_fontSize);
        _infoPanel5.setPreferredSize(new Dimension(_infoPanel5.getWidth(), 1000));

        // -- build the panels --
        setupLabels();
        layoutPanel();

        Font font = new Font("Lucida Console", Font.PLAIN, _fontSize);
        synchronized (_infoPanel0.getTreeLock()) {
            for (Component c : _infoPanel0.getComponents()) {
                c.setFont(font);
            }
        }
        synchronized (_infoPanel1.getTreeLock()) {
            for (Component c : _infoPanel1.getComponents()) {
                c.setFont(font);
            }
        }
        synchronized (_infoPanel2.getTreeLock()) {
            for (Component c : _infoPanel2.getComponents()) {
                c.setFont(font);
            }
        }
        synchronized (_infoPanel3.getTreeLock()) {
            for (Component c : _infoPanel3.getComponents()) {
                c.setFont(font);
            }
        }
        synchronized (_infoPanel4.getTreeLock()) {
            for (Component c : _infoPanel4.getComponents()) {
                c.setFont(font);
            }
        }
        synchronized (_infoPanel5.getTreeLock()) {
            for (Component c : _infoPanel5.getComponents()) {
                c.setFont(font);
            }
        }

        // -- layout the 4 panels in columns --
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(false);
        layout.setAutoCreateContainerGaps(false);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(_infoPanel0)
                .addGroup(
                        layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(_infoPanel1)
                        .addComponent(_infoPanel2)
                        .addComponent(_infoPanel3)
                        .addComponent(_infoPanel4)
                        )
                .addComponent(_infoPanel5));

        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                .addComponent(_infoPanel0)
                .addGroup(
                        layout.createSequentialGroup()
                        .addComponent(_infoPanel1)
                        .addComponent(_infoPanel2)
                        .addComponent(_infoPanel3)
                        .addComponent(_infoPanel4)
                        )
                .addComponent(_infoPanel5));

        updateGUI();
        startUpdate();

    }

    private void layoutPanel() {

        _infoPanel0.add(curPVLabel);
        _infoPanel0.add(curPV);

        // -- column 1 --

        GridBagHelper.constrain(_infoPanel1, curMoveLabel,  0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel1, curMove,       1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel1, curBestLabel,  0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel1, curBest,       1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel1, curDepthLabel, 0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel1, curDepth,      1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel1, curTimeLabel,  0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 1.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel1, curTime,       1, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 1.0, 0, 0, 4, 4);

        // -- column 2 --

        GridBagHelper.constrain(_infoPanel2, curNodesLabel,    0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel2, curNodes,         1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel2, curSpeedLabel,    0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel2, curSpeed,         1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel2, curBoardsLabel,   0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel2, curBoards,        1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel2, curNonQuietLabel, 0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel2, curNonQuiet,      1, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel2, curThreadsLabel,  0, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 1.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel2, curThreads,       1, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 1.0, 0, 0, 4, 4);

        // -- column 3 --

        GridBagHelper.constrain(_infoPanel3, curCacheSizeLabel,   0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel3, curCacheSize,        1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel3, curCacheUseLabel,    0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel3, curCacheUse,         1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel3, curCacheHitsLabel,   0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel3, curCacheHits,        1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel3, curCacheMissesLabel, 0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel3, curCacheMisses,      1, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel3, curConfigLabel,      0, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 1.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel3, curConfig,           1, 4, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 1.0, 0, 0, 4, 4);

        // -- column 4 --

        GridBagHelper.constrain(_infoPanel4, curBoardCacheSizeLabel,   0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel4, curBoardCacheSize,        1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel4, curBoardCacheUseLabel,    0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel4, curBoardCacheUse,         1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel4, curBoardCacheHitsLabel,   0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel4, curBoardCacheHits,        1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 4, 4);

        GridBagHelper.constrain(_infoPanel4, curBoardCacheMissesLabel, 0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 1.0, 0, 0, 4, 4);
        GridBagHelper.constrain(_infoPanel4, curBoardCacheMisses,      1, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1.0, 1.0, 0, 0, 4, 4);

    }

    private void setupLabels() {

        // curPV
        curPVLabel = new JLabel("PV:");
        curPV = new JLabel();
        // curMove
        curMoveLabel = new JLabel("Move:");
        curMove = new JLabel();
        // curValue
        curBestLabel = new JLabel("Best:");
        curBest = new JLabel();
        // curDepth
        curDepthLabel = new JLabel("Depth:");
        curDepth = new JLabel();
        // curDepth
        curNodesLabel = new JLabel("Nodes:");
        curNodes = new JLabel();

        // curDepth
        curSpeedLabel = new JLabel("Speed:");
        curSpeed = new JLabel();
        // curDepth
        curTimeLabel = new JLabel("Time:");
        curTime = new JLabel();
        // curBoards
        curBoardsLabel = new JLabel("Boards:");
        curBoards = new JLabel();
        // curNonQuiet
        curNonQuietLabel = new JLabel("NonQuiet:");
        curNonQuiet = new JLabel();
        // curCacheMisses
        curCacheMissesLabel = new JLabel("Node Cache Misses:");
        curCacheMisses = new JLabel();
        // curCacheHits
        curCacheHitsLabel = new JLabel("Node Cache Hits:");
        curCacheHits = new JLabel();
        // curTotalCache
        curCacheSizeLabel = new JLabel("Node Cache Size:");
        curCacheSize = new JLabel();
        // curUsedCache
        curCacheUseLabel = new JLabel("Node Cache Use:");
        curCacheUse = new JLabel();
        // curCacheMisses
        curBoardCacheMissesLabel = new JLabel("Board Cache Misses:");
        curBoardCacheMisses = new JLabel();
        // curCacheHits
        curBoardCacheHitsLabel = new JLabel("Board Cache Hits:");
        curBoardCacheHits = new JLabel();
        // curTotalCache
        curBoardCacheSizeLabel = new JLabel("Board Cache Size:");
        curBoardCacheSize = new JLabel();
        // curUsedCache
        curBoardCacheUseLabel = new JLabel("Board Cache Use:");
        curBoardCacheUse = new JLabel();
        // curThreads
        curThreadsLabel = new JLabel("Threads:");
        curThreads      = new JLabel();
        // curConfig
        curConfigLabel = new JLabel("Config:");
        curConfig      = new JLabel();

    }

    /**
     * clears all including the player name
     */
    private void clearAll() {
        setBorder(new TitledBorder(new EtchedBorder(), " "));
        clear();
    }

    /**
     * Clears the info panel but leaves the player name
     * Does not clear the text panel.
     */
    private void clear() {
        // curPV
        curPV.setText("");
        curNodes.setText("");
        curSpeed.setText("");
        curBoards.setText("");
        curNonQuiet.setText("");
        curCacheHits.setText("");
        curCacheMisses.setText("");
        curCacheSize.setText("");
        curCacheUse.setText("");
        curBoardCacheHits.setText("");
        curBoardCacheMisses.setText("");
        curBoardCacheSize.setText("");
        curBoardCacheUse.setText("");
        curDepth.setText("");
        lightenText();
    }

    /**
     * clears the info panel while the other player's turn
     */
    private void lightenText() {
        curPV.setForeground(Color.lightGray);
        curMove.setForeground(Color.lightGray);
        curBest.setForeground(Color.lightGray);
        curTime.setForeground(Color.lightGray);
        curNodes.setForeground(Color.lightGray);
        curSpeed.setForeground(Color.lightGray);
        curBoards.setForeground(Color.lightGray);
        curNonQuiet.setForeground(Color.lightGray);
        curCacheHits.setForeground(Color.lightGray);
        curCacheMisses.setForeground(Color.lightGray);
        curCacheSize.setForeground(Color.lightGray);
        curCacheUse.setForeground(Color.lightGray);
        curBoardCacheHits.setForeground(Color.lightGray);
        curBoardCacheMisses.setForeground(Color.lightGray);
        curBoardCacheSize.setForeground(Color.lightGray);
        curBoardCacheUse.setForeground(Color.lightGray);
        curDepth.setForeground(Color.lightGray);
        curThreads.setForeground(Color.lightGray);
        curConfig.setForeground(Color.lightGray);
    }

    /**
     *
     */
    private void darkenText() {
        curPV.setForeground(Color.black);
        curMove.setForeground(Color.black);
        curBest.setForeground(Color.black);
        curTime.setForeground(Color.black);
        curNodes.setForeground(Color.black);
        curSpeed.setForeground(Color.black);
        curBoards.setForeground(Color.black);
        curNonQuiet.setForeground(Color.black);
        curCacheHits.setForeground(Color.black);
        curCacheMisses.setForeground(Color.black);
        curCacheSize.setForeground(Color.black);
        curCacheUse.setForeground(Color.black);
        curBoardCacheHits.setForeground(Color.black);
        curBoardCacheMisses.setForeground(Color.black);
        curBoardCacheSize.setForeground(Color.black);
        curBoardCacheUse.setForeground(Color.black);
        curDepth.setForeground(Color.black);
        curThreads.setForeground(Color.black);repaint();
        curConfig.setForeground(Color.black);
    }

    private void initialUpdate(Game game) {
        _titledBorder = new TitledBorder(new EtchedBorder(), ("Player: "+game.getPlayerBlack().getName()));
        _titledBorder2 = new TitledBorder(new EtchedBorder(), ("Player: "+game.getPlayerBlack().getName()+" <WAITING>"));
        _titledBorder3 = new TitledBorder(new EtchedBorder(), ("Player: "+game.getPlayerBlack().getName()+" <no info available>"));
        _titledBorder4 = new TitledBorder(new EtchedBorder(), ("Player: "+game.getPlayerWhite().getName()));
        _titledBorder5 = new TitledBorder(new EtchedBorder(), ("Player: "+game.getPlayerWhite().getName()+" <WAITING>"));
        _titledBorder6 = new TitledBorder(new EtchedBorder(), ("Player: "+game.getPlayerWhite().getName()+" <no info available>"));
    }

    private void updateGUI() {
        Game game = _model.getCurrentGame();

        if (game != null) {
            if (game.isRunning()) {

                if (_updatedGame != game) {
                    initialUpdate(game);
                    _updatedGame = game;
                }

                if (_engineColor.isBlack()) {
                    this.setBorder(_titledBorder);
                    // -- only update the details when it is a COMPUTER player with an engine --
                    if (game.getPlayerBlack() instanceof ComputerPlayer) {
                        if (game.getPlayerBlack().isWaiting()) {
                            lightenText();
                            this.setBorder(_titledBorder2);
                            updateGUI(game, game.getPlayerBlack());
                        } else {
                            darkenText();
                            updateGUI(game, game.getPlayerBlack());
                        }
                    } else {
                        clear();
                        this.setBorder(_titledBorder3);
                    }
                } else if (_engineColor.isWhite()) {
                    this.setBorder(_titledBorder4);
                    // -- only update the details when it is a COMPUTER player with an engine --
                    if (game.getPlayerWhite() instanceof ComputerPlayer) {
                        if (game.getPlayerWhite().isWaiting()) {
                            lightenText();

                            this.setBorder(_titledBorder5);
                            updateGUI(game, game.getPlayerWhite());
                        } else {
                            darkenText();
                            updateGUI(game, game.getPlayerWhite());
                        }
                    } else {
                        clear();
                        this.setBorder(_titledBorder6);
                    }
                }
            }
        } else {
            clearAll();
        }
        game = null;
    }

    private void updateGUI(Game game, Player player) {
        // -- we can only watch the engine when the Interface "TreeSearchEngineWatcher" is implemented --
        if (((ComputerPlayer)player).getEngine() instanceof ObservableEngine) {

            ObservableEngine engine = (ObservableEngine)((ComputerPlayer)player).getEngine();

            // Print verbose or debug info into the info text panel
            String newInfoText = engine.getInfoText();
            if (!newInfoText.isEmpty()) _infoPanel5.printInfo(newInfoText);

            curPV.setText(printCurPV(game, engine.getCurrentPV(), engine.getCurrentMaxValueMove()));

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
                engineShowCurValue(
                        engine.getCurrentMaxValueMove()
                        );
            }

            // -- current search depth --
            curDepth.setText(engine.getCurrentSearchDepth()+"/"+engine.getCurrentMaxSearchDepth());

            // -- current number of checked nodes --
            curNodes.setText(numberFormat.format(engine.getTotalNodes()) + " N");

            // -- current number of nodes per second --
            curSpeed.setText(numberFormat.format(engine.getCurrentNodesPerSecond()) + " N/s");

            // -- current time used for the move --
            engineShowCurTime(engine.getCurrentUsedTime());

            // -- show the number of boards analysed so far --
            curBoards.setText(numberFormat.format(engine.getTotalBoards()) + " B");

            // -- show the number of non-quiet boards found so far --
            curNonQuiet.setText(numberFormat.format(engine.getTotalNonQuietBoards()) + " NB");

            // -- show the current capacity of the node cache --
            final int curNodeCacheSize = engine.getCurNodeCacheSize();
            curCacheSize.setText(numberFormat.format(curNodeCacheSize));

            // -- show the numer of nodes in the cache --
            final int curNodesInCache = engine.getCurNodesInCache();
            int percent = (int)(100.F * curNodesInCache / curNodeCacheSize);
            curCacheUse.setText(numberFormat.format(curNodesInCache)+" ("+percent+"%)");

            // -- show the number of cache hits ans misses so far --
            long cachehits = engine.getNodeCacheHits();
            long cachemisses = engine.getNodeCacheMisses();
            percent = (int) (100.0F * ((float) cachehits / (float) (cachehits + cachemisses)));
            curCacheMisses.setText(numberFormat.format(cachemisses));
            curCacheHits.setText(numberFormat.format(cachehits) + " (" + percent + "%)");

            // -- show the current capacity of the board cache --
            final int curBoardCacheSize2 = engine.getCurBoardCacheSize();
            curBoardCacheSize.setText(numberFormat.format(curBoardCacheSize2));

            // -- show the number of boards in the cache --
            final int curBoardsInCache = engine.getCurBoardsInCache();
            percent = (int)(100.F * curBoardsInCache / curBoardCacheSize2);
            curBoardCacheUse.setText(numberFormat.format(curBoardsInCache)+ " (" + percent + "%)");

            // -- show the number of cache hits ans misses so far --
            cachehits = engine.getBoardCacheHits();
            cachemisses = engine.getBoardCacheMisses();
            percent = (int) (100.0F * ((float) cachehits / (float) (cachehits + cachemisses)));
            curBoardCacheMisses.setText(numberFormat.format(cachemisses));
            curBoardCacheHits.setText(numberFormat.format(cachehits) + " (" + percent + "%)");

            // -- show the number of threads currently used/configured
            curThreads.setText(numberFormat.format(engine.getCurNumberOfThreads()));

            // -- show the current config as string
            curConfig.setText(engine.getCurConfig());

        } else {
            clear();
            this.setBorder(
                    new TitledBorder(
                            new EtchedBorder(),
                            ("Engine info: "+player.getName()+" <no info available>")));
        }
    }

    /**
     * shows the move the engine is currently working on
     * @param move
     */
    private void engineShowCurMove(int nextMoveNumber, GameMove move, int moveNumber, int numberOfMoves) {
        this.curMove.setText(move.toString() + " (" + moveNumber + '/' + numberOfMoves+")");
    }

    /**
     * shows the highest value and the move so far
     * @param move
     */
    private void engineShowCurValue(GameMove move) {

        String text = printCurValue(move);
        this.curBest.setText(text);

    }

    /**
     * shows the time elapsed so far
     */
    private void engineShowCurTime(long time) {
        final String timeString = HelperTools.formatTime(time, true);
        curTime.setText(timeString);
    }

    /**
     * starts the background thread to update the panel regularly
     */
    private void startUpdate() {
        updater.start();
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
     */
    public static String printCurPV(Game game, List<GameMove> list, GameMove maxMove) {
        String s = "";
        if (list == null || list.size() == 0) return s;
        int mn = game.getCurBoard().getNextHalfMoveNumber()+1;
        for (int i = 0; i < list.size(); ++i) {
            if (i == 0 || (mn+i)%2 == 0) {
                s += (int)(Math.ceil((mn+i)/2)) +". ";
            }
            s += list.get(i)+" ";
        }
        int value = maxMove.getValue();
        s += "(";
        if (value >= 0) {
            s += "+";
        }
        s += value + ")";
        return s;
    }

    private class updateThread extends Thread {

        private updateThread() {
            super("EngineInfoUpdater");
            setPriority(Thread.MIN_PRIORITY);
            setDaemon(true);
        }

        @Override
        public void run() {
            updateRunnable aUpdateRunnable = new updateRunnable();
            while (true) {
                SwingUtilities.invokeLater(aUpdateRunnable);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // -- ignore --
                }
                if (_abort) {
                    return;
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
}
