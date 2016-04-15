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

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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
public class testTimings_Search2 {

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

    @Test
    public void testTiming() {

        prepare();

        int ROUNDS = 5;
        int DURATION = 15;

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
            System.out.println(String.format("Test 1: %,d runs in %,d ms", ITERATIONS, Duration.between(start,Instant.now()).toMillis()));

            prep2();
            System.gc();
            start = Instant.now();
            ITERATIONS=0;
            for(;;) {
                ITERATIONS++;
                test2();
                if (Duration.between(start,Instant.now()).getSeconds() >= DURATION) break;
            }
            System.out.println(String.format("Test 2: %,d runs in %,d ms", ITERATIONS, Duration.between(start,Instant.now()).toMillis()));

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
        //fen = NotationHelper.StandardBoardFEN;
        _omegaPosition1 = new OmegaBoardPosition(fen);
        _omegaPosition2 = new OmegaBoardPosition(fen);

        _omegaEngine1._CONFIGURATION._USE_NODE_CACHE = true;
        _omegaEngine1._CONFIGURATION._USE_BOARD_CACHE = true;
        _omegaEngine1._CONFIGURATION._USE_PRUNING = true;
        _omegaEngine2._CONFIGURATION._USE_NODE_CACHE = true;
        _omegaEngine2._CONFIGURATION._USE_BOARD_CACHE = true;
        _omegaEngine2._CONFIGURATION._USE_PRUNING = true;

        _omegaSearch1 = new OmegaSearch(_omegaEngine1);
        _omegaSearch2 = new OmegaSearch(_omegaEngine2);

    }

    private void prep1() {
        if (_omegaSearch1._evalCache != null) _omegaSearch1._evalCache.clear();
        if (_omegaSearch1._transpositionTable != null ) _omegaSearch1._transpositionTable.clear();
    }

    private void test1() {

        //        // Mate in 3 half moves
        //        String fen = "1r3rk1/1pnnq1bR/p1pp2B1/P2P1p2/1PP1pP2/2B3P1/5PK1/2Q4R w - - 0 1"; // white
        //        _omegaSearch1 = new OmegaSearch(_omegaEngine1);
        //        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition(fen);
        //        _omegaSearch1.configureMaxDepth(4);
        //        _omegaSearch1.startSearch(_omegaPosition);
        //        while (_omegaSearch1.isSearching()) {
        //            try { Thread.sleep(5);
        //            } catch (InterruptedException e) {/* */}
        //        }
        //        //System.out.println(OmegaMove.toString(_omegaEngine.getSearchResult().bestMove));
        //        //System.out.println(_omegaSearch._principalVariation[0].toNotationString());
        //        assertEquals("NORMAL Rh7-h8", OmegaMove.toString(_omegaEngine1.getSearchResult().bestMove));
        //        assertEquals("h7h8 g7h8 h1h8 ",_omegaSearch1._principalVariation[0].toNotationString());
        //
        //        // Mate in 5 half moves
        //        fen = "4rk2/p5p1/1p2P2N/7R/nP5P/5PQ1/b6K/q7 w - - 0 1"; // white
        //        _omegaSearch1 = new OmegaSearch(_omegaEngine1);
        //        _omegaPosition = new OmegaBoardPosition(fen);
        //        _omegaSearch1.configureMaxDepth(5);
        //        _omegaSearch1.startSearch(_omegaPosition);
        //        while (_omegaSearch1.isSearching()) {
        //            try { Thread.sleep(5);
        //            } catch (InterruptedException e) {/* */}
        //        }
        //        //System.out.println(OmegaMove.toString(_omegaEngine.getSearchResult().bestMove));
        //        //System.out.println(_omegaSearch._principalVariation[0].toNotationString());
        //        assertEquals("NORMAL Qg3-d6", OmegaMove.toString(_omegaEngine1.getSearchResult().bestMove));
        //        assertEquals("g3d6 e8e7 d6d8 e7e8 e6e7 ",_omegaSearch1._principalVariation[0].toNotationString());
        //

        // Mate in 5 half moves
        String fen = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - 0 1"; // black
        _omegaSearch1 = new OmegaSearch(_omegaEngine1);
        _omegaPosition1 = new OmegaBoardPosition(fen);
        _omegaSearch1.configureMaxDepth(5);
        _omegaSearch1.startSearch(_omegaPosition1);
        while (_omegaSearch1.isSearching()) {
            try { Thread.sleep(5);
            } catch (InterruptedException e) {/* */}
        }
        //System.out.println(OmegaMove.toString(_omegaEngine1.getSearchResult().bestMove));
        //System.out.println(_omegaSearch1._principalVariation[0].toNotationString());
        assertEquals("NORMAL qd6-d1", OmegaMove.toString(_omegaEngine1.getSearchResult().bestMove));
        //assertEquals("d6d1 c1d1 d7g4 d1e1 d8d1 ",_omegaSearch._principalVariation[0].toNotationString());



    }

    private void prep2() {
        if (_omegaSearch1._evalCache != null) _omegaSearch1._evalCache.clear();
        if (_omegaSearch1._transpositionTable != null ) _omegaSearch1._transpositionTable.clear();
    }

    private void test2() {

        // Mate in 5 half moves
        String fen = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - 0 1"; // black
        _omegaSearch2 = new OmegaSearch(_omegaEngine2);
        _omegaPosition2 = new OmegaBoardPosition(fen);
        _omegaSearch2.configureMaxDepth(5);
        _omegaSearch2.startSearch(_omegaPosition2);
        while (_omegaSearch2.isSearching()) {
            try { Thread.sleep(5);
            } catch (InterruptedException e) {/* */}
        }
        //System.out.println(OmegaMove.toString(_omegaEngine1.getSearchResult().bestMove));
        //System.out.println(_omegaSearch1._principalVariation[0].toNotationString());
        assertEquals("NORMAL qd6-d1", OmegaMove.toString(_omegaEngine2.getSearchResult().bestMove));
        //assertEquals("d6d1 c1d1 d7g4 d1e1 d8d1 ",_omegaSearch._principalVariation[0].toNotationString());

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
