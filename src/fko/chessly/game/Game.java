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
package fko.chessly.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import fko.chessly.mvc.ModelObservable;
import fko.chessly.mvc.ModelEvents.ModelEvent;
import fko.chessly.mvc.ModelEvents.PlayerDependendModelEvent;
import fko.chessly.player.HumanPlayer;
import fko.chessly.player.Player;
import fko.chessly.util.StatusController;

/**
 * <p>This class handles everything needed to run a Chessly rules compliant game.</p>
 *
 * <p>The game class controls a single game of reversi using mainly a board and 2 players. It also has 2 timers
 * for each of the players.</p>
 *
 * <p>A game object can have the one of following states (defined in the Game class as constant fields):<br/>
 * <b>INITIALIZED | RUNNING | PAUSED | STOPPED | OVER | FINISHED</b></p>
 *
 * <p>A game can be in state <b>OVER</b> exactly if:<br/>
 * <ol>
 * <li>There are no more moves at all OR </li>
 * <li>Time is up for at least one player OR </li>
 * <li>One User has resigned the game </li>
 * </ol>
 * When a game is <b>OVER</b> there must be a <b>WINNER</b> or it is a <b>DRAW</b>.
 * </p>
 *
 * <p>A game runs in a seperate thread and must be started (startGame) after creation.</p>
 *
 * <p>A game needs two Players, times for the players if the game is timed.</p>
 *
 * <p>It knows which player has to do a move and asks the player for a move. If the player returns a move it is
 * checked if it is a legal move.<br/>
 * The class also checks if a game is over because there are no more moves our because a player ran out of time.</p>
 *
 * <p>A game can be paused and resumed which stops the timers for the players.</p>
 *
 * @author fkopp
 *
 */
public class Game extends ModelObservable implements Runnable, Observer {

    // Status fields
    private final GameStatusController _gameStatus    = new GameStatusController(Game.GAME_INITIALIZED);
    private final StatusController     _gameOverCause = new StatusController(Game.GAMEOVER_NONE);
    private final StatusController     _gameWinner    = new StatusController(Game.WINNER_NONE_YET);

    // Thread
    private Thread _gameThread = null;

    // Fields
    private GameBoard _curBoard;
    private Player _playerBlack;
    private Player _playerWhite;
    private GameClock _blackClock;
    private GameClock _whiteClock;
    private long _blackTime = 0;
    private long _whiteTime = 0;
    private boolean _isTimedGame = false;
    private GameMove _illegalMove = null;

    // saves each board after each move - used for "take back move"
    private final List<GameBoard> _boardHistory = new ArrayList<GameBoard>(256);

    // -- Flags --
    private AtomicBoolean _illegalMoveFlag = new AtomicBoolean(false);
    private AtomicBoolean _undoMoveFlag = new AtomicBoolean(false);
    private AtomicInteger _numberOfHalfmovesToUndo = new AtomicInteger(0);

    /**
     * Creates a new game object with given players
     * @param whitePlayer
     * @param blackPlayer
     * @param timeWhite
     * @param timeBlack
     * @param timedGame
     *
     * @throws IllegalArgumentException when invalid arguments has been used.
     */
    public Game(Player whitePlayer, Player blackPlayer, long timeWhite,
            long timeBlack, boolean timedGame) {

        // Assert parameter
        if (blackPlayer==null || whitePlayer == null) {
            throw new NullPointerException("Paramter blackPlayer and whitePlayer must not be null.");
        }
        if (timedGame && (timeBlack<1 || timeWhite<1)) {
            throw new IllegalArgumentException(
                    "Paramter timeBlack and timeWhite must be >0 for timed game. Were " + timeBlack + ", " + timeWhite);
        }

        _playerBlack = blackPlayer;
        _playerWhite = whitePlayer;

        _blackTime = timeBlack;
        _whiteTime = timeWhite;
        _blackClock = new GameClock(_playerBlack.getName());
        _whiteClock = new GameClock(_playerWhite.getName());

        if (timedGame && _blackTime > 0 && _whiteTime > 0) {
            _isTimedGame = true;
            _blackClock.setAlarm(_blackTime,this);
            _whiteClock.setAlarm(_whiteTime,this);
        }

        _curBoard = new GameBoardImpl();
        _boardHistory.add(new GameBoardImpl(_curBoard));

    } // end constructor

