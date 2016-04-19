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
public class test_SearchOptimizations {

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
    private String _fen;
    private int _depth;

    @Test
    public void testOptimizations() {

        Instant start;
        int ROUNDS = 5;

        long[] results1 = new long[ROUNDS+1];
        long[] results2 = new long[ROUNDS+1];

        prepare();

        System.out.println("Running Timing Test Test 1 vs. Test 2");
        System.out.println();

        System.out.println(_omegaPosition1);

        for (int j=0; j<=ROUNDS ;j++) {

            if (j==0) System.out.println("WARMUP ROUND");
            else System.out.println(String.format("Run %d of %d", j, ROUNDS));

            prep1();
            start = Instant.now();
            test1();
            results1[j] = Duration.between(start,Instant.now()).toMillis();
            System.out.println(String.format("Test 1: \t%,d ms (Config: %s)", results1[j] , _omegaEngine1.getCurConfig()));

            prep2();
            start = Instant.now();
            test2();
            results2[j] = Duration.between(start,Instant.now()).toMillis();
            System.out.println(String.format("Test 2: \t%,d ms (Config: %s)", results2[j] , _omegaEngine2.getCurConfig()));

            System.out.println();
        }

        // Result Output
        long total1 = 0, total2 = 0;
        System.out.println(String.format(    "Run | Test 1   | Test 2   | %%    |"));
        System.out.println(String.format(    "----|----------|----------|------|"));
        for (int i=1; i <= ROUNDS; i++) {
            System.out.println(String.format("%3d | %,8d | %,8d | %3d%% |",
                    i, results1[i], results2[i], -(100-((100*results2[i])/results1[i])) ));
            total1 += results1[i];
            total2 += results2[i];
        }
        System.out.println(String.format(    "----|----------|----------|------|"));
        final long avg1 = total1/ROUNDS;
        final long avg2 = total2/ROUNDS;
        System.out.println(String.format("    | %,8d | %,8d | %3d%% |",
                avg1, avg2, -(100-((100*avg2)/avg1)) ));

    }

