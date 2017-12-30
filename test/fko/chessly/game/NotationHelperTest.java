package fko.chessly.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import fko.chessly.game.pieces.Pawn;

/**
 * @author fkopp
 *
 */
public class NotationHelperTest {
    
    @Test
    public void testMoveFromSIMPLENotation() {
	
	System.out.println();
	System.out.println("Test Move from SIMPLE Notation");

	String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e4 0 1";
	
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
	
	GameBoard board = new GameBoardImpl(fen);
	
	// Castle
	GameMove m01 = NotationHelper.createNewMoveFromSimpleNotation(board, "e8g8");
	assertTrue(m01 != null);
	GameMove m02 = NotationHelper.createNewMoveFromSimpleNotation(board, "e8-g8");
	assertTrue(m02 != null);
	GameMove m04 = NotationHelper.createNewMoveFromSimpleNotation(board, "e8c8");
	assertTrue(m04 != null);
	
	// piece
	GameMove m1 = NotationHelper.createNewMoveFromSimpleNotation(board, "d7b6");
	assertTrue(m1 != null);
	@SuppressWarnings("unused")
	GameMove m2;
	try {
	    m2 = NotationHelper.createNewMoveFromSimpleNotation(board, "h8h7");
	    fail("not a valid move in this position");
	} catch (InvalidMoveException e) {
	    assertNotNull(e);
	}
	GameMove m3 = NotationHelper.createNewMoveFromSimpleNotation(board, "e6e5");
	assertTrue(m3 != null); 
	GameMove m4 = NotationHelper.createNewMoveFromSimpleNotation(board, "e6d6");
	assertTrue(m4 != null); 
	GameMove m5 = NotationHelper.createNewMoveFromSimpleNotation(board, "c6d6");
	assertTrue(m5 != null); 
	GameMove m6 = NotationHelper.createNewMoveFromSimpleNotation(board, "c4c5");
	assertTrue(m6 != null); 
	GameMove m7 = NotationHelper.createNewMoveFromSimpleNotation(board, "c6e4");
	assertTrue(m7 != null); 

	// pawn
	GameMove m11 = NotationHelper.createNewMoveFromSimpleNotation(board, "b7b6");
	assertTrue(m11 != null); 
	GameMove m12 = NotationHelper.createNewMoveFromSimpleNotation(board, "b7b5");
	assertTrue(m12 != null); 
	GameMove m13 = NotationHelper.createNewMoveFromSimpleNotation(board, "a2-a1");
	assertTrue(m13 != null);
	@SuppressWarnings("unused")
	GameMove m14;
	try {
	    m14 = NotationHelper.createNewMoveFromSimpleNotation(board, "h4");
	    fail("Not whites turn");
	} catch (InvalidMoveException e) {
	    assertNotNull(e);
	}
	
	// en passant
	GameMove m16 = NotationHelper.createNewMoveFromSimpleNotation(board, "f4e3");
	assertTrue(m16 != null);
	GameMove m17 = NotationHelper.createNewMoveFromSimpleNotation(board, "f4e3");
	assertTrue(m17 != null);
	
	// pawn promotion
	GameMove m21 = NotationHelper.createNewMoveFromSimpleNotation(board, "a2a1Q");
	assertTrue(m21 != null);
	GameMove m23 = NotationHelper.createNewMoveFromSimpleNotation(board, "a2b1");
	assertTrue(m23 != null);
	GameMove m24 = NotationHelper.createNewMoveFromSimpleNotation(board, "a2b1Q");
	assertTrue(m24 != null);
	GameMove m26 = NotationHelper.createNewMoveFromSimpleNotation(board, "a2b1R");
	assertTrue(m26 != null);
	
    }