    /**
     * Start a game in a separate thread
     */
    public void startGameThread() {
        _gameStatus.readLock().lock();
        try {
            // -- if the game is not initialized we simply ignore this request --
            if (isInitialized() && _gameThread == null) {
                _gameThread = new Thread(this, "Game");
                _gameThread.setPriority(Thread.MIN_PRIORITY);
                _gameThread.start();
            } else {
                throw new IllegalStateException("Start game failed - not initialized or thread already running.");
            }
        } finally {
            _gameStatus.readLock().unlock();
        }
    }

    /**
     * Stop the game. It does <b>not</b> stop a paused game.
     */
    public void stopGameThread() {
        _gameStatus.readLock().lock();
        try {
            if (_gameThread!=null) {
                _gameThread.interrupt();
            }
        } finally {
            _gameStatus.readLock().unlock();
        }
    }

    /**
     * Stop a running game. It does <b>not</b> stop a paused game.
     */
    public void stopRunningGame() {
        _gameStatus.writeLock().lock();
        try {
            // -- if the game is not running we simply ignore this request --
            if (isRunning() && _gameThread != null) {
                // Set game in state stopped
                _gameStatus.setStatus(Game.GAME_STOPPED);
                // Stop the player clocks
                _blackClock.stopClock();
                _whiteClock.stopClock();
                // We also must stop the player so that they come back from waiting for a move
                _playerBlack.stopPlayer();
                _playerWhite.stopPlayer();
                // -- observer handling is done in the run() method
            } else {
                throw new IllegalStateException("Stop game failed - not running.");
            }
        } finally {
            _gameStatus.writeLock().unlock();
        }
    }

    /**
     * Runnable.run() method where the actual game happens
     */
    @Override
    public void run() {

        try { // to finally reset _gameThread

            if (Thread.currentThread() != _gameThread) {
                throw new UnsupportedOperationException("Direct call of Game.run() is not supported.");
            }

            _gameStatus.writeLock().lock();
            try {
                // Set game status to GAME_RUNNING
                _gameStatus.setStatus(Game.GAME_RUNNING);
                _gameOverCause.setStatus(Game.GAMEOVER_NONE);
                _gameWinner.setStatus(Game.WINNER_NONE_YET);
            } finally {
                _gameStatus.writeLock().unlock();
            }

            // -- start alarms --
            if (_isTimedGame) {
                _blackClock.startAlarm();
                _whiteClock.startAlarm();
            }

            // -- tell the views that model has changed --
            // -- the game thread is actually running now --
            setChanged();
            notifyObservers(new ModelEvent("GAME Runnning",
                    SIG_GAME_RUNNING));

            // -- play game --
            while (isRunningOrPaused() && !Thread.interrupted()) {
                waitWhileGamePaused();

                // -- do the next move --
                nextMove();

                // -- observer handling is done in the methods
                // -- nextMove, doMove, gameOver*

            } // -- end while --

            // -- tell the views that model has changed --
            setChanged();
            notifyObservers(new ModelEvent("GAME stopRunningGame() game stopped()",
                    SIG_GAME_STOPPED));

            // -- stop clock --
            _blackClock.stopAlarm();
            _whiteClock.stopAlarm();

            // Set game status to GAME_FINISHED
            _gameStatus.setStatus(Game.GAME_FINISHED);

            // -- tell the views that model has changed --
            // -- the game thread has actually stopped
            setChanged();
            notifyObservers(new ModelEvent("GAME finished",
                    SIG_GAME_FINISHED));

        } finally {

            // -- free game thread --
            _gameThread = null;
        }

    }

