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

package fko.chessly.player.computer;

import java.util.List;

import fko.chessly.game.GameMove;

public interface EngineWatcher {

    /**
     * return the number of possible moves for the current move
     * @return int
     */
    int getNumberOfMoves();

    /**
     * return the current move number
     * @return int
     */
    int getCurMoveNumber();

    /**
     * returns the current move in calculation
     * @return Move
     */
    GameMove getCurMove();

    /**
     * return the current best move
     * @return Move
     */
    GameMove getMaxValueMove();

    /**
     * returns the current depth in the search tree (without non-quite extra depth)
     * @return int
     */
    int getCurSearchDepth();

    /**
     * returns the current depth in the search tree (with non-quite extra depth)
     * @return int
     */
    int getCurExtraSearchDepth();

    /**
     * return the number of nodes checked so far
     * @return int
     */
    long getNodesChecked();

    /**
     * returns the number of nodes per second for the current calculation
     * @return int
     */
    int getCurNodesPerSecond();

    /**
     * returns the used time for the current move
     * @return long
     */
    long getCurUsedTime();

    /**
     * return the number of boards analysed so far
     * @return int
     */
    long getBoardsChecked();

    /**
     * return the number of non-quiet boards found so far
     * @return int
     */
    long getBoardsNonQuiet();

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
     * @return
     */
    String getCurConfig();

    /**
     * @return Principal Variation
     */
    public abstract List<GameMove> getPV();

    /**
     * Returns a string from the engine which should be displayed
     * in the UI engine info panel.
     * @return String
     */
    String getInfoText();
}
