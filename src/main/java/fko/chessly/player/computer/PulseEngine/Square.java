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

final class Square {

    static final int MASK = 0x7F;

    static final int a1 = 0;   static final int a2 = 16;
    static final int b1 = 1;   static final int b2 = 17;
    static final int c1 = 2;   static final int c2 = 18;
    static final int d1 = 3;   static final int d2 = 19;
    static final int e1 = 4;   static final int e2 = 20;
    static final int f1 = 5;   static final int f2 = 21;
    static final int g1 = 6;   static final int g2 = 22;
    static final int h1 = 7;   static final int h2 = 23;

    static final int a3 = 32;  static final int a4 = 48;
    static final int b3 = 33;  static final int b4 = 49;
    static final int c3 = 34;  static final int c4 = 50;
    static final int d3 = 35;  static final int d4 = 51;
    static final int e3 = 36;  static final int e4 = 52;
    static final int f3 = 37;  static final int f4 = 53;
    static final int g3 = 38;  static final int g4 = 54;
    static final int h3 = 39;  static final int h4 = 55;

    static final int a5 = 64;  static final int a6 = 80;
    static final int b5 = 65;  static final int b6 = 81;
    static final int c5 = 66;  static final int c6 = 82;
    static final int d5 = 67;  static final int d6 = 83;
    static final int e5 = 68;  static final int e6 = 84;
    static final int f5 = 69;  static final int f6 = 85;
    static final int g5 = 70;  static final int g6 = 86;
    static final int h5 = 71;  static final int h6 = 87;

    static final int a7 = 96;  static final int a8 = 112;
    static final int b7 = 97;  static final int b8 = 113;
    static final int c7 = 98;  static final int c8 = 114;
    static final int d7 = 99;  static final int d8 = 115;
    static final int e7 = 100; static final int e8 = 116;
    static final int f7 = 101; static final int f8 = 117;
    static final int g7 = 102; static final int g8 = 118;
    static final int h7 = 103; static final int h8 = 119;

    static final int NOSQUARE = 127;

    static final int[] values = {
            a1, b1, c1, d1, e1, f1, g1, h1,
            a2, b2, c2, d2, e2, f2, g2, h2,
            a3, b3, c3, d3, e3, f3, g3, h3,
            a4, b4, c4, d4, e4, f4, g4, h4,
            a5, b5, c5, d5, e5, f5, g5, h5,
            a6, b6, c6, d6, e6, f6, g6, h6,
            a7, b7, c7, d7, e7, f7, g7, h7,
            a8, b8, c8, d8, e8, f8, g8, h8
    };

    // These are our move deltas.
    // N = north, E = east, S = south, W = west
    static final int N = 16;
    static final int E = 1;
    static final int S = -16;
    static final int W = -1;
    static final int NE = N + E;
    static final int SE = S + E;
    static final int SW = S + W;
    static final int NW = N + W;

    private Square() {
    }

    static boolean isValid(int square) {
        return (square & 0x88) == 0;
    }

    static int getFile(int square) {
        assert isValid(square);

        int file = square & 0xF;
        assert File.isValid(file);

        return file;
    }

    static String getFileChar(int square) {
        assert isValid(square);
        return File.toChar(getFile(square));
    }

    static int getRank(int square) {
        assert isValid(square);

        int rank = square >>> 4;
        assert Rank.isValid(rank);

        return rank;
    }

    static int valueOf(int rank, int file) {
        int lookup = rank * 16 + file;
        if (!isValid(lookup))
            throw new IllegalArgumentException("not a valid Square");
        lookup = rank * 8 + file;
        return values[lookup];
    }

    static int valueOf(String file, String rank) {
        int f = File.NOFILE;
        switch (file) {
            case "a": f = File.a; break;
            case "b": f = File.b; break;
            case "c": f = File.c; break;
            case "d": f = File.d; break;
            case "e": f = File.e; break;
            case "f": f = File.f; break;
            case "g": f = File.g; break;
            case "h": f = File.h; break;
            default:
                throw new IllegalArgumentException("No such file "+file);
        }
        int r = Integer.parseInt(rank)-1;
        if (!Rank.isValid(r))
            throw new IllegalArgumentException("No such rank "+rank);

        return valueOf(r, f);
    }

    static boolean isPromotionRank(int color, int square) {
        switch (color) {
            case Color.WHITE:
                return Square.getRank(square) == 7 ? true : false;
            case Color.BLACK:
                return Square.getRank(square) == 0 ? true : false;
            default:
                throw new IllegalArgumentException("not a valid color");
        }
    }

    static boolean isPawnBaseRank(int color, int square) {
        switch (color) {
            case Color.WHITE:
                return Square.getRank(square) == 1 ? true : false;
            case Color.BLACK:
                return Square.getRank(square) == 6 ? true : false;
            default:
                throw new IllegalArgumentException("not a valid color");
        }
    }

    static String toString(int square) {
        assert isValid(square);
        return ""+Square.getFileChar(square)+(getRank(square)+1);
    }

}
