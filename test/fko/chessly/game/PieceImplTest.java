package fko.chessly.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import fko.chessly.game.pieces.Bishop;
import fko.chessly.game.pieces.King;
import fko.chessly.game.pieces.Knight;
import fko.chessly.game.pieces.Pawn;
import fko.chessly.game.pieces.Queen;
import fko.chessly.game.pieces.Rook;

public class PieceImplTest {

    @Test
    public void testPieceImpl() {
	GamePiece wP,bP; 
	wP= King.create(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = King.create(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Queen.create(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Queen.create(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Rook.create(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Rook.create(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Bishop.create(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Bishop.create(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Knight.create(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Knight.create(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Pawn.create(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Pawn.create(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
    }

    @Test
    public void testEquals() {
	GamePiece wP = King.create(GameColor.WHITE);
	GamePiece wP2 = King.create(GameColor.WHITE);
	GamePiece bP = King.create(GameColor.BLACK);
	assertNotEquals(wP,bP);
	assertEquals(wP,wP2);
    }

}
