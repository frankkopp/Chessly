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

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * List of OmegaMove with additional information such as value.<br/>
 * Moves and Value are encapsulated in a class Entry.</br>
 * </br>
 * List with NOMOVE entries created when this class is instantiated.
 * This saves time during usage of this list.
 *
 */
public class OmegaMoveValueList  {

    private static final int MAX_MOVES = 256;

    private final Entry[] entries = new Entry[MAX_MOVES];
    private int size = 0;

    /**
     * Creates and initializes list with NOMOVE entries
     */
    public OmegaMoveValueList() {
        // prepare principal variation lists
        IntStream.rangeClosed(0, MAX_MOVES-1)
        .forEach((i) -> entries[i]= new Entry());
    }

    /**
     * @param move
     * @param value
     */
    public void add(int move, int value) {
        set(size++, move, value);
    }

    /**
     * @param i
     * @param move
     * @param value
     */
    public void set(int i, int move, int value) {
        if (i>=size) throw new ArrayIndexOutOfBoundsException();
        entries[i].move = move;
        entries[i].value = value;
    }

    /**
     * @param i
     * @return move
     */
    public int getMove(int i) {
        if (i>=size) throw new ArrayIndexOutOfBoundsException();
        return entries[i].move;
    }

    /**
     * @param i
     * @return value
     */
    public int getValue(int i) {
        if (i>=size) throw new ArrayIndexOutOfBoundsException();
        return entries[i].value;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return this.size;
    }

    /**
     * @return true is size == 0
     */
    public boolean isEmpty() {
        return size==0;
    }

    /**
     * Resets the list
     */
    public void clear() {
        IntStream.rangeClosed(0, MAX_MOVES-1)
        .forEach((i) -> entries[i]= new Entry());
        size=0;
    }

    static class Entry {
        int move = OmegaMove.NOMOVE;
        int value = OmegaEvaluation.Value.NOVALUE;

        @Override
        public String toString() {
            return "" + OmegaMove.toString(move) + " (" + value + ") ";
        }
    }

    /**
     * Copies the content of src array into dest array at index 1
     * and sets index 0 of dest array to the specified move.
     * @param move
     * @param value
     * @param src
     * @param dest
     */
    static void savePV(int move, int value, OmegaMoveValueList src, OmegaMoveValueList dest) {
        dest.entries[0].move = move;
        dest.entries[0].value = value;
        System.arraycopy(src.entries, 0, dest.entries, 1, src.size);
        dest.size = src.size + 1;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder s= new StringBuilder();
        IntStream.rangeClosed(0, size-1)
        .forEach((i) -> {
            s.append(OmegaMove.toSimpleString(entries[i].move));
            s.append(" (");
            s.append(entries[i].value);
            s.append(") ");
        });
        return s.toString();
    }



}
