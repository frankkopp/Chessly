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
package fko.chessly.player;

import java.util.Observable;
import java.util.Observer;

import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.mvc.ModelObservable;
import fko.chessly.mvc.ModelEvents.PlayerDependendModelEvent;
import fko.chessly.util.StatusController;

/**
 * <p>This class is the abstract core implementation of a Chessly player.
 * It implements the thread handling and getters to fields.</p>
 *
 * <p>A player has always a certain state:
 * <b>WAITING, THINKING, HAS_MOVE, STOPPED</b></p>
 *
 * <p>This class is thread safe!</p>
 */
public abstract class AbstractPlayer extends ModelObservable implements Player, Observer {

    // Current status of a player.
    // Class is inline at the end of this file.
    protected final PlayerStatusController _playerStatus = new PlayerStatusController(Player.WAITING);

    private volatile Thread _playerThread = null;
    private final Object _playerThreadLock = new Object();

    // Fields
    private String     _name;
    private GameColor _color;
    private Game      _game;
    private GameBoard _curBoard = null;
    private GameMove  _playerMove = null;

    /**
     * Constructor is non-public as it is used by subclasses only
     * @param game
     * @param name
     * @param color
     */
    protected AbstractPlayer(Game game, String name, GameColor color) {
        this._game = game;
        this._name = name;
        this._color = color;
        _playerStatus.setInterruptState(Player.STOPPED);
    }

    /**
     * Constructor is non-public as it is used by subclasses only
     * @param name
     * @param color
     */
    protected AbstractPlayer(String name, GameColor color) {
        this._name = name;
        this._color = color;
        this._game = null;
        _playerStatus.setInterruptState(Player.STOPPED);

    }

    /**
     * This method may be overwritten to do some extra stuff when startPlayer() is called and
     * before the actual Thread is started.
     */
    protected void startPlayerPrepare() {
        // do nothing in abstract class
    }

    /**
     * Starts the player in a new thread. Calls setCurrentGame() to set the game reference as the current
     * game of this player.
     */
    @Override
    public void startPlayer(Game game) {
        _game = game;
        // we call this method so that this method can be overwritten by subclasses
        startPlayerPrepare();
        synchronized(_playerThreadLock) {
            if (_playerThread == null) {
                _playerThread = new Thread(this, "Player: " + _name);
                _playerThread.setPriority(Thread.MIN_PRIORITY);
                _playerThread.setDaemon(true);
                _playerThread.start();
            }
        }
    }

    /**
     * Stop the player by interrupting the players thread and setting its status to Player.STOPPED
     */
    @Override
    public void stopPlayer() {
        synchronized(_playerThreadLock) {
            if (_playerThread != null) {
                _playerThread.interrupt();
            }
        }
        _playerStatus.setStatus(Player.STOPPED);
        joinPlayerThread();
    }

