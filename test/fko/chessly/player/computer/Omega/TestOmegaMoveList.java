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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Frank
 *
 */
public class TestOmegaMoveList {

    /**
     *
     */
    @Test
    public void testList() {
        OmegaMoveList list = new OmegaMoveList();

        // empty list
        testEmptyList(list);

        // add one entry
        int value = 100;
        list.add(value);
        assertTrue(list.size()==1);
        assertFalse(list.empty());
        assertTrue(list.get(0)==value);
        assertTrue(list.getFirst()==value);
        assertTrue(list.getLast()==value);

        // remove one entry
        int element = list.removeLast();
        assertTrue(element==value);
        testEmptyList(list);
        list.add(value);
        element = list.removeFirst();
        assertTrue(element==value);
        testEmptyList(list);

        // add 10 entries
        for (int i=100; i<110; i++) {
            list.add(i);
            assertTrue(list.size()==i-100+1);
            assertFalse(list.empty());
        }

        // get one entry
        assertTrue(list.get(4)==104);

        // remove one entry
        element = list.removeLast();
        assertTrue(element==109);
        assertTrue(list.getLast()==108);
        element = list.removeFirst();
        assertTrue(element==100);
        assertTrue(list.getFirst()==101);
        assertTrue(list.size()==8);

        // get one entry
        assertTrue(list.get(4)==105);

        // get entry higher than size
        try {
            list.get(11);
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }

        // get entry lower zero
        try {
            list.get(-1);
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }

        // add list to list
        OmegaMoveList list2 = new OmegaMoveList();
        // add 10 entries
        for (int i=200; i<210; i++) {
            list2.add(i);
        }
        assertTrue(list2.size()==10);

        // remove elements from list2
        element = list2.removeLast();
        assertTrue(element==209);
        assertTrue(list2.getLast()==208);
        element = list2.removeFirst();
        assertTrue(element==200);
        assertTrue(list2.getFirst()==201);
        assertTrue(list2.size()==8);

        // concatenate lists
        list.add(list2);
        assertTrue(list.size()==16);

        // add many elements
        list2 = new OmegaMoveList();
        // add 10 entries
        for (int i=200; i<456; i++) {
            list2.add(i);
        }
        assertTrue(list2.size()==256);

        // add one too many
        try {
            list2.add(999);
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore
        }
        assertTrue(list2.size()==256);

        // add too many elements
        try {
            list.add(list2);
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore
        }

        // clear
        list.clear();
        assertTrue(list.size()==0 && list.empty());
        list2.clear();
        assertTrue(list2.size()==0 && list2.empty());

    }

    /**
     * @param list
     */
    private static void testEmptyList(OmegaMoveList list) {
        // list is empty
        assertTrue(list.size()==0);
        assertTrue(list.empty());

        // remove from empty list
        try {
            list.removeFirst();
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        // remove from empty list
        try {
            list.removeLast();
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }

        // retrieve from empty list
        try {
            list.get(0);
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        try {
            list.getFirst();
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        try {
            list.getLast();
            fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }

}
