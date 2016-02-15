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
package fko.chessly.player.computer;

import java.util.List;

import fko.chessly.game.GameMove;

/**
 * @author fkopp
 */
public interface ObservableEngine extends Engine {

    /**
     * return the number of possible moves for the current move
     * @return int
     */
    int getNumberOfMoves();

    /**
     * return the current move number
     * @return int
     */
    int getCurrentMoveNumber();

    /**
     * returns the current move in calculation
     * @return Move
     */
    GameMove getCurrentMove();

    /**
     * return the current best move
     * @return Move
     */
    GameMove getCurrentMaxValueMove();

    /**
     * returns the current depth in the search tree (without non-quite extra depth)
     * @return int
     */
    int getCurrentSearchDepth();

    /**
     * returns the current depth in the search tree (with non-quite extra depth)
     * @return int
     */
    int getCurrentMaxSearchDepth();

    /**
     * return the number of nodes checked so far
     * @return int
     */
    long getTotalNodes();

    /**
     * returns the number of nodes per second for the current calculation
     * @return int
     */
    long getCurrentNodesPerSecond();

    /**
     * returns the used time for the current move
     * @return long
     */
    long getCurrentUsedTime();

    /**
     * return the number of boards analysed so far
     * @return int
     */
    long getTotalBoards();

    /**
     * return the number of non-quiet boards found so far
     * @return int
     */
    long getTotalNonQuietBoards();

    /**
     * return the number of cache hits so far
     * @return int
     */
    long getNodeCacheHits();

    /**
     * return the nubmer of cache misses so far
     * @return int
     */
    long getNodeCacheMisses();

    /**
     * return the current cache size
     * @return int
     */
    int getCurNodeCacheSize();

    /**
     * return the current number of boards in cache
     * @return int
     */
    int getCurNodesInCache();

    /**
     * return the number of cache hits so far
     * @return int
     */
    long getBoardCacheHits();

    /**
     * return the nubmer of cache misses so far
     * @return int
     */
    long getBoardCacheMisses();

    /**
     * return the current cache size
     * @return int
     */
    int getCurBoardCacheSize();

    /**
     * return the current number of boards in cache
     * @return int
     */
    int getCurBoardsInCache();

    /**
     * Returns the configured number of threads to use
     * @return number of threads
     */
    int getCurNumberOfThreads();

    /**
     * Returns a string explaining the configuration of the engine.
     * E.g. Pruning, PV, Cache,
     * @return config string
     */
    String getCurConfig();

    /**
     * @return Principal Variation
     */
    public abstract List<GameMove> getCurrentPV();

    /**
     * Returns a string from the engine which should be displayed
     * in the UI engine info panel.
     * @return String
     */
    String getInfoText();

    /**
     * Returns status info about the engine,
     * @return status string to display as engine status
     */
    String getStatusText();

    // -- possible states of a engine --
    /** Status when the engine is not doing anything */
    public static final int IDLE = 0;
    /** Status when the engine is calculating the next move */
    public static final int THINKING = 1;
    /** Status when the engine is pondering over an opponents possible move */
    public static final int PONDERING = 2;

    /**
     * Returns the current state of the engine as
     * defined in the Engine interface
     * WAITING | THINKING | PONDERING
     * @return status one of Engine.WAITING | Engine.THINKING | Engine.PONDERING
     */
    public int getState();

    /**
     * Return the current pondering move.
     * @return the move currently used for pondering - null if not pondering
     */
    public GameMove getPonderMove();

    /**
     * Provide additional information for the UI to collect.
     * E.g. verbose information etc.
     * @param info
     */
    public void printVerboseInfo(String info);
}
