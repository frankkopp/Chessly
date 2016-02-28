/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package fko.chessly.player.computer.Omega;

import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Test;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameMove;
import fko.chessly.game.NotationHelper;

/**
 * @author fkopp
 *
 */
public class TestOmegaBoardPosition {


    private static final int ITERATIONS = 99999;

    /**
     * Some timings to find fastest code - so nfr test
     */
    @Test
    public void testTimings() {

        OmegaPiece[] _x88Board = new OmegaPiece[129];

        // fill array
        System.out.println("x88Board fill with value 1. Arrays.fill 2. for loop");
        Instant start = Instant.now();
        for (int i=0;i<ITERATIONS;i++) Arrays.fill(_x88Board,  OmegaPiece.NOPIECE);
        Instant end = Instant.now();
        System.out.println(Duration.between(start, end));
        start = Instant.now();
        // clear board
        for (int i=0;i<ITERATIONS;i++) {
            for (OmegaSquare s : OmegaSquare.getValueList()) {
                _x88Board[s.ordinal()] = OmegaPiece.NOPIECE;
            }
        }
        end = Instant.now();
        System.out.println(Duration.between(start, end));

        // copy array
        System.out.println("Copy x88Board - 1. System.arraycopy 2. Arrays.copyof");
        _x88Board = new OmegaPiece[128];
        OmegaPiece[] _x88Board2 = new OmegaPiece[128];
        start = Instant.now();
        // clear board
        for (int i=0;i<ITERATIONS;i++) System.arraycopy(_x88Board, 0, _x88Board2, 0, _x88Board.length);
        end = Instant.now();
        System.out.println(Duration.between(start, end));
        start = Instant.now();
        // clear board
        for (int i=0;i<ITERATIONS;i++) _x88Board2 = Arrays.copyOf(_x88Board, _x88Board.length);
        end = Instant.now();
        System.out.println(Duration.between(start, end));

        System.out.println("OmegaBoardPosition creation and Copy Contructor of OmegaBoardPosition");
        String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq e4 0 2";
        OmegaBoardPosition obp = null;
        start = Instant.now();
        for (int i=0;i<ITERATIONS;i++) obp = new OmegaBoardPosition(fen);
        end = Instant.now();
        System.out.println(Duration.between(start, end));
        @SuppressWarnings("unused")
        OmegaBoardPosition obp_copy=null;
        start = Instant.now();
        for (int i=0;i<ITERATIONS;i++) obp_copy = new OmegaBoardPosition(obp);
        end = Instant.now();
        System.out.println(Duration.between(start, end));

        System.out.println("GameBoard creation and Copy Contructor of OmegaBoardPosition");
        GameBoard gb = null;;
        start = Instant.now();
        for (int i=0;i<ITERATIONS;i++) gb = new GameBoardImpl(fen);
        end = Instant.now();
        System.out.println(Duration.between(start, end));
        start = Instant.now();
        for (int i=0;i<ITERATIONS;i++) obp_copy = new OmegaBoardPosition(gb);
        System.out.println(Duration.between(start, end));
    }

    /**
     *
     */
    //@Test
    public void testCastlingRights() {
        // Castling rights
        EnumSet<OmegaCastling> _castlingRights = EnumSet.allOf(OmegaCastling.class);
        _castlingRights.forEach(c -> System.out.print(c));
        System.out.println();
        _castlingRights.remove(OmegaCastling.WHITE_KINGSIDE);
        _castlingRights.forEach(c -> System.out.print(c));
        System.out.println();
        System.out.println(_castlingRights.contains(OmegaCastling.BLACK_QUEENSIDE));
    }

