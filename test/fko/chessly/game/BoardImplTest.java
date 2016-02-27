package fko.chessly.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fko.chessly.game.pieces.Pawn;
import fko.chessly.player.computer.Omega.OmegaBoardPosition;

/**
 * @author fkopp
 *
 */
public class BoardImplTest {

    /**
     * Test Setup from FEN
     */
    @Test
    public void testSetupFromFEN() {
        /*
            ---------------------------------
         8: |bR |   |   |   |bK |   |   |bR |
            ---------------------------------
         7: |   |bP |bP |bN |   |   |   |bP |
            ---------------------------------
         6: |   |   |bQ |   |bQ |   |bN |   |
            ---------------------------------
         5: |   |   |   |   |   |   |   |   |
            ---------------------------------
         4: |   |   |bQ |   |wP |bP |   |   |
            ---------------------------------
         3: |   |   |   |   |   |   |wR |   |
            ---------------------------------
         2: |bP |   |bP |   |   |wP |wP |wP |
            ---------------------------------
         1: |   |wR |   |   |   |   |wK |   |
            ---------------------------------
              A   B   C   D   E   F   G   H

         black, ep on e4, O-O & O-O-O for black
         */
        String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113";

        GameBoard gb = new GameBoardImpl(fen);
        System.out.println(fen);
        System.out.println(gb.toFENString());
        assertTrue (gb.toFENString().equals(fen));

        GameBoard gb2 = new GameBoardImpl(gb);
        System.out.println(gb.toFENString());
        System.out.println(gb2.toFENString());
        assertTrue(gb.equals(gb2));

        gb.makeMove(new GameMoveImpl(
                GamePosition.getGamePosition(2, 7),
                GamePosition.getGamePosition(2, 6),
                Pawn.create(GameColor.BLACK)
                ));
        System.out.println(gb.toFENString());
        assertTrue(gb.toFENString().equals("r3k2r/2pn3p/1pq1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq - 0 114"));
        gb2 = new GameBoardImpl(gb);
        System.out.println(gb2.toFENString());
        assertTrue(gb.equals(gb2));
        assertTrue(gb.hasSamePosition(gb2));

        gb.makeMove(new GameMoveImpl(
                GamePosition.getGamePosition(7, 3),
                GamePosition.getGamePosition(7, 5),
                Pawn.create(GameColor.WHITE)
                ));
        System.out.println(gb.toFENString());
        assertTrue(gb.toFENString().equals("r3k2r/2pn3p/1pq1q1n1/6R1/2q1Pp2/8/p1p2PPP/1R4K1 b kq - 1 114"));
        gb2 = new GameBoardImpl(gb);
        System.out.println(gb2.toFENString());
        assertTrue(gb.equals(gb2));
        assertTrue(gb.hasSamePosition(gb2));

        gb.makeMove(new GameMoveImpl(
                GamePosition.getGamePosition(8, 7),
                GamePosition.getGamePosition(8, 5),
                Pawn.create(GameColor.BLACK)
                ));
        System.out.println(gb.toFENString());
        assertTrue(gb.toFENString().equals("r3k2r/2pn4/1pq1q1n1/6Rp/2q1Pp2/8/p1p2PPP/1R4K1 w kq h6 0 115"));
        gb2 = new GameBoardImpl(gb);
        System.out.println(gb2.toFENString());
        assertTrue(gb.equals(gb2));
        assertTrue(gb.hasSamePosition(gb2));
    }

