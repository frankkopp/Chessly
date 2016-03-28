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

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.IntStream;

import org.junit.Test;

import fko.chessly.game.GameColor;
import fko.chessly.player.Player;
import fko.chessly.player.PlayerFactory;
import fko.chessly.player.PlayerType;

/**
 * @author Frank
 *
 */
public class testOmegaSearch {

    @Test
    public void testStartAndStopSearch() {

        Player _player = createPlayer(GameColor.WHITE);
        OmegaEngine _omegaEngine = new OmegaEngine();
        OmegaSearch _omegaSearch = new OmegaSearch(_omegaEngine);
        _omegaSearch.configure(false, 0, 0, 6, 6);
        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition();

        // init the engine
        _omegaEngine.init(_player);

        // Test stop even if there is no search running - should do nothing
        _omegaSearch.stop();

        // Test start and stop search
        System.out.println("Start and stop search");
        _omegaSearch.startSearch(_omegaPosition);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Test stopping during searching
        _omegaSearch.stop();
        System.out.println(_omegaEngine.getSearchResult());
        System.out.flush();

        System.out.println("Start search and wait for result");
        // test search
        _omegaSearch.configure(false, 0, 0, 4, 4);
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

    @Test
    public void testSearch() {

        Player _player = createPlayer(GameColor.WHITE);
        OmegaEngine _omegaEngine = new OmegaEngine();
        OmegaSearch _omegaSearch = new OmegaSearch(_omegaEngine);
        _omegaSearch.configure(false, 0, 0, 6, 6);
        OmegaBoardPosition _omegaPosition = new OmegaBoardPosition();

        // init the engine
        _omegaEngine.init(_player);

        System.out.println("Start search and wait for result");
        // test search
        _omegaSearch.configure(false, 0, 0, 6, 6);
        _omegaSearch.startSearch(_omegaPosition);
        // what was the move?
        while (_omegaSearch.isSearching()) {
            try { Thread.sleep(200);
            } catch (InterruptedException e) {/* */}
        }
        System.out.println("Nodes / Evaluations: "+ _omegaSearch._nodesVisited +" / "+_omegaSearch._boardsEvaluated);
        System.out.println("Move: "+_omegaEngine.getSearchResult()+" ("+_omegaEngine.getSearchResult().resultValue+")");
        System.out.println("PV: "+_omegaSearch._principalVariation[0].toNotationString());

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

