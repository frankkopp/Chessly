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
final class MoveHistory {

    private static final int MAX_MOVES = 512;

    private final Entry[] entries = new Entry[MAX_MOVES];

    private int currentEntry = 0;

    // stores everything we need to create a chass game notation
    // and should be sufficient to to use when replaying from
    // initial board
    static final class Entry {
        int move = Move.NOMOVE;
        int value = Value.NOVALUE;
    }

    MoveHistory() {
        for (int i = 0; i < MAX_MOVES; ++i) {
            entries[i] = new Entry();
        }
    }

    void addMove(int move) {
        entries[currentEntry++].move = move;
    }

    int removeMove() {
        int move = entries[--currentEntry].move;
        entries[currentEntry].move = Move.NOMOVE;
        entries[currentEntry].value = Value.NOVALUE;
        return move;

    }

    int lastMove() {
        if (currentEntry == 0)
            return Move.NOMOVE;
        return entries[currentEntry - 1].move;
    }

    @Override
    protected MoveHistory clone() {
        MoveHistory clone = new MoveHistory();
        clone.currentEntry = currentEntry;
        for (int i = 0; i <= currentEntry; ++i) {
            clone.entries[i].move = entries[i].move;
            clone.entries[i].value = entries[i].value;
        }
        return clone;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < currentEntry; ++i) {
            s += i + ". " + entries[i] + " ";
        }
        return s;
    }

}
