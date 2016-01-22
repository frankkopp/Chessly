/*
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 * ============================================================================
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

package fko.chessly.player.computer.PulseEngine;

final class Castling {

    static final int WHITE_KINGSIDE = 0;
    static final int WHITE_QUEENSIDE = 1;
    static final int BLACK_KINGSIDE = 2;
    static final int BLACK_QUEENSIDE = 3;
    static final int NOCASTLING = 4;

    static final int[] values = { WHITE_KINGSIDE, WHITE_QUEENSIDE,
            BLACK_KINGSIDE, BLACK_QUEENSIDE };

    private Castling() {
    }

    static boolean isValid(int castling) {
        switch (castling) {
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

    static int valueOf(int color, int castlingType) {
        switch (color) {
            case Color.WHITE:
                switch (castlingType) {
                    case CastlingType.KINGSIDE:
                        return WHITE_KINGSIDE;
                    case CastlingType.QUEENSIDE:
                        return WHITE_QUEENSIDE;
                    case CastlingType.NOCASTLINGTYPE:
                    default:
                        throw new IllegalArgumentException();
                }
            case Color.BLACK:
                switch (castlingType) {
                    case CastlingType.KINGSIDE:
                        return BLACK_KINGSIDE;
                    case CastlingType.QUEENSIDE:
                        return BLACK_QUEENSIDE;
                    case CastlingType.NOCASTLINGTYPE:
                    default:
                        throw new IllegalArgumentException();
                }
            case Color.NOCOLOR:
            default:
                throw new IllegalArgumentException();
        }
    }

    static public String toChar(int castling) {
        switch (castling) {
            case WHITE_KINGSIDE:
                return "K";
            case WHITE_QUEENSIDE:
                return "Q";
            case BLACK_KINGSIDE:
                return "k";
            case BLACK_QUEENSIDE:
                return "q";
            case NOCASTLING:
            default:
                return "-";
        }
    }

}
