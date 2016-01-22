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
 * <p/>
 * The ChesslyColor class represents the two colors of a Chessly game and a special color for empty fields (NONE).
 * This class can not be instanciated. It keeps public references to the only possible instances BLACK, WHITE, NONE.
 * These instances are immutable. As it is not possible to have any other instances of ChesslyColors the use of
 * these instances is as fast as if using an int.
 * </p>
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public enum GameColor {

    BLACK,
    WHITE,
    NONE;
    
    static final GameColor[] values = {
        WHITE, BLACK
    };
    
    /**
     * Returns the other ChesslyColor.
     * @return int - as defined in ChesslyColor
     */
    public GameColor getInverseColor() {
        if      (this == BLACK) {
            return WHITE;
        } else if (this == WHITE) {
            return BLACK;
        } else if (this == NONE ) {
            throw new UnsupportedOperationException("Chessly.NONE has no inverse color");
        } else {
            throw new RuntimeException("Invalid ChesslyColor");
        }
    }

    /**
     * Returns a character to use for a String representation of the field.<br/>
     * It accepts ChesslyColor.BLACK (X), ChesslyColor.WHITE (O), ChesslyColor.EMPTY (-) otherwise returns
     * an empty character.
     * @return char - one of 'X', '-', 'O' or ' '
     */
    public char toCharSymbol() {
        return toChar();
    }


    /**
     * Returns a character to use for a String representation of the field.<br/>
     * It accepts ChesslyColor.BLACK (X), ChesslyColor.WHITE (O), ChesslyColor.EMPTY (-) otherwise returns
     * an empty character.
     * @return char - one of 'b', '-', 'w' or ' '
     */
    public char toChar() {
    	switch (this) {
    	case WHITE:	return 'w';
    	case BLACK: return 'b';
    	case NONE:
    	default: return ' ';
    	}
    }

    /**
     * Convenience method to check if the instance is BLACK
     */
    public boolean isBlack() {
        return this==BLACK;
    }

    /**
     * Convenience method to check if the instance is WHITE
     */
    public boolean isWhite() {
        return this==WHITE;
    }

    /**
     * Convenience method to check if the instance is NONE
     */
    public boolean isNone() {
        return this==NONE;
    }

    /**
     * Convenience method to check if the instance is EMPTY
     */
    public boolean isEmpty() {
        return this==NONE;
    }

    /**
     * Convenience method to check if the instance is not EMPTY
     */
    public boolean isNotEmpty() {
        return this!=NONE;
    }

}
