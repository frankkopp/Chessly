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

/**
 * Simple and fast list class for OmegaMoves which are in fact integer
 * .
 * @author Frank
 */
public class OmegaMoveList extends OmegaIntegerList {


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
    public void add(OmegaIntegerList newList) {
        if (!(newList instanceof OmegaMoveList))
            throw new IllegalArgumentException("not a valid OmegaMoveList: "+newList);
        super.add(newList);
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.Omega.OmegaIntegerList#sort()
     */
    @Override
    public void sort() {
        // TODO - sort Moves
        super.sort();
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.Omega.OmegaIntegerList#toString()
     */
    @Override
    public String toString() {
        String s = "MoveList size="+size()+" available capacity="+(_list.length-size()-_head)+" [";
        for (int i=_head; i<_tail; i++) {
            s += _list[i] + " ("+OmegaMove.toString(_list[i])+")";
            if (i<_tail-1) s += ", ";
        }
        s+="]";
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
}
