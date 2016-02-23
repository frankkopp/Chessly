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

import java.util.Arrays;
import java.util.EnumSet;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameCastling;
import fko.chessly.game.GameColor;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePosition;
import fko.chessly.player.computer.Omega.OmegaSquare.File;

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
    private final OmegaPiece[] _x88Board = new OmegaPiece[BOARDSIZE];

    // Castling rights
    private EnumSet<OmegaCastling> _castlingRights = EnumSet.allOf(OmegaCastling.class);

    // en passant field - if NOSQUARE then we do not have an en passant option
    private OmegaSquare _enPassantSquare = OmegaSquare.NOSQUARE;

    // half move clock - number of half moves since last capture
    private int _halfMoveClock = 0;

    // half move number - the actual half move number to determine the full move number
    private int _halfMoveNumber = 0;

    // next player color
    private OmegaColor _nextPlayer = OmegaColor.WHITE;

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
        if (op == null)
            throw new NullPointerException("Parameter op may not be null");
        System.arraycopy(op._x88Board, 0, this._x88Board, 0, op._x88Board.length);
        this._castlingRights = op._castlingRights.clone();
        this._enPassantSquare = op._enPassantSquare;
        this._halfMoveClock = op._halfMoveClock;
        this._halfMoveNumber = op._halfMoveNumber;
        this._nextPlayer = op._nextPlayer;
    }

    /**
     * Copy constructor from GameBoard - creates a equivalent OmegaBoardPosition
     * from the give GameBoard
     * @param oldBoard
     */
    public OmegaBoardPosition(GameBoard oldBoard) {
        if (oldBoard == null)
            throw new NullPointerException("Parameter oldBoard may not be null");
        // -- copy fields --
        for (int file = 1; file <= 8; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                // we can't do an arraycopy here as we do not know the
                // Implementation of the old board
                GamePiece gp = oldBoard.getPiece(file, rank) == null ? null
                        : (GamePiece) oldBoard.getPiece(file, rank).clone();
                OmegaPiece op = OmegaPiece.convertFromGamePiece(gp);
                _x88Board[OmegaSquare.getSquare(file, rank).ordinal()] = op;
            }
        }
        // -- copy castling flags
        _castlingRights = EnumSet.noneOf(OmegaCastling.class);
        if (oldBoard.isCastlingKingSideAllowed(GameColor.WHITE)) {
            _castlingRights.add(OmegaCastling.WHITE_KINGSIDE);
        }
        if (oldBoard.isCastlingQueenSideAllowed(GameColor.WHITE)) {
            _castlingRights.add(OmegaCastling.WHITE_QUEENSIDE);
        }
        if (oldBoard.isCastlingKingSideAllowed(GameColor.BLACK)) {
            _castlingRights.add(OmegaCastling.BLACK_KINGSIDE);
        }
        if (oldBoard.isCastlingQueenSideAllowed(GameColor.BLACK)) {
            _castlingRights.add(OmegaCastling.BLACK_QUEENSIDE);
        }
        // other fields
        this._enPassantSquare = OmegaSquare.convertFromGamePosition(oldBoard.getEnPassantCapturable());
        this._halfMoveClock = oldBoard.getHalfmoveClock();
        this._halfMoveNumber = oldBoard.getLastHalfMoveNumber();
        this._nextPlayer = OmegaColor.convertFromGameColor(oldBoard.getNextPlayerColor());
    }

    /**
     * @param piece
     * @param file
     * @param rank
     */
    private void putPiece(OmegaPiece piece, int file, int rank) {
        assert OmegaSquare.getSquare(file, rank).isValidSquare();
        _x88Board[OmegaSquare.getSquare(file, rank).ordinal()] = piece;

    }
    /**
     * @param file
     * @param rank
     * @return the removed piece
     */
    private OmegaPiece removePiece(int file, int rank) {
        assert OmegaSquare.getSquare(file, rank).isValidSquare();
        OmegaPiece old = _x88Board[OmegaSquare.getSquare(file, rank).ordinal()];
        _x88Board[OmegaSquare.getSquare(file, rank).ordinal()] = OmegaPiece.NOPIECE;
        return old;
    }

    /**
     * @param fen
     */
    private void initBoard(String fen) {

        // clear board
        Arrays.fill(_x88Board,  OmegaPiece.NOPIECE);

        // Standard Start Board
        setupFromFEN(fen);

        // used for debugging
        //setupFromFEN("8/1P6/6k1/8/8/8/p1K5/8 w - - 0 1");
    }

    /**
     * @param fen
     */
    private void setupFromFEN(String fen) {
        if (fen.isEmpty()) throw new IllegalArgumentException("FEN Syntax not valid - empty string");

        String[] parts = fen.trim().split(" ");
        if (parts.length < 1) throw new IllegalArgumentException("FEN Syntax not valid - need at least two parts separated with space");

        int i = 0;
        int rank = 8;
        int file = 1;
        String s = null;

        // pieces on squares
        for (i=0; i<parts[0].length();i++) {
            s = parts[0].substring(i, i+1);
            if (s.matches("[pnbrqkPNBRQK]")) {
                if (s.toLowerCase() == s) { // black
                    switch (s) {
                        case "p": putPiece(OmegaPiece.BLACK_PAWN, file, rank); break;
                        case "n": putPiece(OmegaPiece.BLACK_KNIGHT, file, rank); break;
                        case "b": putPiece(OmegaPiece.BLACK_BISHOP, file, rank); break;
                        case "r": putPiece(OmegaPiece.BLACK_ROOK, file, rank); break;
                        case "q": putPiece(OmegaPiece.BLACK_QUEEN, file, rank); break;
                        case "k": putPiece(OmegaPiece.BLACK_KING, file, rank); break;
                        default:
                            throw new IllegalArgumentException("FEN Syntax not valid - expected a-hA-H");
                    }
                } else if (s.toUpperCase() == s) { // white
                    switch (s) {
                        case "P": putPiece(OmegaPiece.WHITE_PAWN, file, rank); break;
                        case "N": putPiece(OmegaPiece.WHITE_KNIGHT, file, rank); break;
                        case "B": putPiece(OmegaPiece.WHITE_BISHOP, file, rank); break;
                        case "R": putPiece(OmegaPiece.WHITE_ROOK, file, rank); break;
                        case "Q": putPiece(OmegaPiece.WHITE_QUEEN, file, rank); break;
                        case "K": putPiece(OmegaPiece.WHITE_KING, file, rank); break;
                        default:
                            throw new IllegalArgumentException("FEN Syntax not valid - expected a-hA-H");
                    }
                } else
                    throw new IllegalArgumentException("FEN Syntax not valid - expected a-hA-H");
                file++;
            } else if (s.matches("[1-8]")) {
                int e = Integer.parseInt(s);
                file += e;
            } else if (s.equals("/")) {
                rank--;
                file = 1;
            } else
                throw new IllegalArgumentException("FEN Syntax not valid - expected (1-9a-hA-H/)");

            if (file > 9) {
                throw new IllegalArgumentException("FEN Syntax not valid - expected (1-9a-hA-H/)");
            }
        }

        // next player
        _nextPlayer = null;
        if (parts.length >= 2) {
            s = parts[1];
            if (s.equals("w")) _nextPlayer = OmegaColor.WHITE;
            else if (s.equals("b")) _nextPlayer = OmegaColor.BLACK;
            else throw new IllegalArgumentException("FEN Syntax not valid - expected w or b");
        } else { // default "w"
            _nextPlayer = OmegaColor.WHITE;
        }

        // castling
        // reset all castling first
        _castlingRights = EnumSet.noneOf(OmegaCastling.class);
        if (parts.length >= 3) { // default "-"
            for (i=0; i<parts[2].length(); i++) {
                s = parts[2].substring(i, i+1);
                switch (s) {
                    case "K": _castlingRights.add(OmegaCastling.WHITE_KINGSIDE); break;
                    case "Q": _castlingRights.add(OmegaCastling.WHITE_QUEENSIDE); break;
                    case "k": _castlingRights.add(OmegaCastling.BLACK_KINGSIDE); break;
                    case "q": _castlingRights.add(OmegaCastling.BLACK_QUEENSIDE); break;
                    case "-":
                    default:
                }
            }
        }

        // en passant - which filed and if null no en passant option
        if (parts.length >= 4) { // default "-"
            s = parts[3];
            if (!s.equals("-")) {
                GamePosition enPassantCapturePosition = GamePosition.getGamePosition(s);
                _enPassantSquare = OmegaSquare.convertFromGamePosition(enPassantCapturePosition);
            }
        }

        // half move clock
        if (parts.length >= 5) { // default "0"
            s = parts[4];
            _halfMoveClock = Integer.parseInt(s);
        } else {
            _halfMoveClock = 0 ;
        }

        // full move number - mapping to half move number
        if (parts.length >= 6) { // default "1"
            s = parts[5];
            _halfMoveNumber = (2 * Integer.parseInt(s)) - 1;
        } else {
            _halfMoveNumber = 1 ;
        }
        if (_nextPlayer.isBlack()) _halfMoveNumber++;

        // double check correct numbering
        assert ((_nextPlayer.isWhite() && _halfMoveNumber %2 == 1)
                || _nextPlayer.isBlack() && _halfMoveNumber %2 == 0);

    }

    @Override
    public String toString() {
        return toBoardString();
    }

    /**
     * Returns a String representation the chess position of this OmegaBoardPoistion
     * as a FEN String-
     * @return FEN String of this position
     */
    public String toFENString() {

        String fen = "";

        for (int rank = 8; rank >= 1; rank--) {
            int emptySquares = 0;
            for (int file = 1; file <= 8; file++) {

                OmegaPiece piece = _x88Board[OmegaSquare.getSquare(file, rank).ordinal()];

                if (piece == OmegaPiece.NOPIECE) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        fen += emptySquares;
                        emptySquares = 0;
                    }
                    if (piece.getColor().isWhite()) {
                        fen += piece.toString();
                    } else {
                        fen += piece.toString();
                    }
                }
            }
            if (emptySquares > 0) {
                fen += emptySquares;
            }
            if (rank > 1) {
                fen += '/';
            }
        }
        fen += ' ';

        // Color
        fen += this._nextPlayer.toChar();
        fen += ' ';

        // Castling
        boolean castlingAvailable = false;
        for (OmegaCastling oc : _castlingRights) {
            castlingAvailable = true;
            fen += oc.getShortName();
        }
        if (!castlingAvailable) {
            fen += '-';
        }
        fen += ' ';

        // En passant
        if (this._enPassantSquare != OmegaSquare.NOSQUARE) {
            fen += _enPassantSquare.toString();
        } else {
            fen += '-';
        }
        fen += ' ';

        // Half move clock
        fen += this._halfMoveClock;
        fen += ' ';

        // Full move number
        fen += (_halfMoveNumber+1)/2;

        return fen;
    }

    /**
     * Returns a visual board string for use in a console.
     * Adds FEN String at the end.
     * @return String of visual board for use in console
     */
    public String toBoardString() {

        StringBuilder boardString = new StringBuilder();

        // backwards as highest row is on top
        for (int rank = 8; rank >= 1; rank--) {

            // upper border
            boardString.append("    ---------------------------------\n");

            // rank number
            boardString.append(' ').append(Integer.toString(rank)).append(": |");

            // fields
            for (int file = 1; file <= 8; file++) {
                OmegaPiece p = _x88Board[OmegaSquare.getSquare(file, rank).ordinal()];
                if (p == OmegaPiece.NOPIECE) {
                    boardString.append("   |");
                } else {
                    boardString.append(" ").append(p.toString()).append(" |");
                }
            }
            boardString.append("\n");
        }

        // lower border
        boardString.append("    ---------------------------------\n");

        // file letters
        boardString.append("     "); // 4 * space
        for (int file = 1; file <= 8; file++) {
            boardString.append(' ').append(File.get(file).toString().toUpperCase()).append("  ");
        }
        boardString.append("\n\n");

        boardString.append(toFENString());

        return boardString.toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this._castlingRights == null) ? 0 : this._castlingRights.hashCode());
        result = prime * result + ((this._enPassantSquare == null) ? 0 : this._enPassantSquare.hashCode());
        result = prime * result + this._halfMoveClock;
        result = prime * result + this._halfMoveNumber;
        result = prime * result + ((this._nextPlayer == null) ? 0 : this._nextPlayer.hashCode());
        result = prime * result + Arrays.hashCode(this._x88Board);
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof OmegaBoardPosition)) { return false; }
        OmegaBoardPosition other = (OmegaBoardPosition) obj;
        if (this._castlingRights == null) {
            if (other._castlingRights != null) { return false; }
        } else if (!this._castlingRights.equals(other._castlingRights)) { return false; }
        if (this._enPassantSquare != other._enPassantSquare) { return false; }
        if (this._halfMoveClock != other._halfMoveClock) { return false; }
        if (this._halfMoveNumber != other._halfMoveNumber) { return false; }
        if (this._nextPlayer != other._nextPlayer) { return false; }
        if (!Arrays.equals(this._x88Board, other._x88Board)) { return false; }
        return true;
    }

}