    /**
     *
     */
    @Test
    public void testContructorFromFEN() {

        String fen = NotationHelper.StandardBoardFEN;
        fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113";

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

        OmegaBoardPosition obp = new OmegaBoardPosition(fen);
        System.out.println(fen);
        System.out.println(obp.toFENString());
        assertTrue(obp.toFENString().equals(fen));

        OmegaBoardPosition omegaBoard = new OmegaBoardPosition(fen);
        GameBoard gameBoard = new GameBoardImpl(fen);
        OmegaBoardPosition omegaBoard2 = new OmegaBoardPosition(gameBoard);
        //OmegaBoardPosition omegaBoard3 = new OmegaBoardPosition(fen);
        //assertTrue(omegaBoard.equals(omegaBoard3));

        System.out.println(omegaBoard.toFENString());
        System.out.println(omegaBoard2.toFENString());
        assertTrue(omegaBoard.hashCode() == omegaBoard2.hashCode());
        assertTrue(omegaBoard.equals(omegaBoard2));




    }

    /**
     *
     */
    @Test
    public void testCopyContructor() {
        String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq e3 0 2";
        OmegaBoardPosition obp = new OmegaBoardPosition(fen);
        OmegaBoardPosition obp_copy = new OmegaBoardPosition(obp);
        assertTrue(obp.equals(obp_copy));
        assertTrue(obp.toFENString().equals(obp_copy.toFENString()));
    }

    /**
     *
     */
    @Test
    public void testContructorFromGameBoard() {
        String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq e3 0 2";
        GameBoard gb = new GameBoardImpl(fen);
        OmegaBoardPosition obp_copy = new OmegaBoardPosition(gb);
        assertTrue(gb.toFENString().equals(obp_copy.toFENString()));
    }

    /**
     *
     */
    @Test
    public void testMoveOnBoard() {
        GameBoard gameBoard = new GameBoardImpl();
        OmegaBoardPosition omegaBoard = new OmegaBoardPosition(gameBoard);

        String testFen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113";

        // normal
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        GameMove gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"c4-a4");
        int move = OmegaMove.convertFromGameMove(gameMove);
        GameMove convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/q3Pp2/6R1/p1p2PPP/1R4K1 w kq - 1 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113"));

