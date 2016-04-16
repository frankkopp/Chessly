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

import java.time.Duration;
import java.time.Instant;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fko.chessly.game.NotationHelper;

/**
 * Attempt at a proper Unit Test for Evaluation
 */
public class TestOmegaEvaluation {


    private String _fenStandard;
    private OmegaBoardPosition _omegaPosition;
    private OmegaMoveGenerator _omg;
    private OmegaEvaluation _evaluation;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        _fenStandard = NotationHelper.StandardBoardFEN;
        _omegaPosition = new OmegaBoardPosition(_fenStandard);
        _omg = new OmegaMoveGenerator();
        _evaluation = new OmegaEvaluation(new OmegaEngine(), _omg);
    }

    /**
     * Test method for {@link fko.chessly.player.computer.Omega.OmegaEvaluation#evaluate(fko.chessly.player.computer.Omega.OmegaBoardPosition, int)}.
     */
    @Test
    public final void testEvaluate_startPosValueZero() {
        // standard position should be 0
        // change if next player gets a bonus
        int value = _evaluation.evaluate(_omegaPosition, ply);
        assertEquals("Start Position should be 0", 0, value);
    }

    /**
     * Test method for {@link fko.chessly.player.computer.Omega.OmegaEvaluation#evaluate(fko.chessly.player.computer.Omega.OmegaBoardPosition, int)}.
     */
    @Test
    public void testEvaluate_mirroredPositionEqual() {
        // Mirrored position - should be equal
        String fen = "k6n/7p/6P1/7K/8/8/8/8 w - - 0 1"; // white
        _omegaPosition = new OmegaBoardPosition(fen);
        int value1 = _evaluation.evaluate(_omegaPosition, ply);
        fen = "8/8/8/8/k7/1p6/P7/N6K b - - 0 1"; // black
        _omegaPosition = new OmegaBoardPosition(fen);
        int value2 = _evaluation.evaluate(_omegaPosition, ply);
        assertEquals("Mirrored Position should be equal", value1,value2);
    }

    /**
     * Test method for {@link fko.chessly.player.computer.Omega.OmegaEvaluation#material(fko.chessly.player.computer.Omega.OmegaBoardPosition)}.
     */
    @Test
    public final void testMaterial_OfStartPosition() {
        _omegaPosition = new OmegaBoardPosition(_fenStandard);
        int value = _evaluation.material(_omegaPosition); // 1 == white
        //System.out.println(value);
        assertEquals(0, value);
    }

    /**
     * Test method for {@link fko.chessly.player.computer.Omega.OmegaEvaluation#material(fko.chessly.player.computer.Omega.OmegaBoardPosition)}.
     */
    @Test
    public final void testMaterial_OfDifferentPositions() {
        String fen = "k6n/7p/6P1/7K/8/8/8/8 w - - 0 1"; // white
        _omegaPosition = new OmegaBoardPosition(fen);
        int value = _evaluation.material(_omegaPosition);
        // System.out.println(value);
        assertEquals(-320, value);

        fen = "8/8/8/8/k7/1p6/P7/N6K b - - 0 1"; // black
        _omegaPosition = new OmegaBoardPosition(fen);
        value = _evaluation.material(_omegaPosition);
        // System.out.println(value);
        assertEquals(-320, value);
    }

    /**
     * Test method for {@link fko.chessly.player.computer.Omega.OmegaEvaluation#mobility(fko.chessly.player.computer.Omega.OmegaBoardPosition, int)}.
     */
    @Test
    public final void testMobility_StartPosition() {
        _omegaPosition = new OmegaBoardPosition(_fenStandard);
        int value = _evaluation.mobility(_omegaPosition);
        //System.out.println(value);
        assertEquals(0, value);
    }

    /**
     * Test method for {@link fko.chessly.player.computer.Omega.OmegaEvaluation#mobility(fko.chessly.player.computer.Omega.OmegaBoardPosition, int)}.
     */
    @Test
    public final void testMobility_otherPositions() {
        String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113";
        _omegaPosition = new OmegaBoardPosition(fen);
        int value = _evaluation.mobility(_omegaPosition);
        //System.out.println(value);
        assertEquals(52, value);

        fen = "k6n/7p/6P1/7K/8/8/8/8 w - - 0 1"; // white
        _omegaPosition = new OmegaBoardPosition(fen);
        value = _evaluation.mobility(_omegaPosition);
        //System.out.println(value);
        assertEquals(-4, value);

        fen = "8/8/8/8/k7/1p6/P7/N6K b - - 0 1"; // black
        _omegaPosition = new OmegaBoardPosition(fen);
        value = _evaluation.mobility(_omegaPosition);
        //System.out.println(value);
        assertEquals(-4, value);
    }

    @Test
    public void testTiming() {

        int ROUNDS = 5;
        int DURATION = 2;

        int ITERATIONS = 0;

        Instant start;

        System.out.println("Running Timing Test");

        String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113";
        _omegaPosition = new OmegaBoardPosition(fen);

        for (int j=0; j<ROUNDS ;j++) {
            System.gc();
            start = Instant.now();
            ITERATIONS=0;
            while (true) {
                ITERATIONS++;
                // ### TEST CODE
                testCode();
                // ### /TEST CODE
                if (Duration.between(start,Instant.now()).getSeconds() >= DURATION) break;
            }
            System.out.println(String.format("Timing: %,7d runs/s", ITERATIONS/DURATION));

        }
    }

    private void testCode() {
        _evaluation.evaluate(_omegaPosition, ply);
    }
}