    /**
     * Get next move for this game.<br/>
     */
    private void nextMove() {

        GameMove nextMove = null;

        // Get the next move
        if (_curBoard.getNextPlayerColor().isBlack()) {
            // -- black has next move, ask player for move ---
            _blackClock.startClock();

            // Loop until we have a legal move from the player. Also handles undoMove.
            while (!_curBoard.isLegalMove(nextMove = _playerBlack.getNextMove(new GameBoardImpl(_curBoard)))) {

                // Check if player has been stopped
                if (_playerBlack.isStopped() && isRunningOrPaused()) stopRunningGame();

                // Check if game is still running or return directly.
                if (isRunning()) {
                    // undoMove or IllegalMove?
                    if (_undoMoveFlag.get()) {
                        doUndoMove();
                    } else {
                        // -- set flag illegalMove so that observers can find out --
                        _illegalMoveFlag.set(true);
                        _illegalMove     = nextMove;
                        setChanged();
                        notifyObservers(new PlayerDependendModelEvent(
                                "GAME nextMove() illegalMove BLACK",
                                _playerBlack,
                                SIG_GAME_ILLEGAL_MOVE));
                    }


                } else {
                    return;
                }
            }
            _blackClock.stopClock();
        } else if (_curBoard.getNextPlayerColor().isWhite()) {

            // -- white has next move, ask player for move ---
            _whiteClock.startClock();

            // Loop until we have a legal move from the player. Also handles undoMove.
            while (!_curBoard.isLegalMove(nextMove = _playerWhite.getNextMove(new GameBoardImpl(_curBoard)))) {

                // Check if player has been stopped
                if (_playerWhite.isStopped() && isRunningOrPaused()) stopRunningGame();

                // Check if game is still running or return directly.
                if (isRunning()) {
                    if (_undoMoveFlag.get()) {
                        doUndoMove();
                    } else {
                        // -- set flag illegalMove so that observers can find out --
                        _illegalMoveFlag.set(true);
                        _illegalMove = nextMove;
                        setChanged();
                        notifyObservers(new PlayerDependendModelEvent(
                                "GAME nextMove() illegalMove WHITE",
                                _playerWhite,
                                SIG_GAME_ILLEGAL_MOVE));
                    }


                } else {
                    return;
                }
            }
            _whiteClock.stopClock();
        }

        // -- we have a legal move --> reset illegal move flag --
        _illegalMoveFlag.set(false);
        _illegalMove=null;

        // -- tell the views that model has changed --
        setChanged();
        notifyObservers(new ModelEvent("GAME nextMove() got move",
                SIG_GAME_GOT_MOVE_FROM_PLAYER));

        // -- take the move and actually commit it to the game --
        doMove(nextMove);

    }

    /**
     * Do the move we got from a player on the board and check if the game is over.
     * @param nextMove
     */
    private void doMove(GameMove nextMove) throws IllegalMoveException {
        assert nextMove!=null : "Parameter nextMove may not be null";

        waitWhileGamePaused();

        // -- do move ---
        _gameStatus.readLock().lock();
        try {
            if (isRunningOrPaused()) {
                // -- here we actually commit the move to the game's board --
                _curBoard.makeMove(nextMove);
                //System.out.println(_curBoard.toFEN());
                //System.out.println(_curBoard.toString());
                //save a copy of the current board to board history - used for "take back"/undo
                _boardHistory.add(new GameBoardImpl(_curBoard));

                // -- tell the views that model has changed --
                setChanged();

                // check for repetition
                int repetitionCounter = 0;
                repetitionCounter = countRepetitions();

                // -- check for any more legal moves --> next player not NONE---
                if (_curBoard.isGameOver() || repetitionCounter >= 3 ) {
                    _gameStatus.readLock().unlock();
                    try {
                        gameOverNoMoreMoves(repetitionCounter);
                    } finally {
                        _gameStatus.readLock().lock();
                    }
                }
            }
        } finally {
            _gameStatus.readLock().unlock();
        }
        notifyObservers(new ModelEvent(
                "GAME doMove() move made and checked for game over",
                SIG_GAME_MOVE_MADE));

    }

    public void undoMove(int numberOfHalfmoves) {
        if (!_undoMoveFlag.get() // is not already set
                && this.isRunning() // game still running?
                && _boardHistory.size() - numberOfHalfmoves >= 0 // is there something to undo?
                && this.getNextPlayer() instanceof HumanPlayer // only human player can request undo
                && this.getNextPlayer().isThinking()
                ) {

            // set flag that we want to undo a move
            _undoMoveFlag.set(true);
            _numberOfHalfmovesToUndo.set(numberOfHalfmoves);

            setChanged();
            notifyObservers(new ModelEvent(
                    "GAME undoMove() requested",
                    SIG_GAME_WANTS_UNDO_MOVE));

            // tell the human player that we want to undo a move
            ((HumanPlayer)this.getNextPlayer()).undoMove();

        } // else ignore
    }

