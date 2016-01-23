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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Observable;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameColor;
import fko.chessly.mvc.ModelEvents.ModelEvent;
import fko.chessly.player.HumanPlayer;
import fko.chessly.ui.UserInterface;

/**
 * Graphical User Interface for the Chessly game.
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public final class SwingGUI implements UserInterface {

    // -- the MVC controller handles all user input --
    private MVController _MVController;

    // -- to save and restore the last position of our window
    private static final WindowState windowState = new WindowState();

    // -- the main window --
    private MainWindow mainWindow;
    // -- a clock panel with clocks for both players --
    private ClockPanel clockPanel;
    // -- a panel displaying the reversi board --
    private BoardPanel boardPanel;
    // -- a panel displaying text --
    private InfoPanel infoPanel;
    // -- a panel with the current engine information for the black player --
    private SatelliteWindow engineInfoWindowBlack;
    private EngineInfoPanel engineInfoBlack;
    // -- a panel with the current engine information for the white player --
    private SatelliteWindow engineInfoWindowWhite;
    private EngineInfoPanel engineInfoWhite;
    // -- a panel with table of moves --
    private SatelliteWindow moveListWindow;
    private MoveList moveList;

    // -- get values from properties --
    private boolean _showPossibleMoves = Boolean.valueOf(Chessly.getProperties().getProperty("ui.showPossibleMoves"));
    private boolean _showMoveListWindow = Boolean.valueOf(Chessly.getProperties().getProperty("ui.showMoveListWindow"));
    private boolean _showEngineInfoWindowBlack = Boolean.valueOf(Chessly.getProperties().getProperty("ui.showEngineBlackInfoWindow"));
    private boolean _showEngineInfoWindowWhite = Boolean.valueOf(Chessly.getProperties().getProperty("ui.showEngineWhiteInfoWindow"));

    /**
     * Constructor
     */
    public SwingGUI() {

        // -- set Windows look & feel --
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // -- create the controller object --
        _MVController = new MVController(this);

        // -- create main window --
        mainWindow = new MainWindow(this);

        // -- clock panel --
        clockPanel = new ClockPanel(this);
        mainWindow.getContentPane().add(clockPanel, BorderLayout.NORTH);

        // -- board panel --
        boardPanel = new BoardPanel(this);
        mainWindow.getWorkPane().add(boardPanel);

        // -- info panel --
        infoPanel = new InfoPanel();
        mainWindow.getWorkPane().add(infoPanel);

        // -- move list --
        moveList = new MoveList();
        moveListWindow = new SatelliteWindow("Move List", "movelist");
        moveListWindow.add(moveList, BorderLayout.CENTER);
        // -- show move list --
        if (_showMoveListWindow) {
            moveListWindow.setVisible(true);
        }
        // -- close frame handler --
        moveListWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainWindow.getMenu().getShowMoveList().setState(false);
            }
        });

        // -- engine info pane for black player --
        engineInfoBlack = new EngineInfoPanel(GameColor.BLACK);
        engineInfoBlack.setBorder(new TitledBorder(new EtchedBorder(), "Engine Info Black"));
        engineInfoWindowBlack = new SatelliteWindow("Engine Info Black", "engine_info_black");
        engineInfoWindowBlack.add(engineInfoBlack, BorderLayout.CENTER);
        engineInfoWindowBlack.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainWindow.getMenu().getShowEngineInfoBlack().setState(false);
            }
        });
        // -- show engine info --
        if (_showEngineInfoWindowBlack) {
            engineInfoWindowBlack.setVisible(true);
        }

        // -- engine info pane for white player --
        engineInfoWhite = new EngineInfoPanel(fko.chessly.game.GameColor.WHITE);
        engineInfoWhite.setBorder(new TitledBorder(new EtchedBorder(), "Engine Info White"));
        engineInfoWindowWhite = new SatelliteWindow("Engine Info White", "engine_info_white");
        engineInfoWindowWhite.add(engineInfoWhite, BorderLayout.CENTER);
        engineInfoWindowWhite.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainWindow.getMenu().getShowEngineInfoWhite().setState(false);
            }
        });
        // -- show engine info --
        if (_showEngineInfoWindowWhite) {
            engineInfoWindowWhite.setVisible(true);
        }

        // -- show main window and give the focus to it --
        mainWindow.setVisible(true);
        mainWindow.requestFocus();
    }

    /**
     * is called when model Playroom changed
     */
    private void updateFromPlayroom(Playroom playroom, ModelEvent event) {

        // Playroom is playing - game exists
        if (playroom.isPlaying() && playroom.getCurrentGame() != null) {
            // -- game is initialized --
            if (event.signals(Playroom.SIG_PLAYROOM_GAME_CREATED)) {
                // -- now we want to observe the game --
                playroom.getCurrentGame().addObserver(this);
                // -- there are human players we need to observe them as well to see if they
                // -- want to have a move
                if (playroom.getCurrentGame().getPlayerBlack() instanceof HumanPlayer) {
                    ((Observable) playroom.getCurrentGame().getPlayerBlack()).addObserver(this);
                }
                if (playroom.getCurrentGame().getPlayerWhite() instanceof HumanPlayer) {
                    ((Observable) playroom.getCurrentGame().getPlayerWhite()).addObserver(this);
                }
                // -- check if multiple games in a row should be run --
                if (playroom.getNumberOfGames() > 1) {
                    infoPanel.printInfo(
                            ">>> Multiple games: Game " +
                                    playroom.getCurrentGameNumber() +
                                    " of " +
                                    playroom.getNumberOfGames()
                            );
                }
            }

            // -- finished playing a game --
            if (playroom.getCurrentGame().isFinished() &&
                    event.signals(Playroom.SIG_PLAYROOM_GAME_FINISHED)) {
                // -- check if multiple games in a row should be run --
                if (playroom.getNumberOfGames() > 1) {
                    infoPanel.printInfoln(
                            ">>> Black wins: " + playroom.getCurrentBlackWins() + '\n' +
                            ">>> White wins: " + playroom.getCurrentWhiteWins() + '\n' +
                            ">>> Draws     : " + playroom.getCurrentDraws() + '\n'
                            );
                    infoPanel.printInfoln("");
                }
            }
            updateFromGame(playroom.getCurrentGame(), event);

            // Playroom is not playing - game still exists
        } else if (!playroom.isPlaying() && playroom.getCurrentGame() != null) {
            // -- multiple games completed --
            if (playroom.getCurrentGame().isFinished()) {
                if (event.signals(Playroom.SIG_PLAYROOM_THREAD_END)) {
                    // -- check if multiple games in a row should be run --
                    if (playroom.getNumberOfGames() > 1) {
                        infoPanel.printInfoln("");
                        infoPanel.printInfoln(
                                ">>> Multiple games finished:" + '\n' +
                                ">>> Black wins: " + playroom.getCurrentBlackWins() +
                                " (" + (int)(((float) playroom.getCurrentBlackWins() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n" +
                                ">>> White wins: " + playroom.getCurrentWhiteWins() +
                                " (" + (int)(((float) playroom.getCurrentWhiteWins() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n" +
                                ">>> Draws     : " + playroom.getCurrentDraws() +
                                " (" + (int)(((float) playroom.getCurrentDraws() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n"
                                );
                        infoPanel.printInfoln("");
                    }
                }
            }
            updateFromGame(playroom.getCurrentGame(), event);
            // No game exists
        } else {
            // -- set possible actions (menu) --
            mainWindow.getMenu().getNewGameAction().setEnabled(true);
            mainWindow.getMenu().getStopGameAction().setEnabled(false);
            mainWindow.getMenu().getPauseGameAction().setEnabled(false);
            mainWindow.getMenu().getResumeGameAction().setEnabled(false);
            mainWindow.getMenu().getUndoMoveAction().setEnabled(false);
            mainWindow.getMenu().getTimedGameAction().setEnabled(true);
            mainWindow.getMenu().getTimeBlackAction().setEnabled(true);
            mainWindow.getMenu().getTimeWhiteAction().setEnabled(true);
            mainWindow.getMenu().getBlackLevelAction().setEnabled(true);
            mainWindow.getMenu().getWhiteLevelAction().setEnabled(true);
            mainWindow.getMenu().getShowPossibleMovesAction().setEnabled(true);
            mainWindow.getMenu().getShowEngineInfoBlackAction().setEnabled(true);
            mainWindow.getMenu().getShowEngineInfoWhiteAction().setEnabled(true);
            mainWindow.getMenu().getShowMovelistAction().setEnabled(true);
            mainWindow.repaint();
        }
    }

    /**
     * is called when model Game changed
     */
    private void updateFromGame(Game game, ModelEvent event) {

        // -- draw the current board of the current game --
        boardPanel.setAndDrawBoard(game.getCurBoard());
        // -- update the move list according to the moves in the current game --
        moveList.drawMove(game);
        // -- get the current status of the game --
        int status = game.getStatus();

        // -- find out state of the game --
        switch (status) {

            case Game.GAME_INITIALIZED:
                gameInitializedGuiUpdate(game, event);
                break;

            case Game.GAME_RUNNING:
                gameRunningGuiUpdate(game);
                break;

            case Game.GAME_OVER:
                gameOverGuiUpdate(game, event);
                break;

            case Game.GAME_PAUSED:
                gamePausedGuiUpdate();
                break;

            case Game.GAME_FINISHED:
                gameFinishedGuiUpdate();
                break;

        }
    }

    private void updateFromHumanPlayer(HumanPlayer hp, ModelEvent event) {
        _MVController.setMoveReceiver(hp);
    }

    private void gameFinishedGuiUpdate() {
        // -- set possible actions --
        mainWindow.getMenu().getNewGameAction().setEnabled(true);
        mainWindow.getMenu().getStopGameAction().setEnabled(false);
        mainWindow.getMenu().getPauseGameAction().setEnabled(false);
        mainWindow.getMenu().getResumeGameAction().setEnabled(false);
        mainWindow.getMenu().getUndoMoveAction().setEnabled(false);
        mainWindow.getMenu().getTimedGameAction().setEnabled(true);
        mainWindow.getMenu().getTimeBlackAction().setEnabled(true);
        mainWindow.getMenu().getTimeWhiteAction().setEnabled(true);
        mainWindow.getMenu().getBlackLevelAction().setEnabled(true);
        mainWindow.getMenu().getWhiteLevelAction().setEnabled(true);
        mainWindow.getMenu().getShowPossibleMovesAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoBlackAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoWhiteAction().setEnabled(true);
        mainWindow.getMenu().getShowMovelistAction().setEnabled(true);
        mainWindow.getStatusPanel().setStatusMsg("GAME STOPPED!");
    }

    private void gamePausedGuiUpdate() {
        // -- set possible actions --
        mainWindow.getMenu().getNewGameAction().setEnabled(false);
        mainWindow.getMenu().getStopGameAction().setEnabled(false);
        mainWindow.getMenu().getPauseGameAction().setEnabled(false);
        mainWindow.getMenu().getResumeGameAction().setEnabled(true);
        mainWindow.getMenu().getUndoMoveAction().setEnabled(false);
        mainWindow.getMenu().getTimedGameAction().setEnabled(false);
        mainWindow.getMenu().getTimeBlackAction().setEnabled(false);
        mainWindow.getMenu().getTimeWhiteAction().setEnabled(false);
        mainWindow.getMenu().getBlackLevelAction().setEnabled(true);
        mainWindow.getMenu().getWhiteLevelAction().setEnabled(true);
        mainWindow.getMenu().getShowPossibleMovesAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoBlackAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoWhiteAction().setEnabled(true);
        mainWindow.getMenu().getShowMovelistAction().setEnabled(true);
        mainWindow.getStatusPanel().setStatusMsg("GAME PAUSED!");
    }

    private void gameOverGuiUpdate(Game game, ModelEvent event) {
        // -- only write something to the infoPanel when update came from a certain status
        // -- otherwise we get multiple outputs in the infoPanel for the same state
        if (event.signals(Game.SIG_GAME_OVER)) {
            infoPanel.printInfoln("Game over!");
            if (game.getGameOverCause() == Game.GAMEOVER_CHECKMATE
                    || game.getGameOverCause() == Game.GAMEOVER_STALEMATE) {
                infoPanel.printInfoln("Game Over!");
            } else if (game.getGameOverCause() == Game.GAMEOVER_TIME_IS_UP_FOR_ONE_PLAYER) {
                infoPanel.printInfoln("Out of time!");
            } else if (game.getGameOverCause() == Game.GAMEOVER_ONE_PLAYER_HAS_RESIGNED) {
                infoPanel.printInfoln("Resign!");
            } else {
                Chessly.fatalError(this.getClass().toString() + "Game Over without a valid reason!"+game.getGameOverCause());
            }
            if (game.getGameWinnerStatus() == Game.WINNER_BLACK) {
                infoPanel.printInfoln("Winner: BLACK");
            } else if (game.getGameWinnerStatus() == Game.WINNER_WHITE) {
                infoPanel.printInfoln("Winner: WHITE");
            } else if (game.getGameWinnerStatus() == Game.WINNER_DRAW) {
                infoPanel.printInfoln("Winner: DRAW");
            } else {
                Chessly.fatalError(this.getClass().toString() + "Game Over without a valid result!");
            }
            infoPanel.printInfoln("--- Game over -------------------------");
        }
        // -- set possible actions --
        mainWindow.getMenu().getNewGameAction().setEnabled(true);
        mainWindow.getMenu().getStopGameAction().setEnabled(false);
        mainWindow.getMenu().getPauseGameAction().setEnabled(false);
        mainWindow.getMenu().getResumeGameAction().setEnabled(false);
        mainWindow.getMenu().getUndoMoveAction().setEnabled(false);
        mainWindow.getMenu().getTimedGameAction().setEnabled(true);
        mainWindow.getMenu().getTimeBlackAction().setEnabled(true);
        mainWindow.getMenu().getTimeWhiteAction().setEnabled(true);
        mainWindow.getMenu().getBlackLevelAction().setEnabled(true);
        mainWindow.getMenu().getWhiteLevelAction().setEnabled(true);
        mainWindow.getMenu().getShowPossibleMovesAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoBlackAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoWhiteAction().setEnabled(true);
        mainWindow.getMenu().getShowMovelistAction().setEnabled(true);
        mainWindow.getStatusPanel().setStatusMsg("GAME OVER!");
    }

    private void gameRunningGuiUpdate(Game game) {
        if (mainWindow.getMenu().getResumeGameAction().isEnabled()) {
            mainWindow.getStatusPanel().setStatusMsg("GAME RESUMED!");
        } else {
            mainWindow.getStatusPanel().setStatusMsg("GAME RUNNING!");
        }
        // -- set possible actions --
        mainWindow.getMenu().getNewGameAction().setEnabled(false);
        mainWindow.getMenu().getStopGameAction().setEnabled(true);
        mainWindow.getMenu().getPauseGameAction().setEnabled(true);
        mainWindow.getMenu().getResumeGameAction().setEnabled(false);
        if (game.undoMoveFlag()) {
            mainWindow.getMenu().getUndoMoveAction().setEnabled(false);
        } else if (game.getNextPlayer() instanceof HumanPlayer
                && game.getBoardHistory().size() > 2) {
            mainWindow.getMenu().getUndoMoveAction().setEnabled(true);
        } else {
            mainWindow.getMenu().getUndoMoveAction().setEnabled(false);
        }
        mainWindow.getMenu().getTimedGameAction().setEnabled(false);
        mainWindow.getMenu().getTimeBlackAction().setEnabled(false);
        mainWindow.getMenu().getTimeWhiteAction().setEnabled(false);
        mainWindow.getMenu().getBlackLevelAction().setEnabled(true);
        mainWindow.getMenu().getWhiteLevelAction().setEnabled(true);
        mainWindow.getMenu().getShowPossibleMovesAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoBlackAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoWhiteAction().setEnabled(true);
        mainWindow.getMenu().getShowMovelistAction().setEnabled(true);
    }

    private void gameInitializedGuiUpdate(Game game, ModelEvent event) {
        // -- game object exists --
        mainWindow.getStatusPanel().setStatusMsg("New Game started!");
        // -- game is initialized --
        mainWindow.getStatusPanel().setStatusMsg("GAME INITILAZIED!");

        // -- only write something to the infoPanel when update came from a certain playe
        // -- otherwise we get multiple outputs in the infoPanel for the same state
        if (event.signals(Playroom.SIG_PLAYROOM_GAME_CREATED)) {
            infoPanel.printInfoln("");
            infoPanel.printInfoln("--- New Game started ------------------");
            infoPanel.printInfoln("Player BLACK: "+game.getPlayerBlack().getName());
            infoPanel.printInfoln("Player WHITE: "+game.getPlayerWhite().getName());
            infoPanel.printInfoln("");
        }
        // -- new game so clear the move list --
        moveList.clear();
        // -- set possible actions --
        mainWindow.getMenu().getNewGameAction().setEnabled(false);
        mainWindow.getMenu().getStopGameAction().setEnabled(false);
        mainWindow.getMenu().getPauseGameAction().setEnabled(false);
        mainWindow.getMenu().getResumeGameAction().setEnabled(false);
        mainWindow.getMenu().getUndoMoveAction().setEnabled(false);
        mainWindow.getMenu().getTimedGameAction().setEnabled(false);
        mainWindow.getMenu().getTimeBlackAction().setEnabled(false);
        mainWindow.getMenu().getTimeWhiteAction().setEnabled(false);
        mainWindow.getMenu().getBlackLevelAction().setEnabled(true);
        mainWindow.getMenu().getWhiteLevelAction().setEnabled(true);
        mainWindow.getMenu().getShowPossibleMovesAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoBlackAction().setEnabled(true);
        mainWindow.getMenu().getShowEngineInfoWhiteAction().setEnabled(true);
        mainWindow.getMenu().getShowMovelistAction().setEnabled(true);
    }


    /**
     * This method is called when we want to exit the application
     */
    public void exitReversi() {
        if (mainWindow.userConfirmation("Do you really want to quit?") == JOptionPane.NO_OPTION) {
            return;
        }
        // -- close the move list window --
        moveListWindow.closeWindowAction();
        // -- close the engine info window --
        engineInfoWindowBlack.closeWindowAction();
        // -- close the engine info window --
        engineInfoWindowWhite.closeWindowAction();
        // -- close the main window --
        mainWindow.closeWindowAction();
        // -- save the window state --
        windowState.save();
        // -- tell the model to clean up and exit the programm--
        this._MVController.exitReversi();
    }

    /**
     * shall all possible move be marked in the board display
     * @return true if enabled
     */
    public boolean is_showPossibleMoves() {
        return _showPossibleMoves;
    }

    /**
     * shall all possible move be marked in the board display
     */
    public void set_showPossibleMoves(boolean togglePossibleMoves) {
        this._showPossibleMoves = togglePossibleMoves;
    }

    /**
     * is the the move list wondow shown
     * @return boolean
     */
    public boolean is_showMoveListWindow() {
        return _showMoveListWindow;
    }

    /**
     * shall the move list window be shown
     * @param toggleMoveListWindow
     */
    public void set_showMoveListWindow(boolean toggleMoveListWindow) {
        this._showMoveListWindow = toggleMoveListWindow;
    }

    /**
     * is the the engine info window shown
     * @return boolean
     */
    public boolean is_showEngineInfoWindowBlack() {
        return _showEngineInfoWindowBlack;
    }

    /**
     * shall the engine info window be shown
     * @param toggleEngineInfoWindowBlack
     */
    public void set_showEngineInfoWindowBlack(boolean toggleEngineInfoWindowBlack) {
        this._showEngineInfoWindowBlack = toggleEngineInfoWindowBlack;
    }

    /**
     * is the the engine info window shown
     * @return boolean
     */
    public boolean is_showEngineInfoWindowWhite() {
        return _showEngineInfoWindowWhite;
    }

    /**
     * shall the engine info window be shown
     * @param toggleEngineInfoWindowWhite
     */
    public void set_showEngineInfoWindowWhite(boolean toggleEngineInfoWindowWhite) {
        this._showEngineInfoWindowWhite = toggleEngineInfoWindowWhite;
    }

    public MVController getController() {
        return _MVController;
    }

    public static WindowState getWindowState() {
        return windowState;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public ClockPanel getClockPanel() {
        return clockPanel;
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    public InfoPanel getInfoPanel() {
        return infoPanel;
    }


    public SatelliteWindow getEngineInfoWindowBlack() {
        return engineInfoWindowBlack;
    }

    public EngineInfoPanel getEngineInfoBlack() {
        return engineInfoBlack;
    }

    public SatelliteWindow getEngineInfoWindowWhite() {
        return engineInfoWindowWhite;
    }

    public EngineInfoPanel getEngineInfoWhite() {
        return engineInfoWhite;
    }

    public SatelliteWindow getMoveListWindow() {
        return moveListWindow;
    }

    public MoveList getMoveList() {
        return moveList;
    }

    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param model   the observable object.
     * @param event   an argument passed to the <code>notifyObservers</code>
     *                method.
     */
    @Override
    public void update(Observable model, Object event) {

        ModelEvent mevent = (ModelEvent)event;

        try {

            // Dispatch the update to the appropriate method
            // depending an who is calling

            // Playroom
            if (model instanceof Playroom) {
                if (SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeLater(
                            new updateFromPlayroomRunnable((Playroom) model, mevent));
                } else {
                    SwingUtilities.invokeAndWait(
                            new updateFromPlayroomRunnable((Playroom) model, mevent));
                }
                // Game
            } else if (model instanceof Game) {
                if (SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeLater(
                            new updateFromGameRunnable((Game) model, mevent));
                } else {
                    SwingUtilities.invokeAndWait(
                            new updateFromGameRunnable((Game) model, mevent));
                }
                // HumanPlayer
            } else if (model instanceof HumanPlayer) {
                if (SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeLater(
                            new updateFromHumanPlayerRunnable((HumanPlayer)model, mevent));
                } else {
                    SwingUtilities.invokeAndWait(
                            new updateFromHumanPlayerRunnable((HumanPlayer)model, mevent));
                }
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Runnable called by the Observer update() method
     * through invokeAndWait() or invokeLater()
     */
    private class updateFromPlayroomRunnable implements Runnable {
        private final Playroom _playroom;

        private final ModelEvent _event;


        private updateFromPlayroomRunnable(Playroom playroom, ModelEvent newEvent) {
            this._playroom = playroom;
            this._event = newEvent;
        }

        @Override
        public void run() {
            updateFromPlayroom(_playroom, _event);
            mainWindow.repaint();
        }
    }

    /**
     * Runnable called by the Observer update() method
     * through invokeAndWait() or invokeLater()
     */
    private class updateFromGameRunnable implements Runnable {
        private final Game _game;
        private final ModelEvent _event;

        private updateFromGameRunnable(Game game, ModelEvent newParameter) {
            this._game = game;
            this._event = newParameter;
        }

        @Override
        public void run() {
            updateFromGame(_game, _event);
            mainWindow.repaint();
        }
    }

    /**
     * Runnable called by the Observer update() method
     * through invokeAndWait() or invokeLater()
     */
    private class updateFromHumanPlayerRunnable implements Runnable {
        private final HumanPlayer _humanPlayer;
        private final ModelEvent _event;

        private updateFromHumanPlayerRunnable(HumanPlayer humanPlayer, ModelEvent newEvent) {
            this._humanPlayer = humanPlayer;
            this._event = newEvent;
        }

        @Override
        public void run() {
            updateFromHumanPlayer(_humanPlayer, _event);
            mainWindow.repaint();
        }
    }

}
