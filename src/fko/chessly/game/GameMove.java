/*
 * <p>GPL Dislaimer</p>
 * <p>
 * "Chessly by Frank Kopp"
 * Copyright (c) 2003-2015 Frank Kopp
 * mail-to:frank@familie-kopp.de
 *
 * This file is part of "Chessly by Frank Kopp".
 *
 * "Chessly by Frank Kopp" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * "Chessly by Frank Kopp" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Chessly by Frank Kopp"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </p>
 *
 *
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
