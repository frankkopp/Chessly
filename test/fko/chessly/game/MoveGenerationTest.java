package fko.chessly.game;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import fko.chessly.game.pieces.Bishop;
import fko.chessly.game.pieces.King;
import fko.chessly.game.pieces.Knight;
import fko.chessly.game.pieces.Pawn;

public class MoveGenerationTest {

    @Test
    public void testMoveGen() {
        GameBoard b = new GameBoardImpl("r3k2r/1ppn3p/2q1q1n1/4P3/2q1Pp2/B5R1/pbp2PPP/1R4K1 b kq e3 0 113");
        GameMoveList list = b.generateMoves();
        List<GameMove> ep = list.stream().filter(p -> p.getWasEnPassantCapture()).collect(Collectors.toList());
        assertTrue(ep.size()==1);
        System.out.println(b);
        System.out.println(list);
        System.out.println("En Passant Moves: "+ep);

        b = new GameBoardImpl("rnbqkbnr/1pp1pppp/p7/2PpP3/8/8/PP1P1PPP/RNBQKBNR w KQkq d6 0 1");
        list = b.generateMoves();
        ep = list.stream().filter(p -> p.getWasEnPassantCapture()).collect(Collectors.toList());
        assertTrue(ep.size()==2);
        System.out.println(b);
        System.out.println(list);
        System.out.println("En Passant Moves: "+ep);

        b = new GameBoardImpl("rnbqkbnr/1pp1pppp/p7/8/2pPp3/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1");
        list = b.generateMoves();
        ep = list.stream().filter(p -> p.getWasEnPassantCapture()).collect(Collectors.toList());
        assertTrue(ep.size()==2);
        System.out.println(b);
        System.out.println(list);
        System.out.println("En Passant Moves: "+ep);

        b = new GameBoardImpl("rnbqkbnr/p1pppppp/8/8/p7/8/1PPPPPPP/RNBQKBNR w KQkq - 0 3");
        list = b.generateMoves();
        ep = list.stream().filter(p -> p.getWasEnPassantCapture()).collect(Collectors.toList());
        //assertTrue(ep.size()==2);
        System.out.println(b);
        System.out.println(list);
        System.out.println("En Passant Moves: "+ep);
    }

    @Test
    public void test() {
        GameBoard b = new GameBoardImpl();
        GameMove m;

        // Print Board
        System.out.println(b.toString());

        // e2-e4
        m = NotationHelper.createNewMoveFromSimpleNotation("e2-e4", Pawn.create(GameColor.WHITE));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // e7-e5
        m = NotationHelper.createNewMoveFromSimpleNotation("e7-e5", Pawn.create(GameColor.BLACK));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Ke1-e2
        m = NotationHelper.createNewMoveFromSimpleNotation("e1-e2", King.create(GameColor.WHITE));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Ng8-f6
        m = NotationHelper.createNewMoveFromSimpleNotation("g8-f6", Knight.create(GameColor.BLACK));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Ke2-d3
        m = NotationHelper.createNewMoveFromSimpleNotation("e2-d3", King.create(GameColor.WHITE));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Bf8-c5
        m = NotationHelper.createNewMoveFromSimpleNotation("f8-c5", Bishop.create(GameColor.BLACK));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Kd3-c4
        m = NotationHelper.createNewMoveFromSimpleNotation("d3-c4", King.create(GameColor.WHITE));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // o-o
        m = NotationHelper.createNewMoveFromSimpleNotation("e8-g8", King.create(GameColor.BLACK));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // Ng1-f3
        m = NotationHelper.createNewMoveFromSimpleNotation("g1-f3", Knight.create(GameColor.WHITE));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // d7-d5
        m = NotationHelper.createNewMoveFromSimpleNotation("d7-d5", Pawn.create(GameColor.BLACK));
        assertTrue(b.isLegalMove(m));
        makeMove(b, m);
        // d2-d4
        m = NotationHelper.createNewMoveFromSimpleNotation("d2-d4", Pawn.create(GameColor.WHITE));
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

