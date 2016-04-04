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
package fko.chessly.player.computer;

import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameMove;
import fko.chessly.player.Player;

/**
 * This defines a engine used by an Computer Player to generate the next Move
 * on a a given board.
 */
public interface Engine {

    /**
     * As the engines are dynamically loaded with the default constructor they need to initialized
     * @param player
     */
    public void init(Player player);

    /**
     * calculates next move
     * @param board
     * @return the computed move
     */
    public GameMove getNextMove(GameBoard board);

    /**
     * Sets the current game.
     * @param game
     */
    public void setGame(Game game);

    /**
     * @param n
     */
    public void setNumberOfThreads(int n);

    /**
     * Forwards a stop signal for the player to the engine to also stop all calculations.
     */
    public void stopEngine();

}
