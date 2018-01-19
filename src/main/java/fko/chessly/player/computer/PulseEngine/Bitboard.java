/*
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 * ================================================================================
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

/**
 * Bitboard stores squares as bits in a 64-bit long. We provide methods to
 * convert bit squares to 0x88 squares and vice versa.
 */
final class Bitboard implements Cloneable {

    long squares = 0;

    public Bitboard() {
    }

    public Bitboard(Bitboard oldBitboard) {
        this.squares = oldBitboard.squares;
    }

    static int next(long squares) {
        return toX88Square(Long.numberOfTrailingZeros(squares));
    }

    private static int toX88Square(int square) {
        assert square >= 0 && square < Long.SIZE;

        return ((square & ~7) << 1) | (square & 7);
    }

    private static int toBitSquare(int square) {
        assert Square.isValid(square);

        return ((square & ~7) >>> 1) | (square & 7);
    }

    int size() {
        return Long.bitCount(squares);
    }

    void add(int square) {
        assert Square.isValid(square);
        assert (squares & (1L << toBitSquare(square))) == 0;

        squares |= 1L << toBitSquare(square);
    }

    void remove(int square) {
        assert Square.isValid(square);
        assert (squares & (1L << toBitSquare(square))) != 0;

        squares &= ~(1L << toBitSquare(square));
    }

    @Override
    public String toString() {
        String s = "";
        for (int r = 7; r >= 0; r--) {
            for (int f = 0; f < 8; f++) {
                int sq = Square.values[r * 8 + f];
                if (Square.isValid(sq)) {
                    long b = 1L << Bitboard.toBitSquare(sq);
                    if ((b & squares) == b) {
                        s += "X ";
                    } else {
                        s += "O ";
                    }
                }
            }
            s += "\n";
        }
        return s;
    }

    @Override
    protected Object clone() {
        return new Bitboard(this);
    }

}
