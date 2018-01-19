/*
 * =============================================================================
 * Pulse
 *
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 *
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

/**
 * MoveGenerator class
 *
 * @author fkopp
 * @author Phokham Nonava
 */
final class MoveGenerator {

    private final MoveList _moves = new MoveList();

    /**
     * @param board
     * @param depth
     * @param isCheck
     * @return
     */
    MoveList getLegalMoves(Board board, int depth, boolean isCheck) {
        assert board != null;

        MoveList legalMoves = getPseudoLegalMoves(board, depth, isCheck);

        int size = legalMoves.size;
        legalMoves.size = 0;
        for (int i = 0; i < size; ++i) {
            int move = legalMoves.entries[i].move;

            board.makeMove(move);
            if (!board.isAttacked(Bitboard.next(board.kings[Color.opposite(board.activeColor)].squares), board.activeColor)) {
                legalMoves.entries[legalMoves.size++].move = move;
            }
            board.undoMove();
        }

        return legalMoves;
    }

    /**
     * @param board
     * @param depth if depth==0 only quiescence moves will be generated. >1 for normal moves
     * @param isCheck
     * @return
     */
    MoveList getPseudoLegalMoves(Board board, int depth, boolean isCheck) {
        assert board != null;

        // Reset the existing MoveList
        _moves.size = 0;

        if (depth > 0) {

            // Generate main moves
            addMoves(_moves, board);

            // if not in check also add castling if available
            if (!isCheck) {
                int square = Bitboard.next(board.kings[board.activeColor].squares);
                addCastlingMoves(_moves, square, board);
            }
        } else {

            // Generate quiescent moves
            addMoves(_moves, board);

            // if not in check we only want capturing moves
            if (!isCheck) {
                // filter out all non-capturing moves
                int size = _moves.size;
                _moves.size = 0;
                for (int i = 0; i < size; ++i) {
                    if (Move.getTargetPiece(_moves.entries[i].move) != Piece.NOPIECE) {
                        // Add only capturing moves
                        _moves.entries[_moves.size++].move = _moves.entries[i].move;
                    }
                }
            }
        }

        // rate for (most valued victim - least valued attacker)
        _moves.rateFromMVVLVA();
        // sort the move list for better pruning
        _moves.sort();

        return _moves;
    }

    /**
     * @param list
     * @param board
     */
    private static void addMoves(MoveList list, Board board) {
        assert list != null;

        int activeColor = board.activeColor;

        for (long squares = board.pawns[activeColor].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            addPawnMoves(list, square, board);
        }
        for (long squares = board.knights[activeColor].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            addMoves(list, square, Board.knightDirections, board);
        }
        for (long squares = board.bishops[activeColor].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            addMoves(list, square, Board.bishopDirections, board);
        }
        for (long squares = board.rooks[activeColor].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            addMoves(list, square, Board.rookDirections, board);
        }
        for (long squares = board.queens[activeColor].squares; squares != 0; squares &= squares - 1) {
            int square = Bitboard.next(squares);
            addMoves(list, square, Board.queenDirections, board);
        }
        int square = Bitboard.next(board.kings[activeColor].squares);
        addMoves(list, square, Board.kingDirections, board);
    }

    private static void addMoves(MoveList list, int originSquare, int[] moveDelta, Board board) {

        assert list != null;
        assert Square.isValid(originSquare);
        assert moveDelta != null;

        int originPiece = board.board[originSquare];
        assert Piece.isValid(originPiece);
        boolean sliding = PieceType.isSliding(Piece.getType(originPiece));
        int oppositeColor = Color.opposite(Piece.getColor(originPiece));

        // Go through all move deltas for this piece
        for (int delta : moveDelta) {
            int targetSquare = originSquare + delta;

            // Check if we're still on the board
            while (Square.isValid(targetSquare)) {
                int targetPiece = board.board[targetSquare];

                if (targetPiece == Piece.NOPIECE) {
                    // quiet move
                    list.entries[list.size++].move = Move.valueOf(MoveType.NORMAL, originSquare, targetSquare, originPiece, Piece.NOPIECE,
                            PieceType.NOPIECETYPE);

                    if (!sliding) {
                        break;
                    }

                    targetSquare += delta;
                } else {
                    if (Piece.getColor(targetPiece) == oppositeColor) {
                        // capturing move
                        list.entries[list.size++].move = Move.valueOf(MoveType.CAPTURE, originSquare, targetSquare, originPiece, targetPiece,
                                PieceType.NOPIECETYPE);
                    }

                    break;
                }
            }
        }
    }