    /**
     * Wait until the player thread dies. Ignores InterruptedException.
     */
    @Override
    public void joinPlayerThread() {
        synchronized(_playerThreadLock) {
            while (_playerThread != null) {
                try {
                    _playerThread.join();
                } catch (InterruptedException e) {
                    // -- ignore exception --
                } catch (NullPointerException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * The worker method for Runnable. Does the actual playing by calling getMove() and handling
     * the different status a player can be in.<br/>
     * It loops until the player is stopped (Player.STOPPED).
     */
    @Override
    public void run() {

        try {

            // -- tell the views that model has changed --
            // -- the player thread is actually running now --
            setChanged();
            notifyObservers(new PlayerDependendModelEvent("PLAYER "+_color+ " Thread started", this, SIG_PLAYER_THREAD_STARTED));

            // We want to know what happens in the game
            _game.addObserver(this);

            // If player is stopped we exit out of all waits
            _playerStatus.setInterruptState(Player.STOPPED);

            // -- run until thread is stopped --
            while (!_playerStatus.inStatus(Player.STOPPED)) {
                // -- wait until we are asked to return a new move --
                _playerStatus.waitWhileInState(Player.WAITING);
                // -- wait for state THINKING --
                _playerStatus.waitForState(Player.THINKING);
                // -- startGame thinking --
                if (_playerStatus.inStatus(Player.THINKING)) {
                    setChanged();
                    notifyObservers(new PlayerDependendModelEvent("PLAYER "+_color+ " requesting move from either human or player", this, SIG_PLAYER_REQUESTING_MOVE));
                    _playerMove = getMove();
                    setChanged();
                    notifyObservers(new PlayerDependendModelEvent("PLAYER "+_color+ " reveicved move from either human or player", this, SIG_PLAYER_RECEIVED_MOVE));
                    _playerStatus.writeLock().lock();
                    try {
                        if (this._playerMove != null && !_playerStatus.inStatus(Player.STOPPED)) {
                            // move received
                            _playerStatus.setStatus(Player.HAS_MOVE);
                        } else if (this._playerMove == null && _game.undoMoveFlag() && !_playerStatus.inStatus(Player.STOPPED)) {
                            // null move received -- indicator undoMove
                            _playerStatus.setStatus(Player.HAS_MOVE);
                        }
                    } finally {
                        _playerStatus.writeLock().unlock();
                    }
                }
                _playerStatus.waitWhileInState(Player.HAS_MOVE);
            }

        } catch (Exception e) {
            // I case of an exception set the player to stopped and we re-throw the exception
            _playerStatus.setStatus(Player.STOPPED);
            System.err.println(e.toString());
            e.printStackTrace(System.out);
            throw new RuntimeException(e);

        }
        _playerThread = null;
        // -- tell the views that model has changed --
        // -- the player thread is actually ended now --
        setChanged();
        notifyObservers(new PlayerDependendModelEvent("PLAYER "+_color+ " Thread finished", this, SIG_PLAYER_THREAD_FINISHED));


    }

    /**
     * Return next move by waiting for a move given by the player thread in run().<br/>
     * This is usually called from a game object when it is this player's turn.<br/>
     * It sets the player's status to Player.THINKING and waits for a move (Player.HAS_MOVE)
     * through calling waitForState(Player.HAS_MOVE).
     * The move will be returned and the player will be set to status Player.WAITING.
     * @param board where the next move shall be made
     * @return the move the player wants to play for the given board
     */
    @Override
    public GameMove getNextMove(GameBoard board) {
        _curBoard = board;

        // change state to THINKING so that the player thread
        // starts thinking as this is called by a different thread!
        _playerStatus.writeLock().lock();
        try {
            if (!_playerStatus.inStatus(Player.STOPPED)) {
                _playerStatus.setStatus(Player.THINKING);
            }
        } finally {
            _playerStatus.writeLock().unlock();
        }

        // -- tell the views that model has changed --
        setChanged();

        // -- wait until we found a move --
        _playerStatus.waitForState(Player.HAS_MOVE);

        // -- move found -> change state to WAITING --
        _playerStatus.writeLock().lock();
        try {
            if (!_playerStatus.inStatus(Player.STOPPED)) {
                _playerStatus.setStatus(Player.WAITING);
            }
        } finally {
            _playerStatus.writeLock().unlock();
        }

        return _playerMove;
    }

    /**
     * Returns the current game the player is in.
     * @return current game
     */
    @Override
    public Game getCurrentGame() {
        return _game;
    }

    /**
     * A Player acts as an observer to the game. This method should be overwritten by the subclasses
     * to actually process the updates if needed.<br/>
     * In this implementation this method does nothing at all.<br/>
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param o   the observable object.
     * @param arg an argument passed to the <code>notifyObservers</code>
     *            method.
     */
    @Override
    public void update(Observable o, Object arg) {
        // do nothing by default
    }

    /**
     * Returns the player's color.<br/>
     * Colors are defined in interface Player.
     * @return player's color as defined in interface Player
     */
    @Override
    public GameColor getColor() {
        return _color;
    }

    /**
     * Sets the player's color.<br/>
     * Colors are defined in interface Player.
     * @param color as defined in interface Player
     */
    @Override
    public void setColor(GameColor color) {
        this._color = color;
    }

    /**
     * Returns the name of the player.
     * @return name of player
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * Sets the name of the player.
     * @param name of player
     */
    @Override
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Returns the current board for which the player shall find a move.<br/>
     * The current board is set by getNextMove().
     * @return board the player shall find a move for
     */
    protected GameBoard getCurBoard() {
        return _curBoard;
    }

    /**
     * Returns if the player is in state Player.WAITING
     * @return true if player is in state Player.WAITING
     */
    @Override
    public boolean isWaiting() {
        return _playerStatus.inStatus(WAITING);
    }

    /**
     * Waits until player is in waiting
     * @see StatusController
     */
    @Override
    public void waitUntilWaiting() {
        _playerStatus.waitForState(Player.WAITING);
    }

    /**
     * Returns if the player is in state Player.THINKING
     * @return true if player is in state Player.THINKING
     */
    @Override
    public boolean isThinking() {
        return _playerStatus.inStatus(THINKING);
    }

    /**
     * Returns if the player is in state Player.HAS_MOVE
     * @return true if player is in state Player.HAS_MOVE
     */
    @Override
    public boolean hasMove() {
        return _playerStatus.inStatus(HAS_MOVE);
    }

    /**
     * Returns if the player is in state Player.STOPPED
     * @return true if player is in state Player.STOPPED
     */
    @Override
    public boolean isStopped() {
        return _playerStatus.inStatus(STOPPED);
    }

    /**
     * Returns a string representation of the player.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append("Name: ").append(_name)
                .append(" Color: ").append(_color)
                .append(" Type: ").append(this.getPlayerType())
                .toString();
    }

    /**
     * This extends a StatusController to control legal status changes
     * by overriding the checkTransition method and defining legal transition
     * there.
     */
    public static class PlayerStatusController extends StatusController {
        private PlayerStatusController(int initialState) {
            super(initialState);
            this.setTransitionCheck(true);
        }

        @Override
        protected synchronized boolean checkTransition(int sourceState, int targetState) {
            if (sourceState == targetState) {
                return true;
            }
            switch (sourceState) {
                /**
                 * Define which states are allowed when currently in a certain state.
                 */
                case Player.WAITING:
                    switch (targetState) {
                        case Player.THINKING:
                            return true;
                        case Player.STOPPED:
                            return true;
                        default:
                            return false;
                    }
                case Player.THINKING:
                    switch (targetState) {
                        case Player.HAS_MOVE:
                            return true;
                        case Player.STOPPED:
                            return true;
                        default:
                            return false;
                    }
                case Player.HAS_MOVE:
                    switch (targetState) {
                        case Player.WAITING:
                            return true;
                        case Player.STOPPED:
                            return true;
                        default:
                            return false;
                    }
                default :
                    return false;
            }
        }
    }

    /** */
    public static final int SIG_PLAYER_THREAD_STARTED = 4000;
    /** */
    public static final int SIG_PLAYER_THREAD_FINISHED = 4010;
    /** */
    public static final int SIG_PLAYER_REQUESTING_MOVE = 4020;
    /** */
    public static final int SIG_PLAYER_RECEIVED_MOVE = 4030;


}
