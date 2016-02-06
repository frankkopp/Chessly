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
	wP= King.createKing(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = King.createKing(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Queen.createQueen(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Queen.createQueen(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Rook.createRook(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Rook.createRook(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Bishop.createBishop(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Bishop.createBishop(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Knight.createKnight(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Knight.createKnight(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
	wP = Pawn.createPawn(GameColor.WHITE);
	System.out.println(wP.toString()+" - "+wP.toNotationString()+" - "+wP.hashCode());
	bP = Pawn.createPawn(GameColor.BLACK);
	System.out.println(bP.toString()+" - "+bP.toNotationString()+" - "+bP.hashCode());
    }

    @Test
    public void testEquals() {
	GamePiece wP = King.createKing(GameColor.WHITE);
	GamePiece wP2 = King.createKing(GameColor.WHITE);
	GamePiece bP = King.createKing(GameColor.BLACK);
	assertNotEquals(wP,bP);
	assertEquals(wP,wP2);
    }

}
