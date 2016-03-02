/**
 *
 */
package fko.chessly.game;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author fkopp
 *
 */
public class Chessly_PERFT_Test {

    static long[][] results = {
            //N  Nodes  	Captures EP	Checks	Mates
            { 0, 1, 	0,	 0, 	0, 	0},
            { 1, 20, 	0,	 0,	0,	0},
            { 2, 400, 	0,	 0,	0, 	0},
            { 3, 8902, 	34,	 0,	12, 	0},
            { 4, 197281, 	1576,	 0,	469, 	8},
            { 5, 4865609, 	82719,	 258,	27351, 	347},
            { 6, 119060324,	2812008, 5248,	809099,	10828}
    };

    @Test
    public void test() {

        int maxDepth = 5;

        Chessly_PERFT perftTest = new Chessly_PERFT();

        for (int i=1;i<=maxDepth;i++) {
            perftTest.testSingleThreaded(i);

            assertTrue(perftTest.get_nodes() == results[i][1]);
            assertTrue(perftTest.get_captureCounter() == results[i][2]);
            assertTrue(perftTest.get_enpassantCounter() == results[i][3]);
            assertTrue(perftTest.get_checkCounter() == results[i][4]);
            assertTrue(perftTest.get_checkMateCounter() == results[i][5]);

        }



    }
}
