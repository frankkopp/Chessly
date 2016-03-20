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
package fko.chessly.player.computer.Omega;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveList;
import fko.chessly.mvc.ModelObservable;
import fko.chessly.mvc.ModelEvents.PlayerDependendModelEvent;
import fko.chessly.openingbook.OpeningBookImpl;
import fko.chessly.player.ComputerPlayer;
import fko.chessly.player.Player;
import fko.chessly.player.computer.ObservableEngine;
import fko.chessly.player.computer.Omega.OmegaSearch.SearchResult;
import fko.chessly.player.computer.PulseEngine.Configuration;

/**
 * New engine implementation.
 */
public class OmegaEngine extends ModelObservable implements ObservableEngine {

    /** read in the default configuration - change the public fields if necessary */
    public final OmegaConfiguration _CONFIGURATION = new OmegaConfiguration();

    // The current game this engine is used in
    private Game _game = null;

    // used to wait for move from search
    private CountDownLatch _waitForMoveLatch = new CountDownLatch(0);

    // the search engine itself
    private OmegaSearch _omegaSearch;

    // the search result of the search - null when no result yet
    private SearchResult _searchResult = null;

    // the player owning this engine
    private ComputerPlayer _player = null;
    private GameColor _activeColor = GameColor.NONE;

    // the opening book
    private OpeningBookImpl _openingBook = null;

    // holds the time we started searching
    private long _startTime = 0;;

    /**********************************************************************
     * Engine Interface
     **********************************************************************/

    /**
     * Sets the current game.
     * @param game
     */
    @Override
    public void setGame(Game game) {
        this._game = game;
    }

    // not used
    @Override public void setNumberOfThreads(int n) { /*empty*/ }

    /**
     * Initializes the engine
     */
    @Override
    public void init(Player player) {
        // setup our player and color
        if (!(player instanceof ComputerPlayer)) {
            Chessly.fatalError("Engine objext can only be used with an instance of ComputerPlayer!");
        }
        this._player = (ComputerPlayer) player;
        _activeColor = player.getColor();

        // Create Search
        _omegaSearch = new OmegaSearch(this);

        // initialize opening book
        if (_CONFIGURATION._USE_BOOK) {
            Path path = FileSystems.getDefault().getPath(_CONFIGURATION._OB_FolderPath, _CONFIGURATION._OB_fileNamePlain);
            _openingBook =   new OpeningBookImpl(this,path,_CONFIGURATION._OB_Mode);
        }
    }

    /**
     * Starts calculation and returns next move
     * @param gameBoard
     * @return computed move
     *
     * TODO: pondering
     */
    @Override
    public GameMove getNextMove(GameBoard gameBoard) {
        assert gameBoard !=null : "gameBoard must not be null";

        // Start timer
        _startTime = System.currentTimeMillis();

        // have we been pondering
        if (_CONFIGURATION._USE_PONDERER && ponderHit(gameBoard)) {
            // ponder hit

        } // or ponder miss

        // convert GameBoard to OmegaBoard
        OmegaBoardPosition omegaBoard = new OmegaBoardPosition(gameBoard);

        // tell the ui and the observers out state
        _engineState  = ObservableEngine.THINKING;
        _statusInfo = "Engine calculating.";
        setChanged();
        notifyObservers(new PlayerDependendModelEvent("ENGINE "+_activeColor+" start calculating",
                _player, SIG_ENGINE_START_CALCULATING));

        // Reset all the counters used for the TreeSearchEngineWatcher
        resetCounters();

        // reset last search result
        _searchResult = null;

        // check for move from opening book
        GameMove bookMove = null;
        if (_CONFIGURATION._USE_BOOK) {
            _openingBook.initialize();
            bookMove = _openingBook.getBookMove(gameBoard.toFENString());
            if (bookMove != null) {
                // tell the ui and the observers out state
                _statusInfo = "Book move. Engine waiting.";
                _engineState  = ObservableEngine.IDLE;
                setChanged();
                notifyObservers(new PlayerDependendModelEvent("ENGINE "+_activeColor+" finished calculating", _player, SIG_ENGINE_FINISHED_CALCULATING));
                return bookMove;
            }
        }

        // configure the search
        // if not configured will used default mode
        _omegaSearch.configure(
                _game.isTimedGame(),
                _game.getWhiteTime()-_game.getWhiteClock().getTime(),
                _game.getBlackTime()-_game.getBlackClock().getTime(),
                Playroom.getInstance().getCurrentEngineLevelWhite(),
                Playroom.getInstance().getCurrentEngineLevelBlack()
                );

        // set latch to later wait for move from search
        _waitForMoveLatch = new CountDownLatch(1);

        // do normal search
        _omegaSearch.startSearch(omegaBoard);

        // wait for the result to come in from the search
        try { _waitForMoveLatch.await();
        } catch (InterruptedException e) { /*empty*/ }

        // convert result OmegaMove to GameMove
        GameMove bestMove = OmegaMove.convertToGameMove(_searchResult.bestMove);

        // tell the ui and the observers out state
        _statusInfo = "Engine waiting.";
        _engineState  = ObservableEngine.IDLE;
        setChanged();
        notifyObservers(new PlayerDependendModelEvent("ENGINE "+_activeColor+" finished calculating", _player, SIG_ENGINE_FINISHED_CALCULATING));

        // pondering
        if (_CONFIGURATION._USE_PONDERER) {
            /*
            if (_searchResult != null && _searchResult.ponderMove != 0) {
                _ponderMove = Move.toGameMove(_moveResult.ponderMove);
                _engineState  = ObservableEngine.PONDERING;
                _statusInfo = "Engine pondering.";
                // ponder search
                // make best move
                assert gameBoard.isLegalMove(bestMove);
                gameBoard.makeMove(bestMove);
                // make ponder move
                assert gameBoard.isLegalMove(_ponderMove);
                gameBoard.makeMove(_ponderMove);
                startPondering();
            } else {
                _ponderMove = null;
            }
             */
        }

        return bestMove;
    }

