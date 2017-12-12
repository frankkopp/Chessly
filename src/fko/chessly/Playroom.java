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

package fko.chessly;

import java.util.Arrays;

import fko.chessly.game.Game;
import fko.chessly.game.GameColor;
import fko.chessly.mvc.ModelObservable;
import fko.chessly.mvc.ModelEvents.ModelEvent;
import fko.chessly.player.Player;
import fko.chessly.player.PlayerFactory;
import fko.chessly.player.PlayerType;

/**
 * <p>In the Playroom class the actual games are handled (started, stopped, etc.). It
 * is able to play a given number of games in a row.</p>
 *
 * <p>This implementation actually only handles one game at a certain time. For this
 * playable version this makes sense as the UI is currently only able to present
 * one game at a time in a meaningful way.</p>
 *
 * <p>For self training or a different implementation of a UI a different
 * Playroom will be necessary.</p>
 *
 * <p>The playroom is also the main observable (Model) for the ui. When the playroom properties change
 * the observers will be notified.</p>
 *
 * <p>The playroom is a singleton and can not be instantiated - use getInstance()</p>
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class Playroom extends ModelObservable implements Runnable {

    // -- Singleton instance --
    private static final Playroom _instance = new Playroom();

    // -- the playroom runs in a separate thread --
    private Thread _playroomThread = null;

    // -- status of the playroom --
    private boolean _isPlaying = false;

    // -- this version of the playroom can handle exactly one game at a time --
    private Game _game = null;

    // -- this is needed to stop multiple games when the stop signal is occuring between two games --
    private boolean _stopMultipleGames = false;

    // -- get values from properties --
    // -- these variables are needed to successfully start a game --
    private boolean _isTimedGame = Boolean.valueOf(Chessly.getProperties().getProperty("timedGame").trim());
    private long _timeBlack = 1000 * Integer.parseInt(Chessly.getProperties().getProperty("timeBlack").trim());
    private long _timeWhite = 1000 * Integer.parseInt(Chessly.getProperties().getProperty("timeWhite").trim());
    private int _currentLevelBlack = Integer.parseInt(Chessly.getProperties().getProperty("engine.black.searchDepth").trim());
    private int _currentLevelWhite = Integer.parseInt(Chessly.getProperties().getProperty("engine.white.searchDepth").trim());
    private int _numberOfGames = Integer.parseInt(Chessly.getProperties().getProperty("numberOfGames").trim());
    private PlayerType _playerTypeBlack = PlayerType.valueOf(Chessly.getProperties().getProperty("playerTypeBlack", "1"));
    private String _namePlayerBlack = Chessly.getProperties().getProperty("nameBlackPlayer", "BLACK_PLAYER");
    private PlayerType _playerTypeWhite = PlayerType.valueOf(Chessly.getProperties().getProperty("playerTypeWhite", "1"));
    private String _namePlayerWhite = Chessly.getProperties().getProperty("nameWhitePlayer", "WHITE_PLAYER");

    // -- counters for multiple games --
    private int _currentGameNumber = 0;
    private int _currentWhiteWins = 0;
    private int _currentBlackWins = 0;
    private int _currentDraws = 0;

    // Game request handling
    private final    Object      _gameRequestLock = new Object();
    private          boolean     _gameRequestPending = false;
    private boolean _noMultipleGames = false; // we do not want multiple games with the server

    /**
     * Default constructor is private as we are a singleton.
     */
    private Playroom() {
        // empty
    }

    /**
     * Playroom is a Singleton so use getInstance() to get a reference to the instance.<br/>
     * @return Playroom instance
     */
    public static Playroom getInstance() {
        return _instance;
    }

    /**
     * Start a new playroom thread to play one or multiple games<br/>
     * The thread then calls run() to actually do the work.
     */
    public void startPlayroom() {
        synchronized (_gameRequestLock) { // synchronize so that the local user cannot start a game during our request
            if ((_gameRequestPending) || _isPlaying) { // there is another request or a running game
                throw new IllegalStateException(
                        "startPlayroom(): Another start request is pending or Playroom already is playing.");
            }
            // There is no other game request and no running game so we process this request
            _gameRequestPending = true;
            // Now start the thread
            if (_playroomThread == null) {
                _stopMultipleGames = false;
                _playroomThread = new Thread(this, "Playroom");
                _playroomThread.start();
            } else {
                throw new IllegalStateException("startPlayroom(): Playroom thread already exists.");
            }
        }
    }

    /**
     * Stops the playroom thread and the running game.<br/>
     */
    public synchronized void stopPlayroom() {
        if (_playroomThread==null || !_playroomThread.isAlive() || !_isPlaying) {
            throw new IllegalStateException("stopPlayroom(): Playroom thread is not running");
        }
        // Stopping the game is the only way to stop the playroom thread.
        if (_game != null && _game.isRunningOrPaused()) {
            _game.stopRunningGame();
        }
        _stopMultipleGames = true;
        _playroomThread.interrupt();
    }

    /**
     * Starts one or multiple games.<br/>
     * If multiple games are configured it plays them one after the other in a loop.<br/>
     */
    @Override
    public void run() {

        try { // to finally reset the state of the _playroomThread

            if (Thread.currentThread() != _playroomThread) {
                throw new UnsupportedOperationException("Direct call of Playroom.run() is not supported.");
            }

            // -- set the status to playing --
            _isPlaying = true;

            // Now we are playing, so we don't have a game request pending any more.
            synchronized (_gameRequestLock) {
                _gameRequestPending = false;
            }

            // -- tell the views that model has changed --
            setChanged();
            notifyObservers(new ModelEvent("PLAYROOM Thread started", SIG_PLAYROOM_THREAD_STARTED));

            _currentGameNumber = 1;
            _currentWhiteWins = 0;
            _currentBlackWins = 0;
            _currentDraws = 0;
            do { // Loop for multiple games

                // Play one game
                playOneGame();

                // If the game has not been created or was stopped instead of ending regularly
                // then break out of the loop
                if ((_game!=null && _game.isStopped()) || _stopMultipleGames) {
                    break;
                }

                // Run GC so that to avoid it during a running game
                System.gc();

            } while (!_noMultipleGames && ++_currentGameNumber <= _numberOfGames); // Loop if we play multiple games in a row

        } catch (Exception e) { // in case anything went wrong we can clean up here
            throw new RuntimeException(e);

        } finally {

            _game = null;

            // Set status to not playing
            _isPlaying = false;

            // Free _playroomThread so we can startGame a new one --
            _playroomThread = null;

        }

        // Tell the views that model has changed --
        setChanged();
        notifyObservers(new ModelEvent("PLAYROOM Thread finished", SIG_PLAYROOM_THREAD_END));

    }

    /**
     * Prepares and plays one game. Players are created.
     * A game is created and players and game are started.
     */
    private void playOneGame() {

        // Create the black player (observer handling is done in createPlayer()
        Player playerBlack = createPlayer(GameColor.BLACK);
        assert playerBlack!=null : "Player black may not be null" ;

        // Create the white player (observer handling is done in createPlayer()
        Player playerWhite = createPlayer(GameColor.WHITE);
        assert playerWhite!=null : "Player white may not be null";

        // Create a new game
        _game = new Game(playerWhite, playerBlack, _timeWhite, _timeBlack, _isTimedGame);

        // Tell the views that model has changed
        setChanged();
        notifyObservers(new ModelEvent("PLAYROOM Game created", SIG_PLAYROOM_GAME_CREATED));

        // Does the actual game playing with the created players and the created game
        playGame(playerBlack, playerWhite);

        // If the game ended because it was over (not stopped) we do some logging
        // and we increase the multiple game counters accordingly
        if (_game.isFinished()) {
            if (_game.getGameWinnerStatus() == Game.WINNER_WHITE) {
                _currentWhiteWins++;
            } else if (_game.getGameWinnerStatus() == Game.WINNER_BLACK) {
                _currentBlackWins++;
            } else if (_game.getGameWinnerStatus() == Game.WINNER_DRAW) {
                _currentDraws++;
            }
        }

        // Tell the views that model has changed
        setChanged();
        notifyObservers(new ModelEvent("PLAYROOM Game finished", SIG_PLAYROOM_GAME_FINISHED));

        // clean up player to make it easier for GC
        playerBlack = null;
        playerWhite = null;

    }

    /**
     * Does the actual game playing with provided players and the current game object.
     * @param playerBlack
     * @param playerWhite
     */
    private void playGame(Player playerBlack, Player playerWhite) {
        assert playerBlack!=null && playerWhite!=null;
        assert _game!=null && _game.isInitialized();

        // Start players
        playerBlack.startPlayer(_game);
        playerWhite.startPlayer(_game);
        // Start game
        _game.startGameThread();
        // Wait until game is running
        _game.waitUntilRunning();
        // Wait while game is running
        while (_game.isRunningOrPaused()) {
            if (_game.isRunning()) {
                _game.waitWhileRunning();
            }
            if (_game.isPaused()) {
                _game.waitWhileGamePaused();
            }
        }
        // The game is not running any more but we must wait until it had a chance to clean up
        _game.waitUntilGameFinished();
        // Stop the players
        playerBlack.stopPlayer();
        playerWhite.stopPlayer();

        // Wait for the threads to finish
        playerBlack.joinPlayerThread();
        playerWhite.joinPlayerThread();

        // We must wait for the game thread to complete before starting a new game
        // because other threads (ex. the UI) must get the chance to process the finished game
        // before we eventually startGame a new game
        _game.waitForThreadTermination();
    }

    /**
     * Creates a player.
     * @return player created - null if player creation failed
     */
    private Player createPlayer(GameColor color) {
        assert color.isBlack() || color.isWhite();

        final Player newPlayer;

        try {
            if (color==GameColor.BLACK) {
                newPlayer = PlayerFactory.createPlayer(_playerTypeBlack, _namePlayerBlack, GameColor.BLACK);
                setChanged();
                notifyObservers(
                        new ModelEvent("PLAYROOM Created player Black", SIG_PLAYROOM_CREATED_PLAYER_BLACK));
            } else {
                newPlayer = PlayerFactory.createPlayer(_playerTypeWhite, _namePlayerWhite, GameColor.WHITE);
                setChanged();
                notifyObservers(
                        new ModelEvent("PLAYROOM Created player White", SIG_PLAYROOM_CREATED_PLAYER_WHITE));
            }
        } catch (PlayerFactory.PlayerCreationException e) {
            setChanged();
            if (color==GameColor.BLACK) {
                notifyObservers(
                        new ModelEvent("PLAYROOM Failed to create player Black", SIG_PLAYROOM_CREATE_PLAYER_BLACK_FAILED));
            } else {
                notifyObservers(
                        new ModelEvent("PLAYROOM Failed to create player White", SIG_PLAYROOM_CREATE_PLAYER_WHITE_FAILED));

            }
            throw new RuntimeException("Error creating player.",e);
        }
        return newPlayer;
    }


    /**
     * Returns if the playroom currently is running a game
     *
     * @return true if the playroom is running a game
     */
    public boolean isPlaying() {
        return _isPlaying;
    }


    /**
     * Getter for _gameRequestPending so that the observers can see we have a game request
     *
     * @return true - if we have a pending request for a new game
     */
    public boolean isGameRequestPending() {
        return _gameRequestPending;
    }

    /**
     * Returns the current game - maybe null if no game exists
     *
     * @return aGame
     */
    public Game getCurrentGame() {
        return _game;
    }

    /**
     * Undo a certain number of halfmoves.
     * Usually called by UI with 2 to undo the player's last move after the oppenent
     * has made a move.
     * @param numberOfHalfmoves
     */
    public void undoMove(int numberOfHalfmoves) {
        if (_game != null
                && _game.isRunning()
                && _game.getBoardHistory().size() >= 2
                ) {
            // we have at least two halfmoves
            _game.undoMove(numberOfHalfmoves);
        } // else ignore
    }


    /**
     * Returns if new games are timed.<br/>
     * Does not say anything about the current running game.
     *
     * @return true if the current game is timed
     */
    public boolean isTimedGame() {
        return _isTimedGame;
    }

    /**
     * Defines if the next game shall be a timed game or not.
     *
     * @param boolVal
     */
    public void setTimedGame(boolean boolVal) {
        this._isTimedGame = boolVal;
        // Tell the views that model has changed --
        this.setChanged();
        notifyObservers(new ModelEvent("PLAYROOM set_isTimedGame", SIG_PLAYROOM_SET_IS_TIMED_GAME));
    }

    /**
     * Returns the initial time available for the black player
     *
     * @return time in seconds
     */
    public long getTimeBlack() {
        return _timeBlack;
    }

    /**
     * Sets the initial time available to the black player
     *
     * @param newTimeBlack in seconds
     */
    public void setTimeBlack(long newTimeBlack) {
        if (newTimeBlack<=0) {
            throw new IllegalArgumentException("Parameter newTimeBlack must be > 0. Was " + newTimeBlack);
        }

        this._timeBlack = newTimeBlack;
        // Tell the views that model has changed
        this.setChanged();
        notifyObservers(new ModelEvent("PLAYROOM set_timeBlack", SIG_PLAYROOM_SET_TIME_BLACK));
    }

    /**
     * Returns the initial time available for the white player
     *
     * @return time in seconds
     */
    public long getTimeWhite() {
        return _timeWhite;
    }

    /**
     * Sets the initial time available to the black player
     *
     * @param newTimeWhite in seconds
     */
    public void setTimeWhite(long newTimeWhite) {
        if (newTimeWhite<=0) {
            throw new IllegalArgumentException("Parameter newTimeWhite must be > 0. Was " + newTimeWhite);
        }

        this._timeWhite = newTimeWhite;
        // Tell the views that model has changed
        this.setChanged();
        notifyObservers(new ModelEvent("PLAYROOM set_timeWhite", SIG_PLAYROOM_SET_TIME_WHITE));
    }

    /**
     * Returns the current level for a black player engine.
     * The level is currently the identical to the maximal search death of the engine.
     *
     * @return level
     */
    public int getCurrentEngineLevelBlack() {
        return _currentLevelBlack;
    }

    /**
     * Sets the level for a black player engine.
     * The level is currently the identical to the maximal search death of the engine.
     *
     * @param newLevelBlack
     */
    public void setCurrentLevelBlack(int newLevelBlack) {
        if (newLevelBlack<=0) {
            throw new IllegalArgumentException("Parameter newLevelBlack must be > 0. Was " + newLevelBlack);
        }

        this._currentLevelBlack = newLevelBlack;
        // Tell the views that model has changed
        setChanged();
        notifyObservers(new ModelEvent("PLAYROMM set_currentLevelBlack", SIG_PLAYROOM_SET_CURRENT_LEVEL_BLACK));
    }

    /**
     * Returns the current level for a white player engine.
     * The level is currently the identical to the maximal search death of the engine.
     *
     * @return level
     */
    public int getCurrentEngineLevelWhite() {
        return _currentLevelWhite;
    }

    /**
     * Sets the level for a white player engine.
     * The level is currently the identical to the maximal search death of the engine.
     *
     * @param newLevelWhite
     */
    public void setCurrentLevelWhite(int newLevelWhite) {
        if (newLevelWhite<=0) {
            throw new IllegalArgumentException("Parameter newLevelWhite must be > 0. Was " + newLevelWhite);
        }

        this._currentLevelWhite = newLevelWhite;
        // Tell the views that model has changed
        setChanged();
        notifyObservers(new ModelEvent("PLAYROMM set_currentLevelWhite", SIG_PLAYROOM_SET_CURRENT_LEVEL_WHITE));
    }

    /**
     * Returns the number of games the playroom play in a row.
     *
     * @return number of games
     */
    public int getNumberOfGames() {
        return _numberOfGames;
    }

    /**
     * Sets the number of games the playroom plays in row.
     *
     * @param newNumberOfGames
     */
    public void setNumberOfGames(int newNumberOfGames) {
        if (newNumberOfGames<=0) {
            throw new IllegalArgumentException("Parameter newNumberOfGames must be > 0. Was " + newNumberOfGames);
        }

        this._numberOfGames = newNumberOfGames;
        // Tell the views that model has changed
        setChanged();
        notifyObservers(new ModelEvent("PLAYROMM set_numberOfGames", SIG_PLAYROOM_SET_NUMBER_OF_GAMES));
    }

    /**
     * Returns the current type of the black player
     *
     * @return player type as defined in the interface Player
     */
    public PlayerType getPlayerTypeBlack() {
        return _playerTypeBlack;
    }

    /**
     * Sets the type of the black player
     *
     * @param newPlayerTypeBlack as defined in the interface Player
     */
    public void setPlayerTypeBlack(PlayerType newPlayerTypeBlack) {
        if (!Arrays.asList(PlayerType.values()).contains(newPlayerTypeBlack)) {
            throw new IllegalArgumentException(
                    "Parameter newPlayerTypeBlack not a valid player type. Was " + newPlayerTypeBlack);
        }

        this._playerTypeBlack = newPlayerTypeBlack;
        // -- tell the views that model has changed --
        setChanged();
        notifyObservers(new ModelEvent("PLAYROOM set_playerTypeBlack", SIG_PLAYROOM_SET_PLAYER_TYPE_BLACK));
    }

    /**
     * Returns the current type of the white player
     *
     * @return player type as defined in the interface Player
     */
    public PlayerType getPlayerTypeWhite() {
        return _playerTypeWhite;
    }

    /**
     * Sets the type of the white player
     *
     * @param newPlayerTypeWhite as defined in the interface Player
     */
    public void setPlayerTypeWhite(PlayerType newPlayerTypeWhite) {
        if (!Arrays.asList(PlayerType.values()).contains(newPlayerTypeWhite)) {
            throw new IllegalArgumentException(
                    "Parameter newPlayerTypeWhite not a valid player type. Was " + newPlayerTypeWhite);
        }

        this._playerTypeWhite = newPlayerTypeWhite;
        // -- tell the views that model has changed --
        setChanged();
        notifyObservers(new ModelEvent("PLAYROOM set_playerTypeWhite",
                SIG_PLAYROOM_SET_PLAYER_TYPE_WHITE));
    }

    /**
     * Returns the current name of the black player
     *
     * @return name of player
     */
    public String getNameBlackPlayer() {
        return _namePlayerBlack;
    }


    /**
     * Sets the name of the black player
     *
     * @param newNameBlackPlayer
     */
    public void setNameBlackPlayer(String newNameBlackPlayer) {
        if (newNameBlackPlayer==null) {
            throw new IllegalArgumentException("Parameter newNameBlackPlayer must not be null.");
        }

        this._namePlayerBlack = newNameBlackPlayer;
        // -- tell the views that model has changed --
        setChanged();
        notifyObservers(new ModelEvent("PLAYROOM set_nameBlackPlayer", SIG_PLAYROOM_SET_NAME_BLACK_PLAYER));
    }

    /**
     * Returns the current name of the white player
     *
     * @return name of player
     */
    public String getNameWhitePlayer() {
        return _namePlayerWhite;
    }

    /**
     * Sets the name of the white player
     *
     * @param newNameWhitePlayer
     */
    public void setNameWhitePlayer(String newNameWhitePlayer) {
        if (newNameWhitePlayer==null) {
            throw new IllegalArgumentException("Parameter newNameWhitePlayer must not be null.");
        }

        this._namePlayerWhite = newNameWhitePlayer;
        // -- tell the views that model has changed --
        setChanged();
        notifyObservers(new ModelEvent("PLAYROOM set_nameWhitePlayer", SIG_PLAYROOM_SET_NAME_WHITE_PLAYER));
    }

    /**
     * Returns the number of the current game.
     * Usefull when multiple games are played in a row.
     *
     * @return number of current game
     */
    public int getCurrentGameNumber() {
        return _currentGameNumber;
    }

    /**
     * Returns the number of wins of the white player.
     * Usefull when multiple games are played in a row.
     *
     * @return number of white wins
     */
    public int getCurrentWhiteWins() {
        return _currentWhiteWins;
    }

    /**
     * Returns the number of wins of the black player.
     * Usefull when multiple games are played in a row.
     *
     * @return number of black wins
     */
    public int getCurrentBlackWins() {
        return _currentBlackWins;
    }

    /**
     * Returns the number of draws
     * Usefull when multiple games are played in a row.
     *
     * @return number of draws
     */
    public int getCurrentDraws() {
        return _currentDraws;
    }

    /**
     * Clean up playroom and call Chessly.exit()
     */
    public void exitChessly() {

        // Tell the current game to stop
        if (_playroomThread!=null && _playroomThread.isAlive()) {
            stopPlayroom();
        }
        // Call the exit mothod of Chessly to let that class also do some cleanup
        Chessly.exitChessly();
    }

    /** */
    public static final int SIG_PLAYROOM_THREAD_STARTED = 1000;
    /** */
    public static final int SIG_PLAYROOM_CREATED_PLAYER_BLACK = 1010;
    /** */
    public static final int SIG_PLAYROOM_CREATED_PLAYER_WHITE = 1020;
    /** */
    public static final int SIG_PLAYROOM_CREATE_PLAYER_BLACK_FAILED = 1030;
    /** */
    public static final int SIG_PLAYROOM_CREATE_PLAYER_WHITE_FAILED = 1040;
    /** */
    public static final int SIG_PLAYROOM_GAME_CREATED = 1050;
    /** */
    public static final int SIG_PLAYROOM_GAME_FINISHED = 1060;
    /** */
    public static final int SIG_PLAYROOM_THREAD_END = 1070;
    /** */
    public static final int SIG_PLAYROOM_SET_IS_TIMED_GAME = 1080;
    /** */
    public static final int SIG_PLAYROOM_SET_TIME_BLACK = 1090;
    /** */
    public static final int SIG_PLAYROOM_SET_TIME_WHITE = 1100;
    /** */
    public static final int SIG_PLAYROOM_SET_CURRENT_LEVEL_BLACK = 1110;
    /** */
    public static final int SIG_PLAYROOM_SET_CURRENT_LEVEL_WHITE = 1120;
    /** */
    public static final int SIG_PLAYROOM_SET_NUMBER_OF_GAMES = 1130;
    /** */
    public static final int SIG_PLAYROOM_SET_BOARD_DIMENSION = 1140;
    /** */
    public static final int SIG_PLAYROOM_SET_PLAYER_TYPE_BLACK = 1150;
    /** */
    public static final int SIG_PLAYROOM_SET_NAME_BLACK_PLAYER = 1160;
    /** */
    public static final int SIG_PLAYROOM_SET_PLAYER_TYPE_WHITE = 1170;
    /** */
    public static final int SIG_PLAYROOM_SET_NAME_WHITE_PLAYER = 1180;

}