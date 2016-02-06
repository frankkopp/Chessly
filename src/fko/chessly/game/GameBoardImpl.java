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
package fko.chessly.game;

import java.util.Arrays;

import fko.chessly.game.pieces.Bishop;
import fko.chessly.game.pieces.King;
import fko.chessly.game.pieces.Knight;
import fko.chessly.game.pieces.Pawn;
import fko.chessly.game.pieces.Queen;
import fko.chessly.game.pieces.Rook;

/**
 * The board class has all the information regarding an actual current Chessly
 * board like stones the fields, who is next player, etc..<br/>
 * It also knows if castle is still possible and knows about possible en
 * passant. It also is responsible to generate regular moves according to the
 * Chessly rules and actually execute moves on the given board.
 * <p/>
 * This board returns a String hash key only based on the fields and the next
 * player of the board.
 * <p/>
 * Outside this class fields are addressed starting from 1 to 8<br/>
 * Within this class fields are addressed starting with 0 to 7<br/>
 *
 * This class is not build for speed but for object orientation. For good/fast
 * engines there should be seperate implementations.
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class GameBoardImpl implements GameBoard, Cloneable {

    // Constants -------------------------------------------
    private static final int DIM = 8;

    /** the direction a color move in rows */
    public static final int BLACK_DIRECTION = -1;
    /** the direction a color move in rows */
    public static final int WHITE_DIRECTION = 1;

    /** base line for colors (1..8) */
    public static final int BLACK_BASE_ROW = 8;
    /** base line for colors (1..8) */
    public static final int WHITE_BASE_ROW = 1;

    // Board State START ----------------------------------------

    /**
     * actual array of arrays of fields
     */
    protected GamePiece[][] _fields;

    /**
     * is castling still allowed
     */
    protected GameCastling[] _castlingRights = new GameCastling[4];

    /**
     * save en passant right and position of capturable pawn for one move
     */
    protected GamePosition _enPassantCapturable = null;

    /**
     * count the numbers of halfmoves since the last capture
     */
    protected int _halfmoveClock = 0;

    protected int _halfMoveNumber = 0;

    // Board State END -----------------------------------------

    // Extended Board State ----------------------------------
    // not necessary for a unique position

    /**
     * Memorizes the move path
     */
    protected GameMoveList _moveHistory = null;

    /**
     * last move put the next player into check
     */
    protected boolean _hasCheck = false;
    protected boolean _hasCheckMate = false;
    protected boolean _hasStaleMate = false;

    /**
     * keep track of the kings to quickly check for check
     */
    protected GamePosition _whiteKingField = null;
    protected GamePosition _blackKingField = null;

    /**
     * move list cache - due to multiple calls to getMoves
     */
    protected GameMoveList _moveListCache = null;
    protected boolean _moveListCacheValid = false;

    // Constructors START -----------------------------------------

    /**
     * Creates a standard Chessly board and initialises it with standard chess
     * setup.
     */
    public GameBoardImpl() {
        initBoard(NotationHelper.StandardBoardFEN);
    }

    /**
     * Creates a standard Chessly board and initialises it with a fen position
     * @param fen
     */
    public GameBoardImpl(String fen) {
        initBoard(fen);
    }

    /**
     * Creates a new Chessly board as a exact deep copy of the given BordImpl
     * board. Expensive!
     *
     * @param oldBoard
     */
    public GameBoardImpl(GameBoardImpl oldBoard) {

        if (oldBoard == null) {
            throw new NullPointerException("Parameter oldBoard may not be null");
        }

        _fields = new GamePiece[DIM][DIM];
        _moveListCacheValid = false;

        // -- copy fields --
        for (int col = 0; col < DIM; col++) {
            System.arraycopy(oldBoard._fields[col], 0, _fields[col], 0, DIM);
        }

        // -- copy move history --
        this._moveHistory = oldBoard.getMoveHistory();
        this._halfMoveNumber = oldBoard._halfMoveNumber;

        // -- copy castling flags
        if (oldBoard.isCastlingKingSideAllowed(GameColor.WHITE)) {
            _castlingRights[GameCastling.WHITE_KINGSIDE.ordinal()] = GameCastling.WHITE_KINGSIDE;
        } else {
            removeKingSideCastling(GameColor.WHITE);
        }
        if (oldBoard.isCastlingQueenSideAllowed(GameColor.WHITE)) {
            _castlingRights[GameCastling.WHITE_QUEENSIDE.ordinal()] = GameCastling.WHITE_QUEENSIDE;
        } else {
            removeQueenSideCastling(GameColor.WHITE);
        }
        if (oldBoard.isCastlingKingSideAllowed(GameColor.BLACK)) {
            _castlingRights[GameCastling.BLACK_KINGSIDE.ordinal()] = GameCastling.BLACK_KINGSIDE;
        } else {
            removeKingSideCastling(GameColor.BLACK);
        }
        if (oldBoard.isCastlingQueenSideAllowed(GameColor.BLACK)) {
            _castlingRights[GameCastling.BLACK_QUEENSIDE.ordinal()] = GameCastling.BLACK_QUEENSIDE;
        } else {
            removeQueenSideCastling(GameColor.BLACK);
        }

        // en passant flag
        this._enPassantCapturable = oldBoard.getEnPassantCapturable();

        // check flag
        this._hasCheck = oldBoard._hasCheck;
        this._hasCheckMate = oldBoard._hasCheckMate;
        this._hasStaleMate = oldBoard._hasStaleMate;

        // king positions
        this._whiteKingField = oldBoard._whiteKingField;
        this._blackKingField = oldBoard._blackKingField;

        // halfmove clock
        this._halfmoveClock = oldBoard._halfmoveClock;
        _moveListCacheValid = false;
    }

    /**
     * Creates a new Chessly board as a exact deep copy of the given BordImpl
     * board
     *
     * @param oldBoard
     */
    public GameBoardImpl(GameBoard oldBoard) {
        _fields = new GamePiece[DIM][DIM];
        _moveListCacheValid = false;

        if (oldBoard == null) {
            throw new NullPointerException("Parameter oldBoard may not be null");
        }

        // -- copy fields --
        for (int col = 0; col < DIM; col++) {
            for (int row = 0; row < DIM; row++) {
                // we can't do an arraycopy here as we do not know the
                // Implementation of the old board
                _fields[col][row] = oldBoard.getPiece(col + 1, row + 1) == null ? null
                        : (GamePiece) oldBoard.getPiece(col + 1, row + 1)
                        .clone();
            }
        }

        // -- copy move history --
        this._moveHistory = oldBoard.getMoveHistory();
        this._halfMoveNumber = oldBoard.getLastHalfMoveNumber();

        // -- copy castling flags
        if (oldBoard.isCastlingKingSideAllowed(GameColor.WHITE)) {
            _castlingRights[GameCastling.WHITE_KINGSIDE.ordinal()] = GameCastling.WHITE_KINGSIDE;
        } else {
            removeKingSideCastling(GameColor.WHITE);
        }
        if (oldBoard.isCastlingQueenSideAllowed(GameColor.WHITE)) {
            _castlingRights[GameCastling.WHITE_QUEENSIDE.ordinal()] = GameCastling.WHITE_QUEENSIDE;
        } else {
            removeQueenSideCastling(GameColor.WHITE);
        }
        if (oldBoard.isCastlingKingSideAllowed(GameColor.BLACK)) {
            _castlingRights[GameCastling.BLACK_KINGSIDE.ordinal()] = GameCastling.BLACK_KINGSIDE;
        } else {
            removeKingSideCastling(GameColor.BLACK);
        }
        if (oldBoard.isCastlingQueenSideAllowed(GameColor.BLACK)) {
            _castlingRights[GameCastling.BLACK_QUEENSIDE.ordinal()] = GameCastling.BLACK_QUEENSIDE;
        } else {
            removeQueenSideCastling(GameColor.BLACK);
        }

        // en passant flag
        this._enPassantCapturable = oldBoard.getEnPassantCapturable();

        // check flag
        this._hasCheck = oldBoard.hasCheck();
        this._hasCheckMate = oldBoard.hasCheckMate();
        this._hasStaleMate = oldBoard.hasStaleMate();

        // king positions
        this._whiteKingField = oldBoard.getKingField(GameColor.WHITE);
        this._blackKingField = oldBoard.getKingField(GameColor.BLACK);

        // halfmove clock
        this._halfmoveClock = oldBoard.getHalfmoveClock();
        _moveListCacheValid = false;
    }

    // Contructors END -----------------------------------------

    // ESSENTIAL METHODS START ----------------------------------------

    /**
     * Commits a move to the board. Takes care of captured pieces, castle and
     * promotion.
     * Checks if game is over after move.
     *
     * @see fko.chessly.game.GameBoard#makeMove(fko.chessly.game.GameMove)
     */
    @Override
    public synchronized GamePiece makeMove(GameMove move) {

        // -- assert ---
        if (move == null) {
            throw new NullPointerException(
                    "Error: Parameter move in BoardImpl.makeMove() may not be null");
        }

        // invalidate flags
        _moveListCacheValid = false;
        _hasCheck = false;

        // -- make it more readable
        int fromCol = move.getFromField().x - 1;
        int fromRow = move.getFromField().y - 1;
        int toCol = move.getToField().x - 1;
        int toRow = move.getToField().y - 1;

        final GameColor activeColor = move.getMovedPiece().getColor();
        final GameColor opponentColor = activeColor.getInverseColor();

        // save castling status
        GameCastling[] copy = new GameCastling[4];
        System.arraycopy(_castlingRights, 0, copy, 0, _castlingRights.length);
        move.setCastlingRights(copy);

        // -- save captured piece
        GamePiece capturedPiece = removePiece(toCol, toRow);

        // -- remove from fromField
        GamePiece movedPiece = removePiece(fromCol, fromRow);

        // reset en passant
        _enPassantCapturable = null;

        // en passant?
        // Pawn not moving straight but no captured piece
        // we do not need to do an exhaustive check as this has been done
        // in isLegalMove - we can assume it is a legal move.
        if (movedPiece instanceof Pawn) {
            if (fromCol != toCol && capturedPiece == null) {
                // en passant capture
                GameMove last = _moveHistory.getLast();
                int lastToCol = last.getToField().x - 1;
                int lastToRow = last.getToField().y - 1;

                // remove the piece
                capturedPiece = removePiece(lastToCol, lastToRow);

                move.setWasEnPassantCapture(true);
                move.setEnPassantCapturePosition(GamePosition.getGamePosition(lastToCol + 1, lastToRow + 1));

            } else if (fromCol == toCol) {
                // double pawn move - possible en passant next move
                int baseRow = (move.getMovedPiece().getColor() == GameColor.WHITE ? 2 : 7);
                if (move.getFromField().y == baseRow
                        && Math.abs(move.getToField().y - baseRow) == 2) {
                    _enPassantCapturable = move.getToField();
                    move.setEnPassantNextMovePossible(true);
                }

            }
        }

        // -- place piece
        if (move.getPromotedTo() == null) {
            putPiece(movedPiece, toCol, toRow);
        } else { // pawn promotion
            putPiece(move.getPromotedTo(), toCol, toRow);
        }

        // castling check - neither rook nor king may move to allow future castling
        if (movedPiece instanceof King) {
            switch (activeColor) {
                case WHITE:
                    doWhiteCastling(move, fromCol, toCol);
                    break;
                case BLACK:
                    doBlackCastling(move, fromCol, toCol);
                    break;
                default:
                    throw new IllegalArgumentException("No valid color");
            }
        } else if (movedPiece instanceof Rook) {
            if (fromCol == 0) {
                removeQueenSideCastling(activeColor);
            } else if (fromCol == 7) {
                removeKingSideCastling(activeColor);
            }
        }
        // if the captured piece has been a rook on its original field remove castling right as well
        if (capturedPiece instanceof Rook) {
            if (toCol == 0) {
                removeQueenSideCastling(opponentColor);
            } else if (toCol == 7) {
                removeKingSideCastling(opponentColor);
            }
        }

        // -- save last move ---
        this._moveHistory.add(move);

        // increase halfmovenumber
        _halfMoveNumber++;

        if (capturedPiece == null)
            _halfmoveClock++;
        else
            _halfmoveClock = 0;

        // -- Update Move Object
        move.setHalfMoveNumber(_halfMoveNumber); // save move number
        move.setCapturedPiece(capturedPiece); // store the captured Piece in the Move

        // was this last move check for the opponent?
        move.setWasCheck(this.hasCheck(move));

        this.isGameOver();
        move.setWasCheckMate(_hasCheckMate);
        move.setWasStaleMate(_hasStaleMate);

        return capturedPiece;
    }

    @Override
    public synchronized GameMove undoMove() {
        if (_moveHistory.size() == 0)
            throw new RuntimeException("Error - no move to be undone");

        // moveListCache is now invalid
        _moveListCacheValid = false;

        // remove last move from history
        GameMove lastMove = _moveHistory.removeLast();

        // decrease half move number
        _halfMoveNumber--;

        // decrease half move clock
        _halfmoveClock--;

        GamePiece lastPiece = lastMove.getMovedPiece();

        GamePosition originalSource = lastMove.getFromField();
        GamePosition originalTarget = lastMove.getToField();

        // move piece back to original place
        putPiece(removePiece(originalTarget.x - 1, originalTarget.y - 1),
                originalSource.x - 1, originalSource.y - 1);

        // place back captured piece
        // en passant
        if (lastMove.getWasEnPassantCapture()) {
            GamePosition p = lastMove.getEnPassantCapturePosition();
            putPiece(Pawn.createPawn(lastMove.getCapturedPiece().getColor()), p.x - 1,
                    p.y - 1);
        } else {
            putPiece(lastMove.getCapturedPiece(), originalTarget.x - 1,
                    originalTarget.y - 1);
        }

        // undo promotion
        if (lastMove.getPromotedTo() != null) {
            removePiece(originalSource.x - 1, originalSource.y - 1);
            putPiece(Pawn.createPawn(lastPiece.getColor()), originalSource.x - 1,
                    originalSource.y - 1);
        }

        // en passant possible
        // checks the last move before the move to be undone
        GameMove previousMove = _moveHistory.getLast();
        if (_moveHistory.size() > 0 && previousMove.getMovedPiece() instanceof Pawn
                && previousMove.getFromField().x == previousMove.getToField().x) {
            // double pawn move - possible en passant next move
            int baseRow = (previousMove.getMovedPiece().getColor() == GameColor.WHITE ? 2
                    : 7);
            if (previousMove.getFromField().y == baseRow
                    && Math.abs(previousMove.getToField().y - baseRow) == 2) {
                _enPassantCapturable = previousMove.getToField();
            }
        }

        // castle - restore caslting rights
        _castlingRights = lastMove.getCastlingRights();
        // set back rook
        if (lastMove.getCastlingType() != GameCastling.NOCASTLING) {
            switch (lastMove.getCastlingType()) {
                case WHITE_KINGSIDE:
                    putPiece(removePiece(5, 0), 7, 0);
                    break;
                case WHITE_QUEENSIDE:
                    putPiece(removePiece(3, 0), 0, 0);
                    break;
                case BLACK_KINGSIDE:
                    putPiece(removePiece(5, 7), 7, 7);
                    break;
                case BLACK_QUEENSIDE:
                    putPiece(removePiece(3, 7), 0, 7);
                    break;
                default:
                    throw new RuntimeException("Unknown Castling Type "
                            + lastMove.getCastlingType());
            }
        }

        // check
        _hasCheck = lastMove.getWasCheck();
        // reset Mates - not possible true if we undo a move
        _hasCheckMate = false;
        _hasStaleMate = false;

        return lastMove;
    }

    /**
     * Check if the move will leave the own king in check
     *
     * @param move to be tested
     * @return true if own king would be in check after this move - false
     *         otherwise
     */
    @Override
    public boolean leavesKingInCheck(GameMove move) {

        if (move.getCastlingType() != GameCastling.NOCASTLING) {
            // castling does its own check
            return false;
        }

        boolean result = false;

        // -- make it more readable
        int fromCol = move.getFromField().x - 1;
        int fromRow = move.getFromField().y - 1;
        int toCol = move.getToField().x - 1;
        int toRow = move.getToField().y - 1;

        final GameColor activeColor = move.getMovedPiece().getColor();

        // MAKE PSEUDO MOVE

        // -- save captured piece
        GamePiece capturedPiece = this.removePiece(toCol, toRow);

        // -- remove from fromField
        GamePiece movedPiece = this.removePiece(fromCol, fromRow);

        // en passant? Pawn not moving straight but no captured piece
        boolean enPassantCapture = false;
        if (movedPiece instanceof Pawn) {
            if (fromCol != toCol && capturedPiece == null && _enPassantCapturable != null) {

                // en passant capture
                GamePosition enPassantField = _enPassantCapturable;

                // remove the piece
                capturedPiece = this.removePiece(enPassantField.x-1, enPassantField.y-1);

                // remember this for undo
                enPassantCapture=true;
            }
        }

        // -- place piece (pawn promotion does not matter here)
        this.putPiece(movedPiece, toCol, toRow);

        // CHECK CHECK

        // check if own king is now attacked (in check)
        if (activeColor == GameColor.WHITE) {
            if (this.isFieldControlledBy(this.getKingField(GameColor.WHITE), GameColor.BLACK))
                result = true;
        } else {
            if (this.isFieldControlledBy(this.getKingField(GameColor.BLACK), GameColor.WHITE))
                result = true;
        }

        // UNDO PSEUDO MOVE HERE

        // move back piece
        this.putPiece(this.removePiece(toCol, toRow), fromCol, fromRow);

        // put back capture piece
        if (enPassantCapture) {
            // en passant capture
            GamePosition enPassantField = _enPassantCapturable;

            // remove the piece
            this.putPiece(capturedPiece, enPassantField.x-1, enPassantField.y-1);

        } else {
            // put back captured piece (should be null if none)
            this.putPiece(capturedPiece, toCol, toRow);
        }

        return result;
    }

    private void putPiece(GamePiece p, int col, int row) {
        assert (_fields[col][row] == null);

        _fields[col][row] = p;

        // update king position
        if (p instanceof King) {
            switch (p.getColor()) {
                case WHITE:
                    _whiteKingField = GamePosition.getGamePosition(col + 1, row + 1);
                    break;
                case BLACK:
                    _blackKingField = GamePosition.getGamePosition(col + 1, row + 1);
                    break;
                default:
                    throw new IllegalArgumentException("No valid color");
            }
        }
    }

    private GamePiece removePiece(int col, int row) {
        GamePiece removedPiece = _fields[col][row];
        _fields[col][row] = null;
        // update king position
        if (removedPiece instanceof King) {
            switch (removedPiece.getColor()) {
                case WHITE:
                    _whiteKingField = null;
                    break;
                case BLACK:
                    _blackKingField = null;
                    break;
                default:
                    throw new IllegalArgumentException("No valid color");
            }
        }
        return removedPiece;
    }

    /**
     * Checks for attack on the opponent king and sets _hasCheck
     * @param move
     * @return _hasCheck
     */
    private boolean hasCheck(GameMove move) {
        switch (move.getMovedPiece().getColor().getInverseColor()) {
            case WHITE:
                _hasCheck = isFieldControlledBy(GamePosition.getGamePosition(_whiteKingField.x, _whiteKingField.y), GameColor.BLACK);
                break;
            case BLACK:
                _hasCheck = isFieldControlledBy(GamePosition.getGamePosition(_blackKingField.x, _blackKingField.y), GameColor.WHITE);
                break;
            default:
                throw new IllegalArgumentException("No valid color");
        }
        return _hasCheck;
    }

    private void doBlackCastling(GameMove move, int fromCol, int toCol) {
        // was this move a castling - then move rook as well
        if (fromCol - toCol == 2) {
            // queen side
            putPiece(removePiece(0, 7), 3, 7);
            move.setCastlingType(GameCastling.BLACK_QUEENSIDE);
        } else if (fromCol - toCol == -2) {
            // king side
            putPiece(removePiece(7, 7), 5, 7);
            move.setCastlingType(GameCastling.BLACK_KINGSIDE);
        } else {
            move.setCastlingType(GameCastling.NOCASTLING);
        }
        removeAllCastlingRights(GameColor.BLACK);
        // System.out.println("No more black castling!");
    }

    private void doWhiteCastling(GameMove move, int fromCol, int toCol) {
        // was this move a castling - then move rook as well
        if (fromCol - toCol == 2) {
            // queen side
            putPiece(removePiece(0, 0), 3, 0);
            move.setCastlingType(GameCastling.WHITE_QUEENSIDE);
        } else if (fromCol - toCol == -2) {
            // king side
            putPiece(removePiece(7, 0), 5, 0);
            move.setCastlingType(GameCastling.WHITE_KINGSIDE);
        } else {
            move.setCastlingType(GameCastling.NOCASTLING);
        }
        removeAllCastlingRights(GameColor.WHITE);
        // System.out.println("No more white castling!");
    }

    private void removeKingSideCastling(GameColor color) {
        switch (color) {
            case WHITE:
                _castlingRights[GameCastling.WHITE_KINGSIDE.ordinal()] = GameCastling.NOCASTLING;
                break;
            case BLACK:
                _castlingRights[GameCastling.BLACK_KINGSIDE.ordinal()] = GameCastling.NOCASTLING;
                break;
            default:
                throw new IllegalArgumentException("No valid color");
        }
    }

    private void removeQueenSideCastling(GameColor color) {
        switch (color) {
            case WHITE:
                _castlingRights[GameCastling.WHITE_QUEENSIDE.ordinal()] = GameCastling.NOCASTLING;
                break;
            case BLACK:
                _castlingRights[GameCastling.BLACK_QUEENSIDE.ordinal()] = GameCastling.NOCASTLING;
                break;
            default:
                throw new IllegalArgumentException("No valid color");
        }
    }

    private void removeAllCastlingRights(GameColor color) {
        removeKingSideCastling(color);
        removeQueenSideCastling(color);
    }

    /**
     * Returns a list of all possible legal moves
     *
     * @return List<Moves> of all legal moves for the next player
     */
    @Override
    public synchronized GameMoveList generateMoves() {
        return generateMoves(false);
    }

    /**
     * Returns a list of all possible legal moves
     *
     * @param capturingMovesOnly - true if only capturing moces should be generated
     * @return list of moves
     */
    public GameMoveList generateMoves(boolean capturingMovesOnly) {

        if (_moveListCacheValid && !capturingMovesOnly)
            return _moveListCache;

        GameMoveList moveList = new GameMoveList(60);

        // generate possible pseudo legal moves for each piece on the board
        // iterating over all fields as we do not have a piece list
        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                final GamePiece piece = _fields[c][r];
                if (piece != null && piece.getColor().equals(getNextPlayerColor())) {
                    GameMoveList moves = piece.getPseudoLegalMovesForPiece(this,
                            GamePosition.getGamePosition(c + 1, r + 1), capturingMovesOnly);
                    if (moves.size() > 0)
                        moveList.addAll(moves);
                }
            }
        }
        // filter only legal moves
        moveList = this.filterLegalMovesOnly(moveList);

        // sort Move List according to piece value
        // TODO: sort List according to evaluation
        //Collections.shuffle(moveList);

        // cache of moveList is valid now
        _moveListCache = moveList;
        _moveListCacheValid = true;

        return moveList;
    }

    // ESSENTIALS METHODS END ----------------------------------------

    // Methods -----------------------------------------------

    @Override
    public synchronized boolean isGameOver() {

        if (this.generateMoves().isEmpty()) {
            if (_hasCheck)
                _hasCheckMate = true;
            else _hasStaleMate = true;
            return true;
        }

        if (hasInsufficientMaterial()) {
            return true;
        }

        if (tooManyMovesWithoutCapture()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean tooManyMovesWithoutCapture() {
        if (_halfmoveClock >= 100) {
            _hasStaleMate = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasInsufficientMaterial() {
        // Draw for lack of material
        // count all pieces
        // iterating over all fields as we do not have a piece list
        int counterBishopKnightWhite = 0;
        int counterBishopKnightBlack = 0;
        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                final GamePiece piece = _fields[c][r];
                if (piece != null) {
                    if (piece instanceof Pawn || piece instanceof Queen
                            || piece instanceof Rook) {
                        return false;
                    } else if (piece instanceof Bishop
                            || piece instanceof Knight) {
                        if (piece.isWhite())
                            counterBishopKnightWhite++;
                        if (piece.isBlack())
                            counterBishopKnightBlack++;
                    }
                }
            }
        }
        if (counterBishopKnightWhite < 2 && counterBishopKnightBlack < 2) {
            _hasStaleMate = true;
            return true;
        }
        return false;
    }

    /**
     * Checks for legal moves
     *
     * @param move
     */
    @Override
    public boolean isLegalMove(GameMove move) {

        if (move == null) return false;

        GamePiece fromPiece = null;
        GamePiece toPiece = null;

        try {
            fromPiece = _fields[move.getFromField().x - 1][move.getFromField().y - 1];
            toPiece = _fields[move.getToField().x - 1][move.getToField().y - 1];
        } catch (NullPointerException e) {
            // Chessly.criticalError(e.getMessage());
            // Happens when Game is ended - ignore
        }

        // -- empty from field --
        if (fromPiece == null) {
            // System.out.println("Illegal Move: No piece moved ("+move+")");
            return false;
        }

        // -- wrong color / not my turn --
        if (!fromPiece.getColor().equals(getNextPlayerColor())) {
            // System.out.println("Illegal Move: Wrong color ("+move+")");
            return false;
        }

        // -- move to same field --
        if (move.getFromField().equals(move.getToField())) {
            // System.out.println("Illegal Move: Same field move ("+move+")");
            return false;
        }

        // -- move to field occupied by own piece
        if (toPiece != null && toPiece.getColor().equals(getNextPlayerColor())) {
            // System.out.println("Illegal Move: Occupied by own piece ("+move+")");
            return false;
        }

        // -- move outside of board
        if (!(isWithinBoard(move.getFromField()) && isWithinBoard(move
                .getToField()))) {
            return false;
        }

        // check if the move can be done by the Piece
        GameMoveList ml = fromPiece.getPseudoLegalMovesForPiece(this,
                move.getFromField(), false);

        if (!ml.contains(move)) {
            return false;
        }

        // check if move would leave own king in check
        boolean result = !leavesKingInCheck(move);

        return result;
    }

    // ESSENTIALS METHODS END ----------------------------------------

    // Methods -----------------------------------------------

    /**
     * Checks all move of given pseudo legal move list and returns list with only legal moves.
     * @return list of legal moves
     */
    @Override
    public GameMoveList filterLegalMovesOnly(GameMoveList moves) {
        int size = moves.size();
        GameMoveList copy = new GameMoveList(size);
        for (int i = 0; i < size; i++) {
            GameMove m = moves.get(i);
            if (!this.leavesKingInCheck(m))
                copy.add(m);
        }
        return copy;
    }

    /**
     * Checks if a move to a field is possible - is free or occupied by
     * opponent. Does NOT check if pawn can capture forward! Does NOT check if
     * the way to the field is blocked! Does NOT check if this is otherwise a
     * legal move (e.g. King moving into Check)
     *
     * @return true if to field is empty or occupied by opponent - false
     *         otherwise
     */
    @Override
    public boolean canMoveTo(GamePosition from, GamePosition to) {

        // check for valid position

        // out of board
        if (!isWithinBoard(to))
            return false;

        // if empty then ok
        if (_fields[to.x - 1][to.y - 1] == null)
            return true;

        // oppenent piece
        if (_fields[from.x - 1][from.y - 1]
                .getColor()
                .equals(
                        _fields[to.x - 1][to.y - 1].getColor().getInverseColor()))
            return true;

        return false;
    }

    /**
     * Checks if there is another piece between the from field and the to field
     * along the alowed move path.
     *
     * @return true if no other piece blocking the way - false otherwise
     */
    @Override
    public boolean checkForFreePath(GamePosition from, GamePosition to) {
        int colDir = (from.x == to.x ? 0 : (from.x - to.x) < 0 ? 1 : -1);
        int rowDir = (from.y - to.y == 0 ? 0 : (from.y - to.y) < 0 ? 1 : -1);

        // -- check if something is blocking the way
        int currentCol = from.x + colDir;
        int currentRow = from.y + rowDir;
        while (!(currentCol == to.x && currentRow == to.y)) {
            if (_fields[currentCol - 1][currentRow - 1] != null) {
                // System.out.println("Move BLOCKED!");
                return false;
            }
            currentCol += colDir;
            currentRow += rowDir;
        }
        return true;
    }

    /**
     * Checks if there is another piece between the from field and the to field
     * along the alowed move path.
     *
     * @param move
     * @return true if no other piece blocking the way - false otherwise
     */
    @Override
    public boolean checkForFreePath(GameMove move) {
        // -- make code more readable
        return checkForFreePath(move.getFromField(), move.getToField());
    }

    /**
     * Check if the field is attacked.<br/>
     * TODO: can this be cached somehow?
     * @return true when any other piece can capture on this field in the next
     *         move
     */
    @Override
    public boolean isFieldControlledBy(final GamePosition pos, final GameColor attackingColor) {

        if (!isWithinBoard(pos))
            throw new IllegalArgumentException();

        // check for pawn attacks
        if (attackingColor == GameColor.BLACK) {
            for (int i = 0; i < GamePiece.blackPawnAttackVectors.length; i++) {
                int col_inc = GamePiece.blackPawnAttackVectors[i][0];
                int row_inc = GamePiece.blackPawnAttackVectors[i][1];
                int new_col = pos.x + col_inc;
                int new_row = pos.y + row_inc;
                if (checkForPawnAttack(this, new_col, new_row, attackingColor)) {
                    // System.out.println("Field "+getField(a_col,
                    // a_row)+" attacked by "+getField(new_col,
                    // new_row).getPiece()+" on "+getField(new_col, new_row));
                    return true;
                }
            }
        } else if (attackingColor == GameColor.WHITE) {
            for (int i = 0; i < GamePiece.whitePawnAttackVectors.length; i++) {
                int col_inc = GamePiece.whitePawnAttackVectors[i][0];
                int row_inc = GamePiece.whitePawnAttackVectors[i][1];
                int new_col = pos.x + col_inc;
                int new_row = pos.y + row_inc;
                if (checkForPawnAttack(this, new_col, new_row, attackingColor)) {
                    // System.out.println("Field "+getField(a_col,
                    // a_row)+" attacked by "+getField(new_col,
                    // new_row).getPiece()+" on "+getField(new_col, new_row));
                    return true;
                }
            }
        } else
            throw new IllegalArgumentException();

        // check for Knight attacks
        for (int i = 0; i < GamePiece.knightAttackVectors.length; i++) {
            int col_inc = GamePiece.knightAttackVectors[i][0];
            int row_inc = GamePiece.knightAttackVectors[i][1];
            int new_col = pos.x + col_inc;
            int new_row = pos.y + row_inc;
            if (checkForKnightAttack(this, new_col, new_row, attackingColor)) {
                // System.out.println("Field "+getField(a_col,
                // a_row)+" attacked by "+getField(new_col,
                // new_row).getPiece()+" on "+getField(new_col, new_row));
                return true;
            }
        }

        // check all directions
        for (int i = 0; i < GamePiece.clockwiseLookup.length; i++) {
            int col_inc = GamePiece.clockwiseLookup[i][0];
            int row_inc = GamePiece.clockwiseLookup[i][1];
            int new_col = pos.x + col_inc;
            int new_row = pos.y + row_inc;

            // check king on neighbour fields only
            if ((isWithinBoard(GamePosition.getGamePosition(new_col, new_row)))
                    && getPiece(new_col, new_row) != null
                    && getPiece(new_col, new_row).getColor() == attackingColor
                    && getPiece(new_col, new_row) instanceof King) {
                // System.out.println("Field "+getField(a_col,
                // a_row)+" attacked by "+getField(new_col,
                // new_row).getPiece()+" on "+getField(new_col, new_row));
                return true;
            }

            // traverse and find bishop, rook or queen or
            while (isWithinBoard(GamePosition.getGamePosition(new_col, new_row))) {
                if (getPiece(new_col, new_row) != null) {
                    if (getPiece(new_col, new_row).getColor() == attackingColor
                            && (getPiece(new_col, new_row) instanceof Queen
                                    || (col_inc * row_inc == 0 && getPiece(
                                            new_col, new_row) instanceof Rook) || (col_inc
                                                    * row_inc != 0 && getPiece(new_col, new_row) instanceof Bishop))) {
                        // System.out.println("Field "+getField(a_col,
                        // a_row)+" attacked by "+getField(new_col,
                        // new_row).getPiece()+" on "+getField(new_col,
                        // new_row));
                        return true;
                    }
                    // non attacker found
                    break;
                }
                new_col = new_col + col_inc;
                new_row = new_row + row_inc;
            }
        }

        return false;
    }

    /**
     * @param board
     * @param a_col
     * @param a_row
     * @param attackingColor
     * @return true when a pawn attacks the given field
     */
    protected static boolean checkForPawnAttack(GameBoard board, int col, int row, GameColor attackingColor) {
        if (!board.isWithinBoard(GamePosition.getGamePosition(col, row)))
            return false;
        final GamePiece piece = board.getPiece(col, row);
        if (piece != null && piece.getColor() == attackingColor
                && piece instanceof Pawn)
            return true;
        return false;
    }

    /**
     * @param board
     * @param a_col
     * @param a_row
     * @param attackingColor
     * @return true when a pawn attacks the given field
     */
    protected static boolean checkForKnightAttack(GameBoard board, int col, int row, GameColor attackingColor) {
        if (!board.isWithinBoard(GamePosition.getGamePosition(col, row)))
            return false;
        final GamePiece piece = board.getPiece(col, row);
        if (piece != null && piece.getColor() == attackingColor
                && piece instanceof Knight)
            return true;
        return false;
    }

    @Override
    public GamePiece getPiece(int col, int row) {
        return _fields[col - 1][row - 1];
    }

    @Override
    public GamePiece getPiece(GamePosition pos) {
        return _fields[pos.x - 1][pos.y - 1];
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Board#isWithinBoard(int, int)
     */
    @Override
    public boolean isWithinBoard(GamePosition p) {
        if (p == null) return false;
        return p.x > 0 && p.x < 9 && p.y > 0 && p.y < 9;
    }

    // Getters and Setters ---------------------------------------------------

    /*
     * @see fko.chessly.game.Board#getNextPlayerColor()
     */
    @Override
    public GameColor getNextPlayerColor() {
        // should we store next player color to be quicker
        if (_halfMoveNumber % 2 == 0)
            return GameColor.WHITE;
        return GameColor.BLACK;
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Board#getLastPlayerColor()
     */
    @Override
    public GameColor getLastPlayerColor() {
        return this.getNextPlayerColor().getInverseColor();
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Board#getLastMove()
     */
    @Override
    public GameMove getLastMove() {
        return _moveHistory.size() == 0 ? null : _moveHistory.get(_moveHistory.size() - 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Board#getMoveHistory()
     */
    @Override
    public GameMoveList getMoveHistory() {
        GameMoveList copy = new GameMoveList(_moveHistory.size());
        for (GameMove m : _moveHistory) {
            copy.add(new GameMoveImpl(m));
        }
        return copy;
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Board#getLastHalfMoveNumber()
     */
    @Override
    public int getLastHalfMoveNumber() {
        return _halfMoveNumber;
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Board#getNextHalfMoveNumber()
     */
    @Override
    public int getNextHalfMoveNumber() {
        return getLastHalfMoveNumber() + 1;
    }

    /**
     * @return full move number
     */
    public int getFullMoveNumber() {
        return (_halfMoveNumber+1)/2;
    }

    @Override
    public int getHalfmoveClock() {
        return _halfmoveClock;
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Board#hasCheck()
     */
    @Override
    public boolean hasCheck() {
        return _hasCheck;
    }

    /**
     * @return the _hasCheckMate
     */
    @Override
    public boolean hasCheckMate() {
        return _hasCheckMate;
    }

    /**
     * @return the _hasStaleMate
     */
    @Override
    public boolean hasStaleMate() {
        return _hasStaleMate;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fko.chessly.game.Board#isCastlingKingSideAllowed(fko.chessly.game.PieceColor
     * )
     */
    @Override
    public boolean isCastlingKingSideAllowed(GameColor color) {
        if (color == GameColor.NONE)
            throw new IllegalArgumentException();
        GameCastling c = _castlingRights[GameCastling.valueOf(color,
                GameCastlingType.KINGSIDE).ordinal()];
        return c == GameCastling.NOCASTLING ? false : true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fko.chessly.game.Board#isCastlingQueenSideAllowed(fko.chessly.game.PieceColor
     * )
     */
    @Override
    public boolean isCastlingQueenSideAllowed(GameColor color) {
        if (color == GameColor.NONE)
            throw new IllegalArgumentException();
        GameCastling c = _castlingRights[GameCastling.valueOf(color,
                GameCastlingType.QUEENSIDE).ordinal()];
        return c == GameCastling.NOCASTLING ? false : true;
    }

    /**
     * @return the _whiteKingField
     */
    @Override
    public GamePosition getKingField(GameColor color) {
        switch (color) {
            case WHITE:
                return _whiteKingField;
            case BLACK:
                return _blackKingField;
            default:
                throw new IllegalArgumentException("Not a valid color" + color);
        }
    }

    /**
     * @return the _enPassantCapturable
     */
    @Override
    public GamePosition getEnPassantCapturable() {
        return _enPassantCapturable;
    }

    /**
     * @return true if next move has en passant capture option
     */
    @Override
    public boolean hasEnPassantCapturable() {
        return _enPassantCapturable == null ? false : true;
    }

    /**
     * Checking if the position of this board is identical to the position of
     * the other board. This does not regard the MoveHistory (incl. no of moves)
     * of the game. Is does regard: - next player - castling rights - en passant
     * rights (and file)
     *
     * FIDE rules: Positions are considered the same if and only if the same
     * player has the move, pieces of the same kind and colour occupy the same
     * squares and the possible moves of all the pieces of both players are the
     * same. Thus positions are not the same if: - at the start of the sequence
     * a pawn could have been captured en passant. - a king or rook had castling
     * rights, but forfeited these after moving. The castling rights are lost
     * only after the king or rook is moved.
     *
     * @param b the board to check
     * @return true when the position is identical according to FIDE rules
     */
    @Override
    public boolean hasSamePosition(GameBoard b) {
        if (this == b)
            return true;
        if (b == null)
            return false;

        // same player
        if (this.getNextPlayerColor() != b.getNextPlayerColor())
            return false;

        // check all pieces
        if (b instanceof GameBoardImpl) {
            GameBoardImpl other = (GameBoardImpl) b;
            if (!Arrays.deepEquals(_fields, other._fields))
                return false;
        } else {
            for (int c = 1; c <= 8; c++) {
                for (int r = 1; r <= 8; r++) {
                    if (!this.getPiece(c, r).equals(b.getPiece(c, r)))
                        return false;
                }
            }
        }

        // en passant
        if (!(b.hasEnPassantCapturable() == this.hasEnPassantCapturable())
                ||  (b.getEnPassantCapturable() != null && !b.getEnPassantCapturable().equals(this._enPassantCapturable)))
            return false;

        // castling rights
        for (GameColor color : GameColor.values) {
            if (b.isCastlingKingSideAllowed(color) != this
                    .isCastlingKingSideAllowed(color)
                    || b.isCastlingQueenSideAllowed(color) != this
                    .isCastlingQueenSideAllowed(color)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Create start setup of game board
     */
    protected void initBoard(String fen) {

        _fields = new GamePiece[DIM][DIM];
        _moveHistory = new GameMoveList(100);
        _moveListCacheValid = false;

        // First set all emtpy
        for (int row = 0; row < DIM; row++) {
            for (int col = 0; col < DIM; col++) {
                _fields[col][row] = null;
            }
        }

        // Standard Start Board
        setupFromFEN(fen);

        // DEBUG
        //setupFromFEN("8/1P6/6k1/8/8/8/p1K5/8 w - - 0 1");

        // Test - Mate in 2
        //setupFromFEN("1r3rk1/1pnnq1bR/p1pp2B1/P2P1p2/1PP1pP2/2B3P1/5PK1/2Q4R w - - 0 1");

        // Test - Mate in 3
        //setupFromFEN("4rk2/p5p1/1p2P2N/7R/nP5P/5PQ1/b6K/q7 w - - 0 1");

        // Test - Mate in 3
        //setupFromFEN("4k2r/1q1p1pp1/p3p3/1pb1P3/2r3P1/P1N1P2p/1PP1Q2P/2R1R1K1 b k - 0 1");

        // Test - Mate in 4
        //setupFromFEN("r2r1n2/pp2bk2/2p1p2p/3q4/3PN1QP/2P3R1/P4PP1/5RK1 w - - 0 1");

        // Test - Mate in 5 (1.Sc6+! bxc6 2.Dxa7+!! Kxa7 3.Ta1+ Kb6 4.Thb1+ Kc5 5.Ta5# 1-0)
        //setupFromFEN("1kr4r/ppp2bq1/4n3/4P1pp/1NP2p2/2PP2PP/5Q1K/4R2R w - - 0 1");

        // Test - Mate in 3
        //setupFromFEN("1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - 0 1");

        // Test - Mate in 11
        //setupFromFEN("8/5k2/8/8/2N2N2/2B5/2K5/8 w - - 0 1");

        // Test - Mate in 13
        //setupFromFEN("8/8/6k1/8/8/8/P1K5/8 w - - 0 1");

        // Test - Mate in 15
        //setupFromFEN("8/5k2/8/8/8/8/1BK5/1B6 w - - 0 1");

        // Test - HORIZONT EFFECT
        //setupFromFEN("5r1k/4Qpq1/4p3/1p1p2P1/2p2P2/1p2P3/1K1P4/B7 w - - 0 1");


        /**
         * bm = best move
         *
1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - bm Qd1+; id "BK.01";
3r1k2/4npp1/1ppr3p/p6P/P2PPPP1/1NR5/5K2/2R5 w - - bm d5; id "BK.02";
2q1rr1k/3bbnnp/p2p1pp1/2pPp3/PpP1P1P1/1P2BNNP/2BQ1PRK/7R b - - bm f5; id "BK.03";
rnbqkb1r/p3pppp/1p6/2ppP3/3N4/2P5/PPP1QPPP/R1B1KB1R w KQkq - bm e6; id "BK.04";
r1b2rk1/2q1b1pp/p2ppn2/1p6/3QP3/1BN1B3/PPP3PP/R4RK1 w - - bm Nd5 a4; id "BK.05";
2r3k1/pppR1pp1/4p3/4P1P1/5P2/1P4K1/P1P5/8 w - - bm g6; id "BK.06";
1nk1r1r1/pp2n1pp/4p3/q2pPp1N/b1pP1P2/B1P2R2/2P1B1PP/R2Q2K1 w - - bm Nf6; id "BK.07";
4b3/p3kp2/6p1/3pP2p/2pP1P2/4K1P1/P3N2P/8 w - - bm f5; id "BK.08";
2kr1bnr/pbpq4/2n1pp2/3p3p/3P1P1B/2N2N1Q/PPP3PP/2KR1B1R w - - bm f5; id "BK.09";
3rr1k1/pp3pp1/1qn2np1/8/3p4/PP1R1P2/2P1NQPP/R1B3K1 b - - bm Ne5; id "BK.10";
2r1nrk1/p2q1ppp/bp1p4/n1pPp3/P1P1P3/2PBB1N1/4QPPP/R4RK1 w - - bm f4; id "BK.11";
r3r1k1/ppqb1ppp/8/4p1NQ/8/2P5/PP3PPP/R3R1K1 b - - bm Bf5; id "BK.12";
r2q1rk1/4bppp/p2p4/2pP4/3pP3/3Q4/PP1B1PPP/R3R1K1 w - - bm b4; id "BK.13";
rnb2r1k/pp2p2p/2pp2p1/q2P1p2/8/1Pb2NP1/PB2PPBP/R2Q1RK1 w - - bm Qd2 Qe1; id "BK.14";
2r3k1/1p2q1pp/2b1pr2/p1pp4/6Q1/1P1PP1R1/P1PN2PP/5RK1 w - - bm Qxg7+; id "BK.15";
r1bqkb1r/4npp1/p1p4p/1p1pP1B1/8/1B6/PPPN1PPP/R2Q1RK1 w kq - bm Ne4; id "BK.16";
r2q1rk1/1ppnbppp/p2p1nb1/3Pp3/2P1P1P1/2N2N1P/PPB1QP2/R1B2RK1 b - - bm h5; id "BK.17";
r1bq1rk1/pp2ppbp/2np2p1/2n5/P3PP2/N1P2N2/1PB3PP/R1B1QRK1 b - - bm Nb3; id "BK.18";
3rr3/2pq2pk/p2p1pnp/8/2QBPP2/1P6/P5PP/4RRK1 b - - bm Rxe4; id "BK.19";
r4k2/pb2bp1r/1p1qp2p/3pNp2/3P1P2/2N3P1/PPP1Q2P/2KRR3 w - - bm g4; id "BK.20";
3rn2k/ppb2rpp/2ppqp2/5N2/2P1P3/1P5Q/PB3PPP/3RR1K1 w - - bm Nh6; id "BK.21";
2r2rk1/1bqnbpp1/1p1ppn1p/pP6/N1P1P3/P2B1N1P/1B2QPP1/R2R2K1 b - - bm Bxe4; id "BK.22";
r1bqk2r/pp2bppp/2p5/3pP3/P2Q1P2/2N1B3/1PP3PP/R4RK1 b kq - bm f6; id "BK.23";
r2qnrnk/p2b2b1/1p1p2pp/2pPpp2/1PP1P3/PRNBB3/3QNPPP/5RK1 w - - bm f4; id "BK.24";

r3qb1k/1b4p1/p2pr2p/3n4/Pnp1N1N1/6RP/1B3PP1/1B1QR1K1 w - - 0 1 26. Nxh6!!
         */
    }

    /**
     * @param fen
     */
    private void setupFromFEN(String fen) {

        int i = 0;
        int row = 7;
        int col = 0;

        String[] parts = fen.trim().split(" ");

        if (parts.length < 1) throw new IllegalArgumentException("FEN Syntax not valid - empty string?");

        String s = null;

        for (i=0; i<parts[0].length();i++) {
            s = parts[0].substring(i, i+1);

            if (s.matches("[pnbrqkPNBRQK]")) {
                if (s.toLowerCase() == s) { // black

                    switch (s) {
                        case "p": putPiece(Pawn.createPawn(GameColor.BLACK), col, row); break;
                        case "n": putPiece(Knight.createKnight(GameColor.BLACK), col, row); break;
                        case "b": putPiece(Bishop.createBishop(GameColor.BLACK), col, row); break;
                        case "r": putPiece(Rook.createRook(GameColor.BLACK), col, row); break;
                        case "q": putPiece(Queen.createQueen(GameColor.BLACK), col, row); break;
                        case "k": putPiece(King.createKing(GameColor.BLACK), col, row); break;
                        default:
                            throw new IllegalArgumentException("FEN Syntax not valid - expected a-hA-H");
                    }

                } else if (s.toUpperCase() == s) { // white

                    switch (s) {
                        case "P": putPiece(Pawn.createPawn(GameColor.WHITE), col, row); break;
                        case "N": putPiece(Knight.createKnight(GameColor.WHITE), col, row); break;
                        case "B": putPiece(Bishop.createBishop(GameColor.WHITE), col, row); break;
                        case "R": putPiece(Rook.createRook(GameColor.WHITE), col, row); break;
                        case "Q": putPiece(Queen.createQueen(GameColor.WHITE), col, row); break;
                        case "K": putPiece(King.createKing(GameColor.WHITE), col, row); break;
                        default:
                            throw new IllegalArgumentException("FEN Syntax not valid - expected a-hA-H");
                    }

                } else
                    throw new IllegalArgumentException("FEN Syntax not valid - expected a-hA-H");
                col++;
            } else if (s.matches("[1-8]")) {
                int e = Integer.parseInt(s);
                col += e;
            } else if (s.equals("/")) {
                row--;
                col = 0;
            } else
                throw new IllegalArgumentException("FEN Syntax not valid - expected (1-9a-hA-H/)");

            if (col > 8) {
                throw new IllegalArgumentException("FEN Syntax not valid - expected (1-9a-hA-H/)");
            }
        }

        // next player color

        GameColor nextPlayer = null;
        if (parts.length < 2) { // default "w"
            nextPlayer = GameColor.WHITE;
        } else {
            s = parts[1];
            if (s.equals("w")) nextPlayer = GameColor.WHITE;
            else if (s.equals("b")) nextPlayer = GameColor.BLACK;
            else throw new IllegalArgumentException("FEN Syntax not valid - expected w or b");
        }

        // castling
        // reset all castling first
        for (GameCastling c : GameCastling.values()) {
            if (c.isValid())
                _castlingRights[c.ordinal()] = GameCastling.NOCASTLING;
        }
        if (parts.length < 3) { // default "-"
            // ignore (readablility)
        } else {
            for (i=0; i<parts[2].length(); i++) {
                s = parts[2].substring(i, i+1);
                switch (s) {
                    case "K": _castlingRights[GameCastling.WHITE_KINGSIDE.ordinal()] = GameCastling.WHITE_KINGSIDE; break;
                    case "Q": _castlingRights[GameCastling.WHITE_QUEENSIDE.ordinal()] = GameCastling.WHITE_QUEENSIDE; break;
                    case "k": _castlingRights[GameCastling.BLACK_KINGSIDE.ordinal()] = GameCastling.BLACK_KINGSIDE; break;
                    case "q": _castlingRights[GameCastling.BLACK_QUEENSIDE.ordinal()] = GameCastling.BLACK_QUEENSIDE; break;
                    case "-":
                    default:
                }
            }
        }

        // en passant
        if (parts.length < 4) { // default "-"
            // ignore (readablility)
        } else {
            s = parts[3];
            if (!s.equals("-")) {
                GamePosition enPassantCapturePosition = GamePosition.getGamePosition(s);
                _enPassantCapturable = enPassantCapturePosition;
            }
        }

        // half move clock
        if (parts.length < 5) { // default "0"
            _halfmoveClock = 0 ;
        } else {
            s = parts[4];
            _halfmoveClock = Integer.parseInt(s);
        }

        // full move number
        if (parts.length < 6) { // default "0"
            _halfMoveNumber = 1 ;
        } else {
            s = parts[5];
            _halfMoveNumber = (2 * Integer.parseInt(s)) - 1;
        }
        if (nextPlayer.isWhite()) _halfMoveNumber--;
    }

    @Override
    public String toFEN() {

        String fen = "";

        for (int row = 7; row >= 0; row--) {
            int emptySquares = 0;
            for (int col = 0; col < 8; col++) {

                GamePiece piece = _fields[col][row];

                if (piece == null) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        fen += emptySquares;
                        emptySquares = 0;
                    }
                    if (piece.isWhite()) {
                        fen += piece.toNotationString();
                    } else {
                        fen += piece.toNotationString().toLowerCase();
                    }

                }
            }
            if (emptySquares > 0) {
                fen += emptySquares;
            }
            if (row > 0) {
                fen += '/';
            }
        }
        fen += ' ';

        // Color
        fen += this.getNextPlayerColor().toChar();
        fen += ' ';

        // Castling
        boolean castlingAvailable = false;
        for (GameCastling castling : _castlingRights) {
            if (castling != GameCastling.NOCASTLING) {
                fen += castling.toChar();
                castlingAvailable = true;
            }
        }
        if (!castlingAvailable) {
            fen += '-';
        }
        fen += ' ';

        // En passant
        if (this._enPassantCapturable != null) {
            fen += _enPassantCapturable.toNotationString();
        } else {
            fen += '-';
        }
        fen += ' ';

        // Half move clock
        fen += this._halfmoveClock;
        fen += ' ';

        // Full move number
        fen += Math.max(1, this.getFullMoveNumber());

        return fen;

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder((DIM << 2 + 5) * DIM);

        // backwards as highest row is on top
        for (int row = DIM; row > 0; row--) {

            // upper border
            boardString.append("    -"); // 4 * space
            for (int col = DIM; col > 0; col--) {
                boardString.append("----"); // dim * -
            }
            boardString.append("\n");

            // row number
            if (row < 10) {
                boardString.append(' ').append(Integer.toString(row))
                .append(": |");
            } else {
                boardString.append(Integer.toString(row)).append(": |");
            }

            // col fields
            for (int col = 1; col <= DIM; col++) {
                GamePiece p = getPiece(col, row);
                if (p == null) {
                    boardString.append("   |");
                } else {
                    boardString.append(p.toString()).append(" |");
                }
            }
            boardString.append("\n");
        }

        // lower border
        boardString.append("    -"); // 4 * space
        for (int col = DIM; col > 0; col--) {
            boardString.append("----"); // dim * -
        }
        boardString.append("\n");

        // col numbers
        boardString.append("     "); // 4 * space
        for (int col = 1; col <= DIM; col++) {
            if (col < 10) {
                boardString.append(' ').append(getColString(col)).append("  ");
            } else {
                boardString.append(' ').append(getColString(col)).append(' ');
            }
        }
        boardString.append("\n");

        // boardString.append("The White King is on: "+_whiteKingField+"\n");
        // boardString.append("The Black King is on: "+_blackKingField+"\n");
        if (_moveHistory.size() > 0)
            boardString.append("Last Move was: "
                    + _moveHistory.get(_moveHistory.size() - 1) + "\n");
        boardString.append("Next Player is " + getNextPlayerColor() + "\n");
        boardString.append("Next player has check? " + hasCheck() + "\n");

        return boardString.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Board#getHashKey()
     */
    @Override
    public String getHashKey() {
        return "" + this.hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(_fields);
        result = prime * result
                + ((_moveHistory == null) ? 0 : _moveHistory.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     * Board not on BoardImpl
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof GameBoardImpl))
            return false;
        GameBoardImpl other = (GameBoardImpl) obj;
        if (!Arrays.deepEquals(_fields, other._fields))
            return false;
        if (_moveHistory == null) {
            if (other._moveHistory != null)
                return false;
        } else if (!_moveHistory.equals(other._moveHistory)) {
            return false;
        }
        return true;
    }

    /**
     * @return deep clone of a BoardImpl by calling the copy contructor
     */
    @Override
    public Object clone() {
        return new GameBoardImpl(this);
    }

    /**
     * returns a letter between A..H for a given column number
     *
     * @param col
     * @return String letter representing the column. Returns "null" when column
     *         not between 1..8
     */
    public static String getColString(int col) {
        assert col > 0 && col <= 8;
        switch (col) {
            case 1:
                return "A";
            case 2:
                return "B";
            case 3:
                return "C";
            case 4:
                return "D";
            case 5:
                return "E";
            case 6:
                return "F";
            case 7:
                return "G";
            case 8:
                return "H";
            default:
                return null;
        }
    }

}