    /**********************************************************************
     * Omega Engine Methods
     **********************************************************************/

    /**
     * @param gameBoard
     * @return
     */
    private boolean ponderHit(GameBoard gameBoard) {
        return false;
    }

    /**
     * @param searchResult
     */
    public void storeResult(SearchResult searchResult) {
        _searchResult = searchResult;
        // result received - release the latch
        _waitForMoveLatch.countDown();
    }

    /**
     * @return GameColor color of player for this engine
     */
    public GameColor getActiveColor() {
        return _player.getColor();
    }

    /**
     *
     */
    private void resetCounters() {
        // TODO Auto-generated method stub

    }

    /**********************************************************************
     * ObservableEngine Interface
     **********************************************************************/

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getNumberOfMoves()
     */
    @Override
    public int getNumberOfMoves() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentMoveNumber()
     */
    @Override
    public int getCurrentMoveNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentMove()
     */
    @Override
    public GameMove getCurrentMove() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentMaxValueMove()
     */
    @Override
    public GameMove getCurrentMaxValueMove() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentSearchDepth()
     */
    @Override
    public int getCurrentSearchDepth() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentMaxSearchDepth()
     */
    @Override
    public int getCurrentMaxSearchDepth() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getTotalNodes()
     */
    @Override
    public long getTotalNodes() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentNodesPerSecond()
     */
    @Override
    public long getCurrentNodesPerSecond() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentUsedTime()
     */
    @Override
    public long getCurrentUsedTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getTotalBoards()
     */
    @Override
    public long getTotalBoards() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getTotalNonQuietBoards()
     */
    @Override
    public long getTotalNonQuietBoards() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getNodeCacheHits()
     */
    @Override
    public long getNodeCacheHits() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getNodeCacheMisses()
     */
    @Override
    public long getNodeCacheMisses() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentNodeCacheSize()
     */
    @Override
    public int getCurrentNodeCacheSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentNodesInCache()
     */
    @Override
    public int getCurrentNodesInCache() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getBoardCacheHits()
     */
    @Override
    public long getBoardCacheHits() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getBoardCacheMisses()
     */
    @Override
    public long getBoardCacheMisses() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentBoardCacheSize()
     */
    @Override
    public int getCurrentBoardCacheSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurBoardsInCache()
     */
    @Override
    public int getCurBoardsInCache() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentNumberOfThreads()
     */
    @Override
    public int getCurrentNumberOfThreads() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurConfig()
     */
    @Override
    public String getCurConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentPV()
     */
    @Override
    public GameMoveList getCurrentPV() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getInfoText() {
        return "";
    }

    private String _statusInfo = "";
    @Override
    public String getStatusText() {
        return _statusInfo;
    }

    private int _engineState = ObservableEngine.IDLE;
    @Override
    public int getState() {
        return _engineState;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getPonderMove()
     */
    @Override
    public GameMove getPonderMove() {
        // TODO Auto-generated method stub
        return null;
    }

    /** Will store the VERBOSE info until the EngineWatcher collects it. */
    private static final int _engineInfoTextMaxSize = 10000;
    private final StringBuilder _engineInfoText = new StringBuilder(_engineInfoTextMaxSize);
    /**
     * Provide additional information for the UI to collect.
     * E.g. verbose information etc.
     * Size is limited to avoid out of memory.
     * @param info
     */
    @Override
    public void printVerboseInfo(String info) {
        synchronized (_engineInfoText) {
            _engineInfoText.append(info);
            // out of memory protection if the info is not retrieved
            int oversize = _engineInfoText.length() - _engineInfoTextMaxSize;
            if (oversize > 0) _engineInfoText.delete(0, oversize);
        }
        if (_CONFIGURATION.VERBOSE_TO_SYSOUT) System.out.print(info);
    }


    /** */
    public static final int SIG_ENGINE_START_CALCULATING = 6000;
    /** */
    public static final int SIG_ENGINE_FINISHED_CALCULATING = 6010;
    /** */
    public static final int SIG_ENGINE_START_PONDERING = 6020;
    /** */
    public static final int SIG_ENGINE_FINISHED_PONDERING = 6030;
    /** */
    public static final int SIG_ENGINE_NO_PONDERING = 6040;

}
