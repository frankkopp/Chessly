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

import fko.chessly.game.GameColor;

/**
 * <p/>
 * The Color class represents the two colors of a Chessly game and a special color for empty fields (NONE).
 * This class can not be instantiated. It keeps public references to the only possible instances BLACK, WHITE, NONE.
 * These instances are immutable. As it is not possible to have any other instances of ChesslyColors the use of
 * these instances is as fast as if using an int.
 * </p>
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
@SuppressWarnings("javadoc")
public enum OmegaColor {

    WHITE,
    BLACK,
    NONE;

    public static final OmegaColor[] values = {
            WHITE, BLACK
    };

    /**
     * Returns the other ChesslyColor.
     * @return int - as defined in ChesslyColor
     */
    public OmegaColor getInverseColor() {
        switch (this) {
            case BLACK:
                return WHITE;
            case WHITE:
                return BLACK;
            case NONE:
                throw new UnsupportedOperationException("Color.NONE has no inverse color");
            default:
                throw new RuntimeException("Invalid Color");
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
            case WHITE: return 'w';
            case BLACK: return 'b';
            case NONE:
            default: return ' ';
        }
    }

    /**
     * Convenience method to check if the instance is BLACK
     * @return true if black
     */
    public boolean isBlack() {
        return this==BLACK;
    }

    /**
     * Convenience method to check if the instance is WHITE
     * @return true if white
     */
    public boolean isWhite() {
        return this==WHITE;
    }

    /**
     * Convenience method to check if the instance is NONE
     * @return true if neither white nor black
     */
    public boolean isNone() {
        return this==NONE;
    }

    /**
     * Convert from GameColor
     * @param c
     * @return matching OmegaColor
     */
    public static OmegaColor convertFromGameColor(GameColor c) {
        switch (c) {
            case WHITE:
                return OmegaColor.WHITE;
            case BLACK:
                return OmegaColor.BLACK;
            case NONE:
                return OmegaColor.NONE;
            default:
                throw new RuntimeException("Invalid Color");
        }
    }

    /**
     * Convert to GameColor
     * @return matching GameColor
     */
    public GameColor convertToGameColor() {
        switch (this) {
            case WHITE:
                return GameColor.WHITE;
            case BLACK:
                return GameColor.BLACK;
            case NONE:
                return GameColor.NONE;
            default:
                throw new RuntimeException("Invalid Color");
        }
    }

}
