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

final class Color {

    static final int WHITE = 0;
    static final int BLACK = 1;
    static final int NOCOLOR = 2;

    static final int[] values = { WHITE, BLACK };

    private Color() {
    }

    static boolean isValid(int color) {
        switch (color) {
            case WHITE:
            case BLACK:
                return true;
            case NOCOLOR:
            default:
                return false;
        }
    }

    static int opposite(int color) {
        switch (color) {
            case WHITE:
                return BLACK;
            case BLACK:
                return WHITE;
            case NOCOLOR:
            default:
                throw new IllegalArgumentException();
        }
    }

    static char toChar(int color) {
        switch (color) {
            case WHITE:
                return 'w';
            case BLACK:
                return 'b';
            case NOCOLOR:
            default:
                throw new IllegalArgumentException();
        }
    }

}
