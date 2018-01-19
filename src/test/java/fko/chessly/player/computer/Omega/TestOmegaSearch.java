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


import java.util.Random;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameColor;
import fko.chessly.game.NotationHelper;
import fko.chessly.player.Player;
import fko.chessly.player.PlayerFactory;
import fko.chessly.player.PlayerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Frank
 *
 */
public class TestOmegaSearch {

    @Test
    public void testBasicTimeControl_RemainingTime() {
        Player _player = createPlayer(GameColor.WHITE);
        OmegaEngine _omegaEngine = new OmegaEngine();
        _omegaEngine.init(_player);
        _omegaEngine._CONFIGURATION.VERBOSE_STATS = true;
        _omegaEngine._CONFIGURATION.VERBOSE_VARIATION= false;

        OmegaSearch _omegaSearch = new OmegaSearch(_omegaEngine);
        String fen = NotationHelper.StandardBoardFEN;
        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition(fen);

        _omegaSearch.configureRemainingTime(300, 99);

        _omegaSearch.startSearch(_omegaPosition);

        while (_omegaSearch.isSearching()) {
            try { Thread.sleep(200);
            } catch (InterruptedException e) {/* */}
        }
    }

    /**
     *
     */
    @Test
    public void testBasicTimeControl_TimePerMove() {
        Player _player = createPlayer(GameColor.WHITE);
        OmegaEngine _omegaEngine = new OmegaEngine();
        _omegaEngine.init(_player);
        _omegaEngine._CONFIGURATION.VERBOSE_STATS = true;
        _omegaEngine._CONFIGURATION.VERBOSE_VARIATION= false;

        OmegaSearch _omegaSearch = new OmegaSearch(_omegaEngine);
        String fen = NotationHelper.StandardBoardFEN;
        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition(fen);

        _omegaSearch.configureTimePerMove(15);

        _omegaSearch.startSearch(_omegaPosition);

        while (_omegaSearch.isSearching()) {
            try { Thread.sleep(200);
            } catch (InterruptedException e) {/* */}
        }
    }

    @Test
    public void testMateSearch() {

        // Test - Mate in 2
        //setupFromFEN("1r3rk1/1pnnq1bR/p1pp2B1/P2P1p2/1PP1pP2/2B3P1/5PK1/2Q4R w - - 0 1");

        // Test - Mate in 3
        //setupFromFEN("4rk2/p5p1/1p2P2N/7R/nP5P/5PQ1/b6K/q7 w - - 0 1");

        // Test - Mate in 3
        //setupFromFEN("4k2r/1q1p1pp1/p3p3/1pb1P3/2r3P1/P1N1P2p/1PP1Q2P/2R1R1K1 b k - 0 1");

        // Test - Mate in 4
        //setupFromFEN("r2r1n2/pp2bk2/2p1p2p/3q4/3PN1QP/2P3R1/P4PP1/5RK1 w - - 0 1");

        // Test - Mate in 5 (1.Sc6+! bxc6 2.Dxa7+!! Kxa7 3.Ta1+ Kb6 4.Thb1+ Kc5 5.Ta5# 1-0)
        //setupFromFEN("1kr4r/ppp2bq1/4n3/4P1pp/1NP2p2/2PP2PP/5Q1K/4R2R w - - 0 1");

        // Test - Mate in 3
        //setupFromFEN("1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - 0 1");

        // Test - Mate in 11
        //setupFromFEN("8/5k2/8/8/2N2N2/2B5/2K5/8 w - - 0 1");

        // Test - Mate in 13
        //setupFromFEN("8/8/6k1/8/8/8/P1K5/8 w - - 0 1");

        // Test - Mate in 15
        //setupFromFEN("8/5k2/8/8/8/8/1BK5/1B6 w - - 0 1");

        // Test - HORIZONT EFFECT
        //setupFromFEN("5r1k/4Qpq1/4p3/1p1p2P1/2p2P2/1p2P3/1K1P4/B7 w - - 0 1");

        // Test Pruning
        // 1r1r2k1/2p1qp1p/6p1/ppQB1b2/5Pn1/2R1P1P1/PP5P/R1B3K1 b ;bm Qe4

        Player _player = createPlayer(GameColor.WHITE);
        OmegaEngine _omegaEngine = new OmegaEngine();
        _omegaEngine.init(_player);
        _omegaEngine._CONFIGURATION.VERBOSE_STATS = false;
        _omegaEngine._CONFIGURATION.VERBOSE_VARIATION= false;

        //String fen = NotationHelper.StandardBoardFEN;
        //OmegaBoardPosition _omegaPosition = new OmegaBoardPosition(board);

        // Mate in 3 half moves
        OmegaSearch _omegaSearch = new OmegaSearch(_omegaEngine);
        String fen = "1r3rk1/1pnnq1bR/p1pp2B1/P2P1p2/1PP1pP2/2B3P1/5PK1/2Q4R w - - 0 1"; // white
        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition(fen);
        _omegaSearch.configureMaxDepth(4);
        _omegaSearch.startSearch(_omegaPosition);
        while (_omegaSearch.isSearching()) {
            try { Thread.sleep(200);
            } catch (InterruptedException e) {/* */}
        }
        System.out.println(OmegaMove.toString(_omegaEngine.getSearchResult().bestMove));
        System.out.println(_omegaSearch._principalVariation[0].toNotationString());
        assertEquals("NORMAL Rh7-h8", OmegaMove.toString(_omegaEngine.getSearchResult().bestMove));
        assertEquals("h7h8 g7h8 h1h8 ",_omegaSearch._principalVariation[0].toNotationString());

        // Mate in 5 half moves
        _omegaSearch = new OmegaSearch(_omegaEngine);
        fen = "4rk2/p5p1/1p2P2N/7R/nP5P/5PQ1/b6K/q7 w - - 0 1"; // white
        _omegaPosition = new OmegaBoardPosition(fen);
        _omegaSearch.configureMaxDepth(5);
        _omegaSearch.startSearch(_omegaPosition);
        while (_omegaSearch.isSearching()) {
            try { Thread.sleep(200);
            } catch (InterruptedException e) {/* */}
        }
        System.out.println(OmegaMove.toString(_omegaEngine.getSearchResult().bestMove));
        System.out.println(_omegaSearch._principalVariation[0].toNotationString());
        assertEquals("NORMAL Qg3-d6", OmegaMove.toString(_omegaEngine.getSearchResult().bestMove));
        assertEquals("g3d6 e8e7 d6d8 e7e8 e6e7 ",_omegaSearch._principalVariation[0].toNotationString());


        // Mate in 5half moves
        _omegaSearch = new OmegaSearch(_omegaEngine);
        fen = "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - 0 1"; // black
        _omegaPosition = new OmegaBoardPosition(fen);
        _omegaSearch.configureMaxDepth(5);
        _omegaSearch.startSearch(_omegaPosition);
        while (_omegaSearch.isSearching()) {
            try { Thread.sleep(200);
            } catch (InterruptedException e) {/* */}
        }
        System.out.println(OmegaMove.toString(_omegaEngine.getSearchResult().bestMove));
        System.out.println(_omegaSearch._principalVariation[0].toNotationString());
        assertEquals("NORMAL qd6-d1", OmegaMove.toString(_omegaEngine.getSearchResult().bestMove));
        assertEquals("d6d1 c1d1 d7g4 d1e1 d8d1 ",_omegaSearch._principalVariation[0].toNotationString());

    }

