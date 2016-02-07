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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import fko.chessly.game.Game;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.mvc.ModelEvents.ModelEvent;

/**
 * A player representing a human usually interacting with a user interface.
 * It provides methods to ask for a move, check if a move is wanted and also a method to receive
 * a move and to give it back to the caller.
 * The caller usual calls getMove() requesting a user move. getMove() sets the class' state to
 * wantsMove()==true and signals its observers that it is awaiting a move.
 * getMove() then blocks in a wait() until it gets a move through a call to setMove(Move).
 */
public class HumanPlayer extends AbstractPlayer {

    private AtomicBoolean _wantsMove       = new AtomicBoolean(false);;
    private AtomicBoolean _wantsUndoMove   = new AtomicBoolean(false);
    private AtomicBoolean _isReadyToReturn = new AtomicBoolean(false);;

    private AtomicReference<GameMove> _move = new AtomicReference<GameMove>(null);

    // lock for use instead of "this"
    private final Object _lock = new Object();

    /**
     * This constructor is protected to indicate to use the PlayerFactory to create
     * a new player of this kind
     * @param game - a back reference to the game the player plays in
     * @param name - the name of the player
     * @param color - the color the player has in the current game
     */
    protected HumanPlayer(Game game, String name, GameColor color) {
        super(game, name, color);
    }

    /**
     * This constructor is protected to indicate to use the PlayerFactory to create
     * a new player of this kind
     * @param name - the name of the player
     * @param color - the color the player has in the current game
     */
    protected HumanPlayer(String name, GameColor color) {
        super(name, color);
    }

    /**
     * Returns the int value of the PlayerType for a given player
     */
    @Override
    public PlayerType getPlayerType() {
        return PlayerType.HUMAN;
    }

    /**
     * Called by Game: Implementation of getMove() to determine the next move. For a human player we must
     * do a little extra work in here. First we set a "wantsMove" flag and notify the observers.
     * Usually at least one observer is the gui and there the gui now knows we want a new move
     * from the user.
     * This method checks if we have got a move (setMove) and if not waits until we have one.
     * You can interrupt the wait for a move by telling the player to stop (player.stop())
     * and notifying the current thread.
     * The wait is also interrupted when we want to undo a move - the move will be set to null
     * and returned.
     * @return next move
     */
    @Override
    public GameMove getMove() {

        // indicate that we want to get a move
        setChanged();
        notifyObservers(new ModelEvent("HUMAN PLAYER requesting move from human (UI)", SIG_HUMAN_PLAYER_WANTS_MOVE));

        ModelEvent event = null;
        synchronized (_lock) {

            _isReadyToReturn.set(false);
            _wantsMove.set(true);

            // test if we are stopped or have a move and wait otherwise
            while (!_isReadyToReturn.get()) {

                try {
                    if (!_wantsUndoMove.get()) { // double check not set in the meantime
                        _lock.wait(); // and release lock
                    }
                } catch (InterruptedException e) {
                    // ignore
                }

                // game is over or stopped
                if (this.isStopped()) {
                    _move.set(null);
                    event = new ModelEvent("HUMAN PLAYER player stopped", SIG_HUMAN_PLAYER_PLAYER_STOPPED);

                } else if (_wantsUndoMove.get()) {
                    _move.set(null);
                    event = new ModelEvent("HUMAN PLAYER requesting UNDO move", SIG_HUMAN_PLAYER_UNDO_MOVE);

                } else if (_isReadyToReturn.get()) {
                    assert _move.get() != null;
                    event = new ModelEvent("HUMAN PLAYER received move from human (UI)", SIG_HUMAN_PLAYER_HAS_MOVE);

                } else {
                    throw new RuntimeException("HUMAN PLAYER invalid state");
                }
                _isReadyToReturn.set(true);
                _wantsMove.set(false);
                _wantsUndoMove.set(false);
            }
        }

        // tell the observers that we don't need a move anymore
        setChanged();
        notifyObservers(event);

        return _move.get();
    }

    /**
     * Called by UI: Set the move and reset the indication that we want a move (wantsMove()==false)
     * @param newMove
     */
    public void setMove(GameMove newMove) {
        synchronized (_lock) {
            // if we are not waiting we ignore that
            if (!_wantsMove.get()) return;
            // receive move from UI
            _move.set(newMove);
            _wantsMove.set(false);
            _wantsUndoMove.set(false);
            _isReadyToReturn.set(true);
            // tell getMove() that we have a move and that is should come back from wait()
            _lock.notifyAll();
        }
    }

    /**
     * Called by Game: Tells the player the the game wants to undo a move.
     */
    public void undoMove() {
        synchronized (_lock) {
            // receive undo move from Game
            _move.set(null);
            _wantsMove.set(false);
            _wantsUndoMove.set(true);
            _isReadyToReturn.set(true);
            // tell getMove() that we have a move and that is should come back from wait()
            _lock.notifyAll();
        }
    }

    /**
     * Is the HumanPlayer waiting for a move?
     * @return yes if a move is expected via setMove(Move m)
     */
    public boolean wantsMove() {
        return _wantsMove.get();
    }

    // message for the observers
    /** */public static final int SIG_HUMAN_PLAYER_WANTS_MOVE = 3000;
    /** */public static final int SIG_HUMAN_PLAYER_HAS_MOVE = 3010;
    /** */public static final int SIG_HUMAN_PLAYER_UNDO_MOVE = 3015;
    /** */public static final int SIG_HUMAN_PLAYER_PLAYER_STOPPED = 3020;

}
