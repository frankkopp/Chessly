package fko.chessly.player.computer.PulseEngine;


import org.junit.jupiter.api.Test;

public class Pulse_Board_Test {

	//@Test
	public void testMoves() {
		
//		Board board = new Board();
//
//		System.out.println(board.toBoardString());
//
//		System.out.println();
//		board.makeMove(Move.createNewMove("e2-e4", Color.WHITE));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("e7-e5", Color.BLACK));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Ng1-f3", Color.WHITE));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Nb8-c6", Color.BLACK));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Bf1-b5", Color.WHITE));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Bf8-b4", Color.BLACK));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("O-O", Color.WHITE));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Ng8-f6", Color.BLACK));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Rf1-e1", Color.WHITE));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("O-O", Color.BLACK));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Bb5xc6", Color.WHITE));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("d7xc6", Color.BLACK));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Re1-e2", Color.WHITE));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Rf8-e8", Color.BLACK));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Re2-e1", Color.WHITE));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		System.out.println();
//		board.makeMove(Move.createNewMove("Re8-f8", Color.BLACK));
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//
//		System.out.println("\nundoMove test vvvvvvvvvvvv");
//
//		board.undoMove();
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//		board.undoMove();
//		System.out.println(board);
//		System.out.println(board.toBoardString());
//
//		System.out.println("undoMove test end ^^^^^^^^^^^^");
//
//		System.out.println("makeMove test end");

		
	}

	@SuppressWarnings("unused")
	@Test
	public void testSearch() {
		Board board = new Board();
		System.out.println(board.toBoardString());
		
		PulseEngine_v2 engine = new PulseEngine_v2();
		
		
		
	}
	
	@SuppressWarnings("unused")
	@Test
	public void boardCopyTest() {
		
		Board boardA = new Board();
		boardA.initStandard();
		Board boardB = new Board(boardA);
		
		System.out.println("Test");
		
		
	}
	
}