    private static void addPawnMoves(MoveList list, int pawnSquare, Board board) {

        assert list != null;
        assert Square.isValid(pawnSquare);

        int pawnPiece = board.board[pawnSquare];
        assert Piece.isValid(pawnPiece);
        assert Piece.getType(pawnPiece) == PieceType.PAWN;
        int pawnColor = Piece.getColor(pawnPiece);

        // Generate only capturing moves first (i = 1)
        for (int i = 1; i < Board.pawnDirections[pawnColor].length; ++i) {
            int delta = Board.pawnDirections[pawnColor][i];

            int targetSquare = pawnSquare + delta;
            if (Square.isValid(targetSquare)) {
                int targetPiece = board.board[targetSquare];

                if (targetPiece != Piece.NOPIECE) {
                    if (Piece.getColor(targetPiece) == Color.opposite(pawnColor)) {

                        // Capturing move
                        if ((pawnColor == Color.WHITE && Square.getRank(targetSquare) == Rank.r8)
                                || (pawnColor == Color.BLACK && Square.getRank(targetSquare) == Rank.r1)) {
                            // Pawn promotion capturing move

                            list.entries[list.size++].move = Move.valueOf(MoveType.PAWNPROMOTION, pawnSquare, targetSquare, pawnPiece, targetPiece,
                                    PieceType.QUEEN);
                            list.entries[list.size++].move = Move.valueOf(MoveType.PAWNPROMOTION, pawnSquare, targetSquare, pawnPiece, targetPiece,
                                    PieceType.ROOK);
                            list.entries[list.size++].move = Move.valueOf(MoveType.PAWNPROMOTION, pawnSquare, targetSquare, pawnPiece, targetPiece,
                                    PieceType.BISHOP);
                            list.entries[list.size++].move = Move.valueOf(MoveType.PAWNPROMOTION, pawnSquare, targetSquare, pawnPiece, targetPiece,
                                    PieceType.KNIGHT);
                        } else {
                            // Normal capturing move

                            list.entries[list.size++].move = Move.valueOf(MoveType.CAPTURE, pawnSquare, targetSquare, pawnPiece, targetPiece,
                                    PieceType.NOPIECETYPE);
                        }
                    }
                } else if (targetSquare == board.enPassantSquare) {
                    // En passant move
                    assert (pawnColor == Color.BLACK && Square.getRank(targetSquare) == Rank.r3)
                    || (pawnColor == Color.WHITE && Square.getRank(targetSquare) == Rank.r6);

                    int captureSquare = targetSquare + (pawnColor == Color.WHITE ? Square.S : Square.N);
                    targetPiece = board.board[captureSquare];
                    assert Piece.getType(targetPiece) == PieceType.PAWN;
                    assert Piece.getColor(targetPiece) == Color.opposite(pawnColor);

                    list.entries[list.size++].move = Move.valueOf(MoveType.ENPASSANT, pawnSquare, targetSquare, pawnPiece, targetPiece, PieceType.NOPIECETYPE);
                }
            }
        }

        // Generate non-capturing moves
        int delta = Board.pawnDirections[pawnColor][0];

        // Move one rank forward
        int targetSquare = pawnSquare + delta;
        if (Square.isValid(targetSquare) && board.board[targetSquare] == Piece.NOPIECE) {
            if ((pawnColor == Color.WHITE && Square.getRank(targetSquare) == Rank.r8) || (pawnColor == Color.BLACK && Square.getRank(targetSquare) == Rank.r1)) {
                // Pawn promotion move

                list.entries[list.size++].move = Move.valueOf(MoveType.PAWNPROMOTION, pawnSquare, targetSquare, pawnPiece, Piece.NOPIECE, PieceType.QUEEN);
                list.entries[list.size++].move = Move.valueOf(MoveType.PAWNPROMOTION, pawnSquare, targetSquare, pawnPiece, Piece.NOPIECE, PieceType.ROOK);
                list.entries[list.size++].move = Move.valueOf(MoveType.PAWNPROMOTION, pawnSquare, targetSquare, pawnPiece, Piece.NOPIECE, PieceType.BISHOP);
                list.entries[list.size++].move = Move.valueOf(MoveType.PAWNPROMOTION, pawnSquare, targetSquare, pawnPiece, Piece.NOPIECE, PieceType.KNIGHT);
            } else {
                // Normal move

                list.entries[list.size++].move = Move.valueOf(MoveType.NORMAL, pawnSquare, targetSquare, pawnPiece, Piece.NOPIECE, PieceType.NOPIECETYPE);

                // Move another rank forward
                targetSquare += delta;
                if (Square.isValid(targetSquare) && board.board[targetSquare] == Piece.NOPIECE) {
                    if ((pawnColor == Color.WHITE && Square.getRank(targetSquare) == Rank.r4)
                            || (pawnColor == Color.BLACK && Square.getRank(targetSquare) == Rank.r5)) {
                        // Pawn double move

                        list.entries[list.size++].move = Move.valueOf(MoveType.PAWNDOUBLE, pawnSquare, targetSquare, pawnPiece, Piece.NOPIECE,
                                PieceType.NOPIECETYPE);
                    }
                }
            }
        }
    }

