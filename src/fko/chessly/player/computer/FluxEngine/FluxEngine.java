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
 *
 * ###################################
 *
 * Flux
 *
 * Copyright (C) 2007-2014 Phokham Nonava
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fko.chessly.player.computer.FluxEngine;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveList;
import fko.chessly.mvc.ModelObservable;
import fko.chessly.player.Player;
import fko.chessly.player.computer.Engine;
import fko.chessly.player.computer.ObservableEngine;
import fko.chessly.player.computer.FluxEngine.Search.Result;

/**
 * This class wraps the Flux engine code into a Chessly Engine.
 *
 * @author fkopp
 *
 */
public class FluxEngine extends ModelObservable implements ObservableEngine {

    private Search _search;
    private TranspositionTable _transpositionTable;
    private Position board = null;
    private final int[] timeTable = new int[Depth.MAX_PLY + 1];
    private GameColor _activeColor;
    private Game _game;

    private CountDownLatch _waitForMoveLatch = new CountDownLatch(0);
    private Result _moveResult;
    private int _engineState = ObservableEngine.IDLE;

    /**
     * Constructor
     */
    public FluxEngine() {
        super();
    }

    /**********************************
     * ENGINE interface
     **********************************/

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.Engine#init(fko.chessly.player.Player)
     */
    @Override
    public void init(Player player) {

        _activeColor = player.getColor();
        assert (_activeColor.isWhite() || _activeColor.isBlack());

        initializeTranspositionTable();

        // Create a new search
        this._search = new Search(this, new Position(new GameBoardImpl()), this._transpositionTable, this.timeTable);

    }

    private void initializeTranspositionTable() {
        int numberOfEntries = Configuration.transpositionTableSize * 1024 * 1024 / TranspositionTable.ENTRYSIZE;
        _transpositionTable = new TranspositionTable(numberOfEntries);
        Runtime.getRuntime().gc();
    }

    /**
     * @see fko.chessly.player.computer.Engine#getNextMove(fko.chessly.game.GameBoard)
     */
    @Override
    public GameMove getNextMove(GameBoard gameBoard) {
        assert(gameBoard!=null);

        _moveResult = null;

        _engineState  = ObservableEngine.THINKING;

        this.board = new Position(gameBoard);

        if (this.board != null) {
            if (this._search.isStopped()) {
                // Create a new search
                this._search = new Search(this, this.board, this._transpositionTable, this.timeTable);

                // Set search parameters from current Game

                // set the search time - unlimited for non timed game
                if (_game.isTimedGame()) {
                    final long whiteTimeLeft = _game.getWhiteTime() - _game.getWhiteClock().getTime();
                    this._search.setSearchClock(Color.WHITE, whiteTimeLeft);
                    this._search.setSearchClockIncrement(Color.WHITE, 0); // not used
                    final long blackTimeLeft = _game.getBlackTime() - _game.getBlackClock().getTime();
                    this._search.setSearchClock(Color.BLACK, blackTimeLeft);
                    this._search.setSearchClockIncrement(Color.BLACK, 0); // not used
                } else {
                    // set the max search depth from the level in game
                    if (_activeColor.isWhite()) {
                        this._search.setSearchDepth(Playroom.getInstance().getCurrentEngineLevelWhite());
                    } else {
                        this._search.setSearchDepth(Playroom.getInstance().getCurrentEngineLevelBlack());
                    }
                }

                /*
                if (command.getMovesToGo() != null && command.getMovesToGo() > 0) {
                    this.search.setSearchMovesToGo(command.getMovesToGo());
                }
                if (command.getInfinite()) {
                    this.search.setSearchInfinite();
                }
                if (command.getPonder()) {
                    this.search.setSearchPonder();
                }
                if (command.getSearchMoveList() != null) {
                    this.search.setSearchMoveList(command.getSearchMoveList());
                }
                 */

                // set the latch to wait for the result
                _waitForMoveLatch = new CountDownLatch(1);

                // Go...
                this._search.start();
                this.board = null;
            }
        }

        // wait for the result to come in
        try {
            _waitForMoveLatch.await();
        } catch (InterruptedException e) {
            //e.printStackTrace();
            return null;
        }

        _engineState  = ObservableEngine.IDLE;

        if (_moveResult != null) {
            final GameMove bestGameMove = Move.toGameMove(_moveResult.bestMove);
            bestGameMove.setValue(_moveResult.resultValue);
            return bestGameMove;
        }
        return null;
    }

