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
import java.util.Random;

import org.omg.CORBA._PolicyStub;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameColor;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePosition;
import fko.chessly.player.computer.Omega.OmegaSquare.File;

/**
 * @author Frank
 */
public class OmegaBoardPosition {

    /* Size of 0x88 board */
    private static final int BOARDSIZE = 128;

    /* Max History */
    private static final int MAX_HISTORY = 255;

    /* Standard Board Setup as FEN */
    private final static String STANDARD_BOARD_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /* random generator for use with zobrist hash keys */
    private static final Random random = new Random(0);

    /*
     * The zobrist key to use as a hash key in transposition tables
     * The zobrist key will be updated incrementally every time one of the the state variables change.
     */
    long _zobristKey=0;
    long[] _zobristKey_History = new long[MAX_HISTORY];

    // history counter
    int _historyCounter = 0;

    // **********************************************************
    // Board State START ----------------------------------------
    // unique chess position
    //
    // 0x88 Board
    OmegaPiece[] _x88Board = new OmegaPiece[BOARDSIZE];
    // we can recreate the board through the last move - no need for history of board itself
    int[] _moveHistory = new int[MAX_HISTORY];
    // hash for pieces - piece, board
    static final long[][] _piece_Zobrist = new long[OmegaPiece.values().length][OmegaSquare.values().length];

    // Castling rights
    EnumSet<OmegaCastling> _castlingRights = EnumSet.allOf(OmegaCastling.class);
    @SuppressWarnings("unchecked")
    EnumSet<OmegaCastling>[] _castlingRights_History = new EnumSet[MAX_HISTORY];
    // hash for castling rights
    static final long[] _castlingRights_Zobrist = new long[OmegaCastling.values().length*OmegaCastling.values().length];

    // en passant field - if NOSQUARE then we do not have an en passant option
    OmegaSquare _enPassantSquare = OmegaSquare.NOSQUARE;
    OmegaSquare[] _enPassantSquare_History = new OmegaSquare[MAX_HISTORY];
    // hash for castling rights
    static final long[] _enPassantSquare_Zobrist = new long[OmegaSquare.values().length];

    // half move clock - number of half moves since last capture
    int _halfMoveClock = 0;
    int[] _halfMoveClock_History = new int[MAX_HISTORY];
    // has no zobrist key

    // next player color
    OmegaColor _nextPlayer = OmegaColor.WHITE;
    // hash for castling rights
    static final long _nextPlayer_Zobrist;
    //
    // Board State END ------------------------------------------
    // **********************************************************

    // **********************************************************
    // Extended Board State ----------------------------------
    // not necessary for a unique position
    //
    // half move number - the actual half move number to determine the full move number
    int _nextHalfMoveNumber = 1;

    /**
     * Lists for all pieces
     */
    final EnumSet<OmegaSquare>[] _pawnSquares = new EnumSet[OmegaColor.values.length];
    final EnumSet<OmegaSquare>[] _knightSquares = new EnumSet[OmegaColor.values.length];
    final EnumSet<OmegaSquare>[] _bishopSquares = new EnumSet[OmegaColor.values.length];
    final EnumSet<OmegaSquare>[] _rookSquares = new EnumSet[OmegaColor.values.length];
    final EnumSet<OmegaSquare>[] _queenSquares = new EnumSet[OmegaColor.values.length];
    final EnumSet<OmegaSquare>[] _kingSquares = new EnumSet[OmegaColor.values.length];

    // Material value will always be up to date
    int[] _material;



    // **********************************************************
    // static initialization
    static {
        // all pieces on all squares
        for (OmegaPiece p : OmegaPiece.values()) {
            for (OmegaSquare s : OmegaSquare.values()) {
                _piece_Zobrist[p.ordinal()][s.ordinal()] = Math.abs(random.nextLong());
            }
        }
        // all castling combinations
        for (EnumSet<OmegaCastling> es : OmegaCastling.getCombinationList()) {
            _castlingRights_Zobrist[OmegaCastling.getCombinationList().indexOf(es)] = Math.abs(random.nextLong());
        }
        // all possible positions of the en passant square (easiest to use all fields and not just the
        // ones where en passant is indeed possible)
        for (OmegaSquare s : OmegaSquare.values()) {
            _enPassantSquare_Zobrist[s.ordinal()] = Math.abs(random.nextLong());
        }
        // set or unset this for the two color options
        _nextPlayer_Zobrist = Math.abs(random.nextLong());
    }

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
        initializeLists();
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
        this._nextHalfMoveNumber = op._nextHalfMoveNumber;
        this._nextPlayer = op._nextPlayer;
        this._zobristKey = op._zobristKey;

