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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.junit.Test;

import fko.chessly.game.GameColor;
import fko.chessly.player.Player;
import fko.chessly.player.PlayerFactory;
import fko.chessly.player.PlayerType;

/**
 * @author Frank
 *
 */
public class testTimings_Search {

    final AtomicLong a_long = new AtomicLong();
    final LongAdder long_a = new LongAdder();
    long a = 0;
    private Player _player1;
    private Player _player2;
    private OmegaEngine _omegaEngine1;
    private OmegaEngine _omegaEngine2;
    private OmegaBoardPosition _omegaPosition1;
    private OmegaBoardPosition _omegaPosition2;
    private OmegaSearch _omegaSearch1;
    private OmegaSearch _omegaSearch2;

    /**
     *
     */
    @Test
    public void testTiming() {

        prepare();

        int ROUNDS = 5;
        int DURATION = 10;

        int ITERATIONS = 0;

        Instant start;

        System.out.println("Running Timing Test Test 1 vs. Test 2");

        for (int j=0; j<ROUNDS ;j++) {

            System.out.println(String.format("Run %d of %d", j+1, ROUNDS));

            prep1();
            System.gc();
            start = Instant.now();
            ITERATIONS=0;
            for(;;) {
                ITERATIONS++;
                test1();
                if (Duration.between(start,Instant.now()).getSeconds() >= DURATION) break;
            }
            System.out.println(String.format("Test 1: %,7d runs/s", ITERATIONS/DURATION));

            prep2();
            System.gc();
            start = Instant.now();
            ITERATIONS=0;
            for(;;) {
                ITERATIONS++;
                test2();
                if (Duration.between(start,Instant.now()).getSeconds() >= DURATION) break;
            }
            System.out.println(String.format("Test 2: %,7d runs/s", ITERATIONS/DURATION));

        }

    }

    /**
     *
     */
    private void prepare() {

        _player1 = createPlayer(GameColor.WHITE);
        _player2 = createPlayer(GameColor.WHITE);

        _omegaEngine1 = new OmegaEngine();
        _omegaEngine2 = new OmegaEngine();

        _omegaEngine1.init(_player1);
        _omegaEngine2.init(_player2);

        String fen = "1r3rk1/1pnnq1bR/p1pp2B1/P2P1p2/1PP1pP2/2B3P1/5PK1/2Q4R w - - 0 1"; // white);
        fen =  "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/pbp2PPP/1R4K1 w kq - 0 113";
        //fen = NotationHelper.StandardBoardFEN;
        _omegaPosition1 = new OmegaBoardPosition(fen);
        _omegaPosition2 = new OmegaBoardPosition(fen);

        _omegaEngine1._CONFIGURATION._USE_NODE_CACHE    = true;
        _omegaEngine1._CONFIGURATION._USE_MOVE_CACHE    = true;
        _omegaEngine1._CONFIGURATION._USE_BOARD_CACHE   = true;
        _omegaEngine1._CONFIGURATION._USE_PRUNING       = true;
        _omegaEngine1._CONFIGURATION._USE_MDP           = false;
        _omegaEngine1._CONFIGURATION._USE_PVS           = false;
        _omegaEngine1._CONFIGURATION._USE_QUIESCENCE    = false;

        _omegaEngine2._CONFIGURATION._USE_NODE_CACHE    = true;
        _omegaEngine2._CONFIGURATION._USE_MOVE_CACHE    = true;
        _omegaEngine2._CONFIGURATION._USE_BOARD_CACHE   = true;
        _omegaEngine2._CONFIGURATION._USE_PRUNING       = true;
        _omegaEngine2._CONFIGURATION._USE_MDP           = false;
        _omegaEngine2._CONFIGURATION._USE_PVS           = false;
        _omegaEngine2._CONFIGURATION._USE_QUIESCENCE    = false;

        _omegaSearch1 = new OmegaSearch(_omegaEngine1);
        _omegaSearch2 = new OmegaSearch(_omegaEngine2);

    }

    /*
     * TEST 1 ################################################################
     */

    private void prep1() {
        if (_omegaSearch1._evalCache != null) _omegaSearch1._evalCache.clear();
        if (_omegaSearch1._transpositionTable != null ) _omegaSearch1._transpositionTable.clear();
    }

    private void test1() {
        _omegaSearch1.configureMaxDepth(4);
        _omegaSearch1.startSearch(_omegaPosition1);
        // what was the move?
        while (_omegaSearch1.isSearching()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        _omegaSearch1.stop();
        //        assertEquals("NORMAL Rh7-h8", OmegaMove.toString(_omegaEngine1.getSearchResult().bestMove));
    }

    /*
     * TEST 2 ################################################################
     */

    private void prep2() {
        if (_omegaSearch1._evalCache != null) _omegaSearch1._evalCache.clear();
        if (_omegaSearch1._transpositionTable != null ) _omegaSearch1._transpositionTable.clear();
    }

    private void test2() {
        _omegaSearch2.configureMaxDepth(4);
        _omegaSearch2.startSearch(_omegaPosition2);
        // what was the move?
        while (_omegaSearch2.isSearching()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        _omegaSearch2.stop();
        //        assertEquals("NORMAL Rh7-h8", OmegaMove.toString(_omegaEngine2.getSearchResult().bestMove));
    }



    private static Player createPlayer(GameColor color) {
        final Player newPlayer;
        try {
            if (color==GameColor.BLACK) {
                newPlayer = PlayerFactory.createPlayer(PlayerType.COMPUTER, "BLACK", GameColor.BLACK);
            } else {
                newPlayer = PlayerFactory.createPlayer(PlayerType.COMPUTER, "WHITE", GameColor.WHITE);
            }
        } catch (PlayerFactory.PlayerCreationException e) {
            throw new RuntimeException("Error creating player.",e);
        }
        return newPlayer;
    }



}
