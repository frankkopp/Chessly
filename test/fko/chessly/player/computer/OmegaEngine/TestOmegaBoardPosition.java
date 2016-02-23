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

package fko.chessly.player.computer.OmegaEngine;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Test;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.NotationHelper;
import fko.chessly.player.computer.Omega.OmegaBoardPosition;
import fko.chessly.player.computer.Omega.OmegaCastling;
import fko.chessly.player.computer.Omega.OmegaPiece;
import fko.chessly.player.computer.Omega.OmegaSquare;

/**
 * @author fkopp
 *
 */
public class TestOmegaBoardPosition {


    private static final int ITERATIONS = 99999;

    /**
     *
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
        fen = "8/1P6/6k1/8/8/8/p1K5/8 w - - 0 1";
        fen = "4k2r/1q1p1pp1/p3p3/1pb1P3/2r3P1/P1N1P2p/1PP1Q2P/2R1R1K1 b k - 0 1";
        fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq e4 0 2";

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
        //System.out.println(fen);
        //System.out.println(obp.toFENString());
        assertTrue(obp.toFENString().equals(fen));

    }

    /**
     *
     */
    @Test
    public void testCopyContructor() {
        String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq e4 0 2";
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
        String fen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 w kq e4 0 2";
        GameBoard gb = new GameBoardImpl(fen);
        OmegaBoardPosition obp_copy = new OmegaBoardPosition(gb);
        assertTrue(gb.toFENString().equals(obp_copy.toFENString()));
    }


}
