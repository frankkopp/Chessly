/*
 * Copyright (C) 2007-2014 Phokham Nonava
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package fko.chessly.player.computer.FluxEngine;

final class File {

    static final int a = 0;
    static final int b = 1;
    static final int c = 2;
    static final int d = 3;
    static final int e = 4;
    static final int f = 5;
    static final int g = 6;
    static final int h = 7;
    static final int NOFILE = 8;

    static final int[] values = { a, b, c, d, e, f, g, h };

    static final String[] chars = { "a", "b", "c", "d", "e", "f", "g", "h" };

    private File() {
    }

    static boolean isValid(int file) {
        switch (file) {
            case a:
            case b:
            case c:
            case d:
            case e:
            case f:
            case g:
            case h:
                return true;
            case NOFILE:
            default:
                return false;
        }
    }

    public static String toChar(int file) {
        if (!isValid(file))
            throw new IllegalArgumentException();
        return chars[file];
    }

}

