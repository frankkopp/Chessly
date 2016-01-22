package fko.chessly.game;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fko.chessly.game.pieces.Bishop;
import fko.chessly.game.pieces.King;
import fko.chessly.game.pieces.Knight;
import fko.chessly.game.pieces.Pawn;

public class MoveGenerationTest {

    @Test
    public void test() {
	GameBoard b = new GameBoardImpl();
	GameMove m;

	// Print Board
	System.out.println(b.toString());

	// e2-e4
	m = NotationHelper.createNewMoveFromSimpleNotation("e2-e4", Pawn.createPawn(GameColor.WHITE));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// e7-e5
	m = NotationHelper.createNewMoveFromSimpleNotation("e7-e5", Pawn.createPawn(GameColor.BLACK));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// Ke1-e2
	m = NotationHelper.createNewMoveFromSimpleNotation("e1-e2", King.createKing(GameColor.WHITE));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// Ng8-f6
	m = NotationHelper.createNewMoveFromSimpleNotation("g8-f6", Knight.createKnight(GameColor.BLACK));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// Ke2-d3
	m = NotationHelper.createNewMoveFromSimpleNotation("e2-d3", King.createKing(GameColor.WHITE));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// Bf8-c5
	m = NotationHelper.createNewMoveFromSimpleNotation("f8-c5", Bishop.createBishop(GameColor.BLACK));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// Kd3-c4
	m = NotationHelper.createNewMoveFromSimpleNotation("d3-c4", King.createKing(GameColor.WHITE));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// o-o
	m = NotationHelper.createNewMoveFromSimpleNotation("e8-g8", King.createKing(GameColor.BLACK));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// Ng1-f3
	m = NotationHelper.createNewMoveFromSimpleNotation("g1-f3", Knight.createKnight(GameColor.WHITE));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// d7-d5
	m = NotationHelper.createNewMoveFromSimpleNotation("d7-d5", Pawn.createPawn(GameColor.BLACK));
	assertTrue(b.isLegalMove(m));
	makeMove(b, m);
	// d2-d4
	m = NotationHelper.createNewMoveFromSimpleNotation("d2-d4", Pawn.createPawn(GameColor.WHITE));
	assertFalse(b.isLegalMove(m)); // check - illegal move
	makeMove(b, m);

	System.out.println();

	List<GameMove> moveList = b.generateMoves();

	System.out.println("All legal moves: ("+moveList.size()+") "+moveList);

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
	    e.printStackTrace();
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

