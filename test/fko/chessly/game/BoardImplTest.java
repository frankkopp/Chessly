package fko.chessly.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author fkopp
 *
 */
public class BoardImplTest {

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
    @SuppressWarnings("static-method")
    protected void print(GameBoard b, GameMove m) {
        System.out.println("Move: "+m.toString());
        System.out.println();
        System.out.println(b.toString());
    }

}
