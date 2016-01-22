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
