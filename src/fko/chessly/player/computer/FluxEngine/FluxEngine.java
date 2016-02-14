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

import java.util.concurrent.CountDownLatch;

import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.mvc.ModelObservable;
import fko.chessly.player.Player;
import fko.chessly.player.computer.Engine;

/**
 * This class wraps the Flux engine code into a Chessly Engine.
 *
 * @author fkopp
 *
 */
public class FluxEngine extends ModelObservable implements Engine {

    private Search search;
    private TranspositionTable transpositionTable;
    private Position board = null;
    private final int[] timeTable = new int[Depth.MAX_PLY + 1];
    private GameColor _activeColor;
    private Game _game;

    private CountDownLatch _waitForMoveLatch = new CountDownLatch(0);
    private GameMove _bestMove = null;;

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
        this.search = new Search(this, new Position(new GameBoardImpl()), this.transpositionTable, this.timeTable);

    }

    private void initializeTranspositionTable() {
        int numberOfEntries = Configuration.transpositionTableSize * 1024 * 1024 / TranspositionTable.ENTRYSIZE;
        transpositionTable = new TranspositionTable(numberOfEntries);
        Runtime.getRuntime().gc();
    }

    /**
     * @see fko.chessly.player.computer.Engine#getNextMove(fko.chessly.game.GameBoard)
     */
    @Override
    public GameMove getNextMove(GameBoard gameBoard) {
        assert(gameBoard!=null);

        this.board = new Position(gameBoard);

        if (this.board != null) {
            if (this.search.isStopped()) {
                // Create a new search
                this.search = new Search(this, this.board, this.transpositionTable, this.timeTable);

                // Set search parameters from current Game

                // set the search time - unlimited for non timed game
                if (_game.isTimedGame()) {
                    final long whiteTimeLeft = _game.getWhiteTime() - _game.getWhiteClock().getTime();
                    this.search.setSearchClock(Color.WHITE, whiteTimeLeft);
                    this.search.setSearchClockIncrement(Color.WHITE, 0); // not used
                    final long blackTimeLeft = _game.getBlackTime() - _game.getBlackClock().getTime();
                    this.search.setSearchClock(Color.BLACK, blackTimeLeft);
                    this.search.setSearchClockIncrement(Color.BLACK, 0); // not used
                } else {
                    // set the max search depth from the level in game
                    if (_activeColor.isWhite()) {
                        this.search.setSearchDepth(Playroom.getInstance().getCurrentEngineLevelWhite());
                    } else {
                        this.search.setSearchDepth(Playroom.getInstance().getCurrentEngineLevelBlack());
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
                this.search.start();
                this.board = null;
            }
        }

        // wait for the result to come in
        try {
            _waitForMoveLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return _bestMove;
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
     * @param gameMove
     */
    public void storeResult(GameMove gameMove) {
        _bestMove = gameMove;
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


}
