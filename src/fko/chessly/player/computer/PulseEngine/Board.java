/*
 * =============================================================================
 * Pulse
 *
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 *
 * =============================================================================
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

import java.util.List;
import java.util.Random;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.game.GamePosition;

/**
 * This is our internal board.
 */
final class Board {

    static final int MAX_MOVES = Depth.MAX_PLY + 1024;
    static final int BOARDSIZE = 128;

    static final int[][] pawnDirections = { { Square.N, Square.NE, Square.NW }, // Color.WHITE
            { Square.S, Square.SE, Square.SW } // Color.BLACK
    };
    static final int[] knightDirections = { Square.N + Square.N + Square.E,
            Square.N + Square.N + Square.W, Square.N + Square.E + Square.E,
            Square.N + Square.W + Square.W, Square.S + Square.S + Square.E,
            Square.S + Square.S + Square.W, Square.S + Square.E + Square.E,
            Square.S + Square.W + Square.W };
    static final int[] bishopDirections = { Square.NE, Square.NW, Square.SE,
            Square.SW };
    static final int[] rookDirections = { Square.N, Square.E, Square.S,
            Square.W };
    static final int[] queenDirections = { Square.N, Square.E, Square.S,
            Square.W, Square.NE, Square.NW, Square.SE, Square.SW };
    static final int[] kingDirections = { Square.N, Square.E, Square.S,
            Square.W, Square.NE, Square.NW, Square.SE, Square.SW };

    final int[] board = new int[BOARDSIZE];

    final Bitboard[] pawns = new Bitboard[Color.values.length];
    final Bitboard[] knights = new Bitboard[Color.values.length];
    final Bitboard[] bishops = new Bitboard[Color.values.length];
    final Bitboard[] rooks = new Bitboard[Color.values.length];
    final Bitboard[] queens = new Bitboard[Color.values.length];
    final Bitboard[] kings = new Bitboard[Color.values.length];

    final int[] material = new int[Color.values.length];

    final int[] castlingRights = new int[Castling.values.length];
    int enPassantSquare = Square.NOSQUARE;
    int activeColor = Color.WHITE;
    int halfmoveClock = 0;
    int halfmoveNumber = 2;

    long zobristKey = 0;

    // We will save some board parameters in a State before making a move.
    // Later we will restore them before undoing a move.
    private final State[] stack = new State[MAX_MOVES];
    private int stackSize = 0;

    // Move History
    public MoveHistory moveHistory = new MoveHistory();

    /**
     * Constructure setting up a standard chess board
     */
    public Board() {

        // Initialize stack
        for (int i = 0; i < stack.length; ++i) {
            stack[i] = new State();
        }

        // Initialize piece type lists
        for (int color : Color.values) {
            pawns[color] = new Bitboard();
            knights[color] = new Bitboard();
            bishops[color] = new Bitboard();
            rooks[color] = new Bitboard();
            queens[color] = new Bitboard();
            kings[color] = new Bitboard();
        }

        // Initialize material
        for (int color : Color.values) {
            material[color] = 0;
        }

        // Initialize board
        for (int square : Square.values) {
            board[square] = Piece.NOPIECE;
        }

        // Castling rights
        for (int color : Color.values) {
            castlingRights[Castling.valueOf(color, CastlingType.KINGSIDE)] = File.h;
            zobristKey ^= Zobrist.castlingRights[Castling.valueOf(color,
                    CastlingType.KINGSIDE)];
            castlingRights[Castling.valueOf(color, CastlingType.QUEENSIDE)] = File.a;
            zobristKey ^= Zobrist.castlingRights[Castling.valueOf(color,
                    CastlingType.QUEENSIDE)];
        }

    }

