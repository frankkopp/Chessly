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

import fko.chessly.player.computer.Omega.OmegaMoveValueList.Entry;
import fko.chessly.util.SimpleIntList;

/**
 * Simple and fast list class for OmegaMoves which are in fact integers.
 *
 * @author Frank
 */
public class OmegaMoveList extends SimpleIntList {

    /**
     * Creates a list with a maximum of <tt>MAX_ENTRIES</tt> elements
     */
    public OmegaMoveList() {
        super();
    }

    /**
     * Creates a list with a maximum of max_site elements
     * @param max
     */
    public OmegaMoveList(int max) {
        super(max);
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.Omega.OmegaIntegerList#add(int)
     */
    @Override
    public void add(int move) {
        if (!OmegaMove.isValid(move))
            throw new IllegalArgumentException("not a valid move: "+move);
        super.add(move);
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.Omega.OmegaIntegerList#add(fko.chessly.player.computer.Omega.OmegaIntegerList)
     */
    @Override
    public void add(SimpleIntList newList) {
        if (!(newList instanceof OmegaMoveList))
            throw new IllegalArgumentException("not a valid OmegaMoveList: "+newList);
        super.add(newList);
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.Omega.OmegaIntegerList#toString()
     */
    @Override
    public String toString() {
        String s = "MoveList size="+size()+" available capacity="+getAvailableCapacity()+" [";
        for (int i=0; i<size(); i++) {
            s += get(i) + " ("+OmegaMove.toString(get(i))+")";
            if (i<size()-1) s += ", ";
        }
        s+="]";
        return s;
    }

    /**
     * Print the list as a string of move simple move notations.<br/>
     * e2-e4 e7-e5 ....
     * @return string containing the moves of the list
     */
    public String toNotationString() {
        String s = "";
        for (int i=0; i<size(); i++) {
            s += OmegaMove.toSimpleString(get(i))+" ";
        }
        return s;
    }

    /**
     * clones the list
     */
    @Override
    public OmegaMoveList clone() {
        OmegaMoveList n = new OmegaMoveList();
        n.add(this);
        return n;
    }

    /**
     * Copies the content of src array into dest array at index 1
     * and sets index 0 of dest array to the specified move.
     * @param move
     * @param value
     * @param src
     * @param dest
     */
    static void savePV(int move, OmegaMoveList src, OmegaMoveList dest) {
        dest._list[dest._head] = move;
        System.arraycopy(src._list, src._head, dest._list, 1, src.size());
        dest._tail = src.size() + 1;
    }
}
