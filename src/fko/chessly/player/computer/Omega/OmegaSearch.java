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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * @author Frank
 *
 */
public class OmegaSearch implements Runnable {

    private OmegaEngine _omegaEngine;
    private OmegaBoard _omegaBoard;

    // the thread in which we will do the actual search
    private Thread _searchThread = null;

    // used to wait for move from search
    private CountDownLatch _waitForInitializaitonLatch = new CountDownLatch(1);

    // flag to indicate if engine has been configured sine the last start
    private boolean _isConfigured = false;

    // flag to indicate to stop the search - can be called externally or via the timer clock.
    private boolean _stopSearch = true;

    /**
     * @param omegaEngine
     */
    public OmegaSearch(OmegaEngine omegaEngine) {
        _omegaEngine = omegaEngine;
    }

    /**
     * @param omegaBoard
     */
    public void startSearch(OmegaBoard omegaBoard) {
        assert omegaBoard != null : "omegaBoard must not be null";

        // has OmegaSearch.configure been called?
        if (!_isConfigured) {
            System.err.println("Search started without configuration - using defaults");
        }

        _omegaBoard = omegaBoard;

        _waitForInitializaitonLatch = new CountDownLatch(1);

        _stopSearch = false;

        // create new search thread
        _searchThread = new Thread(this, "OmegaEngine"+_omegaEngine.getActiveColor().toString());

        // start the search thread
        this._searchThread.start();

        // Wait for initialization in run() before returning from call
        try { _waitForInitializaitonLatch.await();
        } catch (InterruptedException e) { /* empty*/}
    }

    /**
     *
     */
    public void stop() {
        // TODO - do something that stops search

        _stopSearch = true;

        // Wait for the thread to die
        try { this._searchThread.join();
        } catch (InterruptedException e) { /* empty*/ }

        // clear thread
        _searchThread=null;
    }

    @Override
    public void run() {
        // initialize
        SearchResult searchResult = new SearchResult();

        // release latch so the caller can continue
        _waitForInitializaitonLatch.countDown();

        // search
        // DEBUG code
        try {
            Thread.sleep(2000); // pretend calculation
        } catch (InterruptedException e) { // ignore
        }

        // send the result
        _omegaEngine.storeResult(searchResult);

        // reset configuration flag
        _isConfigured = false;

    }

    /**
     * @param timedGame
     * @param remainingTimeWhite in ms
     * @param remainginTiemBlack in ms
     * @param currentEngineLevelWhite max search depth white
     * @param currentEngineLevelBlack max search depth black
     */
    public void configure(boolean timedGame,
            long remainingTimeWhite, long remainginTiemBlack,
            int currentEngineLevelWhite, int currentEngineLevelBlack) {

        _isConfigured = true;
        // TODO Auto-generated method stub

    }

    /**
     * Parameter class for the search result
     */
    static final class SearchResult {
        int bestMove = 0;
        int ponderMove = 0;
        int value = 0;
        int resultValue = 0;
        long time = -1;
        int moveNumber = 0;
        int depth = 0;
    }

}