    /**
     * Undos move by using a copy of a board from history as the current board.
     */
    private void doUndoMove() {

        for (int i=0; i<_numberOfHalfmovesToUndo.get(); i++) {
            _boardHistory.remove(_boardHistory.get(_boardHistory.size()-1));
        }

        _curBoard = new GameBoardImpl(_boardHistory.get(_boardHistory.size()-1));
        _undoMoveFlag.set(false);

        setChanged();
        notifyObservers(new PlayerDependendModelEvent(
                "GAME undoMove()",
                getNextPlayer(),
                SIG_GAME_UNDO_MOVE));

    }

    /**
     * Checks for position repetition using the board history. If history is not available
     * this will repetition cannot be determinded.
     * @return number of repetitions.
     */
    public int countRepetitions() {
        int repetitionCounter = 0;
        int i=_boardHistory.size()-1;
        while (i > 3) {
            GameBoard b1 = _boardHistory.get(_boardHistory.size()-1);
            GameBoard b2 = _boardHistory.get(i-4);
            if (b1.hasSamePosition(b2)) {
                repetitionCounter++;
            }
            i -= 4;
        }
        return repetitionCounter;
    }

    /**
     * Is called when there are no more legal moves
     */
    private void gameOverNoMoreMoves(int repetitionCounter) {

        _gameStatus.writeLock().lock();
        try {
            if (_curBoard.hasCheckMate()) { // checkmate
                _gameOverCause.setStatus(Game.GAMEOVER_CHECKMATE);
                if (_curBoard.getNextPlayerColor() == GameColor.WHITE) {
                    _gameWinner.setStatus(WINNER_BLACK);
                } else {
                    _gameWinner.setStatus(WINNER_WHITE);
                }
            } else if (_curBoard.hasStaleMate()) { // stalemate
                _gameOverCause.setStatus(Game.GAMEOVER_STALEMATE);
                _gameWinner.setStatus(WINNER_DRAW);
            } else if (repetitionCounter >= 3) { // repetition
                _gameOverCause.setStatus(Game.GAMEOVER_STALEMATE);
                _gameWinner.setStatus(WINNER_DRAW);
            } else // material, 50 moves rule
                throw new RuntimeException(
                        "No more Moves but no checkmate or stalemate");
            _gameStatus.setStatus(Game.GAME_OVER);
            // -- model has changed --
            setChanged();
        } finally {
            _gameStatus.writeLock().unlock();
        }
        notifyObservers(new ModelEvent("GAME game over: no more moves",
                SIG_GAME_OVER));
    }

    /**
     * Game is an Observer to Clocks (Observable) and this implements the Observer interface.
     * It is only called when the time is up meaning the clock reached the alarm.
     */
    @Override
    public void update(Observable o, Object arg) {
        outOfTime((GameClock) o);
    }

    /**
     * Called from the Observer update(...) from the Observable clocks when time is up for a player.
     * @param clock
     */
    private void outOfTime(GameClock clock) {
        _gameStatus.readLock().lock();
        try {
            if (isRunning()) {
                _blackClock.stopClock();
                _blackClock.stopAlarm();
                _blackClock.deleteObservers();
                _whiteClock.stopClock();
                _whiteClock.stopAlarm();
                _whiteClock.deleteObservers();
                _gameStatus.readLock().unlock();
                try {
                    gameOverOutOfTime(clock);
                } finally {
                    _gameStatus.readLock().lock();
                }
            }
        } finally {
            _gameStatus.readLock().unlock();
        }
    }

    /**
     * Is called when one player is out of time
     * @param clock
     */
    private void gameOverOutOfTime(GameClock clock) {
        _gameStatus.writeLock().lock();
        try {
            // -- We want to know which clock is calling this so we do not use .equals() here --
            if (clock.equals(_blackClock)) {
                _gameWinner.setStatus(Game.WINNER_WHITE);
            } else if (clock.equals(_whiteClock)) {
                _gameWinner.setStatus(Game.WINNER_BLACK);
            } else {
                assert false;
            }
            _gameOverCause.setStatus(Game.GAMEOVER_TIME_IS_UP_FOR_ONE_PLAYER);
            _gameStatus.setStatus(Game.GAME_OVER);
            // -- model has changed --
            setChanged();
        } finally {
            _gameStatus.writeLock().unlock();
        }
        // -- tell the views that model has changed --
        notifyObservers(new ModelEvent("GAME game over: outOfTime",
                SIG_GAME_OVER));
    }