        initializeLists();
        // copy piece lists
        for (int i=0; i<=1; i++) { // foreach color
            this._pawnSquares[i] = op._pawnSquares[i];
            this._knightSquares[i] = op._knightSquares[i];
            this._bishopSquares[i] = op._bishopSquares[i];
            this._rookSquares[i] = op._rookSquares[i];
            this._queenSquares[i] = op._queenSquares[i];
            this._kingSquares[i] = op._kingSquares[i];
        }
        this._material[0] = op._material[0];
        this._material[1] = op._material[1];

    }

    /**
     * Copy constructor from GameBoard - creates a equivalent OmegaBoardPosition
     * from the give GameBoard
     * @param oldBoard
     */
    public OmegaBoardPosition(GameBoard oldBoard) {
        if (oldBoard == null)
            throw new NullPointerException("Parameter oldBoard may not be null");

        initializeLists();

        // fill board with NOPIECE
        Arrays.fill(_x88Board,  OmegaPiece.NOPIECE);

        // -- fields --
        for (int file = 1; file <= 8; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                // we can't do an arraycopy here as we do not know the
                // Implementation of the old board
                GamePiece gp = oldBoard.getPiece(file, rank) == null ? null
                        : (GamePiece) oldBoard.getPiece(file, rank).clone();
                OmegaPiece op = OmegaPiece.convertFromGamePiece(gp);
                if (op != OmegaPiece.NOPIECE) putPiece(OmegaSquare.getSquare(file, rank), op);
            }
        }

        // next player
        this._nextPlayer = OmegaColor.convertFromGameColor(oldBoard.getNextPlayerColor());
        if (_nextPlayer == OmegaColor.BLACK) {
            _zobristKey ^= _nextPlayer_Zobrist; // only when black to have the right in/out rhythm
        }
        this._halfMoveClock = oldBoard.getHalfmoveClock();
        this._nextHalfMoveNumber = oldBoard.getNextHalfMoveNumber();

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
        _zobristKey ^= _castlingRights_Zobrist[OmegaCastling.getCombinationIndex(_castlingRights)];

        // en passant
        this._enPassantSquare = OmegaSquare.convertFromGamePosition(oldBoard.getEnPassantCapturable());
        if (_enPassantSquare!=OmegaSquare.NOSQUARE) {
            _zobristKey ^= _enPassantSquare_Zobrist[_enPassantSquare.ordinal()]; // in
        }

    }

    /**
     *
     */
    private void initializeLists() {
        for (int i=0; i<=1; i++) { // foreach color
            _pawnSquares[i] = EnumSet.noneOf(OmegaSquare.class);
            _knightSquares[i] = EnumSet.noneOf(OmegaSquare.class);
            _bishopSquares[i] = EnumSet.noneOf(OmegaSquare.class);
            _rookSquares[i] = EnumSet.noneOf(OmegaSquare.class);
            _queenSquares[i] = EnumSet.noneOf(OmegaSquare.class);
            _kingSquares[i] = EnumSet.noneOf(OmegaSquare.class);
        }
        _material = new int[2];
    }

    /**
     * Commits a move to the board.
     * Due to performance there is no check if this move is legal
     * on the current board. Legal check needs to be done beforehand.
     * Usually the move will be generated by our MoveGenerator and therefore
     * the move will be assumed legal anyway.
     *
     * @param move the move
     */
    public void makeMove(int move) {
        assert (move != OmegaMove.NOMOVE);

        OmegaSquare fromSquare = OmegaMove.getStart(move); assert fromSquare.isValidSquare();
        OmegaSquare toSquare   = OmegaMove.getEnd(move); assert toSquare.isValidSquare();
        OmegaPiece  piece      = OmegaMove.getPiece(move); assert piece != OmegaPiece.NOPIECE;
        OmegaPiece  target     = OmegaMove.getTarget(move);
        OmegaPiece  promotion  = OmegaMove.getPromotion(move);

        // Save state for undoMove
        _moveHistory[_historyCounter] = move;
        _castlingRights_History[_historyCounter] = _castlingRights.clone();
        _enPassantSquare_History[_historyCounter] = _enPassantSquare;
        _halfMoveClock_History[_historyCounter] = _halfMoveClock;
        _zobristKey_History[_historyCounter] = _zobristKey;
        _historyCounter++;

        // make move
        switch (OmegaMove.getMoveType(move)) {
            case NORMAL:
                makeNormalMove(fromSquare, toSquare, piece, target);
                break;
            case PAWNDOUBLE:
                assert fromSquare.isPawnBaseRow(piece.getColor());
                assert !piece.getColor().isNone();
                movePiece(fromSquare, toSquare, piece);
                // set en passant target field - always one "behind" the toSquare
                _enPassantSquare = piece.getColor().isWhite() ? toSquare.getSouth() : toSquare.getNorth();
                if (_enPassantSquare!=OmegaSquare.NOSQUARE) {
                    _zobristKey ^= _enPassantSquare_Zobrist[_enPassantSquare.ordinal()]; // in
                }
                _halfMoveClock = 0; // reset half move clock because of pawn move
                break;
            case ENPASSANT:
                assert target != OmegaPiece.NOPIECE;
                assert target.getType() == OmegaPieceType.PAWN;
                assert !target.getColor().isNone();
                OmegaSquare targetSquare = target.getColor().isWhite() ? toSquare.getNorth() : toSquare.getSouth();
                removePiece(targetSquare, target);
                movePiece(fromSquare, toSquare, piece);
                // clear en passant
                if (_enPassantSquare!=OmegaSquare.NOSQUARE) {
                    _zobristKey ^= _enPassantSquare_Zobrist[_enPassantSquare.ordinal()]; // out
                    _enPassantSquare = OmegaSquare.NOSQUARE;
                }
                _halfMoveClock = 0; // reset half move clock because of pawn move
                break;
            case CASTLING:
                makeCastlingMove(fromSquare, toSquare, piece);
                // clear en passant
                if (_enPassantSquare!=OmegaSquare.NOSQUARE) {
                    _zobristKey ^= _enPassantSquare_Zobrist[_enPassantSquare.ordinal()]; // out
                    _enPassantSquare = OmegaSquare.NOSQUARE;
                }
                _halfMoveClock++;
                break;
            case PROMOTION:
                if (target != OmegaPiece.NOPIECE) removePiece(toSquare, target);
                removePiece(fromSquare, piece);
                putPiece(toSquare, promotion);
                // clear en passant
                if (_enPassantSquare!=OmegaSquare.NOSQUARE) {
                    _zobristKey ^= _enPassantSquare_Zobrist[_enPassantSquare.ordinal()]; // out
                    _enPassantSquare = OmegaSquare.NOSQUARE;
                }
                _halfMoveClock = 0; // reset half move clock because of pawn move
                break;
            case NOMOVETYPE:
            default:
                throw new IllegalArgumentException();
        }

        // update halfMoveNumber
        _nextHalfMoveNumber++;

        // change color (active player)
        _nextPlayer = _nextPlayer.getInverseColor();
        _zobristKey ^= _nextPlayer_Zobrist;

    }

    /**
     * Takes back the last move from the board
     */
    public void undoMove() {
        // Get state for undoMove
        _historyCounter--;

        int move = _moveHistory[_historyCounter];

        // undo piece move / restore board
        OmegaSquare fromSquare = OmegaMove.getStart(move); assert fromSquare.isValidSquare();
        OmegaSquare toSquare   = OmegaMove.getEnd(move); assert toSquare.isValidSquare();
        OmegaPiece  piece      = OmegaMove.getPiece(move); assert piece != OmegaPiece.NOPIECE;
        OmegaPiece  target     = OmegaMove.getTarget(move);
        OmegaPiece  promotion  = OmegaMove.getPromotion(move);

        switch (OmegaMove.getMoveType(move)) {
            case NORMAL:
                movePiece(toSquare, fromSquare, piece);
                if (target != OmegaPiece.NOPIECE) {
                    putPiece(toSquare, target);
                }
                break;
            case PAWNDOUBLE:
                movePiece(toSquare, fromSquare, piece);
                // set en passant target field - always one "behind" the toSquare
                break;
            case ENPASSANT:
                OmegaSquare targetSquare = target.getColor().isWhite() ? toSquare.getNorth() : toSquare.getSouth();
                movePiece(toSquare, fromSquare, piece);
                putPiece(targetSquare, target);
                break;
            case CASTLING:
                undoCastlingMove(fromSquare, toSquare, piece);
                break;
            case PROMOTION:
                removePiece(toSquare, promotion);
                putPiece(fromSquare, piece);
                if (target != OmegaPiece.NOPIECE) putPiece(toSquare, target);
                break;
            case NOMOVETYPE:
            default:
                throw new IllegalArgumentException();
        }

        // restore castling rights
        _castlingRights = _castlingRights_History[_historyCounter];

        // restore en passant square
        _enPassantSquare = _enPassantSquare_History[_historyCounter];

        // restore halfMoveClock
        _halfMoveClock = _halfMoveClock_History[_historyCounter];

        // decrease _halfMoveNumber
        _nextHalfMoveNumber--;

        // change back color
        _nextPlayer = _nextPlayer.getInverseColor();

        // zobristKey - just overwrite - should be the same as before the move
        _zobristKey = _zobristKey_History[_historyCounter];

    }

    /**
     * @param fromSquare
     * @param toSquare
     * @param piece
     * @param target
     */
    private void makeNormalMove(OmegaSquare fromSquare, OmegaSquare toSquare, OmegaPiece piece, OmegaPiece target) {
        // _take out castling rights from zobrist to set new later
        _zobristKey ^= _castlingRights_Zobrist[OmegaCastling.getCombinationIndex(_castlingRights)]; // out
        // check for castling rights invalidation
        switch (fromSquare) {
            case e1: // white king
                _castlingRights.remove(OmegaCastling.WHITE_KINGSIDE);
                _castlingRights.remove(OmegaCastling.WHITE_QUEENSIDE);
                break;
            case e8: // black king
                _castlingRights.remove(OmegaCastling.BLACK_KINGSIDE);
                _castlingRights.remove(OmegaCastling.BLACK_QUEENSIDE);
                break;
            case a1: // rook a1
                _castlingRights.remove(OmegaCastling.WHITE_QUEENSIDE);
                break;
            case h1: // rook h1
                _castlingRights.remove(OmegaCastling.WHITE_KINGSIDE);
                break;
            case a8: // rook a8
                _castlingRights.remove(OmegaCastling.BLACK_QUEENSIDE);
                break;
            case h8: // rook h8
                _castlingRights.remove(OmegaCastling.BLACK_KINGSIDE);
                break;
            default:
                break;
        }
        switch (toSquare) {
            case e1: // white king
                assert false; // king capture should not happen
                _castlingRights.remove(OmegaCastling.WHITE_KINGSIDE);
                _castlingRights.remove(OmegaCastling.WHITE_QUEENSIDE);
                break;
            case e8: // black king
                assert false; // king capture should not happen
                _castlingRights.remove(OmegaCastling.BLACK_KINGSIDE);
                _castlingRights.remove(OmegaCastling.BLACK_QUEENSIDE);
                break;
            case a1: // rook a1
                _castlingRights.remove(OmegaCastling.WHITE_QUEENSIDE);
                break;
            case h1: // rook h1
                _castlingRights.remove(OmegaCastling.WHITE_KINGSIDE);
                break;
            case a8: // rook a8
                _castlingRights.remove(OmegaCastling.BLACK_QUEENSIDE);
                break;
            case h8: // rook h8
                _castlingRights.remove(OmegaCastling.BLACK_KINGSIDE);
                break;
            default:
                break;
        }
        _zobristKey ^= _castlingRights_Zobrist[OmegaCastling.getCombinationIndex(_castlingRights)]; // in

        if (target != OmegaPiece.NOPIECE) {
            removePiece(toSquare, target);
            _halfMoveClock = 0; // reset half move clock because of capture
        }  else if (piece.getType() == OmegaPieceType.PAWN) {
            _halfMoveClock = 0; // reset half move clock because of pawn move
        } else {
            _halfMoveClock++;
        }
        movePiece(fromSquare, toSquare, piece);
        // clear en passant
        if (_enPassantSquare != OmegaSquare.NOSQUARE) {
            _zobristKey ^= _enPassantSquare_Zobrist[_enPassantSquare.ordinal()]; // out
            _enPassantSquare = OmegaSquare.NOSQUARE;
        }

    }

    /**
     * @param fromSquare
     * @param toSquare
     * @param piece
     */
    private void makeCastlingMove(OmegaSquare fromSquare, OmegaSquare toSquare, OmegaPiece piece) {
        assert piece.getType()==OmegaPieceType.KING;
        // update castling rights
        OmegaPiece  rook = OmegaPiece.NOPIECE;
        OmegaSquare rookFromSquare = OmegaSquare.NOSQUARE;
        OmegaSquare rookToSquare = OmegaSquare.NOSQUARE;
        // _take out castling rights from zobrist to set new later
        _zobristKey ^= _castlingRights_Zobrist[OmegaCastling.getCombinationIndex(_castlingRights)]; // out
        switch (toSquare) {
            case g1: // white kingside
                rook = OmegaPiece.WHITE_ROOK;
                rookFromSquare = OmegaSquare.h1;
                rookToSquare = OmegaSquare.f1;
                _castlingRights.remove(OmegaCastling.WHITE_KINGSIDE);
                _castlingRights.remove(OmegaCastling.WHITE_QUEENSIDE);
                break;
            case c1: // white queenside
                rook = OmegaPiece.WHITE_ROOK;
                rookFromSquare = OmegaSquare.a1;
                rookToSquare = OmegaSquare.d1;
                _castlingRights.remove(OmegaCastling.WHITE_KINGSIDE);
                _castlingRights.remove(OmegaCastling.WHITE_QUEENSIDE);
                break;
            case g8: // black kingside
                rook = OmegaPiece.BLACK_ROOK;
                rookFromSquare = OmegaSquare.h8;
                rookToSquare = OmegaSquare.f8;
                _castlingRights.remove(OmegaCastling.BLACK_KINGSIDE);
                _castlingRights.remove(OmegaCastling.BLACK_QUEENSIDE);
                break;
            case c8: // black queenside
                rook = OmegaPiece.BLACK_ROOK;
                rookFromSquare = OmegaSquare.a8;
                rookToSquare = OmegaSquare.d8;
                _castlingRights.remove(OmegaCastling.BLACK_KINGSIDE);
                _castlingRights.remove(OmegaCastling.BLACK_QUEENSIDE);
                break;
            default:
                throw new IllegalArgumentException("Castling to wrong square "+toSquare.toString());
        }
        _zobristKey ^= _castlingRights_Zobrist[OmegaCastling.getCombinationIndex(_castlingRights)]; // in
        // King
        movePiece(fromSquare, toSquare, piece);
        // Rook
        movePiece(rookFromSquare, rookToSquare, rook);
    }

    /**
     * @param fromSquare
     * @param toSquare
     * @param piece
     */
    private void undoCastlingMove(OmegaSquare fromSquare, OmegaSquare toSquare, OmegaPiece piece) {
        // update castling rights
        OmegaPiece  rook = OmegaPiece.NOPIECE;
        OmegaSquare rookFromSquare = OmegaSquare.NOSQUARE;
        OmegaSquare rookToSquare = OmegaSquare.NOSQUARE;
        // _take out castling rights from zobrist to set new later
        _zobristKey ^= _castlingRights_Zobrist[OmegaCastling.getCombinationIndex(_castlingRights)]; // out
        switch (toSquare) {
            case g1: // white kingside
                rook = OmegaPiece.WHITE_ROOK;
                rookFromSquare = OmegaSquare.h1;
                rookToSquare = OmegaSquare.f1;
                break;
            case c1: // white queenside
                rook = OmegaPiece.WHITE_ROOK;
                rookFromSquare = OmegaSquare.a1;
                rookToSquare = OmegaSquare.d1;
                break;
            case g8: // black kingside
                rook = OmegaPiece.BLACK_ROOK;
                rookFromSquare = OmegaSquare.h8;
                rookToSquare = OmegaSquare.f8;
                break;
            case c8: // black queenside
                rook = OmegaPiece.BLACK_ROOK;
                rookFromSquare = OmegaSquare.a8;
                rookToSquare = OmegaSquare.d8;
                break;
            default:
                throw new IllegalArgumentException("Castling to wrong square "+toSquare.toString());
        }
        _zobristKey ^= _castlingRights_Zobrist[OmegaCastling.getCombinationIndex(_castlingRights)]; // in
        // King
        movePiece(toSquare, fromSquare, piece);
        // Rook
        movePiece(rookToSquare, rookFromSquare, rook);
    }

    /**
     * @param fromSquare
     * @param toSquare
     * @param piece
     */
    private void movePiece(OmegaSquare fromSquare, OmegaSquare toSquare, OmegaPiece piece) {
        assert fromSquare.isValidSquare();
        assert toSquare.isValidSquare();
        assert piece!=OmegaPiece.NOPIECE;
        assert _x88Board[fromSquare.ordinal()] == piece; // check if moved piece is indeed there
        assert _x88Board[toSquare.ordinal()] == OmegaPiece.NOPIECE; // should be empty
        // due to performance we do not call remove and put
        // no need to update counters when moving
        // remove
        _x88Board[fromSquare.ordinal()] = OmegaPiece.NOPIECE;
        _zobristKey ^= _piece_Zobrist[piece.ordinal()][fromSquare.ordinal()]; // out
        // update piece lists
        final int color = piece.getColor().ordinal();
        removeFromPieceLists(fromSquare, piece, color);
        // put
        _x88Board[toSquare.ordinal()] = piece;
        _zobristKey ^= _piece_Zobrist[piece.ordinal()][toSquare.ordinal()]; // in
        // update piece lists
        addToPieceLists(toSquare, piece, color);

    }

    /**
     * @param square
     * @param piece
     */
    private void putPiece(OmegaSquare square, OmegaPiece piece) {
        assert square.isValidSquare();
        assert piece!=OmegaPiece.NOPIECE;
        assert _x88Board[square.ordinal()] == OmegaPiece.NOPIECE; // should be empty
        // put
        _x88Board[square.ordinal()] = piece;
        _zobristKey ^= _piece_Zobrist[piece.ordinal()][square.ordinal()]; // in
        // update piece lists
        final int color = piece.getColor().ordinal();
        addToPieceLists(square, piece, color);
        // update material
        _material[color] += piece.getType().getValue();
    }

    /**
     * @param file
     * @param rank
     * @return the removed piece
     */
    private OmegaPiece removePiece(OmegaSquare square, OmegaPiece piece) {
        assert square.isValidSquare();
        assert piece!=OmegaPiece.NOPIECE;
        assert _x88Board[square.ordinal()] == piece; // check if removed piece is indeed there
        // remove
        OmegaPiece old = _x88Board[square.ordinal()];
        _x88Board[square.ordinal()] = OmegaPiece.NOPIECE;
        _zobristKey ^= _piece_Zobrist[piece.ordinal()][square.ordinal()]; // out
        // update piece lists
        final int color = piece.getColor().ordinal();
        removeFromPieceLists(square, piece, color);
        // update material
        _material[color] -= piece.getType().getValue();
        // return the remove piece
        return old;
    }

    /**
     * @param toSquare
     * @param piece
     * @param color
     */
    private void addToPieceLists(OmegaSquare toSquare, OmegaPiece piece, final int color) {
        switch(piece.getType()) {
            case PAWN: _pawnSquares[color].add(toSquare); break;
            case KNIGHT: _knightSquares[color].add(toSquare); break;
            case BISHOP: _bishopSquares[color].add(toSquare); break;
            case ROOK: _rookSquares[color].add(toSquare); break;
            case QUEEN: _queenSquares[color].add(toSquare); break;
            case KING: _kingSquares[color].add(toSquare); break;
            default:
                break;
        }
    }

    /**
     * @param fromSquare
     * @param piece
     * @param color
     */
    private void removeFromPieceLists(OmegaSquare fromSquare, OmegaPiece piece, final int color) {
        switch(piece.getType()) {
            case PAWN: _pawnSquares[color].remove(fromSquare); break;
            case KNIGHT: _knightSquares[color].remove(fromSquare); break;
            case BISHOP: _bishopSquares[color].remove(fromSquare); break;
            case ROOK: _rookSquares[color].remove(fromSquare); break;
            case QUEEN: _queenSquares[color].remove(fromSquare); break;
            case KING: _kingSquares[color].remove(fromSquare); break;
            default:
                break;
        }
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
        assert _zobristKey==0;

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
                        case "p": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.BLACK_PAWN); break;
                        case "n": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.BLACK_KNIGHT); break;
                        case "b": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.BLACK_BISHOP); break;
                        case "r": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.BLACK_ROOK); break;
                        case "q": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.BLACK_QUEEN); break;
                        case "k": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.BLACK_KING); break;
                        default:
                            throw new IllegalArgumentException("FEN Syntax not valid - expected a-hA-H");
                    }
                } else if (s.toUpperCase() == s) { // white
                    switch (s) {
                        case "P": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.WHITE_PAWN); break;
                        case "N": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.WHITE_KNIGHT); break;
                        case "B": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.WHITE_BISHOP); break;
                        case "R": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.WHITE_ROOK); break;
                        case "Q": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.WHITE_QUEEN); break;
                        case "K": putPiece(OmegaSquare.getSquare(file, rank), OmegaPiece.WHITE_KING); break;
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
            else if (s.equals("b")) {
                _nextPlayer = OmegaColor.BLACK;
                _zobristKey ^= _nextPlayer_Zobrist; // only when black to have the right in/out rhythm
            }
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
        _zobristKey ^= _castlingRights_Zobrist[OmegaCastling.getCombinationIndex(_castlingRights)];

        // en passant - which filed and if null no en passant option
        if (parts.length >= 4) { // default "-"
            s = parts[3];
            if (!s.equals("-")) {
                GamePosition enPassantCapturePosition = GamePosition.getGamePosition(s);
                _enPassantSquare = OmegaSquare.convertFromGamePosition(enPassantCapturePosition);
            }
        }
        // set en passant if not NOSQUARE
        if (_enPassantSquare!=OmegaSquare.NOSQUARE) {
            _zobristKey ^= _enPassantSquare_Zobrist[_enPassantSquare.ordinal()]; // in
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
            _nextHalfMoveNumber = (2 * Integer.parseInt(s));
        } else {
            _nextHalfMoveNumber = 2 ;
        }
        if (_nextPlayer.isWhite()) _nextHalfMoveNumber--;

        // double check correct numbering
        assert ((_nextPlayer.isWhite() && _nextHalfMoveNumber %2 == 1)
                || _nextPlayer.isBlack() && _nextHalfMoveNumber %2 == 0);

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
        fen += (_nextHalfMoveNumber+1)/2;

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
        result = prime * result + this._nextHalfMoveNumber;
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
        if (this._zobristKey != other._zobristKey) { return false; }
        /* these should be covered by the zobrist key
        if (this._castlingRights == null) {
            if (other._castlingRights != null) { return false; }
        } else if (!this._castlingRights.equals(other._castlingRights)) { return false; }
        if (this._enPassantSquare != other._enPassantSquare) { return false; }
        if (this._nextPlayer != other._nextPlayer) { return false; }
        if (!Arrays.equals(this._x88Board, other._x88Board)) { return false; }
         */
        if (this._halfMoveClock != other._halfMoveClock) { return false; }
        if (this._nextHalfMoveNumber != other._nextHalfMoveNumber) { return false; }
        return true;
    }

    /**
     * @return the zobristKey
     */
    public long getZobristKey() {
        return this._zobristKey;
    }

    /**
     * @param c OmegaColor
     * @return the material value
     */
    public int getMaterial(OmegaColor c) {
        return this._material[c.ordinal()];
    }

    /**
     * @return color of next player
     */
    public OmegaColor getNextPlayer() {
        return _nextPlayer;
    }

    /**
     * @return true if current position has check for next player
     */
    public boolean hasCheck() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * This checks if a certain square is currently under attack by the player of the
     * given color. It does not matter who has the next move on this position.
     * It also is checking if the actual attack can be done as a legal move. E.g. a
     * pinned piece could not actually make a capture on the square.
     *
     * @param color
     * @param omegaSquare
     * @return true if under attack
     */
    public boolean isAttacked(OmegaColor color, OmegaSquare omegaSquare) {
        assert (omegaSquare != OmegaSquare.NOSQUARE);
        assert (!color.isNone());

        final int os_Index = omegaSquare.ordinal();
        final boolean isWhite = color.isWhite();

        /*
         * Checks are ordered for likelihood to return from this as fast as possible
         */

        // check pawns
        // reverse direction to look for pawns which could attack
        final int pawnDir = isWhite ? -1 : 1;
        final OmegaPiece attackerPawn = isWhite ? OmegaPiece.WHITE_PAWN : OmegaPiece.BLACK_PAWN;
        for (int d : OmegaSquare.pawnAttackDirections) {
            final int i = os_Index+d*pawnDir;
            if ((i & 0x88) == 0 && _x88Board[i] == attackerPawn) return true;
        }

        // check sliding horizontal (rook + queen) if there are any
        if (!(_rookSquares[color.ordinal()].isEmpty() && _queenSquares[color.ordinal()].isEmpty())) {
            for (int d : OmegaSquare.rookDirections) {
                int i = os_Index+d;
                while ((i & 0x88) == 0) { // slide while valid square
                    if (_x88Board[i] != OmegaPiece.NOPIECE // not empty
                            && _x88Board[i].getColor() == color // attacker piece
                            && (_x88Board[i].getType() == OmegaPieceType.ROOK || _x88Board[i].getType() == OmegaPieceType.QUEEN)
                            ) {
                        return true;
                    }
                    i += d; // next sliding field in this direction
                }
            }
        }

        // check sliding diagonal (bishop + queen) if there are any
        if (!(_bishopSquares[color.ordinal()].isEmpty() && _queenSquares[color.ordinal()].isEmpty())) {
            for (int d : OmegaSquare.bishopDirections) {
                int i = os_Index+d;
                while ((i & 0x88) == 0) { // slide while valid square
                    if (_x88Board[i] != OmegaPiece.NOPIECE // not empty
                            && _x88Board[i].getColor() == color // attacker piece
                            && (_x88Board[i].getType() == OmegaPieceType.BISHOP || _x88Board[i].getType() == OmegaPieceType.QUEEN)
                            ) {
                        return true;
                    }
                    i += d; // next sliding field in this direction
                }
            }
        }

        // check knights if there are any
        if (!(_knightSquares[color.ordinal()].isEmpty())) {
            for (int d : OmegaSquare.knightDirections) {
                int i = os_Index+d;
                if ((i & 0x88) == 0) { // valid square
                    if (_x88Board[i] != OmegaPiece.NOPIECE // not empty
                            && _x88Board[i].getColor() == color // attacker piece
                            && (_x88Board[i].getType() == OmegaPieceType.KNIGHT)
                            ) {
                        return true;
                    }
                }
            }
        }

        // check king
        for (int d : OmegaSquare.kingDirections) {
            int i = os_Index+d;
            if ((i & 0x88) == 0) { // valid square
                if (_x88Board[i] != OmegaPiece.NOPIECE // not empty
                        && _x88Board[i].getColor() == color // attacker piece
                        && (_x88Board[i].getType() == OmegaPieceType.KING)
                        ) {
                    return true;
                }
            }
        }

        // check en passant
        if (this._enPassantSquare != OmegaSquare.NOSQUARE){
            if (isWhite // white is attacker
                    && _x88Board[_enPassantSquare.getSouth().ordinal()] == OmegaPiece.BLACK_PAWN // black is target
                    && this._enPassantSquare.getSouth() == omegaSquare) { //this is indeed the en passant attacked square
                // left
                int i = os_Index + OmegaSquare.W;
                if ((i & 0x88) == 0 && _x88Board[i] == OmegaPiece.WHITE_PAWN) return true;
                // right
                i = os_Index + OmegaSquare.E;
                if ((i & 0x88) == 0 && _x88Board[i] == OmegaPiece.WHITE_PAWN) return true;
            }
            else if (!isWhite // black is attacker (assume not noColor)
                    && _x88Board[_enPassantSquare.getNorth().ordinal()] == OmegaPiece.WHITE_PAWN // white is target
                    && this._enPassantSquare.getNorth() == omegaSquare) { //this is indeed the en passant attacked square
                // attack from left
                int i = os_Index + OmegaSquare.W;
                if ((i & 0x88) == 0 && _x88Board[i] == OmegaPiece.BLACK_PAWN) return true;
                // attack from right
                i = os_Index + OmegaSquare.E;
                if ((i & 0x88) == 0 && _x88Board[i] == OmegaPiece.BLACK_PAWN) return true;
            }
        }

        return false;

    }
}
