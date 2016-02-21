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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This enumeration class represents all squares on a chess board.
 * It uses a numbering for a x88 board so that a1=0 and a2=16
 * It has several convenience methods for calculation in relation
 * to other squares.
 *
 * As enumeration is type safe and also very fast this is preferred over
 * static final int.
 */
@SuppressWarnings("javadoc")
public enum OmegaSquare {

    /*
     0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15 */
    a1, b1, c1, d1, e1, f1, g1, h1, i1, j1, k1, l1, m1, n1, o1, p1, // 0-15
    a2, b2, c2, d2, e2, f2, g2, h2, i2, j2, k2, l2, m2, n2, o2, p2, // 16-31
    a3, b3, c3, d3, e3, f3, g3, h3, i3, j3, k3, l3, m3, n3, o3, p3, // 32-47
    a4, b4, c4, d4, e4, f4, g4, h4, i4, j4, k4, l4, m4, n4, o4, p4, // 48-63
    a5, b5, c5, d5, e5, f5, g5, h5, i5, j5, k5, l5, m5, n5, o5, p5, // 64-79
    a6, b6, c6, d6, e6, f6, g6, h6, i6, j6, k6, l6, m6, n6, o6, p6, // 80-95
    a7, b7, c7, d7, e7, f7, g7, h7, i7, j7, k7, l7, m7, n7, o7, p7, // 96-111
    a8, b8, c8, d8, e8, f8, g8, h8, i8, j8, k8, l8, m8, n8, o8, p8, // 112-127
    INVALID;

    // Move deltas north, south, east, west and combinations
    public static final int N = 16;
    public static final int E = 1;
    public static final int S = -16;
    public static final int W = -1;
    public static final int NE = N + E;
    public static final int SE = S + E;
    public static final int SW = S + W;
    public static final int NW = N + W;

    public static final int[] pawnDirections = {
            N, NE, NW
    };
    public static final int[] knightDirections = {
            N + N + E,
            N + E + E,
            S + E + E,
            S + S + E,
            S + S + W,
            S + W + W,
            N + W + W,
            N + N + W
    };
    public static final int[] bishopDirections = {
            NE, SE, SW, NW
    };
    public static final int[] rookDirections = {
            N, E, S, W
    };
    public static final int[] queenDirections = {
            N, NE, E, SE,
            S, SW, W, NW
    };
    public static final int[] kingDirections = {
            N, NE, E, SE,
            S, SW, W, NW
    };

    public static final OmegaSquare WHITE_CASTLING_KINGSIDE = g1;
    public static final OmegaSquare WHITE_CASTLING_QUEENSIDE = c1;
    public static final OmegaSquare BLACK_CASTLING_KINGSIDE = g8;
    public static final OmegaSquare BLACK_CASTLING_QUEENSIDE = c8;

    // pre-computed if square is valid
    private final boolean _validSquare;

    // pre-filled list with all valid squares
    private static List<OmegaSquare> valueList = null;

    static {
        valueList = Arrays.asList(values()).stream().filter(p -> p.isValidSquare()).collect(Collectors.toList());
    }

    private OmegaSquare() {
        _validSquare = (this.ordinal() & 0x88) == 0;
    }

    /**
     * @param index
     * @return the OmegaSquare for the given index of a 0x88 board - returns INVALID if not a valid index
     */
    public static OmegaSquare getSquare(int index) {
        if ((index & 0x88) != 0) return INVALID;
        return (OmegaSquare.values())[index];
    }

    /**
     * @return true if OmegaSquare is a valid chess square
     */
    public boolean isValidSquare() {
        return _validSquare;
    }

    /**
     * @return OmegaSquare.File for this QmegaSquare
     */
    public File getFile() {
        if (!this._validSquare) return File.NOFILE;
        return File.values() [this.ordinal() % 16];
    }

    /**
     * @return OmegaSquare.Rank for this QmegaSquare
     */
    public Rank getRank() {
        if (!this._validSquare) return Rank.NORANK;
        return Rank.values() [this.ordinal() >>> 4];
    }

    /**
     * Returns a list of all valid squares in the correct order.
     * [0]=a1, [63]=h8
     *
     * @author fkopp
     */
    public static List<OmegaSquare> getValueList() {
        return valueList;
    }


    /**
     * This enum represents all files of a chess board.
     * If used in a loop via values() omit NOFILE.
     */
    public enum File {
        a, b, c, d, e, f, g, h, NOFILE;

        @Override
        public String toString() {
            if (this == NOFILE) return "-";
            return this.name();
        }
    }

    /**
     * This enum represents all ranks of a chess board
     * If used in a loop via values() omit NORANK.
     */
    public enum Rank {
        r1, r2, r3, r4, r5, r6, r7, r8, NORANK;

        @Override
        public String toString() {
            if (this == NORANK) return "-";
            return ""+(this.ordinal()+1);
        }
    }

    public static final EnumSet<OmegaSquare> WHITE_PAWNBASE_ROW = EnumSet.of(a2, b2, c2, d2, e2, f2, g2, h2);
    public static final EnumSet<OmegaSquare> BLACK_PAWNBASE_ROW = EnumSet.of(a7, b7, c7, d7, e7, f7, g7, h7);
    public static final EnumSet<OmegaSquare> WHITE_PROMOTION_ROW = EnumSet.of(a8, b8, c8, d8, e8, f8, g8, h8);
    public static final EnumSet<OmegaSquare> BLACK_PROMOTION_ROW = EnumSet.of(a1, b1, c1, d1, e1, f1, g1, h1);

    public boolean isWhitePawnBaseRow() {
        return WHITE_PAWNBASE_ROW.contains(this);
    }

    public boolean isBlackPawnBaseRow() {
        return BLACK_PAWNBASE_ROW.contains(this);
    }

    public boolean isPawnBaseRow(OmegaColor c) {
        switch (c) {
            case WHITE: return isWhitePawnBaseRow();
            case BLACK: return isBlackPawnBaseRow();
            default:
                throw new RuntimeException("Invalid Color");
        }
    }

}
