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

import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameMove;
import fko.chessly.mvc.ModelObservable;
import fko.chessly.player.Player;
import fko.chessly.player.computer.Engine;
import fko.chessly.player.computer.ObservableEngine;

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
        initializeTranspositionTable();

        // Create a new search
        this.search = new Search(new GameBoardImpl(), this.transpositionTable, this.timeTable);

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
    public GameMove getNextMove(GameBoard board) {

        // FIXME getNextMove
        /*        if (this.board != null) {
            if (this.search.isStopped()) {
              // Create a new search
              this.search = new Search(getProtocol(), this.board, this.transpositionTable, this.timeTable);

              // Set all search parameters
              if (command.getDepth() != null && command.getDepth() > 0) {
                this.search.setSearchDepth(command.getDepth());
              }
              if (command.getNodes() != null && command.getNodes() > 0) {
                this.search.setSearchNodes(command.getNodes());
              }
              if (command.getMoveTime() != null && command.getMoveTime() > 0) {
                this.search.setSearchTime(command.getMoveTime());
              }
              for (GenericColor side : GenericColor.values()) {
                if (command.getClock(side) != null && command.getClock(side) > 0) {
                  this.search.setSearchClock(Color.valueOfColor(side), command.getClock(side));
                }
                if (command.getClockIncrement(side) != null && command.getClockIncrement(side) > 0) {
                  this.search.setSearchClockIncrement(Color.valueOfColor(side), command.getClockIncrement(side));
                }
              }
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

              // Go...
              this.search.start();
              this.board = null;
            }
        }*/

        return null;
    }

    /**
     * @see fko.chessly.player.computer.Engine#setGame(fko.chessly.game.Game)
     */
    @Override
    public void setGame(Game game) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.Engine#setNumberOfThreads(int)
     */
    @Override
    public void setNumberOfThreads(int n) {
        // TODO Auto-generated method stub

    }

    /**********************************
     * ObservableEngine interface
     **********************************/


}