    /**
     * @see fko.chessly.player.computer.Engine#setGame(fko.chessly.game.Game)
     */
    @Override
    public void setGame(Game game) {
        _game = game;
    }

    /**
     * @see fko.chessly.player.computer.Engine#setNumberOfThreads(int)
     */
    @Override
    public void setNumberOfThreads(int n) {
        // ignore
    }

    /**********************************
     * FluxEngine methods
     **********************************/

    /**
     * @param moveResult
     */
    public void storeResult(Result moveResult) {
        _moveResult = moveResult;
        // result received - release the latch
        _waitForMoveLatch.countDown();

    }

    /**
     * @return the activeColor
     */
    public GameColor getActiveColor() {
        return this._activeColor;
    }




    /**********************************
     * ObservableEngine interface
     **********************************/


    private GameMove _currentMove = null;;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentMove()
     * */
    @Override
    public GameMove getCurrentMove() {
        return _currentMove;
    }
    /**
     * @param currentMove the currentMove to set
     */
    public void setCurrentMove(GameMove currentMove) {
        this._currentMove = currentMove;
    }

    private int _currentMoveNumber = 0;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentMoveNumber()
     */
    @Override
    public int getCurrentMoveNumber() {
        return _currentMoveNumber;
    }
    /**
     * @param currentMoveNumber the currentMoveNumber to set
     */
    public void setCurrentMoveNumber(int currentMoveNumber) {
        this._currentMoveNumber = currentMoveNumber;
    }

    private int _numberOfMoves = 0;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getNumberOfMoves()
     */
    @Override
    public int getNumberOfMoves() {
        return _numberOfMoves;
    }
    /**
     * @param numberOfMoves the getNumberOfMoves to set
     */
    public void setNumberOfMoves(int numberOfMoves) {
        this._numberOfMoves = numberOfMoves;
    }

    private int _currentSearchDepth = 0;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentSearchDepth()
     */
    @Override
    public int getCurrentSearchDepth() {
        return _currentSearchDepth;
    }
    /**
     * @param currentSearchDepth the currentSearchDepth to set
     */
    public void setCurrentSearchDepth(int currentSearchDepth) {
        this._currentSearchDepth = currentSearchDepth;
    }

    private int _currentMaxSearchDepth = 0;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentMaxSearchDepth()
     */
    @Override
    public int getCurrentMaxSearchDepth() {
        return _currentMaxSearchDepth;
    }
    /**
     * @param currentMaxSearchDepth the currentMaxSearchDepth to set
     */
    public void setCurrentMaxSearchDepth(int currentMaxSearchDepth) {
        this._currentMaxSearchDepth = currentMaxSearchDepth;
    }

    private long _currentNodesPerSecond = 0;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentNodesPerSecond()
     */
    @Override
    public long getCurrentNodesPerSecond() {
        return _currentNodesPerSecond;
    }
    /**
     * @param currentNPS the currentNodesPerSecond to set
     */
    public void setCurrentNodesPerSecond(long currentNPS) {
        this._currentNodesPerSecond = currentNPS;
    }

    private long _currentUsedTime = 0;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentUsedTime()
     */
    @Override
    public long getCurrentUsedTime() {
        return _currentUsedTime;
    }
    /**
     * @param currentUsedTime the currentUsedTime to set
     */
    public void setCurrentUsedTime(long currentUsedTime) {
        this._currentUsedTime = currentUsedTime;
    }

    private long _totalNodes = 0;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getTotalNodes()
     */
    @Override
    public long getTotalNodes() {
        return _totalNodes;
    }
    /**
     * @param totalNodes the totalNodes to set
     */
    public void setTotalNodes(long totalNodes) {
        this._totalNodes = totalNodes;
    }

    private long _totalBoards = 0;

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getTotalBoards()
     */
    @Override
    public long getTotalBoards() {
        return _totalBoards;
    }
    /**
     * @param totalBoards the totalBoards to set
     */
    public void setTotalBoards(long totalBoards) {
        this._totalBoards = totalBoards;
    }