    /**
     * <SAN move descriptor piece moves> ::= <Piece symbol>[<from file>|<from rank>|<from square>]['x']<to square> 
     * <SAN move descriptor pawn captures> ::= <from file>[<from rank>] 'x' <to square>[<promoted to>] 
     * <SAN move descriptor pawn push> ::= <to square>[<promoted to>]
     * 
     * Ambiguities 
     * If the piece is sufficient to unambiguously determine the
     * origin square, the whole from square is omitted. Otherwise, if two
     * (or more) pieces of the same kind can move to the same square, the
     * piece's initial is followed by (in descending order of preference) 
     * 1. file of departure if different 
     * 2. rank of departure if the files are the same but the ranks differ 
     * 3. the complete origin square
     * coordinate otherwise
     * 
     * Captures 
     * Captures are denoted by the lower case letter "x"
     * immediately prior to the destination square. Pawn captures with the
     * omitted piece symbol, include the file letter of the originating
     * square of the capturing pawn prior to the "x" character, even if not
     * required for unambiguousness. Some SAN variations in printed media
     * even omit the target rank if unambiguous, like dxe, which might not
     * be accepted as input format.
     * 
     * En passant 
     * The PGN-Standard does not require En passant captures have
     * any special notation, and is written as if the captured pawn were on
     * the capturing pawn's destination square.FIDE states the redundant
     * move suffix "e.p." optional (after 1 July 2014) [11].
     * 
     * In the case of an ‘en passant’ capture, ‘e.p.’ may be appended to the
     * notation.
     * 
     * Pawn promotion 
     * A pawn promotion requires the information about the
     * chosen piece, appended as trailing Piece letter behind the target
     * square. The SAN PGN-Standard requires an equal sign ('=') immediately
     * following the destination square.
     * 
     * Castling 
     * Castling is indicated by the special notations, "O-O" for
     * kingside castling and "O-O-O" for queenside castling. While the FIDE
     * handbook [12] uses the digit zero, the SAN PGN-Standard requires the
     * capital letter 'O' for its export format.
     * 
     * Converting Moves
     * Due to the most compact representation, considering
     * ambiguities concerning the origin square, converting moves with pure
     * from- and to-squares to SAN requires not only an underlying board
     * representation to determine piece initials, but also legal move
     * generation for a subset of moves to the destination square. Pseudo
     * legal, but illegal moves for instance with a Pinned piece must not be
     * considered in ambiguous issues in an export format.
     */
    @SuppressWarnings("unused")
	@Test
    public void testMoveFromSANNotation() {

	System.out.println();
	System.out.println("Test Move from PGN SAN Notation");

	String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e4 0 1";
	
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
	
	GameBoard board = new GameBoardImpl(fen);
	
	// Castle
	GameMove m01 = NotationHelper.createNewMoveFromSANNotation(board, "O-O");
	assertTrue(m01 != null);
	GameMove m02 = NotationHelper.createNewMoveFromSANNotation(board, "0-0");
	assertTrue(m02 != null);
	GameMove m03 = NotationHelper.createNewMoveFromSANNotation(board, "o-o");
	assertTrue(m03 != null);
	GameMove m04 = NotationHelper.createNewMoveFromSANNotation(board, "O-O-O");
	assertTrue(m04 != null);
	
	// piece
	GameMove m1 = NotationHelper.createNewMoveFromSANNotation(board, "Nb6");
	assertTrue(m1 != null);
	GameMove m2;
	try {
	    m2 = NotationHelper.createNewMoveFromSANNotation(board, "Rxg6");
	    fail("not a valid move in this position");
	} catch (InvalidMoveException e) {
	    assertNotNull(e);
	}
	GameMove m3 = NotationHelper.createNewMoveFromSANNotation(board, "Qe5");
	assertTrue(m3 != null); 
	GameMove m4 = NotationHelper.createNewMoveFromSANNotation(board, "Qed6");
	assertTrue(m4 != null); 
	GameMove m5 = NotationHelper.createNewMoveFromSANNotation(board, "Qcd6");
	assertTrue(m5 != null); 
	GameMove m6 = NotationHelper.createNewMoveFromSANNotation(board, "Q4c5");
	assertTrue(m6 != null); 
	GameMove m7 = NotationHelper.createNewMoveFromSANNotation(board, "Qc6xe4");
	assertTrue(m7 != null); 
	GameMove m8;
	try {
	    m8 = NotationHelper.createNewMoveFromSANNotation(board, "Qcxe4");
	    fail("Ambiguous move");
	} catch (InvalidMoveException e) {
	    assertNotNull(e);
	}
	GameMove m9;
	try {
	    m9 = NotationHelper.createNewMoveFromSANNotation(board, "Q6xe4");
	    fail("Ambiguous move");
	} catch (InvalidMoveException e) {
	    assertNotNull(e);
	}
	
	// pawn
	GameMove m11 = NotationHelper.createNewMoveFromSANNotation(board, "b6");
	assertTrue(m11 != null); 
	GameMove m12 = NotationHelper.createNewMoveFromSANNotation(board, "b5");
	assertTrue(m12 != null); 
	GameMove m13 = NotationHelper.createNewMoveFromSANNotation(board, "a1Q");
	assertTrue(m13 != null);
	GameMove m14;
	try {
	    m14 = NotationHelper.createNewMoveFromSANNotation(board, "h4");
	    fail("Not whites turn");
	} catch (InvalidMoveException e) {
	    assertNotNull(e);
	}
	
	// en passant
	GameMove m16 = NotationHelper.createNewMoveFromSANNotation(board, "fxe3");
	assertTrue(m16 != null);
	GameMove m17 = NotationHelper.createNewMoveFromSANNotation(board, "fxe3e.p.");
	assertTrue(m17 != null);
	
	// pawn promotion
	GameMove m21 = NotationHelper.createNewMoveFromSANNotation(board, "a1=Q");
	assertTrue(m21 != null);
	GameMove m23 = NotationHelper.createNewMoveFromSANNotation(board, "axb1=Q");
	assertTrue(m23 != null);
	GameMove m24;
	try {
	    m24 = NotationHelper.createNewMoveFromSANNotation(board, "xb1=Q");
	    fail("Ambiguous move");
	} catch (InvalidMoveException e) {
	    assertNotNull(e);
	}
	GameMove m25;
	try {
	    m25 = NotationHelper.createNewMoveFromSANNotation(board, "b1Q");
	    fail("Ambiguous move");
	} catch (InvalidMoveException e) {
	    assertNotNull(e);
	}
	GameMove m26 = NotationHelper.createNewMoveFromSANNotation(board, "axb1R");
	assertTrue(m26 != null);
	
	// LAN
	GameMove m31 = NotationHelper.createNewMoveFromSANNotation(board, "b7-b5");
	assertTrue(m31 != null);
	GameMove m32 = NotationHelper.createNewMoveFromSANNotation(board, "Nd7-b6");
	assertTrue(m32 != null);
	GameMove m33 = NotationHelper.createNewMoveFromSANNotation(board, "a2xb1R");
	assertTrue(m33 != null);
	GameMove m34 = NotationHelper.createNewMoveFromSANNotation(board, "f4xe3");
	assertTrue(m34 != null);
    }