    /**
     *
     */
    private void prepare() {

        _player1 = createPlayer(GameColor.WHITE);
        _player2 = createPlayer(GameColor.WHITE);

        _omegaEngine1 = new OmegaEngine();
        _omegaEngine2 = new OmegaEngine();

        _depth = 4;
        //        { 0, 1,         0,       0,     0,      0},
        //        { 1, 20,        0,       0,     0,      0},
        //        { 2, 400,       0,       0,     0,      0},
        //        { 3, 8902,      34,      0,     12,     0},
        //        { 4, 197281,    1576,    0,     469,    8},
        //        { 5, 4865609,   82719,   258,   27351,  347},
        //        { 6, 119060324, 2812008, 5248,  809099, 10828},
        //_fen = NotationHelper.StandardBoardFEN;
        //_fen = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - 0 1"; // black
        //_fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/pbp2PPP/1R4K1 b kq e3 0 113";
        //_fen = "r3k2r/1ppn3p/2q1q1n1/4P3/2q1Pp2/6R1/pbp2PPP/1R4K1 w kq - 0 113";
        //_fen = "8/1P6/6k1/8/8/8/p1K5/8 w - - 0 1";
        //_fen = "4rk2/p5p1/1p2P2N/7R/nP5P/5PQ1/b6K/q7 w - - 0 1"; // good case for move cache
        //_fen = "4k2r/1q1p1pp1/p3p3/1pb1P3/2r3P1/P1N1P2p/1PP1Q2P/2R1R1K1 b k - 0 1";
        //_fen = "r2r1n2/pp2bk2/2p1p2p/3q4/3PN1QP/2P3R1/P4PP1/5RK1 w - - 0 1";
        //_fen = "1kr4r/ppp2bq1/4n3/4P1pp/1NP2p2/2PP2PP/5Q1K/4R2R w - - 0 1";
        //_fen = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - 0 1";
        //_fen = "R6R/3Q4/1Q4Q1/4Q3/2Q4Q/Q4Q2/pp1Q4/kBNN1KB1 w - - 0 1"; // 218 moves to make
        //_fen = "r3brk1/ppq3bp/4Qnp1/4N3/1PPB4/2n5/P3B1PP/3R1RK1 b - -"; // e8f7 e5f7 c3e2
        //_fen = "6k1/5pp1/8/4P1qp/1Q3p2/pP6/P1P3PP/2K5 b - -"; // f4f3
        //_fen = "1r4k1/2r3p1/4p2p/Rp1bPp2/1P1p1P2/2qP3P/2R3PK/1Q2N3 b - -"; //
        _fen = "6k1/1Pr3p1/4p2p/R2bPp2/3p1P2/2qP3P/6PK/4N3 w - -"; // promotion test
        _omegaPosition1 = new OmegaBoardPosition(_fen);
        _omegaPosition2 = new OmegaBoardPosition(_fen);

        OmegaConfiguration.PERFT                        = false;
        // without PERFT mates will not be counted

        _omegaEngine1._CONFIGURATION._USE_PONDERER      = false;
        _omegaEngine1._CONFIGURATION._USE_BOOK          = false;
        _omegaEngine2._CONFIGURATION._USE_PONDERER      = false;
        _omegaEngine2._CONFIGURATION._USE_BOOK          = false;

        _omegaEngine1._CONFIGURATION._USE_BOARD_CACHE   = true;
        _omegaEngine1._CONFIGURATION._USE_NODE_CACHE    = true;
        _omegaEngine1._CONFIGURATION._USE_MOVE_CACHE    = true;
        _omegaEngine1._CONFIGURATION._USE_PRUNING       = true;
        _omegaEngine1._CONFIGURATION._USE_PVS           = true;
        _omegaEngine1._CONFIGURATION._USE_MDP           = true;
        _omegaEngine1._CONFIGURATION._USE_MPP           = false;
        _omegaEngine1._CONFIGURATION._USE_QUIESCENCE    = false;

        _omegaEngine2._CONFIGURATION._USE_BOARD_CACHE   = true;
        _omegaEngine2._CONFIGURATION._USE_NODE_CACHE    = true;
        _omegaEngine2._CONFIGURATION._USE_MOVE_CACHE    = true;
        _omegaEngine2._CONFIGURATION._USE_PRUNING       = true;
        _omegaEngine2._CONFIGURATION._USE_PVS           = true;
        _omegaEngine2._CONFIGURATION._USE_MDP           = true;
        _omegaEngine2._CONFIGURATION._USE_MPP           = true;
        _omegaEngine2._CONFIGURATION._USE_QUIESCENCE    = false;

        _omegaEngine1.init(_player1);
        _omegaEngine2.init(_player2);
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

        // Mate in 5 half moves
        _omegaSearch1 = new OmegaSearch(_omegaEngine1);
        _omegaPosition1 = new OmegaBoardPosition(_fen);

        _omegaSearch1.configureMaxDepth(_depth);
        _omegaSearch1.startSearch(_omegaPosition1);

        while (_omegaSearch1.isSearching()) {
            try { _omegaSearch1._searchThread.join();
            } catch (InterruptedException e) { e.printStackTrace(); }
        }

        System.out.print(OmegaMove.toString(_omegaEngine1.getSearchResult().bestMove));
        System.out.print("\tPV: "+_omegaSearch1._principalVariation[0].toNotationString());
        System.out.println(String.format("\tNodes: %,9d \tBoards: %,9d \tPrunings: %,6d \tPV Researches: %,6d \tMoveC: %,6d \tMoveG: %,6d",
                _omegaSearch1._nodesVisited, _omegaSearch1._boardsEvaluated, _omegaSearch1._prunings, _omegaSearch1._pv_researches, _omegaSearch1._MovesFromCache, _omegaSearch1._MovesGenerated));
        //assertEquals("NORMAL qd6-d1", OmegaMove.toString(_omegaEngine1.getSearchResult().bestMove));

    }

    /*
     * TEST 2 ################################################################
     */

    private void prep2() {
        if (_omegaSearch2._evalCache != null) _omegaSearch2._evalCache.clear();
        if (_omegaSearch2._transpositionTable != null ) _omegaSearch2._transpositionTable.clear();
    }

    private void test2() {

        // Mate in 5 half moves
        _omegaSearch2 = new OmegaSearch(_omegaEngine2);
        _omegaPosition2 = new OmegaBoardPosition(_fen);

        _omegaSearch2.configureMaxDepth(_depth);
        _omegaSearch2.startSearch(_omegaPosition2);

        while (_omegaSearch2.isSearching()) {
            try { _omegaSearch2._searchThread.join();
            } catch (InterruptedException e) { e.printStackTrace(); }
        }

        System.out.print(OmegaMove.toString(_omegaEngine2.getSearchResult().bestMove));
        System.out.print("\tPV: "+_omegaSearch2._principalVariation[0].toNotationString());
        System.out.println(String.format("\tNodes: %,9d \tBoards: %,9d \tPrunings: %,6d \tPV Researches: %,6d \tMoveC: %,6d \tMoveG: %,6d",
                _omegaSearch2._nodesVisited, _omegaSearch2._boardsEvaluated, _omegaSearch2._prunings, _omegaSearch2._pv_researches, _omegaSearch2._MovesFromCache, _omegaSearch2._MovesGenerated));
        //assertEquals("NORMAL qd6-d1", OmegaMove.toString(_omegaEngine2.getSearchResult().bestMove));

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
