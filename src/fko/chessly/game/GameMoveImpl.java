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

import java.io.Serializable;

import fko.chessly.game.pieces.King;
import fko.chessly.game.pieces.Pawn;


/**
 * <p>A class representing a move in a Chessly game.</p>
 * <p>It also contains a value for this move if already calculated.</p>
 * <p>A move can only have a meaningful value when it is related to a actual board.</p>
 * <p>A Move object also holds all information to undo a move and restore the last board.
 * This needs to include castling rights, en passant moves, etc.</p>
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class GameMoveImpl implements GameMove, Cloneable, Serializable {

    private static final long serialVersionUID = 7011908028071836016L;

    // Independent of board
    private final GamePosition _fromField;
    private final GamePosition _toField;
    private final GamePiece _pieceMoved;

    // Last board State
    // these can only be know in a context of a board - must be set by makeMove
    // of Board
    private int _halfMoveNumber;
    private GamePiece _pieceCaptured;
    private GamePiece _promotedTo;
    private int _halfMoveClock;
    private boolean _wasCheck;
    private boolean _wasCheckMate;
    private boolean _wasStaleMate;
    private boolean _wasEnPassantCapture;
    private GamePosition _enPassantCapturePosition;
    private boolean _enPassantNextMovePossible;
    protected GameCastling _castlingType = GameCastling.NOCASTLING;
    protected GameCastling[] _castlingRights = new GameCastling[4];

    // this is the evaluation of a board where this move was the last move
    private int _value;


    /**
     * Create a new Move from a fromField and a toField with a _pieceMoved
     *
     * @param _fromField
     * @param _toField
     * @param _pieceMoved
     */
    public GameMoveImpl(GamePosition _fromField, GamePosition _toField, GamePiece _pieceMoved) {
        this._fromField = _fromField;
        this._toField = _toField;
        this._pieceMoved = _pieceMoved;
        this._halfMoveNumber = 0;
        this._pieceCaptured = null;
        this._promotedTo = null;
        this._halfMoveClock = 0;
        this._wasCheck = false;
        this._wasCheckMate = false;
        this._wasStaleMate = false;
        this._wasEnPassantCapture = false;
        this._enPassantNextMovePossible = false;
        // init castling rights
        for (GameCastling c : GameCastling.values()) {
            if (c.isValid())
                _castlingRights[c.ordinal()] = c;
        }
        this._value = GameMove.VALUE_UNKNOWN;
    }

    /**
     * Creates a new Move based as a copy of a given move.
     * @param move
     */
    public GameMoveImpl(GameMove move) {
        this(move.getFromField(), move.getToField(), move.getMovedPiece());
        this._halfMoveNumber = move.getHalfMoveNumber();
        this._pieceCaptured = move.getCapturedPiece();
        this._promotedTo = move.getPromotedTo();
        this._halfMoveClock = move.getHalfMoveClock();
        this._wasCheck = move.getWasCheck();
        this._wasCheckMate = move.getWasCheckMate();
        this._wasStaleMate = move.getWasStaleMate();
        this._wasEnPassantCapture = move.getWasEnPassantCapture();
        this._enPassantNextMovePossible = move.isEnPassantNextMovePossible();
        this._castlingRights = move.getCastlingRights();
        this._value = move.getValue();
    }

    /**
     * @param _halfMoveNumber
     *            the _halfMoveNumber to set
     */
    @Override
    public void setHalfMoveNumber(int _halfMoveNumber) {
        this._halfMoveNumber = _halfMoveNumber;
    }

    /**
     * @return the _moveNumber
     */
    @Override
    public int getHalfMoveNumber() {
        return _halfMoveNumber;
    }

    /**
     * @return the _fromField
     */
    @Override
    public GamePosition getFromField() {
        return _fromField;
    }

    /**
     * @return the _toField
     */
    @Override
    public GamePosition getToField() {
        return _toField;
    }

    /**
     * @return the _pieceMoved
     */
    @Override
    public GamePiece getMovedPiece() {
        return _pieceMoved;
    }

    /**
     * @param _pieceCaptured
     *            the _pieceCaptured to set
     */
    @Override
    public void setCapturedPiece(GamePiece _pieceCaptured) {
        this._pieceCaptured = _pieceCaptured;
    }

    /**
     * @return the _pieceCaptured
     */
    @Override
    public GamePiece getCapturedPiece() {
        return _pieceCaptured;
    }

    /**
     * If the move is a pawn promotion then this is the piece the pawn is
     * promoted to
     *
     * @param piece
     *            the pawn will be promoted to
     */
    @Override
    public void setPromotedTo(GamePiece _promotedTo) {
        this._promotedTo = _promotedTo;
    }

    /**
     * If the move is a pawn promotion then this is the piece the pawn is
     * promoted to
     *
     * @return piece the pawn will be promoted to
     */
    @Override
    public GamePiece getPromotedTo() {
        return _promotedTo;
    }

    /**
     * Stores the half move clock before the move (is reset when capture or pawn move)
     * @param halfmoveClock
     */
    @Override
    public void setHalfMoveClock(int halfmoveClock) {
        _halfMoveClock = halfmoveClock;
    }

    /**
     * If the move is a pawn promotion then this is the piece the pawn is promoted to
     * @return piece the pawn will be promoted to
     */
    @Override
    public int getHalfMoveClock() {
        return _halfMoveClock;
    }

    /**
     * @param _wasCheck
     *            the _wasCheck to set
     */
    @Override
    public void setWasCheck(boolean _wasCheck) {
        this._wasCheck = _wasCheck;
    }

    /**
     * @return the _wasCheck
     */
    @Override
    public boolean getWasCheck() {
        return _wasCheck;
    }

    /**
     * @param _wasCheckMate
     *            the _wasCheckMate to set
     */
    @Override
    public void setWasCheckMate(boolean _wasCheckMate) {
        this._wasCheckMate = _wasCheckMate;
    }

    /**
     * @return the _wasCheckMate
     */
    @Override
    public boolean getWasCheckMate() {
        return _wasCheckMate;
    }

    /**
     * @param _wasCheckMate
     *            the _wasCheckMate to set
     */
    @Override
    public void setWasStaleMate(boolean _wasStaleMate) {
        this._wasStaleMate = _wasStaleMate;
    }

    /**
     * @return the _wasStaleMate
     */
    @Override
    public boolean getWasStaleMate() {
        return _wasStaleMate;
    }

    @Override
    public void setWasEnPassantCapture(boolean _wasEnPassantCapture) {
        this._wasEnPassantCapture = _wasEnPassantCapture;
    }

    @Override
    public boolean getWasEnPassantCapture() {
        return _wasEnPassantCapture;
    }

    @Override
    public void setEnPassantCapturePosition(
            GamePosition _enPassantCapturePosition) {
        this._enPassantCapturePosition = _enPassantCapturePosition;
    }

    @Override
    public GamePosition getEnPassantCapturePosition() {
        return _enPassantCapturePosition;
    }

    /**
     * @param _enPassantNextMovePossible
     *            the _enPassantNextMovePossible to set
     */
    @Override
    public void setEnPassantNextMovePossible(boolean _enPassantNextMovePossible) {
        this._enPassantNextMovePossible = _enPassantNextMovePossible;
    }

    /**
     * @return the _enPassantNextMovePossible
     */
    @Override
    public boolean isEnPassantNextMovePossible() {
        return _enPassantNextMovePossible;
    }

    @Override
    public void setCastlingType(GameCastling _castlingType) {
        this._castlingType = _castlingType;
    }

    @Override
    public GameCastling getCastlingType() {
        return _castlingType;
    }

    @Override
    public GameCastling[] getCastlingRights() {
        GameCastling[] copy = new GameCastling[4];
        System.arraycopy(_castlingRights, 0, copy, 0, _castlingRights.length);
        return copy;
    }

    @Override
    public void setCastlingRights(GameCastling[] _castlingRights) {
        // init castling rights
        this._castlingRights = _castlingRights;
    }

    /**
     * Setter for value.<br/>
     * A move can only have a meaningful value when it is related to a actual
     * board.
     *
     * @param value
     */
    @Override
    public synchronized void setValue(int value) {
        this._value = value;
    }

    /**
     * Getter for value.<br/>
     * Returns Move.VALUE_UNKNOWN when value is not set.<br/>
     * A move can only have a meaningful value when it is related to a actual
     * board.
     *
     * @return value
     */
    @Override
    public synchronized int getValue() {
        return _value;
    }

    /**
     * Returns a standard move notation without move number.
     *
     * @return Long algebraic notation
     */
    @Override
    public String toLongAlgebraicNotationString() {
        StringBuilder s = new StringBuilder();

        // castling
        if (_pieceMoved instanceof King && _fromField.getFile() - _toField.getFile() == 2)
            s.append("O-O-O"); // queen side
        else if (_pieceMoved instanceof King && _fromField.getFile() - _toField.getFile() == -2)
            s.append("O-O"); // king side
        else {
            // piece letter or nothing for pawn
            if (_pieceMoved != null && !(_pieceMoved instanceof Pawn))
                s.append(_pieceMoved.toNotationString());
            // field
            s.append(_fromField.toNotationString());
            // "moves to"="-" or "captured at"="x"
            if (_pieceCaptured != null) {
                s.append("x");
            } else {
                s.append("-");
            }
            s.append(_toField.toNotationString());
            if (this.getPromotedTo() != null)
                s.append(this.getPromotedTo().toNotationString());
            if (this._wasCheck)
                s.append("+");
            if (this._wasCheckMate)
                s.append("+");
            if (this._wasStaleMate)
                s.append(" 1:1");
        }
        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        // castling
        if (_pieceMoved instanceof King && _fromField.getFile() - _toField.getFile() == 2)
            s.append("O-O-O"); // queen side
        else if (_pieceMoved instanceof King && _fromField.getFile() - _toField.getFile() == -2)
            s.append("O-O"); // king side
        else {
            // piece letter or nothing for pawn
            if (_pieceMoved != null && !(_pieceMoved instanceof Pawn))
                s.append(_pieceMoved.toNotationString());
            // field
            s.append(_fromField.toNotationString());
            // "moves to"="-" or "captured at"="x"
            if (_pieceCaptured != null) {
                s.append("x");
            } else {
                s.append("-");
            }
            s.append(_toField.toNotationString());
            if (this.getPromotedTo() != null)
                s.append(this.getPromotedTo().toNotationString());
            if (this._wasCheck)
                s.append("+");
            if (this._wasCheckMate)
                s.append("+");
            if (this._wasStaleMate)
                s.append(" 1:1");
        }
        return s.toString();
    }

    /* (non-Javadoc)
     * @see fko.chessly.game.GameMove#toSimpleString()
     */
    @Override
    public String toSimpleString() {
        StringBuilder s = new StringBuilder();
        // field
        s.append(_fromField.toNotationString());
        s.append(_toField.toNotationString());
        return s.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_fromField == null) ? 0 : _fromField.hashCode());
        result = prime * result
                + ((_pieceMoved == null) ? 0 : _pieceMoved.hashCode());
        result = prime * result
                + ((_toField == null) ? 0 : _toField.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GameMoveImpl)) {
            return false;
        }
        GameMoveImpl other = (GameMoveImpl) obj;
        if (_fromField == null) {
            if (other._fromField != null) {
                return false;
            }
        } else if (!_fromField.equals(other._fromField)) {
            return false;
        }
        if (_pieceMoved == null) {
            if (other._pieceMoved != null) {
                return false;
            }
        } else if (!_pieceMoved.equals(other._pieceMoved)) {
            return false;
        }
        if (_toField == null) {
            if (other._toField != null) {
                return false;
            }
        } else if (!_toField.equals(other._toField)) {
            return false;
        } else if (!_promotedTo.equals(other._promotedTo)) {
            return false;
        }
        return true;
    }

    @Override
    public Object clone() {
        return new GameMoveImpl(this);
    }



}