    @SuppressWarnings("deprecation")
	@Test
    public void testMoveFromOldSimpleNotation() {
    
        System.out.println("Test Move from Simple Notation");
    
        GameMove m1 = NotationHelper.createNewMoveFromSimpleNotation("e2-e4",
        	Pawn.create(GameColor.WHITE));
        GameMove m1twin = NotationHelper.createNewMoveFromSimpleNotation("e2-e4",
        	Pawn.create(GameColor.WHITE));
        GameMove m1copy = new GameMoveImpl(m1);
        GameMove m2 = NotationHelper.createNewMoveFromSimpleNotation("d2-d4",
        	Pawn.create(GameColor.WHITE));
        GameMove m3 = NotationHelper.createNewMoveFromSimpleNotation("e2-e3",
        	Pawn.create(GameColor.WHITE));
        GameMove m4 = NotationHelper.createNewMoveFromSimpleNotation("a2a4",
        	Pawn.create(GameColor.WHITE));
    
        System.out
        	.println("Move Orig: " + m1.toString() + ": " + m1.hashCode());
        System.out.println("Move Twin: " + m1twin.toString() + ": "
        	+ m1twin.hashCode());
        System.out.println("Move Copy: " + m1copy.toString() + ": "
        	+ m1copy.hashCode());
        System.out.println("Move 2: " + m2.toString() + ": " + m2.hashCode());
        System.out.println("Move 3: " + m3.toString() + ": " + m3.hashCode());
        System.out.println("Move 4: " + m4.toString() + ": " + m4.hashCode());
    
        assertEquals(m1, m1twin);
        assertEquals(m1.hashCode(), m1twin.hashCode());
        assertEquals(m1, m1copy);
        assertEquals(m1.hashCode(), m1copy.hashCode());
        assertNotEquals(m1, m2);
        assertNotEquals(m1, m3);
        assertNotEquals(m2, m3);
        assertNotEquals(m3, m4);
    
    }

}