    /**
     * Pause a running game
     */
    public void pauseGame() {
        _gameStatus.writeLock().lock();
        try {
            if (isRunning()) {
                _gameStatus.setStatus(Game.GAME_PAUSED);
                _blackClock.stopClock();
                _whiteClock.stopClock();
                // -- model has changed --
                setChanged();
            } else {
                throw new RuntimeException("Pause to a not running game");
            }
        } finally {
            _gameStatus.writeLock().unlock();
        }
        // -- tell the views that model has changed --
        notifyObservers(new ModelEvent("GAME pauseGame",
                SIG_GAME_PAUSE_GAME));

    }

    /**
     * Resumes a paused game
     */
    public void resumeGame() {
        _gameStatus.writeLock().lock();
        try {
            if (isPaused()) {
                _gameStatus.setStatus(Game.GAME_RUNNING);
                if (_curBoard.getNextPlayerColor().isBlack()) {
                    _blackClock.startClock();
                } else {
                    _whiteClock.startClock();
                }
                // -- model has changed --
                setChanged();
            } else {
                throw new RuntimeException("Resume to a not paused game");
            }
        } finally {
            _gameStatus.writeLock().unlock();
        }
        // -- tell the views that model has changed --
        notifyObservers(new ModelEvent("GAME resumeGame",
                SIG_GAME_RESUME_GAME));
    }

    /**
     * This extends a StatusController to control legal status changes
     * by overriding the checkTransition method and defining legal transitions.
     */
    private static class GameStatusController extends StatusController {
        private GameStatusController(int initialState) {
            super(initialState);
            this.setTransitionCheck(true);
        }
        @Override
        protected boolean checkTransition(int sourceState, int targetState) {
            readLock().lock();
            try {
                if (sourceState == targetState) {
                    return true;
                }
                switch (sourceState) {
                    /**
                     * Define which states are allowed when currently in a certain state.
                     */
                    case Game.GAME_INITIALIZED:
                        switch (targetState) {
                            case Game.GAME_RUNNING:
                                return true;
                            default:
                                return false;
                        }
                    case Game.GAME_RUNNING:
                        switch (targetState) {
                            case Game.GAME_PAUSED:
                                return true;
                            case Game.GAME_OVER:
                                return true;
                            case Game.GAME_STOPPED:
                                return true;
                            default:
                                return false;
                        }
                    case Game.GAME_PAUSED:
                        switch (targetState) {
                            case Game.GAME_RUNNING:
                                return true;
                            default:
                                return false;
                        }
                    case Game.GAME_OVER:
                        switch (targetState) {
                            case Game.GAME_FINISHED:
                                return true;
                            default:
                                return false;
                        }
                    case Game.GAME_STOPPED:
                        switch (targetState) {
                            case Game.GAME_FINISHED:
                                return true;
                            default:
                                return false;
                        }
                    default :
                        return false;
                }
            } finally {
                readLock().unlock();
            }
        }
    }

    /**
     * Returns true when the game is in status Game.GAME_INITIALIZED
     * @return true if game is initialized
     */
    public boolean isInitialized() { return _gameStatus.inStatus(GAME_INITIALIZED); }
    /**
     * Returns true when the game is in status Game.GAME_RUNNING
     * @return true if game is running
     */
    public boolean isRunning() { return _gameStatus.inStatus(GAME_RUNNING); }
    /**
     * Returns true when the game is in status Game.GAME_PAUSED
     * @return true if game is paused
     */
    public boolean isPaused() { return _gameStatus.inStatus(GAME_PAUSED); }
    /**
     * Returns true when the game is in status Game.GAME_STOPPED
     * @return true if game is stopped
     */
    public boolean isStopped() { return _gameStatus.inStatus(GAME_STOPPED); }
    /**
     * Returns true when the game is in status Game.GAME_OVER
     * @return true if game is over
     */
    public boolean isOver() { return _gameStatus.inStatus(GAME_OVER); }
    /**
     * Returns true when the game is in status Game.GAME_FINISHED
     * @return true if game is finished (was over and stopped and has cleaned up)
     */
    public boolean isFinished() { return _gameStatus.inStatus(GAME_FINISHED); }
    /**
     * Returns true when the game is in status Game.GAME_RUNNING or Game.GAME_PAUSED
     * @return true if game is running or paused
     */
    public boolean isRunningOrPaused() {
        _gameStatus.readLock().lock();
        try {
            return isRunning() || isPaused();
        } finally {
            _gameStatus.readLock().unlock();
        }
    }