    @Test
    public void testSearch() {

        Player _player = createPlayer(GameColor.WHITE);
        OmegaEngine _omegaEngine = new OmegaEngine();
        _omegaEngine.init(_player);
        _omegaEngine._CONFIGURATION.VERBOSE_STATS = true;

        OmegaSearch _omegaSearch = new OmegaSearch(_omegaEngine);

        String fen = NotationHelper.StandardBoardFEN;
        //fen = "k6n/7p/6P1/7K/8/8/8/8 w - - 0 1"; // white
        //fen = "8/8/8/8/k7/1p6/P7/N6K b - - 0 1"; // black
        GameBoard board = new GameBoardImpl(fen);
        //makeMoves(board, "a2a4 a7a5 b2b3 b7b6");

        System.out.println(board);

        System.out.println("Start search and wait for result");

        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition(board);

        // test search
        _omegaSearch.configureMaxDepth(5);
        _omegaSearch.startSearch(_omegaPosition);
        // what was the move?
        while (_omegaSearch.isSearching()) {
            try { Thread.sleep(200);
            } catch (InterruptedException e) {/* */}
        }

    }


    @Test
    public void testMulitpleStartAndStopSearch() {

        Player _player = createPlayer(GameColor.WHITE);

        OmegaEngine _omegaEngine = new OmegaEngine();
        _omegaEngine.init(_player);

        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition();

        OmegaSearch _omegaSearch = new OmegaSearch(_omegaEngine);
        OmegaSearch _omegaSearch2 = new OmegaSearch(_omegaEngine);

        // Test start and stop search
        System.out.println("Start and stop search");

        for(int i=0;i<20;i++) {
            System.out.print("Search...");
            _omegaSearch.configurePondering();
            _omegaSearch2.configurePondering();
            _omegaSearch.startSearch(_omegaPosition);
            _omegaSearch2.startSearch(_omegaPosition);
            try { Thread.sleep(new Random().nextInt(1000));}
            catch (InterruptedException e) {/* */}

            // Test stopping during searching
            _omegaSearch.stop();
            _omegaSearch2.stop();

            System.out.println(_omegaEngine.getSearchResult());
            System.out.flush();
        }

    }

    @Test
    public void testStartAndStopSearch() {

        Player _player = createPlayer(GameColor.WHITE);
        OmegaEngine _omegaEngine = new OmegaEngine();
        OmegaSearch _omegaSearch = new OmegaSearch(_omegaEngine);
        _omegaSearch.configureTimePerMove(5);
        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition();

        // init the engine
        _omegaEngine.init(_player);

        // Test stop even if there is no search running - should do nothing
        _omegaSearch.stop();

        // Test start and stop search
        System.out.println("Start and stop search");
        _omegaSearch.startSearch(_omegaPosition);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Test stopping during searching
        _omegaSearch.stop();
        System.out.println(_omegaEngine.getSearchResult());
        System.out.flush();

        System.out.println("Start search and wait for result");
        // test search
        _omegaSearch.configureTimePerMove(2);
        _omegaSearch.startSearch(_omegaPosition);
        // what was the move?
        while (_omegaSearch.isSearching()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(_omegaEngine.getSearchResult());

    }

    /**
     */
    @SuppressWarnings("unused")
	private static void makeMoves(GameBoard board, String movesString) {
        String[] moves = movesString.split(" ");
        for (String move : moves) {
            board.makeMove(NotationHelper.createNewMoveFromSimpleNotation(board, move));
        }
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

