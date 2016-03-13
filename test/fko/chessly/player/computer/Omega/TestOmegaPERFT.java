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

import org.junit.Test;

/**
 * @author Frank
 *
 */
public class TestOmegaPERFT {

    static long[][] results = {
            //N  Nodes      Captures EP     Checks  Mates
            { 0, 1,         0,       0,     0,      0},
            { 1, 20,        0,       0,     0,      0},
            { 2, 400,       0,       0,     0,      0},
            { 3, 8902,      34,      0,     12,     0},
            { 4, 197281,    1576,    0,     469,    8},
            { 5, 4865609,   82719,   258,   27351,  347},
            { 6, 119060324, 2812008, 5248,  809099, 10828}
    };

    /**
     * Perft Test
     */
    @Test
    public void testPerft() {

        int maxDepth = 4;

        OmegaPERFT perftTest = new OmegaPERFT();

        for (int i=1;i<=maxDepth;i++) {
            perftTest.testPerft(i);

            //assertTrue(perftTest.get_nodes() == results[i][1]);
            //            assertTrue(perftTest.get_captureCounter() == results[i][2]);
            //            assertTrue(perftTest.get_enpassantCounter() == results[i][3]);
            //            assertTrue(perftTest.get_checkCounter() == results[i][4]);
            //            assertTrue(perftTest.get_checkMateCounter() == results[i][5]);

        }

    }

}