    /**
     * Returns true when the game is in status Game.GAME_OVER or Game.GAME_STOPPED
     * @return true if game is over or stopped
     */
    public boolean isOverOrStopped() {
        _gameStatus.readLock().lock();
        try {
            return isOver() || isStopped();
        } finally {
            _gameStatus.readLock().unlock();
        }
    }

    /**
     * Waits while the game is not finished
     */
    public void waitUntilGameFinished() {
        _gameStatus.waitForState(Game.GAME_FINISHED);
    }

    /**
     * Waits while game is paused
     * @see StatusController
     */
    public void waitWhileGamePaused() {
        _gameStatus.waitWhileInState(Game.GAME_PAUSED);
    }

    /**
     * Waits while game is in running
     * @see StatusController
     */
    public void waitWhileRunning() {
        _gameStatus.waitWhileInState(Game.GAME_RUNNING);
    }

    /**
     * Waits until game is in running
     * @see StatusController
     */
    public void waitUntilRunning() {
        _gameStatus.waitForState(Game.GAME_RUNNING);
    }

    /**
     * Allows other threads to wait for the game thread to terminate.
     * Uses join();
     */
    public void waitForThreadTermination() {
        while (_gameThread != null && _gameThread.isAlive())  {
            try {
                _gameThread.join();
            } catch (InterruptedException e) {
                // -- ignore --
            }
        }
    }

    /**
     * Return the reason for the game to be over
     * Check game status with getStatus()
     * @return int (see Game.GAMEOVER_* contants)
     */
    public int getGameOverCause() {
        return _gameOverCause.getStatus();
    }

    /**
     * Return the winner if the game is over.
     * Check game status with getStatus()
     * @return int (see Game.WINNER_* contants)
     */
    public int getGameWinnerStatus() {
        return _gameWinner.getStatus();
    }

    public List<GameBoard> getBoardHistory() {
        return _boardHistory;
    }

    /**
     * returns current curBoard
     * @return current board
     */
    public GameBoard getCurBoard() {
        return this._curBoard;
    }

    /**
     * Returns the player who has the next move
     * @return Player - the player who has the next move
     */
    public Player getNextPlayer() {
        return _curBoard.getNextPlayerColor().isBlack() ? _playerBlack : _playerWhite;
    }

    /**
     * Returns the player who has the next move
     * @return Player - the player who has the next move
     */
    public Player getLastPlayer() {
        return _curBoard.getLastPlayerColor().isBlack() ? _playerBlack : _playerWhite;
    }

    /**
     * returns black player
     * @return black player
     */
    public Player getPlayerBlack() {
        return _playerBlack;
    }

    /**
     * returns white player
     * @return white player
     */
    public Player getPlayerWhite() {
        return _playerWhite;
    }

    /**
     * getter for black timer
     * @return black clock
     */
    public GameClock getBlackClock() {
        return _blackClock;
    }

    /**
     * getter for white timer
     * @return white clock
     */
    public GameClock getWhiteClock() {
        return _whiteClock;
    }

    /**
     * Returns if the current game is a timed game.
     * @return true if setting for timed game is true
     */
    public boolean isTimedGame() {
        return _isTimedGame;
    }

    /**
     * Returns the initial black time
     * @return initial black time
     */
    public long getBlackTime() {
        return _blackTime;
    }

    /**
     * Returns the initial white time
     * @return initial white time
     */
    public long getWhiteTime() {
        return _whiteTime;
    }

