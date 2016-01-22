/*
 * <p>GPL Disclaimer</p>
 * <p>
 * "Chessly by Frank Kopp"
 * Copyright (c) 2003-2015 Frank Kopp
 * mail-to:frank@familie-kopp.de
 *
 * This file is part of "Chessly by Frank Kopp".
 *
 * "Chessly by Frank Kopp" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * "Chessly by Frank Kopp" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Chessly by Frank Kopp"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </p>
 *
 *
 */

package fko.chessly.player.computer.Adam;

import fko.chessly.game.GameBoard;
import fko.chessly.game.Game;
import fko.chessly.game.GameMove;
import fko.chessly.player.Player;
import fko.chessly.player.computer.Engine;

/**
 * Very simple Chessly computer player engine.
 * This engine always takes the first Move it gets from
 * board.generateMoves()
 */
public class AdamEngine implements Engine {

    /**
     * Initializes the engine
     */
    public void init(Player player) {
    }

    /**
     * Starts calculation and returns next move
     * @param board
     * @return first legal move
     */
    public GameMove getNextMove(GameBoard board) {
        return board.generateMoves().get(0);
    }

    /**
     * Sets the current game.
     * @param game
     */
    public void setGame(Game game) {
        // we don't need a game
    }

    public void setNumberOfThreads(int n) {
    }

    public void printInfo(String info) {
    }

}