    /**
     *
     */
    @Test
    public void test() {

        System.out.println("Test BoardImpl");

        GameBoard b = new GameBoardImpl();

        //System.out.println(b.toString());

        /**
		for (int c=1;c<=8;c++) {
			for (int r=1;r<=8;r++) {
				Field f = b.getField(c,r);
				System.out.println("Field: "+f.toNotationString()+" / "+f.toString());
			}
		}
         */

        GameMove m;
        // e2-e4
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "e2-e4");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // e7-e5
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "e7-e5");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Ke1-e2
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "e1-e2");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Ng8-f6
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "g8-f6");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Ke2-d3
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "e2-d3");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Bf8-c5
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "f8-c5");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Kd3-c4
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "d3-c4");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // o-o
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "e8-g8");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Ng1-f3
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "g1-f3");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // d7-d5
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "d7-d5");
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // d2-d4
        try {
            m = NotationHelper.createNewMoveFromSimpleNotation(b, "d2-d4");
        } catch (InvalidMoveException e) {
            e.printStackTrace();
            assertFalse(b.isLegalMove(m));
        }
        //makeMove(b, m);

        //System.out.println(b.getMoveHistory().toString());
        System.out.println("Test BoardImpl DONE");
        System.out.println();
        System.out.flush();
    }

    /**
     *
     */
    @Test
    public void testBoardEquals() {
        System.out.println("Test Equal");

        GameMove m;
        GameBoard b = new GameBoardImpl();
        GameBoard c = new GameBoardImpl();

        // same hash code?
        assertTrue(b.hashCode()==c.hashCode());
        // same position?
        assertTrue(b.hasSamePosition(c));
        // boards equal?
        assertEquals(b, c);

        // -- make same move and test
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "e2-e4");
        makeMove(b, m);
        assertNotEquals(b, c);
        m = NotationHelper.createNewMoveFromSimpleNotation(c, "e2-e4");
        makeMove(c, m);
        // same hash code?
        assertTrue(b.hashCode()==c.hashCode());
        // same position?
        assertTrue(b.hasSamePosition(c));
        // boards equal?
        assertEquals(b, c);

        // -- make same 2nd move and test
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "e7-e5");
        makeMove(b, m);
        //System.out.println(b.toString());
        assertNotEquals(b, c);

        m = NotationHelper.createNewMoveFromSimpleNotation(c, "e7-e5");
        makeMove(c, m);
        //System.out.println(c.toString());
        assertEquals(b, c);

        // -- make different moves to same position and test
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "d2-d3");
        makeMove(b, m);
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "d7-d6");
        makeMove(b, m);
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "d3-d4");
        makeMove(b, m);
        m = NotationHelper.createNewMoveFromSimpleNotation(b, "d6-d5");
        makeMove(b, m);
        //System.out.println(b.getMoveHistory().toString());

        m = NotationHelper.createNewMoveFromSimpleNotation(c, "d2-d4");
        makeMove(c, m);
        m = NotationHelper.createNewMoveFromSimpleNotation(c, "d7-d5");
        makeMove(c, m);
        //System.out.println(c.getMoveHistory().toString());

        assertNotEquals(b, c);
        // same position?
        assertFalse(b.hasSamePosition(c)); // because of en passant field

        // copy constructor BoardImpl
        GameBoard b_copy = new GameBoardImpl((GameBoardImpl)b);
        //System.out.println(b.toString());
        //System.out.println(b.getMoveHistory().toString());
        //System.out.println(b_copy.toString());
        //System.out.println(b_copy.getMoveHistory().toString());
        // same hash code?
        assertTrue(b.hashCode()==b_copy.hashCode());
        // same position?
        assertTrue(b.hasSamePosition(b_copy));
        // boards equal?
        assertEquals(b, b_copy);

        // copy constructor Board
        GameBoard b_copy2 = new GameBoardImpl(b);
        //System.out.println(b.toString());
        //System.out.println(b.getMoveHistory().toString());
        //System.out.println(b_copy2.toString());
        //System.out.println(b_copy2.getMoveHistory().toString());
        // same hash code?
        assertTrue(b.hashCode()==b_copy2.hashCode());
        // same position?
        assertTrue(b.hasSamePosition(b_copy2));
        // boards equal?
        assertEquals(b, b_copy2);

        System.out.println("Test Equal DONE");
        System.out.println();
        System.out.flush();
    }

    /**
     * @param b
     * @param m
     */
    protected void makeMove(GameBoard b, GameMove m) {
        try {
            b.makeMove(m);
        } catch (IllegalMoveException e) {
            //e.printStackTrace();
        }
        print(b, m);
    }

    /**
     * @param b
     * @param m
     */
    protected void print(GameBoard b, GameMove m) {
        System.out.println("Move: "+m.toString());
        System.out.println();
        System.out.println(b.toString());
    }

}
