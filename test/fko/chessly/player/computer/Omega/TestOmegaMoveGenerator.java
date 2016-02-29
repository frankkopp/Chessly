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

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author Frank
 *
 */
public class TestOmegaMoveGenerator {

    /**
     * Tests the generated moves from a standard board setup
     */
    @Test
    public void testFromStandardBoard() {

        String testFen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113";
        //String testFen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq - 0 113";

        OmegaBoardPosition board = new OmegaBoardPosition(testFen);
        OmegaMoveGenerator moveGenerator = new OmegaMoveGenerator();
        OmegaMoveList moves = moveGenerator.getPseudoLegalMoves(board, false);

        System.out.println(moves);

    }

    /**
     * Tests the timing
     */
    @Test
    public void testTiming() {

        int ITERATIONS = 100000;

        String testFen = "r3k2r/1ppn3p/2q1q1n1/4P3/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113";
        //String testFen = "r3k2r/1ppn3p/2q1q1n1/4P3/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq - 0 113";

        OmegaBoardPosition board = new OmegaBoardPosition(testFen);
        OmegaMoveGenerator moveGenerator = new OmegaMoveGenerator();

        OmegaMoveList moves = null;
        Instant start = Instant.now();
        for (int i=0;i<ITERATIONS;i++) {
            moves = moveGenerator.getPseudoLegalMoves(board, false);
        }
        Instant end = Instant.now();
        System.out.println(Duration.between(start, end));

        System.out.println(board);
        System.out.println(moves);

    }

}
