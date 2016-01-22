/*
 * <p>GPL Dislaimer</p>
 * <p>
 * "Reversi by Frank Kopp"
 * Copyright (c) 2003-2015 Frank Kopp
 * mail-to:frank@familie-kopp.de
 *
 * This file is part of "Reversi by Frank Kopp".
 *
 * "Reversi by Frank Kopp" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * "Reversi by Frank Kopp" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Reversi by Frank Kopp"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </p>
 *
 *
 */

package fko.chessly.ui.SwingGUI;

import javax.swing.JOptionPane;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.player.HumanPlayer;
import fko.chessly.player.PlayerType;

/**
 * This class acts a the MVC controller an handles all user input actions.
 * It is the only class in the ReversiGUI package which writes to the model.
 */
public class MVController {

    // -- back reference to ui --
    private SwingGUI _ui;

    // -- reference to the _model (playroom) --
    private Playroom _model;

    // -- we only can tell the model the users move when we know which player we have to use --
    private HumanPlayer _moveReceiver = null;
    private final Object _moveReceiverLock = new Object();

    /**
     * creates a controller object with a back reference to the ui main class
     * @param ui
     */
    public MVController(SwingGUI ui) {
        this._ui = ui;
        this._model = Chessly.getPlayroom();
    }

   /**
    * This method is called from a mouse event from the user. It hands a move
    * over to a known HumanPlayer and then nulls the reference to the HumanPlayer. To
    * make a HumanPlayer known call setMoveReceiver(HumanPlayer). If no HumanPlayer is
    * known to the object nothing happens
    * @param point representing the coordinates on the current board
    */
   public void setPlayerMove(GameMove move) {
       synchronized(_moveReceiverLock) {
           if (_moveReceiver!=null) {
                _moveReceiver.setMove(move);
           }
           // After we have handed over the move to the receiver player we delete the reference
           // to the receiver. This will be set again by setMoveReceiver by  the observer update
           // through the SwingGUI
           _moveReceiver=null;
       }
   }

    /**
     * Is called when a HumanPlayer has notified its Oberservers (ReversiGUI) that it is waiting
     * for a move from a human player.
     * If the receiving player is know to the class it accepts new moves through setPlayerMove()
     * usual from mouse input
     * @param player
     */
    public void setMoveReceiver(HumanPlayer player) {
        synchronized(_moveReceiverLock) {
            _moveReceiver = player;
        }
    }

    /**
     * this method tells the playroom (_model) to set the black player type
     * @param type
     */
    public void setPlayerTypeBlackAction(PlayerType type) {
        _model.setPlayerTypeBlack(type);
    }

    /**
     * this method tells the playroom (_model) to set the black player type
     * @param type
     */
    public void setPlayerTypeWhiteAction(PlayerType type) {
        _model.setPlayerTypeWhite(type);
    }

    /**
     * sets the players name and starts a new game
     * @param blackName
     * @param whiteName
     */
    public void startNewGame(String blackName, String whiteName) {
        _model.setNameBlackPlayer(blackName);
        _model.setNameWhitePlayer(whiteName);
        newGame();
    }

    /**
     * displays a dialog to start a new game
     */
    public void newGameDialog() {
        NewGameDialog dialog = new NewGameDialog(_ui);
        AbstractDialog.centerComponent(dialog);
        dialog.setVisible(true);
    }
    
    /**
     * starts a new game
     */
    private synchronized void newGame() {
        if (!noCurrentGame()) {
            return;
        }
        _model.startPlayroom();
    }

    /**
     * stops a running game
     */
    public void stopCurrentGame() {
        if (_ui.getMainWindow().userConfirmation("Do you really want to stop the game?") == JOptionPane.YES_OPTION) {
            _model.stopPlayroom();
        }
    }

    /**
     * pauses a game
     */
    public void pauseOrResumeCurrentGame() {
        if (_model.getCurrentGame().isPaused()) {
           _model.getCurrentGame().resumeGame();
        } else if (_model.getCurrentGame().isRunning()) {
            _model.getCurrentGame().pauseGame();
        }
    }

    /**
     * Tell the model to clean up and exit
     */
    public void exitReversi() {
        _model.exitReversi();
    }
    
    /**
     * Undo Move
     */
    public void undoMove() {
	_ui.getMainWindow().getMenu().getUndoMoveAction().setEnabled(false);
	// take back two halfmoves
        _model.undoMove(2);
    }
    
    
    /**
     * toggles the timed game setting
     */
    public void toggleTimedGame() {
        _model.setTimedGame(!_model.isTimedGame());
    }

    /**
     * set the time for black
     * @param sec
     */
    public void setTimeBlackAction(int sec) {
        _model.setTimeBlack(sec);
    }

    /**
     * set the time for white
     * @param sec
     */
    public void setTimeWhiteAction(int sec) {
        _model.setTimeWhite(sec);
    }

    /**
     * set the level of the player
     * @param player
     * @param level
     */
    public void setLevelAction(GameColor player, int level) {
        if (player.isBlack()) {
            _model.setCurrentLevelBlack(level);
        } else if (player.isWhite()) {
            _model.setCurrentLevelWhite(level);
        } else {
            throw new IllegalArgumentException("Player color must be BLACK or WHITE. Was "+player.toString());
        }
    }

    /**
     * toggles the setting to show all possible moves
     */
    public void toggleShowPossibleMovesAction() {
        _ui.set_showPossibleMoves(_ui.getMainWindow().getMenu().getShowPossibleMoves().getState());
        _ui.getMainWindow().repaint();
    }

    /**
     * toggles the setting to show all possible moves
     */
    public void toggleShowMoveListAction() {
        _ui.set_showMoveListWindow(_ui.getMainWindow().getMenu().getShowMoveList().getState());
        _ui.getMoveListWindow().setVisible(_ui.getMainWindow().getMenu().getShowMoveList().getState());
    }


    /**
     * toggles the setting to show all possible moves
     */
    public void toggleShowEngineInfoBlackAction() {
        _ui.set_showEngineInfoWindowBlack(_ui.getMainWindow().getMenu().getShowEngineInfoBlack().getState());
        _ui.getEngineInfoWindowBlack().setVisible(_ui.getMainWindow().getMenu().getShowEngineInfoBlack().getState());
    }

    /**
     * toggles the setting to show all possible moves
     */
    public void toggleShowEngineInfoWhiteAction() {
        _ui.set_showEngineInfoWindowWhite(_ui.getMainWindow().getMenu().getShowEngineInfoWhite().getState());
        _ui.getEngineInfoWindowWhite().setVisible(_ui.getMainWindow().getMenu().getShowEngineInfoWhite().getState());
    }

    public void setNumberOfGamesAction(int number) {
        _model.setNumberOfGames(number);
    }
    
    /**
     * displays a dialog to set the number of threads
     */
    public void numberOfTreadsDialog() {
        EngineThreadsDialog dialog = new EngineThreadsDialog(_ui);
        AbstractDialog.centerComponent(dialog);
        dialog.setVisible(true);
    }

       /**
     * determines if the game is over or stopped
     * @return true when game is over or stopped
     */
    private boolean noCurrentGame() {
        if (_model.getCurrentGame() == null) {
            return true;
        }
        return _model.getCurrentGame().isFinished();
    }

}