    private long _totalNonQuietBoards = 0;;
    /**
     * @see fko.chessly.player.computer.ObservableEngine#getTotalNonQuietBoards()
     */
    @Override
    public long getTotalNonQuietBoards() {
        return _totalNonQuietBoards;
    }
    /**
     * @param totalNonQuietBoards the totalNonQuietBoards to set
     */
    public void setTotalNonQuietBoards(long totalNonQuietBoards) {
        this._totalNonQuietBoards = totalNonQuietBoards;
    }




    private List<GameMove> _currentPV = null;
    /**
     * Get the current PV as a GameMoveList.
     * @return copy of the PV list as a GameMoveList
     *
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentPV()
     */
    @Override
    public GameMoveList getCurrentPV() {
        if (_currentPV == null) {
            return new GameMoveList();
        }
        // make a copy so we can change as we like
        return new GameMoveList(_currentPV);
    }
    /**
     * @param currentPV the currentPV to set
     */
    public void setCurrentPV(List<GameMove> currentPV) {
        this._currentPV = currentPV;
    }

    private GameMove _currentMaxValueMove = null;

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentMaxValueMove()
     */
    @Override
    public GameMove getCurrentMaxValueMove() {
        return _currentMaxValueMove;
    }
    /**
     * @param currentMaxValueMove the currentMaxValueMove to set
     */
    public void setCurrentMaxValueMove(GameMove currentMaxValueMove) {
        this._currentMaxValueMove = currentMaxValueMove;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getPonderMove()
     */
    @Override
    public GameMove getPonderMove() {
        if (_moveResult==null || _moveResult.ponderMove == Move.NOMOVE) return null;
        return Move.toGameMove(_moveResult.ponderMove);
    }

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurrentNodeCacheSize()
     */
    @Override
    public int getCurrentNodeCacheSize() {
        return Configuration.useTranspositionTable ? _transpositionTable.getSize() : 0;
    }

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurNodesInCache()
     */
    @Override
    public int getCurrentNodesInCache() {
        return Configuration.useTranspositionTable ? _transpositionTable.getNumberOfEntries() : 0;
    }

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getNodeCacheHits()
     */
    @Override
    public long getNodeCacheHits() {
        return _search.getNodeCacheHits();
    }

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getNodeCacheMisses()
     */
    @Override
    public long getNodeCacheMisses() {
        return _search.getNodeCacheMisses();
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurBoardCacheSize()
     */
    @Override
    public int getCurrentBoardCacheSize() {
        return _search.getCurrentBoardCacheSize();
    }

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getCurBoardsInCache()
     */
    @Override
    public int getCurBoardsInCache() {
        return _search.getCurBoardsInCache();
    }

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getBoardCacheHits()
     */
    @Override
    public long getBoardCacheHits() {
        return _search.getBoardCacheHits();
    }

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getBoardCacheMisses()
     */
    @Override
    public long getBoardCacheMisses() {
        return _search.getBoardCacheMisses();
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurNumberOfThreads()
     */
    @Override
    public int getCurNumberOfThreads() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getCurConfig()
     */
    @Override
    public String getCurConfig() {
        // TODO Auto-generated method stub
        return "N/A";
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getStatusText()
     */
    @Override
    public String getStatusText() {
        return "N/A";
    }

    /**
     * @see fko.chessly.player.computer.ObservableEngine#getState()
     */
    @Override
    public int getState() {
        return _engineState;
    }

    /** Will store the VERBOSE info until the EngineWatcher collects it. */
    private static final int _engineInfoTextMaxSize = 10000;
    private final StringBuilder _engineInfoText = new StringBuilder(_engineInfoTextMaxSize);
    /**
     * Provide additional information for the UI to collect.
     * E.g. verbose information etc.
     * Size is limit to avoid out of memory.
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
        if (Configuration.VERBOSE_TO_SYSOUT) System.out.print(info);
    }

    /**
     * The UI can collect additional text to display. E.g. verbose output.
     * The info will be deleted after collection.
     * The info buffer is limited and older entries will be deleted every time
     * new info is added an the maximum size is exceeded.
     */
    @Override
    public String getInfoText() {
        String s;
        synchronized (_engineInfoText) {
            s = _engineInfoText.toString();
            _engineInfoText.setLength(0);
        }
        return s;
    }

    private void printInfoln() {
        printVerboseInfo(String.format("%n"));
    }


}
