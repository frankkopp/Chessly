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
public class TestOmegaPERFT {

    /**
     * Perft Test
     */
    @Test
    public void testStandardPerft() {

        System.out.println("Standard PERFT Test");
        System.out.println("==============================");

        long[][] results = {
                //N  Nodes      Captures EP     Checks  Mates
                { 0, 1,         0,       0,     0,      0},
                { 1, 20,        0,       0,     0,      0},
                { 2, 400,       0,       0,     0,      0},
                { 3, 8902,      34,      0,     12,     0},
                { 4, 197281,    1576,    0,     469,    8},
                { 5, 4865609,   82719,   258,   27351,  347},
                { 6, 119060324, 2812008, 5248,  809099, 10828},
        };

        int maxDepth = 5;

        OmegaPERFT perftTest = new OmegaPERFT();

        for (int i=1;i<=maxDepth;i++) {
            perftTest.testPerft(i);

            assertTrue(perftTest.get_nodes() == results[i][1]);
            assertTrue(perftTest.get_captureCounter() == results[i][2]);
            assertTrue(perftTest.get_enpassantCounter() == results[i][3]);
            assertTrue(perftTest.get_checkCounter() == results[i][4]);
            assertTrue(perftTest.get_checkMateCounter() == results[i][5]);
        }
        System.out.println("==============================");
    }

    /**
     * Perft Test
     */
    @Test
    public void testKiwipetePerft() {

        System.out.println("Kiwipete PERFT Test");
        System.out.println("==============================");

        long[][] results = {
                //N  Nodes      Captures EP     Checks  Mates
                { 0, 0,         0,       0,     0,      0},
                { 1, 48,        8,       0,     0,      0},
                { 2, 2039,      351,     1,     3,      0},
                { 3, 97862,     17102,   45,    993,    1},
                { 4, 4085603,   757163,  1929,  25523,  43},
                { 5, 193690690, 35043416,73365, 3309887,30171},
        };

        int maxDepth = 5;

        OmegaPERFT perftTest = new OmegaPERFT("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");

        for (int i=1;i<=maxDepth;i++) {
            perftTest.testPerft(i);

            assertTrue(perftTest.get_nodes() == results[i][1]);
            assertTrue(perftTest.get_captureCounter() == results[i][2]);
            assertTrue(perftTest.get_enpassantCounter() == results[i][3]);
            assertTrue(perftTest.get_checkCounter() == results[i][4]);
            assertTrue(perftTest.get_checkMateCounter() == results[i][5]);
        }
        System.out.println("==============================");
    }

    /**
     * Perft Test
     */
    @Test
    public void testPos5Perft() {

        System.out.println("Pos5 PERFT Test");
        System.out.println("==============================");

        long[][] results = {
                //N  Nodes      Captures EP     Checks  Mates
                { 0, 0,         0,       0,     0,      0},
                { 1, 44,        0,       0,     0,      0},
                { 2, 1486,      0,       0,     0,      0},
                { 3, 62379,     0,       0,     0,      0},
                { 4, 2103487,   0,       0,     0,      0},
                { 5, 89941194,  0,       0,     0,      0}
        };

        int maxDepth = 3;

        OmegaPERFT perftTest = new OmegaPERFT("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");

        for (int i=1;i<=maxDepth;i++) {
            perftTest.testPerft(i);

            assertTrue(perftTest.get_nodes() == results[i][1]);
            //            assertTrue(perftTest.get_captureCounter() == results[i][2]);
            //            assertTrue(perftTest.get_enpassantCounter() == results[i][3]);
            //            assertTrue(perftTest.get_checkCounter() == results[i][4]);
            //            assertTrue(perftTest.get_checkMateCounter() == results[i][5]);
        }

        System.out.println("==============================");
    }

}
