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
package fko.chessly.player;

import fko.chessly.game.GameColor;

/**
 * A factory for players.
 */
public class PlayerFactory {

    // -- Factories should be instantiated --
    private PlayerFactory() {}

    /**
     * Factory method for player creation without game.<br/>
     * @param playerType
     * @param name
     * @param color
     * @return a new player
     * @throws PlayerCreationException
     */
    public static Player createPlayer(PlayerType playerType, String name, GameColor color) throws PlayerCreationException {
        switch (playerType) {
            case HUMAN :
                return new HumanPlayer(name, color);
            case COMPUTER :
                return new ComputerPlayer(name, color);
            default :
                throw new PlayerCreationException("Unknown player type");
        }
    }

    /**
     * The PlayerCreationException is thrown when a player could not be created.
     */
    public static class PlayerCreationException extends Exception {

        private static final long serialVersionUID = -7789726686263929411L;

        /**
         * Constructs a new <code>PlayerCreationException</code> with no detail message.
         */
        public PlayerCreationException() {
        }

        /**
         * Constructs a new <code>PlayerCreationException</code> with the
         * specified detail message.
         *
         * @param msg the detail message.
         */
        public PlayerCreationException(String msg) {
            super(msg);
        }
    }
}
