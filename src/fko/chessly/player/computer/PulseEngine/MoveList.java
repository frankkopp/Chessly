/*
 * =============================================================================
 * Pulse
 *
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 *
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


/**
 * This class stores our moves for a specific position. For the root node we
 * will populate pv for every root move.
 */
final class MoveList {

    private static final int MAX_MOVES = 256;

    final Entry[] entries = new Entry[MAX_MOVES];
    int size = 0;

    MoveList() {
        for (int i = 0; i < MAX_MOVES; ++i) {
            entries[i] = new Entry();
        }
    }

    /**
     * Sorts the move list using a stable insertion sort.
     */
    void sort() {
        for (int i = 1; i < size; ++i) {
            Entry entry = entries[i];

            int j = i;
            while ((j > 0) && (entries[j - 1].value < entry.value)) {
                entries[j] = entries[j - 1];
                --j;
            }

            entries[j] = entry;
        }
    }

    /**
     * Rates the moves in the list according to
     * "Most Valuable Victim - Least Valuable Aggressor".
     */
    void rateFromMVVLVA() {
        for (int i = 0; i < size; ++i) {
            int move = entries[i].move;
            int value = 0;

            int pieceTypeValue = PieceType.getValue(Piece.getType(Move
                    .getOriginPiece(move)));
            value += PieceType.KING_VALUE / pieceTypeValue;

            int target = Move.getTargetPiece(move);
            if (Piece.isValid(target)) {
                value += 10 * PieceType.getValue(Piece.getType(target));
            }

            assert value >= (PieceType.KING_VALUE / PieceType.KING_VALUE)
                    && value <= (PieceType.KING_VALUE / PieceType.PAWN_VALUE)
                    + 10 * PieceType.QUEEN_VALUE;

            entries[i].value = value;
        }
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < size; ++i) {
            s += i + ". " + entries[i] + " ";
        }
        return s;
    }

    static final class Entry {
        int move = Move.NOMOVE;
        int value = Value.NOVALUE;
        //final MoveVariation pv = new MoveVariation();

        @Override
        public String toString() {
            return "" + Move.toString(move) + " (" + value + ") ";
        }

    }

    static final class MoveVariation {
        final int[] moves = new int[Depth.MAX_PLY];
        int size = 0;

        @Override
        public String toString() {
            String s = "";
            for(int i=0;i<size;i++) {
                s += Move.toString(moves[i])+" ";
            }
            return s;
        }

    }

}
