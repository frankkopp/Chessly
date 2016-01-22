/*
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 * ================================================================================
 * Chessly
 *
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
 */
package fko.chessly.player.computer.PulseEngine;

/**
 * This class represents a move as a int value. The fields are represented by
 * the following bits.
 * <p/>
 * <code> 0 -  2</code>: type (required) <code> 3 -  9</code>: origin square
 * (required) <code>10 - 16</code>: target square (required)
 * <code>17 - 21</code>: origin piece (required) <code>22 - 26</code>: target
 * piece (optional) <code>27 - 29</code>: promotion type (optional)
 */
final class Move {

    // These are our bit masks
    private static final int TYPE_SHIFT = 0;
    private static final int TYPE_MASK = MoveType.MASK << TYPE_SHIFT;
    private static final int ORIGINSQUARE_SHIFT = 3;
    private static final int ORIGINSQUARE_MASK = Square.MASK << ORIGINSQUARE_SHIFT;
    private static final int TARGETSQUARE_SHIFT = 10;
    private static final int TARGETSQUARE_MASK = Square.MASK << TARGETSQUARE_SHIFT;
    private static final int ORIGINPIECE_SHIFT = 17;
    private static final int ORIGINPIECE_MASK = Piece.MASK << ORIGINPIECE_SHIFT;
    private static final int TARGETPIECE_SHIFT = 22;
    private static final int TARGETPIECE_MASK = Piece.MASK << TARGETPIECE_SHIFT;
    private static final int PROMOTION_SHIFT = 27;
    private static final int PROMOTION_MASK = PieceType.MASK << PROMOTION_SHIFT;

    // We don't use 0 as a null value to protect against errors.
    static final int NOMOVE = (MoveType.NOMOVETYPE << TYPE_SHIFT)
	    | (Square.NOSQUARE << ORIGINSQUARE_SHIFT)
	    | (Square.NOSQUARE << TARGETSQUARE_SHIFT)
	    | (Piece.NOPIECE << ORIGINPIECE_SHIFT)
	    | (Piece.NOPIECE << TARGETPIECE_SHIFT)
	    | (PieceType.NOPIECETYPE << PROMOTION_SHIFT);

    private Move() {
    }

    static int valueOf(int type, int originSquare, int targetSquare,
	    int originPiece, int targetPiece, int promotion) {
	int move = 0;

	// Encode type
	assert MoveType.isValid(type);
	move |= type << TYPE_SHIFT;

	// Encode origin square
	assert Square.isValid(originSquare);
	move |= originSquare << ORIGINSQUARE_SHIFT;

	// Encode target square
	assert Square.isValid(targetSquare);
	move |= targetSquare << TARGETSQUARE_SHIFT;

	// Encode origin piece
	assert Piece.isValid(originPiece);
	move |= originPiece << ORIGINPIECE_SHIFT;

	// Encode target piece
	assert Piece.isValid(targetPiece) || targetPiece == Piece.NOPIECE;
	move |= targetPiece << TARGETPIECE_SHIFT;

	// Encode promotion
	assert PieceType.isValidPromotion(promotion)
		|| promotion == PieceType.NOPIECETYPE;
	move |= promotion << PROMOTION_SHIFT;

	return move;
    }

    static int getType(int move) {
	int type = (move & TYPE_MASK) >>> TYPE_SHIFT;
	assert MoveType.isValid(type);

	return type;
    }

    static int getOriginSquare(int move) {
	int originSquare = (move & ORIGINSQUARE_MASK) >>> ORIGINSQUARE_SHIFT;
	assert Square.isValid(originSquare);

	return originSquare;
    }

    static int getTargetSquare(int move) {
	int targetSquare = (move & TARGETSQUARE_MASK) >>> TARGETSQUARE_SHIFT;
	assert Square.isValid(targetSquare);

	return targetSquare;
    }

    static int getOriginPiece(int move) {
	int originPiece = (move & ORIGINPIECE_MASK) >>> ORIGINPIECE_SHIFT;
	assert Piece.isValid(originPiece);

	return originPiece;
    }

    static int getTargetPiece(int move) {
	int targetPiece = (move & TARGETPIECE_MASK) >>> TARGETPIECE_SHIFT;
	assert Piece.isValid(targetPiece) || targetPiece == Piece.NOPIECE;

	return targetPiece;
    }

    static int getPromotion(int move) {
	int promotion = (move & PROMOTION_MASK) >>> PROMOTION_SHIFT;
	assert PieceType.isValidPromotion(promotion)
		|| promotion == PieceType.NOPIECETYPE;

	return promotion;
    }

    /**
     * Create a new Move from a simple notation String "e2-e4"
     * 
     * @param notation
     *            String
     * @param _pieveMoved
     * @return
     */
    static public int createNewMove(String notation, int color) {

	int move = moveFromNotation(notation, color);

	return move;
    }

