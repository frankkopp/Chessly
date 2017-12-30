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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Frank
 *
 */
public class TestOmegaEvaluationCache {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     *
     */
    @Test
    public final void test_Cache() {
        OmegaEvaluationCache cache = new OmegaEvaluationCache(32);
        assertEquals(1198372, cache.getMaxEntries());
        assertEquals(32, cache.getSize());
        cache.put(123412341234L, 999);
        assertEquals(1, cache.getNumberOfEntries());
        assertEquals(999,cache.get(123412341234L));
        assertEquals(Integer.MIN_VALUE,cache.get(1234L));
        cache.put(123412341234L, 1111);
        assertEquals(1111,cache.get(123412341234L));
        assertEquals(1, cache.getNumberOfEntries());
        cache.clear();
        assertEquals(0, cache.getNumberOfEntries());
    }

    /**
     *
     */
    @Test
    public void testSize() {
        System.out.println("Testing Transposition Table size:");
        int[] megabytes = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512};
        for (int i : megabytes) {
            System.gc();
            long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            OmegaEvaluationCache oec = new OmegaEvaluationCache(i);
            System.gc();
            long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long hashAllocation = (usedMemoryAfter - usedMemoryBefore) / (1024 * 1024);
            System.out.format("TT Size (config): %dMB = %dMB real size - Nodes: %d%n", i, hashAllocation, oec.getMaxEntries());
            oec = null;
        }
    }



}
