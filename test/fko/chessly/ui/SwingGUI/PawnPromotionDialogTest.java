package fko.chessly.ui.SwingGUI;

import static org.junit.Assert.assertTrue;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.junit.Test;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameColor;
import fko.chessly.game.IllegalMoveException;
import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveImpl;
import fko.chessly.game.GamePiece;
import fko.chessly.game.pieces.Pawn;

public class PawnPromotionDialogTest {

	@Test
	public void testPawnPromotionDialog() {
		SwingGUI ui = new SwingGUI();
		PawnPromotionDialog dialog = new PawnPromotionDialog(ui);
        AbstractDialog.centerComponent(dialog);
        dialog.setVisible(true);
	}
	
	@Test
	public void testPawnPromotion() {
		JFrame frame = new JFrame("FrameDemo");
		frame.setSize(new Dimension(500,500));
		frame.setMinimumSize(new Dimension(100,100));
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		GameBoard b = new GameBoardImpl();
		GameMove m;

		BoardPanel bp = new BoardPanel(null);
		
		// e2-e4
		m = new GameMoveImpl(b.getPiece(5, 2), b.getPiece(5, 4), Pawn.createPawn(GameColor.WHITE));
		assertTrue(b.isLegalMove(m));
		makeMove(b, m);
		
		bp.setAndDrawBoard(b);
		
		frame.getContentPane().add(bp, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.requestFocus();
		while (frame.isShowing()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param b
	 * @param m
	 */
	protected void makeMove(GameBoard b, GameMove m) {
		GamePiece pC;
		/*
		System.out.println("Next Player:      "+b.getNextPlayerColor().toString());
		System.out.println("Next Move Color:  "+m.getMoveColor().toString());
		if (b.getField(m.getFromField().getCol(),m.getFromField().getRow()).getPiece() != null) {
			System.out.println("From Field Color: "+b.getField(m.getFromField().getCol(),m.getFromField().getRow()).getPiece().getColor());
		} else {
			System.out.println("From Field Color: EMPTY");
		}
		*/
		try {
			pC = b.makeMove(m);
		} catch (IllegalMoveException e) {
			//e.printStackTrace();
		}
		//print(b, m);
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