    /**
     * Find out if the last move from the player object was illegal
     * @return is true if last move was illegal
     */
    public boolean wasIllegalMoveFlag() {
        return _illegalMoveFlag.get();
    }

    /**
     * Return the last illegal move from the player object.
     * @return the last illegal move
     */
    public GameMove getIllegalMove() {
        return _illegalMove;
    }

    public boolean undoMoveFlag() {
        return _undoMoveFlag.get() ;
    }

    /**
     * Return the current status of the game.
     * @return status (see Game.GAME_* contants)
     */
    public int getStatus() {
        return this._gameStatus.getStatus();
    }

    /**
     * Status when a game is initialized but not yet started.
     * Query the game status with getStatus()
     */
    public static final int GAME_INITIALIZED = 0;
    /**
     * Status when a game is running.
     * Query the game status with getStatus()
     */
    public static final int GAME_RUNNING = 1;
    /**
     * Status when a game is paused.
     * Query the game status with getStatus()
     */
    public static final int GAME_PAUSED = 2;
    /**
     * Status when a game is stopped but not over.
     * Query the game status with getStatus()
     */
    public static final int GAME_STOPPED = 3;
    /**
     * Status when a game is over (no more moves or time out).
     * Query the game status with getStatus()
     */
    public static final int GAME_OVER = 4;
    /**
     * Status after a game was over or stopped and everything is cleaned up.
     * It is more or less the opposite of GAME_INITIALIZED
     * Query the game status with getStatus()
     */
    public static final int GAME_FINISHED = 5;
    /**
     * Reason for status GAME_OVER: None. Should game be over at all?
     * If game is in status GAME_OVER this indicates a problem with the status handling.
     * Query the game over reason with get_gameOverCause()
     */
    public static final int GAMEOVER_NONE = 0;
    /**
     * Reason for status GAME_OVER: There are no more posssible moves.
     * Query the game over reason with get_gameOverCause()
     */
    public static final int GAMEOVER_CHECKMATE = 1;
    /**
     * Reason for status GAME_OVER: There are no more posssible moves.
     * Query the game over reason with get_gameOverCause()
     */
    public static final int GAMEOVER_STALEMATE = 2;
    /**
     * Reason for status GAME_OVER: Time is up for one of the players
     * Query the game over reason with get_gameOverCause()
     */
    public static final int GAMEOVER_TIME_IS_UP_FOR_ONE_PLAYER = 3;
    /**
     * Reason for status GAME_OVER: One player has resigned.
     * Query the game over reason with get_gameOverCause()
     */
    public static final int GAMEOVER_ONE_PLAYER_HAS_RESIGNED = 4;

    /**
     * If game is GAME_OVER we must have a winner or a draw.
     * Query winner with getGameWinner()
     * No winner yet.
     */
    public static final int WINNER_NONE_YET = 0;
    /**
     * If game is GAME_OVER we must have a winner or a draw.
     * Query winner with getGameWinner()
     * We have a draw.
     */
    public static final int WINNER_DRAW = 2;
    /**
     * If game is GAME_OVER we must have a winner or a draw.
     * Query winner with getGameWinner()
     * The black player has won.
     */
    public static final int WINNER_BLACK = -1;
    /**
     * If game is GAME_OVER we must have a winner or a draw.
     * Query winner with getGameWinner()
     * The white player has won.
     */
    public static final int WINNER_WHITE = 1;

    public static final int SIG_GAME_RUNNING = 2000;
    public static final int SIG_GAME_FINISHED = 2010;
    public static final int SIG_GAME_WANTS_UNDO_MOVE = 2015;
    public static final int SIG_GAME_UNDO_MOVE = 2020;
    public static final int SIG_GAME_ILLEGAL_MOVE = 2030;
    public static final int SIG_GAME_GOT_MOVE_FROM_PLAYER = 2050;
    public static final int SIG_GAME_MOVE_MADE = 2060;
    public static final int SIG_GAME_OVER = 2070;
    public static final int SIG_GAME_STOPPED = 2080;
    public static final int SIG_GAME_OUT_OF_TIME = 2090;
    public static final int SIG_GAME_PAUSE_GAME = 2100;
    public static final int SIG_GAME_RESUME_GAME = 2110;


}
