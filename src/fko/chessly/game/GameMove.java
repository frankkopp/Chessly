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


/**
 * <p>This interface describes a move in Chessly.</p>
 * <p>It also contains a value for this move if already calculated.</p>
 * <p>A move can only have a meaningful value when it is related to a actual board.</p>
 * <p>A Move object also holds all informaion to undo a move restore the last board.
 * This needs to include castling rights, en passant moves, etc.</p>
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public interface GameMove {

    int VALUE_UNKNOWN = Integer.MIN_VALUE;

    /**
     * Returns the number of the half move of this move. Returns 0 if number is unknown yet
     * @return half move number. 0 when unknown
     */
    int getHalfMoveNumber();

    /**
     * When the Move is committed to a Board the board stores the half move number in the Move
     * @param i - half move number of the comitted move
     */
    void setHalfMoveNumber(int i);

    /**
     * Returns the from Field of the move
     * @return {@link Field}
     */
    GamePosition getFromField();

    /**
     * Returns the to Field of the move
     * @return {@link Field}
     */
    GamePosition getToField();

    /**
     * Each Move has a Piece which in turn has a color.
     * @return {@link GamePiece}ece moved with this Move
     */
    GamePiece getMovedPiece();

    /**
     * When the Move is committed to a Board the board stores the captured piece in the Move
     * @param _pieceCaptured
     */
    void setCapturedPiece(GamePiece _pieceCaptured);

    /**
     * Return a captured Piece or null when unknown or none
     * @return captured {@link GamePiece}ece or null when unknown or none
     */
    GamePiece getCapturedPiece();

    /**
     * If the move is a pawn promotion then this is the piece the pawn is promoted to
     * @param piece the pawn will be promoted to
     */
    public void setPromotedTo(GamePiece _promotedTo);

    /**
     * If the move is a pawn promotion then this is the piece the pawn is promoted to
     * @return piece the pawn will be promoted to
     */
    public GamePiece getPromotedTo();

    public void setWasCheck(boolean _wasCheck);
    public boolean getWasCheck();

    public void setWasCheckMate(boolean _wasStaleMate);
    public boolean getWasCheckMate();

    public void setWasStaleMate(boolean _wasStaleMate);
    public boolean getWasStaleMate();

    public void setEnPassantNextMovePossible(boolean _enPassantNextMovePossible);
    public boolean isEnPassantNextMovePossible();

    public void setWasEnPassantCapture(boolean _wasEnPassantCapture);
    public boolean getWasEnPassantCapture();

    void setCastlingRights(GameCastling[] _castlingRights);
    public GameCastling[] getCastlingRights();

    public void setCastlingType(GameCastling _castlingType);
    public GameCastling getCastlingType();

    public void setEnPassantCapturePosition(GamePosition _enPassantCapturePosition);
    public GamePosition getEnPassantCapturePosition();

    void setValue(int value);
    int getValue();

    public String toLongAlgebraicNotationString();
    @Override
    public String toString();

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

}
