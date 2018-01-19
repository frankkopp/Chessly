/*
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 * ============================================================================
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
package fko.chessly.player.computer.PulseEngine;


final class PieceType {

    static final int MASK = 0x7;

    static final int PAWN = 0;
    static final int KNIGHT = 1;
    static final int BISHOP = 2;
    static final int ROOK = 3;
    static final int QUEEN = 4;
    static final int KING = 5;
    static final int NOPIECETYPE = 6;

    static final int[] values = { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING };

    // Piece values as defined by Larry Kaufman
    static final int PAWN_VALUE = 100;
    static final int KNIGHT_VALUE = 325;
    static final int BISHOP_VALUE = 325;
    static final int ROOK_VALUE = 500;
    static final int QUEEN_VALUE = 975;
    static final int KING_VALUE = 20000;

    private PieceType() {
    }

    static boolean isValid(int pieceType) {
        switch (pieceType) {
            case PAWN:
            case KNIGHT:
            case BISHOP:
            case ROOK:
            case QUEEN:
            case KING:
                return true;
            case NOPIECETYPE:
            default:
                return false;
        }
    }

    static boolean isValidPromotion(int pieceType) {
        switch (pieceType) {
            case KNIGHT:
            case BISHOP:
            case ROOK:
            case QUEEN:
                return true;
            case PAWN:
            case KING:
            case NOPIECETYPE:
            default:
                return false;
        }
    }

    static boolean isSliding(int pieceType) {
        switch (pieceType) {
            case BISHOP:
            case ROOK:
            case QUEEN:
                return true;
            case PAWN:
            case KNIGHT:
            case KING:
                return false;
            case NOPIECETYPE:
            default:
                throw new IllegalArgumentException();
        }
    }

    static int getValue(int pieceType) {
        switch (pieceType) {
            case PAWN:
                return PAWN_VALUE;
            case KNIGHT:
                return KNIGHT_VALUE;
            case BISHOP:
                return BISHOP_VALUE;
            case ROOK:
                return ROOK_VALUE;
            case QUEEN:
                return QUEEN_VALUE;
            case KING:
                return KING_VALUE;
            case NOPIECETYPE:
            default:
                throw new IllegalArgumentException();
        }
    }

    static String toChar(int pieceType) {
        switch (pieceType) {
            case PAWN:
                return "P";
            case KNIGHT:
                return "N";
            case BISHOP:
                return "B";
            case ROOK:
                return "R";
            case QUEEN:
                return "Q";
            case KING:
                return "K";
            case NOPIECETYPE:
                return "";
            default:
                throw new IllegalArgumentException();
        }
    }

    static int fromChar(String s) {
        switch (s) {
            case "":
            case " ":
            case "P":
                return PAWN;
            case "N":
                return KNIGHT;
            case "B":
                return BISHOP;
            case "R":
                return ROOK;
            case "Q":
                return QUEEN;
            case "K":
                return KING;
            default:
                throw new IllegalArgumentException();
        }
    }

}
