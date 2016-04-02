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
 * @author Frank
 *
 */
public class OmegaSquareList {

    final OmegaSquare[] list = new OmegaSquare[64];
    int size = 0;

    /**
     * Adds the given square to the beginning of the list
     * @param square
     */
    public void add(OmegaSquare square) {
        list[size++] = square;
    }

    /**
     * Remove the given element from the list.
     * Does nothing if element is not in the list.
     * @param square
     */
    public void remove(OmegaSquare square) {
        /*
         * We need to go over the whole array ones as the element can
         * be in the list several times.
         * If we find the element we check the last element and if this is
         * not the element we exchange the elements and reduce the size by one.
         * If the last is also the element, we reduce the size and try again.
         *
         */
        for (int i=0; i<size; i++) {
            // found the element
            if (list[i] == square) {
                // is last also element then remove it be reducing size by 1
                while (list[size-1] == square) {
                    size--;
                    if (size<=i) return; // list empty
                }
                // now exchange with last
                list[i] = list[size-1];
                size--;
                if (size<=i) return; // list empty
            }
        }

    }

    @Override
    public String toString() {
        String s = "["+size+"] ";
        for (int i=0; i<size; i++) {
            s += list[i] + " ";
        }
        return s;
    }


}