        // normal pawn move
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"b7-b6");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/2pn3p/1pq1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq - 0 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113"));

        // normal capture
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"c4-e4");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/4qp2/6R1/p1p2PPP/1R4K1 w kq - 0 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113"));

        // pawn double
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"b7-b5");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/2pn3p/2q1q1n1/1p6/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq b6 0 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113"));

        // castling
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"e8-g8");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        assertTrue(omegaBoard.toFENString().equals("r4rk1/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w - - 1 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113"));

        // promotion
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"a2-a1Q");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/2p2PPP/qR4K1 w kq - 0 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113"));

        // promotion capture
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"a2-b1R");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/2p2PPP/1r4K1 w kq - 0 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113"));

        // en passant
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"f4-e3");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q5/4p1R1/p1p2PPP/1R4K1 w kq - 0 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113"));

        // multiple moves
        // normal
        gameBoard = new GameBoardImpl(testFen);
        omegaBoard = new OmegaBoardPosition(gameBoard);
        // en passant
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"f4-e3");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        gameBoard.makeMove(convertedMove);
        System.out.println(omegaBoard);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q5/4p1R1/p1p2PPP/1R4K1 w kq - 0 114"));
        // pawn capture
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"f2-e3");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        gameBoard.makeMove(convertedMove);
        System.out.println(omegaBoard);
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q5/4P1R1/p1p3PP/1R4K1 b kq - 0 114"));
        // castling
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"e8-g8");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        gameBoard.makeMove(convertedMove);
        System.out.println(omegaBoard);
        assertTrue(omegaBoard.toFENString().equals("r4rk1/1ppn3p/2q1q1n1/8/2q5/4P1R1/p1p3PP/1R4K1 w - - 1 115"));
        // pawn double
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"h2-h4");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        gameBoard.makeMove(convertedMove);
        System.out.println(omegaBoard);
        assertTrue(omegaBoard.toFENString().equals("r4rk1/1ppn3p/2q1q1n1/8/2q4P/4P1R1/p1p3P1/1R4K1 b - h3 0 115"));
        // pawn promotion
        gameMove = NotationHelper.createNewMoveFromSimpleNotation(gameBoard,"a2-b1R");
        move = OmegaMove.convertFromGameMove(gameMove);
        convertedMove = OmegaMove.convertToGameMove(move);
        assert(gameMove.equals(convertedMove));
        omegaBoard.makeMove(move);
        gameBoard.makeMove(convertedMove);
        System.out.println(omegaBoard);
        assertTrue(omegaBoard.toFENString().equals("r4rk1/1ppn3p/2q1q1n1/8/2q4P/4P1R1/2p3P1/1r4K1 w - - 0 116"));
        // now test undo
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r4rk1/1ppn3p/2q1q1n1/8/2q4P/4P1R1/p1p3P1/1R4K1 b - h3 0 115"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r4rk1/1ppn3p/2q1q1n1/8/2q5/4P1R1/p1p3PP/1R4K1 w - - 1 115"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q5/4P1R1/p1p3PP/1R4K1 b kq - 0 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals("r3k2r/1ppn3p/2q1q1n1/8/2q5/4p1R1/p1p2PPP/1R4K1 w kq - 0 114"));
        omegaBoard.undoMove();
        assertTrue(omegaBoard.toFENString().equals(testFen));
        System.out.println(omegaBoard);

    }

    /**
     * Test Zobrist Key generation
     */
    @Test
    public void testZobrist() {

        String testFen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e3 0 113";
        long initialZobrist = 0;
        long zobrist = 0;

        OmegaBoardPosition omegaBoard = new OmegaBoardPosition(testFen);
        GameBoard gameBoard = new GameBoardImpl(testFen);
        OmegaBoardPosition omegaBoard2 = new OmegaBoardPosition(gameBoard);

        System.out.println("Test if board are equal.");
        System.out.println(omegaBoard.toFENString());
        System.out.println(omegaBoard2.toFENString());
        assertTrue(omegaBoard.equals(omegaBoard2));

        System.out.println("Test if zobrists are equal.");
        System.out.println(omegaBoard.getZobristKey());
        System.out.println(omegaBoard2.getZobristKey());
        assertTrue(omegaBoard.getZobristKey()==omegaBoard2.getZobristKey());

        int testMove = OmegaMove.createMove(
                OmegaMoveType.NORMAL,
                OmegaSquare.b7,
                OmegaSquare.b6,
                OmegaPiece.BLACK_PAWN,
                OmegaPiece.NOPIECE,
                OmegaPiece.NOPIECE);

        System.out.println("Test if zobrist after move/undo are equal.");
        initialZobrist = omegaBoard.getZobristKey();
        System.out.println(initialZobrist);
        omegaBoard.makeMove(testMove);
        zobrist = omegaBoard.getZobristKey();
        System.out.println(zobrist);
        omegaBoard.undoMove();
        zobrist = omegaBoard.getZobristKey();
        System.out.println(zobrist);
        assertTrue(zobrist==initialZobrist);

        // test if zobrist key is identical if one board comes to the same position
        // as a newly created one
        String fenAfterMove = "r3k2r/2pn3p/1pq1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq - 0 114";
        omegaBoard.makeMove(testMove);
        omegaBoard2 = new OmegaBoardPosition(fenAfterMove);
        System.out.println(omegaBoard.getZobristKey()+" "+omegaBoard.toFENString());
        System.out.println(omegaBoard2.getZobristKey()+" "+omegaBoard2.toFENString());
        assertTrue(omegaBoard.toFENString().equals(omegaBoard2.toFENString()));
        assertTrue(omegaBoard.getZobristKey() == omegaBoard2.getZobristKey());
        assertTrue(omegaBoard.equals(omegaBoard2));


    }


}