    Board(Board o) {

        this();

        // Copy board
        System.arraycopy(o.board, 0, this.board, 0, o.board.length);

        activeColor = o.activeColor;
        System.arraycopy(o.castlingRights, 0, this.castlingRights, 0,
                castlingRights.length);
        enPassantSquare = o.enPassantSquare;

        halfmoveClock = o.halfmoveClock;
        halfmoveNumber = o.halfmoveNumber;

        int white = Color.WHITE;
        int black = Color.BLACK;

        material[white] = o.material[white];
        material[black] = o.material[black];

        pawns[white] = (Bitboard) o.pawns[white].clone();
        pawns[black] = (Bitboard) o.pawns[black].clone();
        knights[white] = (Bitboard) o.knights[white].clone();
        knights[black] = (Bitboard) o.knights[black].clone();
        bishops[white] = (Bitboard) o.bishops[white].clone();
        bishops[black] = (Bitboard) o.bishops[black].clone();
        rooks[white] = (Bitboard) o.rooks[white].clone();
        rooks[black] = (Bitboard) o.rooks[black].clone();
        queens[white] = (Bitboard) o.queens[white].clone();
        queens[black] = (Bitboard) o.queens[black].clone();
        kings[white] = (Bitboard) o.kings[white].clone();
        kings[black] = (Bitboard) o.kings[black].clone();

        // Zobrist
        // Copy board memory
        for (int i = 0; i < stack.length; ++i) {
            stack[i].zobristKey = o.stack[i].zobristKey;
            System.arraycopy(o.stack[i].castlingRights, 0,
                    stack[i].castlingRights, 0, stack[i].castlingRights.length);
            stack[i].enPassantSquare = o.stack[i].enPassantSquare;
            stack[i].halfmoveClock = o.stack[i].halfmoveClock;
        }

        // copy MoveHistory
        moveHistory = o.moveHistory.clone();

        // hask Key
        zobristKey = o.zobristKey;

    }

    /**
     * Generate a new board as a deep copy from a Board (Interface). This is
     * part of the facade.
     *
     * @param oldboard
     */
    public Board(GameBoard oldboard) {
        this();
        initFromGameBoard(oldboard);
    }