    /**
     * @param notation
     * @param color
     * @throws IllegalArgumentException
     */
    static private int moveFromNotation(String notation, int color)
	    throws IllegalArgumentException {

	String[] splitString = new String[8];
	String patternCasling = "(O-O)((-O)?)";
	String pattern = "([KQBNR]?)([a-h])([1-8])([x-])([a-h])([1-8])([QBNR]?)(e\\.p\\.)?";
	// $1 = Piece, $2 = from Square, $3 = move type, $4 = to Square, $5 =
	// PromotionType, $en passant

	int type;
	int origin;
	int target;
	int originPiece;
	int originPieceType;
	int targetPiece;
	int promotion;

	if (notation.matches(patternCasling)) {
	    type = MoveType.CASTLING;

	    String s2 = notation.replaceAll(patternCasling, "$2");

	    if (s2.isEmpty()) { // King side
		switch (color) {
		case Color.WHITE:
		    origin = Square.e1;
		    target = Square.g1;
		    originPiece = Piece.WHITE_KING;
		    break;
		case Color.BLACK:
		    origin = Square.e8;
		    target = Square.g8;
		    originPiece = Piece.BLACK_KING;
		    break;
		default:
		    throw new RuntimeException();
		}
		// System.out.println("CASTLING KING SIDE");
	    } else { // Queen side
		switch (color) {
		case Color.WHITE:
		    origin = Square.e1;
		    target = Square.c1;
		    originPiece = Piece.WHITE_KING;
		    break;
		case Color.BLACK:
		    origin = Square.e8;
		    target = Square.c8;
		    originPiece = Piece.BLACK_KING;
		    break;
		default:
		    throw new RuntimeException();
		}
		// System.out.println("CASTLING QUEEN SIDE");
	    }
	    targetPiece = Piece.NOPIECE;
	    promotion = PieceType.NOPIECETYPE;

	    return Move.valueOf(type, origin, target, originPiece, targetPiece,
		    promotion);

	} else if (notation.matches(pattern)) {

	    splitString[0] = notation.replaceAll(pattern, "$1"); // piece
	    splitString[1] = notation.replaceAll(pattern, "$2"); // file
	    splitString[2] = notation.replaceAll(pattern, "$3"); // rank
	    splitString[3] = notation.replaceAll(pattern, "$4"); // op
	    splitString[4] = notation.replaceAll(pattern, "$5"); // file
	    splitString[5] = notation.replaceAll(pattern, "$6"); // rank
	    splitString[6] = notation.replaceAll(pattern, "$7"); // promotion
	    splitString[7] = notation.replaceAll(pattern, "$8"); // en passant

	    origin = Square.valueOf(splitString[1], splitString[2]);
	    if (!Square.isValid(origin))
		throw new IllegalArgumentException(
			"Not a valid origin square: " + splitString[1]
				+ splitString[2]);

	    target = Square.valueOf(splitString[4], splitString[5]);
	    if (!Square.isValid(target))
		throw new IllegalArgumentException(
			"Not a valid origin square: " + splitString[4]
				+ splitString[5]);

	    originPieceType = PieceType.fromChar(splitString[0]);
	    originPiece = Piece.valueOf(color, originPieceType);

	    // PieceType is unknown in notation - needs an actual board to be
	    // determined
	    targetPiece = Piece.NOPIECE;

	    if (splitString[6].isEmpty()) {
		promotion = PieceType.NOPIECETYPE;
	    } else {
		promotion = PieceType.fromChar(splitString[4]);
	    }

	    // NORMAL, PAWNDOUBLE, PAWNPROMOTION, ENPASSANT
	    type = MoveType.NORMAL;
	    if (Piece.getType(originPiece) == PieceType.PAWN) {
		if (Square.isPawnBaseRank(color, origin)
			&& ((target == origin
				+ (2 * Board.pawnDirections[color][0])))) {
		    // System.out.println("PAWNDOUBLE");
		    type = MoveType.PAWNDOUBLE;
		} else if (target == origin + Board.pawnDirections[color][0]) {
		    // System.out.println("PAWNNORMAL");
		    type = MoveType.NORMAL;
		} else if (target == origin + Board.pawnDirections[color][1]
			|| target == origin + Board.pawnDirections[color][2]) {
		    if (splitString[5].isEmpty()) {
			// System.out.println("PAWNNORMAL CAPTURE");
			type = MoveType.CAPTURE;
		    } else if (splitString[5].equals("e.p.")) { // en passant -
								// target piece
								// is unknow
								// when from
								// notation
			// System.out.println("ENPASSANT CAPTURE");
			type = MoveType.ENPASSANT;
			targetPiece = Piece.valueOf(Color.opposite(color),
				PieceType.PAWN);
		    }
		}
		if (Square.isPromotionRank(color, target)) {
		    // System.out.println("PAWNPROMOTION: "+promotion);
		    type = MoveType.PAWNPROMOTION;
		}
	    }
	    if (splitString[3].equals("x")) {
		// this might be the case althoug we do not know if
		// there is a piece on the target square.
		type = MoveType.CAPTURE;
	    }

	    return Move.valueOf(type, origin, target, originPiece, targetPiece,
		    promotion);

	} else {
	    throw new IllegalArgumentException("Not a valid move notation: "
		    + notation);
	}

    }

    /**
     * 
     * @param move
     * @return
     */
    public static String toString(int move) {

	if (move == Move.NOMOVE)
	    return "NO MOVE";

	String s = "";

	int type = Move.getType(move);
	int origin = Move.getOriginSquare(move);
	int target = Move.getTargetSquare(move);
	int oPiece = Move.getOriginPiece(move);
	int tPiece = Move.getTargetPiece(move);
	int promotion = Move.getPromotion(move);

	s += Piece.getType(oPiece) == PieceType.PAWN ? "" : PieceType
		.toChar(Piece.getType(oPiece));
	s += Square.toString(origin);
	s += tPiece == Piece.NOPIECE ? "-" : "x";
	s += Square.toString(target);

	switch (type) {
	case MoveType.PAWNPROMOTION:
	    s += PieceType.toChar(promotion);
	    break;
	case MoveType.ENPASSANT:
	    s += "e.p.";
	    s = s.replace("-", "x");
	    break;
	case MoveType.CASTLING:
	    s = Square.getFile(origin) < Square.getFile(target) ? "O-O"
		    : "O-O-O";
	}

	// pawn capture
	if (Piece.getType(oPiece) == PieceType.PAWN
		&& Square.getFile(origin) != Square.getFile(target)) {
	    s = s.replace("-", "x");
	}

	return s;
    }

}