    private static void addCastlingMoves(MoveList list, int kingSquare, Board board) {

        assert list != null;
        assert Square.isValid(kingSquare);

        int kingPiece = board.board[kingSquare];
        assert Piece.isValid(kingPiece);
        assert Piece.getType(kingPiece) == PieceType.KING;

        if (Piece.getColor(kingPiece) == Color.WHITE) {
            // Do not test g1 whether it is attacked as we will test it in
            // isLegal()
            if (board.castlingRights[Castling.WHITE_KINGSIDE] != File.NOFILE && board.board[Square.f1] == Piece.NOPIECE
                    && board.board[Square.g1] == Piece.NOPIECE && !board.isAttacked(Square.f1, Color.BLACK)) {
                assert board.board[Square.e1] == Piece.WHITE_KING;
                assert board.board[Square.h1] == Piece.WHITE_ROOK;

                list.entries[list.size++].move = Move.valueOf(MoveType.CASTLING, kingSquare, Square.g1, kingPiece, Piece.NOPIECE, PieceType.NOPIECETYPE);
            }
            // Do not test c1 whether it is attacked as we will test it in
            // isLegal()
            if (board.castlingRights[Castling.WHITE_QUEENSIDE] != File.NOFILE && board.board[Square.b1] == Piece.NOPIECE
                    && board.board[Square.c1] == Piece.NOPIECE && board.board[Square.d1] == Piece.NOPIECE && !board.isAttacked(Square.d1, Color.BLACK)) {
                assert board.board[Square.e1] == Piece.WHITE_KING;
                assert board.board[Square.a1] == Piece.WHITE_ROOK;

                list.entries[list.size++].move = Move.valueOf(MoveType.CASTLING, kingSquare, Square.c1, kingPiece, Piece.NOPIECE, PieceType.NOPIECETYPE);
            }
        } else {
            // Do not test g8 whether it is attacked as we will test it in
            // isLegal()
            if (board.castlingRights[Castling.BLACK_KINGSIDE] != File.NOFILE && board.board[Square.f8] == Piece.NOPIECE
                    && board.board[Square.g8] == Piece.NOPIECE && !board.isAttacked(Square.f8, Color.WHITE)) {
                assert board.board[Square.e8] == Piece.BLACK_KING;
                assert board.board[Square.h8] == Piece.BLACK_ROOK;

                list.entries[list.size++].move = Move.valueOf(MoveType.CASTLING, kingSquare, Square.g8, kingPiece, Piece.NOPIECE, PieceType.NOPIECETYPE);
            }
            // Do not test c8 whether it is attacked as we will test it in
            // isLegal()
            if (board.castlingRights[Castling.BLACK_QUEENSIDE] != File.NOFILE && board.board[Square.b8] == Piece.NOPIECE
                    && board.board[Square.c8] == Piece.NOPIECE && board.board[Square.d8] == Piece.NOPIECE && !board.isAttacked(Square.d8, Color.WHITE)) {
                assert board.board[Square.e8] == Piece.BLACK_KING;
                assert board.board[Square.a8] == Piece.BLACK_ROOK;

                list.entries[list.size++].move = Move.valueOf(MoveType.CASTLING, kingSquare, Square.c8, kingPiece, Piece.NOPIECE, PieceType.NOPIECETYPE);
            }
        }
    }

}