    /**
     * Commits a move to this board.
     * @param move
     */
    void makeMove(int move) {
        State entry = stack[stackSize];

        // Get variables
        int type = Move.getType(move);
        int originSquare = Move.getOriginSquare(move);
        int targetSquare = Move.getTargetSquare(move);
        int originPiece = Move.getOriginPiece(move);
        int originColor = Piece.getColor(originPiece);
        int targetPiece = Move.getTargetPiece(move);

        // Save zobristKey
        entry.zobristKey = zobristKey;

        // Save castling rights
        for (int castling : Castling.values) {
            entry.castlingRights[castling] = castlingRights[castling];
        }

        // Save enPassantSquare
        entry.enPassantSquare = enPassantSquare;

        // Save halfmoveClock
        entry.halfmoveClock = halfmoveClock;

        // Remove target piece and update castling rights
        if (targetPiece == Piece.NOPIECE && type == MoveType.CAPTURE) {
            // this might happen when move comes from notation
            targetPiece = board[targetSquare];
        }

        if (targetPiece != Piece.NOPIECE) {

            int captureSquare = targetSquare;
            if (type == MoveType.ENPASSANT) {
                captureSquare += (originColor == Color.WHITE ? Square.S	: Square.N);
            }
            assert targetPiece == board[captureSquare];
            assert Piece.getType(targetPiece) != PieceType.KING;
            remove(captureSquare);

            clearCastling(captureSquare);
        }

        // Move piece
        assert originPiece == board[originSquare];
        remove(originSquare);
        if (type == MoveType.PAWNPROMOTION) {
            put(Piece.valueOf(originColor, Move.getPromotion(move)),
                    targetSquare);
        } else {
            put(originPiece, targetSquare);
        }

        // Move rook and update castling rights
        if (type == MoveType.CASTLING) {
            int rookOriginSquare;
            int rookTargetSquare;
            switch (targetSquare) {
                case Square.g1:
                    rookOriginSquare = Square.h1;
                    rookTargetSquare = Square.f1;
                    break;
                case Square.c1:
                    rookOriginSquare = Square.a1;
                    rookTargetSquare = Square.d1;
                    break;
                case Square.g8:
                    rookOriginSquare = Square.h8;
                    rookTargetSquare = Square.f8;
                    break;
                case Square.c8:
                    rookOriginSquare = Square.a8;
                    rookTargetSquare = Square.d8;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            assert Piece.getType(board[rookOriginSquare]) == PieceType.ROOK;
            int rookPiece = remove(rookOriginSquare);
            put(rookPiece, rookTargetSquare);
        }

        // Update castling
        clearCastling(originSquare);

        // Update enPassantSquare
        if (enPassantSquare != Square.NOSQUARE) {
            zobristKey ^= Zobrist.enPassantSquare[enPassantSquare];
        }
        if (type == MoveType.PAWNDOUBLE) {
            enPassantSquare = targetSquare
                    + (originColor == Color.WHITE ? Square.S : Square.N);
            assert Square.isValid(enPassantSquare);
            zobristKey ^= Zobrist.enPassantSquare[enPassantSquare];
        } else {
            enPassantSquare = Square.NOSQUARE;
        }

        // Update activeColor
        activeColor = Color.opposite(activeColor);
        zobristKey ^= Zobrist.activeColor;

        // Update halfmoveClock
        if (Piece.getType(originPiece) == PieceType.PAWN
                || targetPiece != Piece.NOPIECE) {
            halfmoveClock = 0;
        } else {
            ++halfmoveClock;
        }

        // Update fullMoveNumber
        ++halfmoveNumber;

        ++stackSize;
        assert stackSize < MAX_MOVES;

        moveHistory.addMove(move);
    }

    /**
     * Undos the last move made
     */
    void undoMove() {
        // read last move and delete it from move history
        int lastmove = moveHistory.lastMove();
        undoMove(lastmove);
    }

    /**
     * Called by undo move
     * @param move
     */
    private void undoMove(int move) {
        --stackSize;
        assert stackSize >= 0;

        State entry = stack[stackSize];

        moveHistory.removeMove();

        // Get variables
        int type = Move.getType(move);
        int originSquare = Move.getOriginSquare(move);
        int targetSquare = Move.getTargetSquare(move);
        int originPiece = Move.getOriginPiece(move);
        int originColor = Piece.getColor(originPiece);
        int targetPiece = Move.getTargetPiece(move);

        // Update fullMoveNumber
        halfmoveNumber--;

        // Update activeColor
        activeColor = Color.opposite(activeColor);

        // Undo move rook
        if (type == MoveType.CASTLING) {
            int rookOriginSquare;
            int rookTargetSquare;
            switch (targetSquare) {
                case Square.g1:
                    rookOriginSquare = Square.h1;
                    rookTargetSquare = Square.f1;
                    break;
                case Square.c1:
                    rookOriginSquare = Square.a1;
                    rookTargetSquare = Square.d1;
                    break;
                case Square.g8:
                    rookOriginSquare = Square.h8;
                    rookTargetSquare = Square.f8;
                    break;
                case Square.c8:
                    rookOriginSquare = Square.a8;
                    rookTargetSquare = Square.d8;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            assert Piece.getType(board[rookTargetSquare]) == PieceType.ROOK;
            int rookPiece = remove(rookTargetSquare);
            put(rookPiece, rookOriginSquare);
        }

        // Undo move piece
        remove(targetSquare);
        put(originPiece, originSquare);

        // Restore target piece
        if (targetPiece != Piece.NOPIECE) {
            int captureSquare = targetSquare;
            if (type == MoveType.ENPASSANT) {
                captureSquare += (originColor == Color.WHITE ? Square.S
                        : Square.N);
                assert Square.isValid(captureSquare);
            }
            put(targetPiece, captureSquare);
        }

        // Restore halfmoveClock
        halfmoveClock = entry.halfmoveClock;

        // Restore enPassantSquare
        enPassantSquare = entry.enPassantSquare;

        // Restore castling rights
        for (int castling : Castling.values) {
            if (entry.castlingRights[castling] != castlingRights[castling]) {
                castlingRights[castling] = entry.castlingRights[castling];
            }
        }

        // Restore zobristKey
        zobristKey = entry.zobristKey;
    }

    /**
     * TODO: BUG? Checks only 1 repetition
     * @return
     */
    boolean isRepetition() {
        // Search back until the last halfmoveClock reset
        int j = Math.max(0, stackSize - halfmoveClock);
        for (int i = stackSize - 2; i >= j; i -= 2) {
            if (zobristKey == stack[i].zobristKey) {
                return true;
            }
        }
        return false;
    }

    boolean hasInsufficientMaterial() {
        // If there is only one minor left, we are unable to checkmate
        return pawns[Color.WHITE].size() == 0
                && pawns[Color.BLACK].size() == 0
                && rooks[Color.WHITE].size() == 0
                && rooks[Color.BLACK].size() == 0
                && queens[Color.WHITE].size() == 0
                && queens[Color.BLACK].size() == 0
                && (knights[Color.WHITE].size() + bishops[Color.WHITE].size() <= 1)
                && (knights[Color.BLACK].size() + bishops[Color.BLACK].size() <= 1);
    }

    boolean isCheck() {
        // Check whether our king is attacked by any opponent piece
        return isAttacked(Bitboard.next(kings[activeColor].squares),
                Color.opposite(activeColor));
    }

    int getFullmoveNumber() {
        return halfmoveNumber / 2;
    }

    /**
     * Puts a piece at the square. We need to update our board and the
     * appropriate piece type list.
     *
     * @param piece
     *            the Piece.
     * @param square
     *            the Square.
     */
    private void put(int piece, int square) {
        assert Piece.isValid(piece);
        assert Square.isValid(square);
        assert board[square] == Piece.NOPIECE;

        int pieceType = Piece.getType(piece);
        int color = Piece.getColor(piece);

        switch (pieceType) {
            case PieceType.PAWN:
                pawns[color].add(square);
                material[color] += PieceType.PAWN_VALUE;
                break;
            case PieceType.KNIGHT:
                knights[color].add(square);
                material[color] += PieceType.KNIGHT_VALUE;
                break;
            case PieceType.BISHOP:
                bishops[color].add(square);
                material[color] += PieceType.BISHOP_VALUE;
                break;
            case PieceType.ROOK:
                rooks[color].add(square);
                material[color] += PieceType.ROOK_VALUE;
                break;
            case PieceType.QUEEN:
                queens[color].add(square);
                material[color] += PieceType.QUEEN_VALUE;
                break;
            case PieceType.KING:
                kings[color].add(square);
                material[color] += PieceType.KING_VALUE;
                break;
            default:
                throw new IllegalArgumentException();
        }

        board[square] = piece;

        zobristKey ^= Zobrist.board[piece][square];
    }

    /**
     * Removes a piece from the square. We need to update our board and the
     * appropriate piece type list.
     *
     * @param square
     *            the Square.
     * @return the Piece which was removed.
     */
    private int remove(int square) {
        assert Square.isValid(square);
        assert Piece.isValid(board[square]);

        int piece = board[square];

        int pieceType = Piece.getType(piece);
        int color = Piece.getColor(piece);

        switch (pieceType) {
            case PieceType.PAWN:
                pawns[color].remove(square);
                material[color] -= PieceType.PAWN_VALUE;
                break;
            case PieceType.KNIGHT:
                knights[color].remove(square);
                material[color] -= PieceType.KNIGHT_VALUE;
                break;
            case PieceType.BISHOP:
                bishops[color].remove(square);
                material[color] -= PieceType.BISHOP_VALUE;
                break;
            case PieceType.ROOK:
                rooks[color].remove(square);
                material[color] -= PieceType.ROOK_VALUE;
                break;
            case PieceType.QUEEN:
                queens[color].remove(square);
                material[color] -= PieceType.QUEEN_VALUE;
                break;
            case PieceType.KING:
                kings[color].remove(square);
                material[color] -= PieceType.KING_VALUE;
                break;
            default:
                throw new IllegalArgumentException();
        }

        board[square] = Piece.NOPIECE;

        zobristKey ^= Zobrist.board[piece][square];

        return piece;
    }

    private void clearCastling(int square) {
        assert Square.isValid(square);

        switch (square) {
            case Square.a1:
                clearCastlingRights(Castling.WHITE_QUEENSIDE);
                break;
            case Square.h1:
                clearCastlingRights(Castling.WHITE_KINGSIDE);
                break;
            case Square.a8:
                clearCastlingRights(Castling.BLACK_QUEENSIDE);
                break;
            case Square.h8:
                clearCastlingRights(Castling.BLACK_KINGSIDE);
                break;
            case Square.e1:
                clearCastlingRights(Castling.WHITE_QUEENSIDE);
                clearCastlingRights(Castling.WHITE_KINGSIDE);
                break;
            case Square.e8:
                clearCastlingRights(Castling.BLACK_QUEENSIDE);
                clearCastlingRights(Castling.BLACK_KINGSIDE);
                break;
            default:
                break;
        }
    }

    private void clearCastlingRights(int castling) {
        assert Castling.isValid(castling);

        if (castlingRights[castling] != File.NOFILE) {
            castlingRights[castling] = File.NOFILE;
            zobristKey ^= Zobrist.castlingRights[castling];
        }
    }

    /**
     * Returns whether the targetSquare is attacked by any piece from the
     * attackerColor. We will backtrack from the targetSquare to find the piece.
     *
     * @param targetSquare
     *            the target Square.
     * @param attackerColor
     *            the attacker Color.
     * @return whether the targetSquare is attacked.
     */
    boolean isAttacked(int targetSquare, int attackerColor) {
        assert Square.isValid(targetSquare);
        assert Color.isValid(attackerColor);

        // Pawn attacks
        int pawnPiece = Piece.valueOf(attackerColor, PieceType.PAWN);
        for (int i = 1; i < pawnDirections[attackerColor].length; ++i) {
            int attackerSquare = targetSquare
                    - pawnDirections[attackerColor][i];
            if (Square.isValid(attackerSquare)) {
                int attackerPawn = board[attackerSquare];

                if (attackerPawn == pawnPiece) {
                    return true;
                }
            }
        }

        return isAttackedNonSliding(targetSquare,
                Piece.valueOf(attackerColor, PieceType.KNIGHT),
                knightDirections)

                // The queen moves like a bishop, so check both piece types
                || isAttackedSliding(targetSquare,
                        Piece.valueOf(attackerColor, PieceType.BISHOP),
                        Piece.valueOf(attackerColor, PieceType.QUEEN),
                        bishopDirections)

                // The queen moves like a rook, so check both piece types
                || isAttackedSliding(targetSquare,
                        Piece.valueOf(attackerColor, PieceType.ROOK),
                        Piece.valueOf(attackerColor, PieceType.QUEEN),
                        rookDirections)

                || isAttackedNonSliding(targetSquare,
                        Piece.valueOf(attackerColor, PieceType.KING),
                        kingDirections);
    }

    /**
     * Returns whether the targetSquare is attacked by a non-sliding piece.
     */
    private boolean isAttackedNonSliding(int targetSquare, int attackerPiece,
            int[] moveDelta) {
        assert Square.isValid(targetSquare);
        assert Piece.isValid(attackerPiece);
        assert moveDelta != null;

        for (int delta : moveDelta) {
            int attackerSquare = targetSquare + delta;

            if (Square.isValid(attackerSquare)
                    && board[attackerSquare] == attackerPiece) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the targetSquare is attacked by a sliding piece.
     */
    private boolean isAttackedSliding(int targetSquare, int attackerPiece,
            int queenPiece, int[] moveDelta) {
        assert Square.isValid(targetSquare);
        assert Piece.isValid(attackerPiece);
        assert Piece.isValid(queenPiece);
        assert moveDelta != null;

        for (int delta : moveDelta) {
            int attackerSquare = targetSquare + delta;

            while (Square.isValid(attackerSquare)) {
                int piece = board[attackerSquare];

                if (Piece.isValid(piece)) {
                    if (piece == attackerPiece || piece == queenPiece) {
                        return true;
                    }
                    break;
                }
                attackerSquare += delta;
            }
        }

        return false;
    }

    /**
     * Put the pieces for the standard setup onto the board.
     */
    void initStandard() {

        // rooks
        put(Piece.WHITE_ROOK, Square.a1);
        put(Piece.WHITE_ROOK, Square.h1);
        put(Piece.BLACK_ROOK, Square.a8);
        put(Piece.BLACK_ROOK, Square.h8);
        // knights
        put(Piece.WHITE_KNIGHT, Square.b1);
        put(Piece.WHITE_KNIGHT, Square.g1);
        put(Piece.BLACK_KNIGHT, Square.b8);
        put(Piece.BLACK_KNIGHT, Square.g8);
        // bishops
        put(Piece.WHITE_BISHOP, Square.c1);
        put(Piece.WHITE_BISHOP, Square.f1);
        put(Piece.BLACK_BISHOP, Square.c8);
        put(Piece.BLACK_BISHOP, Square.f8);
        // queens
        put(Piece.WHITE_QUEEN, Square.d1);
        put(Piece.BLACK_QUEEN, Square.d8);
        // kings
        put(Piece.WHITE_KING, Square.e1);
        put(Piece.BLACK_KING, Square.e8);

        // pawns
        for (int i = 0; i < 8; i++) {
            put(Piece.WHITE_PAWN, Square.valueOf(1, i));
            put(Piece.BLACK_PAWN, Square.valueOf(6, i));
        }

    }

    /**
     * Initializes from a Board interface.
     *
     * @param oldboard
     */
    private void initFromGameBoard(GameBoard oldboard) {
        if (oldboard == null)
            throw new NullPointerException("Parameter oldBoard may not be null");

        // -- copy fields --
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                fko.chessly.game.GamePiece p = oldboard.getPiece(file + 1,
                        rank + 1);
                if (p != null) {
                    switch (p.getColor()) {
                        case WHITE:
                            switch (p.getType()) {
                                case KING:
                                    put(Piece.WHITE_KING, Square.valueOf(rank, file));
                                    break;
                                case QUEEN:
                                    put(Piece.WHITE_QUEEN, Square.valueOf(rank, file));
                                    break;
                                case ROOK:
                                    put(Piece.WHITE_ROOK, Square.valueOf(rank, file));
                                    break;
                                case BISHOP:
                                    put(Piece.WHITE_BISHOP, Square.valueOf(rank, file));
                                    break;
                                case KNIGHT:
                                    put(Piece.WHITE_KNIGHT, Square.valueOf(rank, file));
                                    break;
                                case PAWN:
                                    put(Piece.WHITE_PAWN, Square.valueOf(rank, file));
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "Invalid Piece type");
                            }
                            break;
                        case BLACK:
                            switch (p.getType()) {
                                case KING:
                                    put(Piece.BLACK_KING, Square.valueOf(rank, file));
                                    break;
                                case QUEEN:
                                    put(Piece.BLACK_QUEEN, Square.valueOf(rank, file));
                                    break;
                                case ROOK:
                                    put(Piece.BLACK_ROOK, Square.valueOf(rank, file));
                                    break;
                                case BISHOP:
                                    put(Piece.BLACK_BISHOP, Square.valueOf(rank, file));
                                    break;
                                case KNIGHT:
                                    put(Piece.BLACK_KNIGHT, Square.valueOf(rank, file));
                                    break;
                                case PAWN:
                                    put(Piece.BLACK_PAWN, Square.valueOf(rank, file));
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "Invalid Piece type");
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid ChessColor");
                    }
                }
            }
        }

        // next player
        activeColor = oldboard.getNextPlayerColor() == GameColor.WHITE ? Color.WHITE : Color.BLACK;

        // -- copy castling flags
        if (oldboard.isCastlingKingSideAllowed(GameColor.WHITE)) {
            castlingRights[Castling.WHITE_KINGSIDE] = File.h;
        } else {
            castlingRights[Castling.WHITE_KINGSIDE] = File.NOFILE;
        }
        if (oldboard.isCastlingQueenSideAllowed(GameColor.WHITE)) {
            castlingRights[Castling.WHITE_QUEENSIDE] = File.a;
        } else {
            castlingRights[Castling.WHITE_QUEENSIDE] = File.NOFILE;
        }
        if (oldboard.isCastlingKingSideAllowed(GameColor.BLACK)) {
            castlingRights[Castling.BLACK_KINGSIDE] = File.h;
        } else {
            castlingRights[Castling.BLACK_KINGSIDE] = File.NOFILE;
        }
        if (oldboard.isCastlingQueenSideAllowed(GameColor.BLACK)) {
            castlingRights[Castling.BLACK_QUEENSIDE] = File.a;
        } else {
            castlingRights[Castling.BLACK_QUEENSIDE] = File.NOFILE;
        }

        // en passant flag
        GamePosition ep = oldboard.getEnPassantCapturable();
        if (ep != null) {
            enPassantSquare = Square.valueOf(ep.y - 1, ep.x - 1);
        }

        // halfmove clock
        halfmoveClock = oldboard.getHalfmoveClock();

        // halfmove number
        halfmoveNumber = oldboard.getLastHalfMoveNumber() + 2;

        // -- copy move history --
        // this._moveHistory = oldBoard.getMoveHistory();
        List<GameMove> oldHisotry = oldboard.getMoveHistory();
        for (GameMove m : oldHisotry) {
            // originSquare
            int originSquare = Square.valueOf(m.getFromField().y - 1,
                    m.getFromField().x - 1);
            assert Square.isValid(originSquare);
            // targetSquare
            int targetSquare = Square.valueOf(m.getToField().y - 1,
                    m.getToField().x - 1);
            assert Square.isValid(targetSquare);
            // originPiece
            int color = m.getMovedPiece().getColor().isWhite() ? Color.WHITE
                    : Color.BLACK;
            assert Color.isValid(color);
            int originPieceType = PieceType.fromChar(m.getMovedPiece()
                    .getType().toChar());
            PieceType.isValid(originPieceType);
            int originPiece = Piece.valueOf(color, originPieceType);
            assert Piece.isValid(originPiece);
            // targetPiece
            int targetPiece;
            if (m.getCapturedPiece() != null) {
                int targetPieceType = PieceType.fromChar(m.getCapturedPiece()
                        .getType().toChar());
                PieceType.isValid(targetPieceType);
                targetPiece = Piece.valueOf(Color.opposite(color),
                        targetPieceType);
            } else {
                targetPiece = Piece.NOPIECE;
            }
            // promotionPiece
            int promotionPieceType;
            if (m.getPromotedTo() == null) {
                promotionPieceType = PieceType.NOPIECETYPE;
            } else {
                promotionPieceType = PieceType.fromChar(m.getPromotedTo()
                        .getType().toChar());
                PieceType.isValidPromotion(promotionPieceType);
            }
            // moveType
            int moveType = MoveType.NORMAL;
            if (m.getWasEnPassantCapture()) {
                moveType = MoveType.ENPASSANT;
            } else if (m.getCapturedPiece() != null) {
                moveType = MoveType.CAPTURE;
            } else if (m.isEnPassantNextMovePossible()) {
                moveType = MoveType.PAWNDOUBLE;
            }
            if (m.getPromotedTo() != null) {
                moveType = MoveType.PAWNPROMOTION;
            }

            int move = Move.valueOf(moveType, originSquare, targetSquare,
                    originPiece, targetPiece, promotionPieceType);
            moveHistory.addMove(move);
        }

    }

    /**
     * Generates a string representation in form of FEN.
     * http://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
     *
     * @return String representing the board as FEN
     */
    @Override
    public String toString() {
        String fen = "";
        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            for (int file = 0; file < 8; file++) {
                int piece = this.board[Square.valueOf(rank, file)];
                if (piece == Piece.NOPIECE) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        fen += emptySquares;
                        emptySquares = 0;
                    }
                    fen += Piece.toChar(piece);
                }
            }
            if (emptySquares > 0) {
                fen += emptySquares;
            }
            if (rank > 0) {
                fen += '/';
            }
        }
        fen += ' ';
        // Color
        fen += Color.toChar(this.activeColor);
        fen += ' ';
        // Castling
        boolean castlingAvailable = false;
        for (int color : Color.values) {
            for (int castlingType : CastlingType.values) {
                if (castlingType != File.NOFILE) {
                    fen += Castling.toChar(Castling
                            .valueOf(color, castlingType));
                    castlingAvailable = true;
                }
            }
        }
        if (!castlingAvailable) {
            fen += '-';
        }
        fen += ' ';
        // En passant
        if (this.enPassantSquare != Square.NOSQUARE) {
            fen += Square.toString(this.enPassantSquare);
        } else {
            fen += '-';
        }
        fen += ' ';
        // Half move clock
        fen += this.halfmoveClock;
        fen += ' ';
        // Full move number
        fen += getFullmoveNumber();
        return fen;
    }

    /**
     * Prints a visual baord as a string and adds the FEN notation.
     *
     * @return Visual board as String
     */
    public String toBoardString() {
        StringBuilder boardString = new StringBuilder((8 << 2 + 5) * 8);
        // backwards as highest row is on top
        for (int rank = 7; rank >= 0; rank--) {
            // upper border
            boardString.append("    -"); // 4 * space
            for (int col = 7; col >= 0; col--) {
                boardString.append("----"); // dim * -
            }
            boardString.append("\n");
            // row number
            boardString.append(' ').append(Integer.toString(rank + 1))
            .append(": |");
            // col fields
            for (int file = 0; file < 8; file++) {
                int p = board[Square.valueOf(rank, file)];
                if (Piece.isValid(p)) {
                    boardString.append(" ").append(Piece.toChar(p))
                    .append(" |");
                } else {
                    boardString.append("   |");
                }
            }
            boardString.append("\n");
        }
        // lower border
        boardString.append("    -");
        for (int file = 7; file >= 0; file--) {
            boardString.append("----");
        }
        boardString.append("\n");
        // col numbers
        boardString.append("     ");
        for (int file = 0; file < 8; file++) {
            boardString
            .append(' ')
            .append(File.toChar(Square.getFile(Square.valueOf(1, file))))
            .append("  ");
        }
        boardString.append("\n\n");
        boardString.append(this);
        boardString.append("\n");
        boardString.append("Last Move: "
                + Move.toString(moveHistory.lastMove()));
        boardString.append("\n");
        boardString.append("White Material: " + material[Color.WHITE]
                + " Black material: " + material[Color.BLACK]);
        boardString.append("\n");
        boardString.append("Hash Key: " + zobristKey);
        return boardString.toString();
    }

    private static final class Zobrist {
        // Generate a random number - use a seed to make sure that
        // each invocation will generate the same numbers.
        // When used with one thread in the search this should result
        // in similar searches (number of nodes searched).
        // This makes debugging much easier.
        private static final Random random = new Random(1234567890);


        static final long[][] board = new long[Piece.values.length][BOARDSIZE];
        static final long[] castlingRights = new long[Castling.values.length];
        static final long[] enPassantSquare = new long[BOARDSIZE];
        static final long activeColor = nextRandom();

        // Initialize the zobrist keys
        static {
            for (int piece : Piece.values) {
                for (int i = 0; i < BOARDSIZE; ++i) {
                    board[piece][i] = nextRandom();
                }
            }

            for (int castling : Castling.values) {
                castlingRights[castling] = nextRandom();
            }

            for (int i = 0; i < BOARDSIZE; ++i) {
                enPassantSquare[i] = nextRandom();
            }
        }

        private static long nextRandom() {
            //long hash =  Math.abs(ThreadLocalRandom.current().nextLong());
            long hash =  Math.abs(random.nextLong());
            return hash;
        }
    }

    private static final class State {
        private long zobristKey = 0;
        private final int[] castlingRights = new int[Castling.values.length];
        private int enPassantSquare = Square.NOSQUARE;
        private int halfmoveClock = 0;

        private State() {
            for (int castling : Castling.values) {
                castlingRights[castling] = File.NOFILE;
            }
        }
    }

}
