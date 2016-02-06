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
package fko.chessly.player.computer.Random;

import java.util.List;

import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameMove;
import fko.chessly.player.Player;
import fko.chessly.player.computer.Engine;

/**
 * Simple Chessly computer player engine.
 * This engine always takes a random Move from
 * board.generateMoves()
 */
public class RandomEngine implements Engine {

    /**
     * Initializes the engine
     */
    @Override
    public void init(Player player) {
    }

    /**
     * starts calculation and returns next move
     * @param board
     * @return random legal move
     */
    @Override
    public GameMove getNextMove(GameBoard board) {

        try {
            Thread.sleep( 500+(int)(2000 * Math.random()) );
        } catch (InterruptedException e) {
            // ignore
        }

        List<GameMove> moves = board.generateMoves();
        if (!moves.isEmpty()) {
            int move = (int) Math.round((moves.size() - 1) * Math.random());
            return moves.get(move);
        } else {
            return null;
        }
    }

    /**
     * Sets the current game.
     * @param game
     */
    @Override
    public void setGame(Game game) {
        // we don't need a game
    }

    @Override
    public void setNumberOfThreads(int n) {
    }


    @Override
    public void printInfo(String info) {
    }


}
