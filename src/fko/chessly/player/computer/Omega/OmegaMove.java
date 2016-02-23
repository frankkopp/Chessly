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

import fko.chessly.player.computer.FluxEngine.Color;
import fko.chessly.player.computer.FluxEngine.MoveType;
import fko.chessly.player.computer.FluxEngine.Piece;
import fko.chessly.player.computer.FluxEngine.PieceType;

/**
 * This class represents a move in the Omega Engine.
 * The data structure is optimized for speed using only int and enum.
 *
 * @author Frank
 */
public class OmegaMove {

    // MASKs
    private static final int SQUARE_bitMASK = 0x7F;
    private static final int PIECE_bitMASK = 0xF;
    private static final int MOVETYPE_bitMASK = 0x7;

    // Bit operation values
    private static final int START_SQUARE_SHIFT = 0;
    private static final int START_SQUARE_MASK = SQUARE_bitMASK << START_SQUARE_SHIFT;

    private static final int END_SQUARE_SHIFT = 7;
    private static final int END_MASK = SQUARE_bitMASK << END_SQUARE_SHIFT;

    private static final int PIECE_SHIFT = 14;
    private static final int PIECE_MASK = PIECE_bitMASK << PIECE_SHIFT;

    private static final int TARGET_SHIFT = 18;
    private static final int TARGET_MASK = PIECE_bitMASK << TARGET_SHIFT;

    private static final int PROMOTION_SHIFT = 22;
    private static final int PROMOTION_MASK = PIECE_bitMASK << PROMOTION_SHIFT;

    private static final int MOVETYPE_SHIFT = 25;
    private static final int MOVETYPE_MASK = MOVETYPE_bitMASK << MOVETYPE_SHIFT;

    // no instantiation of this class
    private OmegaMove() {
    }

    /**
     * Create a OmegaMove.
     */
    static int createMove(OmegaMoveType movetype, OmegaSquare start, OmegaSquare end,
            OmegaPiece piece, OmegaPiece target, OmegaPiece promotion) {

        int move = 0;

        // Encode start
        move |= start.ordinal() << START_SQUARE_SHIFT;

        // Encode end
        move |= end.ordinal() << END_SQUARE_SHIFT;

        // Encode piece
        assert piece == Piece.NOPIECE
                || (Piece.getChessman(piece) == PieceType.PAWN)
                || (Piece.getChessman(piece) == PieceType.KNIGHT)
                || (Piece.getChessman(piece) == PieceType.BISHOP)
                || (Piece.getChessman(piece) == PieceType.ROOK)
                || (Piece.getChessman(piece) == PieceType.QUEEN)
                || (Piece.getChessman(piece) == PieceType.KING);
        assert piece == Piece.NOPIECE
                || (Piece.getColor(piece) == Color.WHITE)
                || (Piece.getColor(piece) == Color.BLACK);
        move |= piece << CHESSMAN_PIECE_SHIFT;

        // Encode target
        assert target == Piece.NOPIECE
                || (Piece.getChessman(target) == PieceType.PAWN)
                || (Piece.getChessman(target) == PieceType.KNIGHT)
                || (Piece.getChessman(target) == PieceType.BISHOP)
                || (Piece.getChessman(target) == PieceType.ROOK)
                || (Piece.getChessman(target) == PieceType.QUEEN);
        assert target == Piece.NOPIECE
                || (Piece.getColor(target) == Color.WHITE)
                || (Piece.getColor(target) == Color.BLACK);
        move |= target << TARGET_PIECE_SHIFT;

        // Encode promotion
        assert promotion == Piece.NOPIECE
                || (promotion == PieceType.KNIGHT)
                || (promotion == PieceType.BISHOP)
                || (promotion == PieceType.ROOK)
                || (promotion == PieceType.QUEEN);
        move |= promotion << PROMOTION_SHIFT;

        // Encode move
        assert (type == MoveType.NORMAL)
        || (type == MoveType.PAWNDOUBLE)
        || (type == MoveType.PAWNPROMOTION)
        || (type == MoveType.ENPASSANT)
        || (type == MoveType.CASTLING);
        move |= type << MOVE_SHIFT;

        return move;
    }

}
