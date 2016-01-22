/*
 * <p>GPL Dislaimer</p>
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

package fko.chessly.game;

/**
 * Thrown to indicate that we tried to create a move from a notation.
 * where the move was not valid on a given position.<br/>
 */
public class InvalidMoveException extends RuntimeException {

    private static final long serialVersionUID = 8763651318617006698L;

    /**
     * Constructs a new <code>InvalidMoveException</code> with the
     * specified detail message.
     * Used for moves generated through notations where the 
     * move was not valid on a given position.  
     *
     * @param msg the detail message.
     */
    public InvalidMoveException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new <code>IllegalMoveException</code> with no detail message.
     */
    public InvalidMoveException() {}

}
