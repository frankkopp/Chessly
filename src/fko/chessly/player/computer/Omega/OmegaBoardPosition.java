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

import java.util.EnumSet;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;

/**
 * @author Frank
 */
public class OmegaBoardPosition {

    /**
     * Size of 0x88 board
     */
    private static final int BOARDSIZE = 128;

    /**
     * Standard Board Setup as FEN
     */
    private final static String STANDARD_BOARD_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // **********************************************************
    // Board State START ----------------------------------------
    // unique chess position

    // 0x88 Board
    private final int[] x88Board = new int[BOARDSIZE];

    // Castling rights
    private EnumSet<OmegaCastling> _castlingRights = EnumSet.allOf(OmegaCastling.class);

    // en passant field - if NOSQUARE then we do not have an en passant option
    private OmegaSquare _enPasantSquare = OmegaSquare.NOSQUARE;

    // half move clock - number of half moves since last capture
    private int _halfMoveClock = 0;

    // half move number - the actual half move number to determine the full move number
    private int _halfMoveNumber = 0;

    // Board State END ------------------------------------------
    // **********************************************************

    // Extended Board State ----------------------------------
    // not necessary for a unique position




    // Constructors START -----------------------------------------

    /**
     * Creates a standard Chessly board and initializes it with standard chess
     * setup.
     */
    public OmegaBoardPosition() {
        this(STANDARD_BOARD_FEN);
    }

    /**
     * Creates a standard Chessly board and initializes it with a fen position
     * @param fen
     */
    public OmegaBoardPosition(String fen) {
        initBoard(fen);
    }

    /**
     * Copy constructor - creates a copy of the give OmegaBoardPosition
     * @param op
     */
    public OmegaBoardPosition(OmegaBoardPosition op) {

    }

    /**
     * Copy constructor from GameBoard - creates a equivalent OmegaBoardPosition
     * from the give GameBoard
     * @param board
     */
    public OmegaBoardPosition(GameBoard board) {

    }

    /**
     * @param fen
     */
    private void initBoard(String fen) {
        // TODO Auto-generated method stub
    }

}
