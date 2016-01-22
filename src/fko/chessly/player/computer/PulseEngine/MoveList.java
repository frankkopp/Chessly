/*
 * =============================================================================
 * Pulse
 *
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 *
 * =============================================================================
 * Chessly
 *
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
