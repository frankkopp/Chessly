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
package fko.chessly.game;

/**
 * @author fkopp
 */
enum GameCastling {

    WHITE_KINGSIDE(0),
    WHITE_QUEENSIDE(1),
    BLACK_KINGSIDE(2),
    BLACK_QUEENSIDE(3),
    NOCASTLING(4);

    private final int index;

    static final GameCastling[] values = {
            WHITE_KINGSIDE, WHITE_QUEENSIDE,
            BLACK_KINGSIDE, BLACK_QUEENSIDE
    };

    private GameCastling(int i) {
        this.index = i;
    }

    public int getIndex() {
        return index;
    }

    boolean isValid() {
        switch (this) {
            case WHITE_KINGSIDE:
            case WHITE_QUEENSIDE:
            case BLACK_KINGSIDE:
            case BLACK_QUEENSIDE:
                return true;
            case NOCASTLING:
            default:
                return false;
        }
    }

    static GameCastling valueOf(GameColor color, GameCastlingType castlingType) {
        switch (color) {
            case WHITE:
                switch (castlingType) {
                    case KINGSIDE:
                        return WHITE_KINGSIDE;
                    case QUEENSIDE:
                        return WHITE_QUEENSIDE;
                    case NOCASTLINGTYPE:
                    default:
                        throw new IllegalArgumentException();
                }
            case BLACK:
                switch (castlingType) {
                    case KINGSIDE:
                        return BLACK_KINGSIDE;
                    case QUEENSIDE:
                        return BLACK_QUEENSIDE;
                    case NOCASTLINGTYPE:
                    default:
                        throw new IllegalArgumentException();
                }
            case NONE:
            default:
                throw new IllegalArgumentException();
        }
    }

    public String toChar() {
        switch (this) {
            case WHITE_KINGSIDE: return "K";
            case WHITE_QUEENSIDE: return "Q";
            case BLACK_KINGSIDE: return "k";
            case BLACK_QUEENSIDE: return "q";
            case NOCASTLING:
            default:
                return "-";
        }
    }

}
