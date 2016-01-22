/*
 * =============================================================================
 * Pulse
 *
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

final class Evaluation {

    static final int TEMPO = 1;

    static int materialWeight = 100;
    static int mobilityWeight = 80;
    private static final int MAX_WEIGHT = 100;

    /**
     * Evaluates the board.
     *
     * @param board
     *            the board.
     * @return the evaluation value in centipawns.
     */
    int evaluate(Board board) {
        assert board != null;

        // for debugging more expensive eval function to se if board cache actually helps
        //int x=0;
        //for(int i=0;i<500;i++) {x=x+i;};

        // Initialize
        int myColor = board.activeColor;
        int oppositeColor = Color.opposite(myColor);
        int value = 0;

        // Evaluate material
        int materialScore = (evaluateMaterial(myColor, board) - evaluateMaterial(
                oppositeColor, board)) * materialWeight / MAX_WEIGHT;
        value += materialScore;

        // Evaluate mobility
        int mobilityScore = (evaluateMobility(myColor, board) - evaluateMobility(
                oppositeColor, board)) * mobilityWeight / MAX_WEIGHT;
        value += mobilityScore;

        // Add Tempo
        value += TEMPO;

        // This is just a safe guard to protect against overflow in our
        // evaluation
        // function.
        if (value <= -Value.CHECKMATE_THRESHOLD
                || value >= Value.CHECKMATE_THRESHOLD) {
            assert false;
        }

        return value;
    }

    private int evaluateMaterial(int color, Board board) {
        assert Color.isValid(color);
        assert board != null;

        int material = board.material[color];

        // Add bonus for bishop pair
        if (board.bishops[color].size() >= 2) {
            material += 50;
        }

        return material;
    }

    private int evaluateMobility(int color, Board board) {
        assert Color.isValid(color);
        assert board != null;

        int knightMobility = 0;
        for (long squares = board.knights[color].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            knightMobility += evaluateMobilityForPiece(color, board, square,
                    Board.knightDirections);
        }

        int bishopMobility = 0;
        for (long squares = board.bishops[color].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            bishopMobility += evaluateMobilityForPiece(color, board, square,
                    Board.bishopDirections);
        }

        int rookMobility = 0;
        for (long squares = board.rooks[color].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            rookMobility += evaluateMobilityForPiece(color, board, square,
                    Board.rookDirections);
        }

        int queenMobility = 0;
        for (long squares = board.queens[color].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            queenMobility += evaluateMobilityForPiece(color, board, square,
                    Board.queenDirections);
        }

        return knightMobility * 4 + bishopMobility * 5 + rookMobility * 2
                + queenMobility;
    }

    private int evaluateMobilityForPiece(int color, Board board, int square, int[] moveDelta) {
        assert Color.isValid(color);
        assert board != null;
        assert Piece.isValid(board.board[square]);
        assert moveDelta != null;

        int mobility = 0;
        boolean sliding = PieceType.isSliding(Piece
                .getType(board.board[square]));

        for (int delta : moveDelta) {
            int targetSquare = square + delta;

            while (Square.isValid(targetSquare)) {
                ++mobility;

                if (sliding && board.board[targetSquare] == Piece.NOPIECE) {
                    targetSquare += delta;
                } else {
                    break;
                }
            }
        }

        return mobility;
    }

}
