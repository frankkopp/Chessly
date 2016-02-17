/*
 * Copyright (C) 2007-2014 Phokham Nonava
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package fko.chessly.player.computer.FluxEngine;

/**
 * Notes: Ideas from Fruit. I specially like the Idea how to handle the state
 * list.
 */
final class MoveGenerator {

    private static final int HISTORYSIZE = Depth.MAX_PLY + 1;
    private static final int STATELISTSIZE = 256;

    // States
    private static final int GEN_TRANSPOSITION = 0;
    private static final int GEN_GOODCAPTURE = 1;
    private static final int GEN_KILLER = 2;
    private static final int GEN_NONCAPTURE = 3;
    private static final int GEN_BADCAPTURE = 4;
    private static final int GEN_EVASION = 5;
    private static final int GEN_GOODCAPTURE_QS = 6;
    private static final int GEN_CHECK_QS = 7;
    private static final int GEN_END = 8;

    // Generator
    private final class Generator {
        int statePosition = -1;
        int testState = GEN_END;
        int transpositionMove = Move.NOMOVE;
        int primaryKillerMove = Move.NOMOVE;
        int secondaryKillerMove = Move.NOMOVE;
    }

    // Board
    private static Position _myPosition;

    // Tables
    private static KillerTable killerTable;
    private static HistoryTable historyTable;

    // Move list
    private static MoveList moveList;
    private static MoveList tempMoveList;
    private static MoveList nonCaptureMoveList;

    // Generator history
    private static final Generator[] generator = new Generator[HISTORYSIZE];
    private static int generatorHistory = 0;

    // State list
    private static final int[] stateList = new int[STATELISTSIZE];
    private static final int statePositionMain;
    private static final int statePositionQuiescentAll;
    private static final int statePositionQuiescentCapture;
    private static final int statePositionEvasion;

    static {
        // Initialize state list
        int position = 0;

        statePositionMain = position;
        stateList[position++] = GEN_TRANSPOSITION;
        stateList[position++] = GEN_GOODCAPTURE;
        stateList[position++] = GEN_KILLER;
        stateList[position++] = GEN_NONCAPTURE;
        stateList[position++] = GEN_BADCAPTURE;
        stateList[position++] = GEN_END;

        statePositionQuiescentAll = position;
        stateList[position++] = GEN_GOODCAPTURE_QS;
        stateList[position++] = GEN_CHECK_QS;
        stateList[position++] = GEN_END;

        statePositionQuiescentCapture = position;
        stateList[position++] = GEN_GOODCAPTURE_QS;
        stateList[position++] = GEN_END;

        statePositionEvasion = position;
        stateList[position] = GEN_END;
    }

    /**
     * Creates a new MoveGenerator.
     *
     * @param newBoard the board.
     */
    MoveGenerator(Position newBoard, KillerTable newKillerTable, HistoryTable newHistoryTable) {
        assert newBoard != null;
        assert newKillerTable != null;
        assert newHistoryTable != null;

        _myPosition = newBoard;
        killerTable = newKillerTable;
        historyTable = newHistoryTable;

        moveList = new MoveList();
        tempMoveList = new MoveList();
        nonCaptureMoveList = new MoveList();

        // Initialize generator
        for (int i = 0; i < generator.length; i++) {
            generator[i] = new Generator();
        }
        generatorHistory = 0;
    }

    static void initializeMain(Attack attack, int height, int transpositionMove) {
        moveList.newList();
        tempMoveList.newList();
        nonCaptureMoveList.newList();
        generatorHistory++;

        generator[generatorHistory].transpositionMove = transpositionMove;
        generator[generatorHistory].primaryKillerMove = killerTable.getPrimaryKiller(height);
        generator[generatorHistory].secondaryKillerMove = killerTable.getSecondaryKiller(height);

        if (attack.isCheck()) {
            generateEvasion(attack);
            moveList.rateEvasion(generator[generatorHistory].transpositionMove, generator[generatorHistory].primaryKillerMove, generator[generatorHistory].secondaryKillerMove, historyTable);
            moveList.sort();
            generator[generatorHistory].statePosition = statePositionEvasion;
            generator[generatorHistory].testState = GEN_EVASION;

            // Set the move number
            attack.numberOfMoves = moveList.getLength();
        } else {
            generator[generatorHistory].statePosition = statePositionMain;
        }
    }

    static void initializeQuiescent(Attack attack, boolean generateCheckingMoves) {
        moveList.newList();
        tempMoveList.newList();
        nonCaptureMoveList.newList();
        generatorHistory++;

        generator[generatorHistory].transpositionMove = Move.NOMOVE;
        generator[generatorHistory].primaryKillerMove = Move.NOMOVE;
        generator[generatorHistory].secondaryKillerMove = Move.NOMOVE;

        if (attack.isCheck()) {
            generateEvasion(attack);
            moveList.rateEvasion(generator[generatorHistory].transpositionMove, generator[generatorHistory].primaryKillerMove, generator[generatorHistory].secondaryKillerMove, historyTable);
            moveList.sort();
            generator[generatorHistory].statePosition = statePositionEvasion;
            generator[generatorHistory].testState = GEN_EVASION;

            // Set the move number
            attack.numberOfMoves = moveList.getLength();
        } else if (generateCheckingMoves) {
            generator[generatorHistory].statePosition = statePositionQuiescentAll;
        } else {
            generator[generatorHistory].statePosition = statePositionQuiescentCapture;
        }
    }

    static void destroy() {
        generatorHistory--;
        nonCaptureMoveList.deleteList();
        tempMoveList.deleteList();
        moveList.deleteList();
    }

    static int getNextMove() {
        while (true) {
            if (moveList.index < moveList.tail) {
                int move = moveList.moves[moveList.index++];

                switch (generator[generatorHistory].testState) {
                    case GEN_TRANSPOSITION:
                        assert isLegal(move);
                        assert moveList.getLength() == 1;
                        break;
                    case GEN_GOODCAPTURE:
                        if (move == generator[generatorHistory].transpositionMove) {
                            continue;
                        }
                        if (!isLegal(move)) {
                            continue;
                        }
                        assert Move.getTarget(move) != Piece.NOPIECE;
                        if (!isGoodCapture(move)) {
                            tempMoveList.moves[tempMoveList.tail++] = move;
                            continue;
                        }
                        break;
                    case GEN_KILLER:
                        if (move == generator[generatorHistory].transpositionMove) {
                            continue;
                        }
                        if (!isPseudo(move)) {
                            continue;
                        }
                        if (!isLegal(move)) {
                            continue;
                        }
                        break;
                    case GEN_NONCAPTURE:
                        if (move == generator[generatorHistory].transpositionMove) {
                            continue;
                        }
                        if (move == generator[generatorHistory].primaryKillerMove) {
                            continue;
                        }
                        if (move == generator[generatorHistory].secondaryKillerMove) {
                            continue;
                        }
                        if (!isLegal(move)) {
                            continue;
                        }
                        break;
                    case GEN_BADCAPTURE:
                        assert isLegal(move);
                        assert !isGoodCapture(move) : _myPosition.convertToGameBoard().toString() + ", " + Move.toGameMove(move).toString();
                        break;
                    case GEN_EVASION:
                        assert isLegal(move);
                        break;
                    case GEN_GOODCAPTURE_QS:
                        if (!isLegal(move)) {
                            continue;
                        }
                        // FIXME assert valueOfIntChessman
                        //assert Move.getTarget(move) != Piece.NOPIECE : Piece.valueOfIntChessman(Move.getTarget(move)).toString();
                        if (!isGoodCapture(move)) {
                            continue;
                        }
                        break;
                    case GEN_CHECK_QS:
                        if (!isLegal(move)) {
                            continue;
                        }
                        if (See.seeMove(move, Move.getChessmanColor(move)) < 0) {
                            continue;
                        }
                        assert _myPosition.isCheckingMove(move) : _myPosition.convertToGameBoard().toString() + ", " + Move.toGameMove(move).toString();
                        break;
                    case GEN_END:
                        assert false : stateList[generator[generatorHistory].statePosition];
                    break;
                    default:
                        assert false : stateList[generator[generatorHistory].statePosition];
                    break;
                }

                return move;
            }

            // Move generation
            int state = stateList[generator[generatorHistory].statePosition++];
            moveList.resetList();

            switch (state) {
                case GEN_TRANSPOSITION:
                    if (Configuration.useTranspositionTable) {
                        if (generator[generatorHistory].transpositionMove != Move.NOMOVE) {
                            moveList.moves[moveList.tail++] = generator[generatorHistory].transpositionMove;
                        }
                        generator[generatorHistory].testState = GEN_TRANSPOSITION;
                    } else {
                        generator[generatorHistory].transpositionMove = Move.NOMOVE;
                    }
                    break;
                case GEN_GOODCAPTURE:
                    generateCaptures();
                    tempMoveList.resetList();
                    moveList.rateFromMVVLVA();
                    moveList.sort();
                    generator[generatorHistory].testState = GEN_GOODCAPTURE;
                    break;
                case GEN_KILLER:
                    if (Configuration.useKillerTable) {
                        if (generator[generatorHistory].primaryKillerMove != Move.NOMOVE) {
                            moveList.moves[moveList.tail++] = generator[generatorHistory].primaryKillerMove;
                        }
                        if (generator[generatorHistory].secondaryKillerMove != Move.NOMOVE) {
                            moveList.moves[moveList.tail++] = generator[generatorHistory].secondaryKillerMove;
                        }
                        generator[generatorHistory].testState = GEN_KILLER;
                    } else {
                        generator[generatorHistory].primaryKillerMove = Move.NOMOVE;
                        generator[generatorHistory].secondaryKillerMove = Move.NOMOVE;
                    }
                    break;
                case GEN_NONCAPTURE:
                    generateNonCaptures();
                    if (Configuration.useHistoryTable) {
                        moveList.rateFromHistory(historyTable);
                        moveList.sort();
                    }
                    generator[generatorHistory].testState = GEN_NONCAPTURE;
                    break;
                case GEN_BADCAPTURE:
                    System.arraycopy(tempMoveList.moves, tempMoveList.head, moveList.moves, moveList.tail, tempMoveList.getLength());
                    moveList.tail += tempMoveList.getLength();
                    generator[generatorHistory].testState = GEN_BADCAPTURE;
                    break;
                case GEN_GOODCAPTURE_QS:
                    generateCaptures();
                    moveList.rateFromMVVLVA();
                    moveList.sort();
                    generator[generatorHistory].testState = GEN_GOODCAPTURE_QS;
                    break;
                case GEN_CHECK_QS:
                    generateChecks();
                    generator[generatorHistory].testState = GEN_CHECK_QS;
                    break;
                case GEN_END:
                    return Move.NOMOVE;
                default:
                    assert false : state;
                break;
            }
        }
    }

    private static boolean isPseudo(int move) {
        int chessmanPosition = Move.getStart(move);
        int piece = _myPosition.board[chessmanPosition];

        // Check chessman
        if (piece == Piece.NOPIECE || Move.getChessman(move) != Piece.getChessman(piece)) {
            return false;
        }

        int color = Move.getChessmanColor(move);

        // Check color
        if (color != Piece.getColor(piece)) {
            return false;
        }

        assert color == _myPosition.activeColor;

        int targetPosition = Move.getEnd(move);

        // Check empty target
        if (_myPosition.board[targetPosition] != Piece.NOPIECE) {
            return false;
        }

        assert Move.getTarget(move) == Piece.NOPIECE;

        int type = Move.getType(move);

        switch (type) {
            case MoveType.NORMAL:
                break;
            case MoveType.PAWNDOUBLE:
                int delta;
                if (color == Color.WHITE) {
                    delta = 16;
                } else {
                    assert color == Color.BLACK;

                    delta = -16;
                }

                if (_myPosition.board[chessmanPosition + delta] == Piece.NOPIECE) {
                    assert _myPosition.board[chessmanPosition + 2 * delta] == Piece.NOPIECE;
                    return true;
                }
                return false;
            case MoveType.PAWNPROMOTION:
            case MoveType.ENPASSANT:
                return false;
            case MoveType.CASTLING:
                switch (targetPosition) {
                    case Square.g1:
                        // Do not test g1 whether it is attacked as we will test it in isLegal()
                        if ((_myPosition.castling & Castling.WHITE_KINGSIDE) != 0
                        && _myPosition.board[Square.f1] == Piece.NOPIECE
                        && _myPosition.board[Square.g1] == Piece.NOPIECE
                        && !_myPosition.isAttacked(Square.f1, Color.BLACK)) {
                            assert _myPosition.board[Square.e1] == Piece.WHITE_KING;
                            assert _myPosition.board[Square.h1] == Piece.WHITE_ROOK;

                            return true;
                        }
                        break;
                    case Square.c1:
                        // Do not test c1 whether it is attacked as we will test it in isLegal()
                        if ((_myPosition.castling & Castling.WHITE_QUEENSIDE) != 0
                        && _myPosition.board[Square.b1] == Piece.NOPIECE
                        && _myPosition.board[Square.c1] == Piece.NOPIECE
                        && _myPosition.board[Square.d1] == Piece.NOPIECE
                        && !_myPosition.isAttacked(Square.d1, Color.BLACK)) {
                            assert _myPosition.board[Square.e1] == Piece.WHITE_KING;
                            assert _myPosition.board[Square.a1] == Piece.WHITE_ROOK;

                            return true;
                        }
                        break;
                    case Square.g8:
                        // Do not test g8 whether it is attacked as we will test it in isLegal()
                        if ((_myPosition.castling & Castling.BLACK_KINGSIDE) != 0
                        && _myPosition.board[Square.f8] == Piece.NOPIECE
                        && _myPosition.board[Square.g8] == Piece.NOPIECE
                        && !_myPosition.isAttacked(Square.f8, Color.WHITE)) {
                            assert _myPosition.board[Square.e8] == Piece.BLACK_KING;
                            assert _myPosition.board[Square.h8] == Piece.BLACK_ROOK;

                            return true;
                        }
                        break;
                    case Square.c8:
                        // Do not test c8 whether it is attacked as we will test it in isLegal()
                        if ((_myPosition.castling & Castling.BLACK_QUEENSIDE) != 0
                        && _myPosition.board[Square.b8] == Piece.NOPIECE
                        && _myPosition.board[Square.c8] == Piece.NOPIECE
                        && _myPosition.board[Square.d8] == Piece.NOPIECE
                        && !_myPosition.isAttacked(Square.d8, Color.WHITE)) {
                            assert _myPosition.board[Square.e8] == Piece.BLACK_KING;
                            assert _myPosition.board[Square.a8] == Piece.BLACK_ROOK;

                            return true;
                        }
                        break;
                    default:
                        assert false : Move.toGameMove(move);
                    break;
                }
                break;
            default:
                assert false : type;
            break;
        }

        int chessman = Piece.getChessman(piece);
        assert chessman == Move.getChessman(move);

        // Check pawn move
        if (chessman == PieceType.PAWN) {
            int delta;
            if (color == Color.WHITE) {
                delta = 16;
            } else {
                assert color == Color.BLACK;

                delta = -16;
            }

            assert _myPosition.board[chessmanPosition + delta] == Piece.NOPIECE;
            return true;
        }

        // Check normal move
        if (_myPosition.canAttack(chessman, color, chessmanPosition, targetPosition)) {
            return true;
        }

        return false;
    }

    /**
     * Returns whether the move is legal.
     *
     * @param move the move.
     * @return true if the move is legal, false otherwise.
     */
    private static boolean isLegal(int move) {
        // Slow test for en passant
        if (Move.getType(move) == MoveType.ENPASSANT) {
            int activeColor = _myPosition.activeColor;
            _myPosition.makeMove(move);
            boolean isCheck = _myPosition.getAttack(activeColor).isCheck();
            _myPosition.undoMove(move);

            return !isCheck;
        }

        int chessmanColor = Move.getChessmanColor(move);

        // Special test for king
        if (Move.getChessman(move) == PieceType.KING) {
            return !_myPosition.isAttacked(Move.getEnd(move), Color.switchColor(chessmanColor));
        }

        assert _myPosition.kingList[chessmanColor].size == 1;
        if (_myPosition.isPinned(Move.getStart(move), chessmanColor)) {
            // We are pinned. Test if we move on the line.
            int kingPosition = _myPosition.kingList[chessmanColor].position[0];
            int attackDeltaStart = Attack.deltas[kingPosition - Move.getStart(move) + 127];
            int attackDeltaEnd = Attack.deltas[kingPosition - Move.getEnd(move) + 127];
            return attackDeltaStart == attackDeltaEnd;
        }

        return true;
    }

    private static boolean isGoodCapture(int move) {
        if (Move.getType(move) == MoveType.PAWNPROMOTION) {
            return Move.getPromotion(move) == PieceType.QUEEN;
        }

        int chessman = Move.getChessman(move);
        int target = Move.getTarget(move);

        assert chessman != Piece.NOPIECE;
        assert target != Piece.NOPIECE;

        if (Piece.getValueFromChessman(chessman) <= Piece.getValueFromChessman(target)) {
            return true;
        }

        return See.seeMove(move, Move.getChessmanColor(move)) >= 0;
    }

    /**
     * Generates the pseudo legal move list.
     */
    private static void generateNonCaptures() {
        assert _myPosition != null;
        assert moveList != null;

        int activeColor = _myPosition.activeColor;

        PositionList tempChessmanList = _myPosition.pawnList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            addPawnNonCaptureMovesTo(_myPosition.board[position], activeColor, position);
        }
        System.arraycopy(nonCaptureMoveList.moves, nonCaptureMoveList.head, moveList.moves, moveList.tail, nonCaptureMoveList.getLength());
        moveList.tail += nonCaptureMoveList.getLength();
        assert _myPosition.kingList[activeColor].size == 1;
        int position = _myPosition.kingList[activeColor].position[0];
        int king = _myPosition.board[position];
        addCastlingMoveIfAllowed(king, position, activeColor);
    }

    private static void generateCaptures() {
        assert _myPosition != null;
        assert moveList != null;

        int activeColor = _myPosition.activeColor;

        PositionList tempChessmanList = _myPosition.pawnList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            addPawnCaptureMovesTo(_myPosition.board[position], activeColor, position);
        }
        tempChessmanList = _myPosition.knightList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.knightDirections, Square.NOPOSITION);
        }
        tempChessmanList = _myPosition.bishopList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.bishopDirections, Square.NOPOSITION);
        }
        tempChessmanList = _myPosition.rookList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.rookDirections, Square.NOPOSITION);
        }
        tempChessmanList = _myPosition.queenList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.queenDirections, Square.NOPOSITION);
        }
        assert _myPosition.kingList[activeColor].size == 1;
        int position = _myPosition.kingList[activeColor].position[0];
        addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.kingDirections, Square.NOPOSITION);
    }

    private static void generateEvasion(Attack attack) {
        assert _myPosition != null;
        assert moveList != null;

        int activeColor = _myPosition.activeColor;
        assert _myPosition.kingList[activeColor].size == 1;
        int kingPosition = _myPosition.kingList[activeColor].position[0];
        int king = _myPosition.board[kingPosition];
        int attackerColor = Color.switchColor(activeColor);
        int oppositeColor = Piece.getColorOpposite(king);
        int moveTemplate = Move.createMove(MoveType.NORMAL, kingPosition, kingPosition, king, Piece.NOPIECE, Piece.NOPIECE);

        // Generate king moves
        for (int delta : Square.kingDirections) {
            assert attack.count > 0;
            boolean isOnCheckLine = false;
            for (int i = 0; i < attack.count; i++) {
                if (Piece.isSliding(_myPosition.board[attack.position[i]]) && delta == attack.delta[i]) {
                    isOnCheckLine = true;
                    break;
                }
            }
            if (!isOnCheckLine) {
                int end = kingPosition + delta;
                if ((end & 0x88) == 0 && !_myPosition.isAttacked(end, attackerColor)) {
                    int target = _myPosition.board[end];
                    if (target == Piece.NOPIECE) {
                        int move = Move.setEndPosition(moveTemplate, end);
                        moveList.moves[moveList.tail++] = move;
                    } else {
                        if (Piece.getColor(target) == oppositeColor) {
                            assert Piece.getChessman(target) != PieceType.KING;
                            int move = Move.setEndPositionAndTarget(moveTemplate, end, target);
                            moveList.moves[moveList.tail++] = move;
                        }
                    }
                }
            }
        }

        // Double check
        if (attack.count >= 2) {
            return;
        }

        assert attack.count == 1;

        int attackerPosition = attack.position[0];
        int attacker = _myPosition.board[attackerPosition];

        // Capture the attacker

        addPawnCaptureMovesToTarget(activeColor, attacker, attackerPosition);
        PositionList tempChessmanList = _myPosition.knightList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            if (!_myPosition.isPinned(position, activeColor)) {
                addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.knightDirections, attackerPosition);
            }
        }
        tempChessmanList = _myPosition.bishopList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            if (!_myPosition.isPinned(position, activeColor)) {
                addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.bishopDirections, attackerPosition);
            }
        }
        tempChessmanList = _myPosition.rookList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            if (!_myPosition.isPinned(position, activeColor)) {
                addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.rookDirections, attackerPosition);
            }
        }
        tempChessmanList = _myPosition.queenList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            if (!_myPosition.isPinned(position, activeColor)) {
                addDefaultCaptureMovesTo(_myPosition.board[position], position, Square.queenDirections, attackerPosition);
            }
        }

        int attackDelta = attack.delta[0];

        // Interpose a chessman
        if (Piece.isSliding(_myPosition.board[attackerPosition])) {
            int end = attackerPosition + attackDelta;
            while (end != kingPosition) {
                assert (end & 0x88) == 0;
                assert _myPosition.board[end] == Piece.NOPIECE;

                addPawnNonCaptureMovesToTarget(activeColor, end);
                tempChessmanList = _myPosition.knightList[activeColor];
                for (int i = 0; i < tempChessmanList.size; i++) {
                    int position = tempChessmanList.position[i];
                    if (!_myPosition.isPinned(position, activeColor)) {
                        addDefaultNonCaptureMovesTo(_myPosition.board[position], position, Square.knightDirections, end);
                    }
                }
                tempChessmanList = _myPosition.bishopList[activeColor];
                for (int i = 0; i < tempChessmanList.size; i++) {
                    int position = tempChessmanList.position[i];
                    if (!_myPosition.isPinned(position, activeColor)) {
                        addDefaultNonCaptureMovesTo(_myPosition.board[position], position, Square.bishopDirections, end);
                    }
                }
                tempChessmanList = _myPosition.rookList[activeColor];
                for (int i = 0; i < tempChessmanList.size; i++) {
                    int position = tempChessmanList.position[i];
                    if (!_myPosition.isPinned(position, activeColor)) {
                        addDefaultNonCaptureMovesTo(_myPosition.board[position], position, Square.rookDirections, end);
                    }
                }
                tempChessmanList = _myPosition.queenList[activeColor];
                for (int i = 0; i < tempChessmanList.size; i++) {
                    int position = tempChessmanList.position[i];
                    if (!_myPosition.isPinned(position, activeColor)) {
                        addDefaultNonCaptureMovesTo(_myPosition.board[position], position, Square.queenDirections, end);
                    }
                }

                end += attackDelta;
            }
        }
    }

    private static void generateChecks() {
        int activeColor = _myPosition.activeColor;

        assert _myPosition.kingList[Color.switchColor(activeColor)].size == 1;
        int enemyKingColor = Color.switchColor(activeColor);
        int enemyKingPosition = _myPosition.kingList[enemyKingColor].position[0];

        PositionList tempChessmanList = _myPosition.pawnList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            boolean isPinned = _myPosition.isPinned(position, enemyKingColor);
            addPawnNonCaptureCheckMovesTo(_myPosition.board[position], activeColor, position, enemyKingPosition, isPinned);
        }
        tempChessmanList = _myPosition.knightList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            boolean isPinned = _myPosition.isPinned(position, enemyKingColor);
            addDefaultNonCaptureCheckMovesTo(_myPosition.board[position], PieceType.KNIGHT, activeColor, position, Square.knightDirections, enemyKingPosition, isPinned);
        }
        tempChessmanList = _myPosition.bishopList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            boolean isPinned = _myPosition.isPinned(position, enemyKingColor);
            addDefaultNonCaptureCheckMovesTo(_myPosition.board[position], PieceType.BISHOP, activeColor, position, Square.bishopDirections, enemyKingPosition, isPinned);
        }
        tempChessmanList = _myPosition.rookList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            boolean isPinned = _myPosition.isPinned(position, enemyKingColor);
            addDefaultNonCaptureCheckMovesTo(_myPosition.board[position], PieceType.ROOK, activeColor, position, Square.rookDirections, enemyKingPosition, isPinned);
        }
        tempChessmanList = _myPosition.queenList[activeColor];
        for (int i = 0; i < tempChessmanList.size; i++) {
            int position = tempChessmanList.position[i];
            boolean isPinned = _myPosition.isPinned(position, enemyKingColor);
            addDefaultNonCaptureCheckMovesTo(_myPosition.board[position], PieceType.QUEEN, activeColor, position, Square.queenDirections, enemyKingPosition, isPinned);
        }
        assert _myPosition.kingList[activeColor].size == 1;
        int position = _myPosition.kingList[activeColor].position[0];
        int king = _myPosition.board[position];
        boolean isPinned = _myPosition.isPinned(position, enemyKingColor);
        addDefaultNonCaptureCheckMovesTo(king, PieceType.KING, activeColor, position, Square.kingDirections, enemyKingPosition, isPinned);
        addCastlingCheckMoveIfAllowed(king, position, activeColor, enemyKingPosition);
    }

    /**
     * Add non-capturing moves from the move delta of the chessman.
     *
     * @param piece          the piece.
     * @param moveDelta      the move delta list.
     * @param targetPosition the target position.
     */
    private static void addDefaultNonCaptureMovesTo(int piece, int position, int[] moveDelta, int targetPosition) {
        assert _myPosition != null;
        assert moveList != null;
        assert moveDelta != null;

        boolean sliding = Piece.isSliding(piece);
        int moveTemplate = Move.createMove(MoveType.NORMAL, position, position, piece, Piece.NOPIECE, Piece.NOPIECE);

        for (int delta : moveDelta) {
            int end = position + delta;

            // Get moves to empty squares
            while ((end & 0x88) == 0 && _myPosition.board[end] == Piece.NOPIECE) {
                if (targetPosition == Square.NOPOSITION || end == targetPosition) {
                    int move = Move.setEndPosition(moveTemplate, end);
                    moveList.moves[moveList.tail++] = move;
                }

                if (!sliding) {
                    break;
                }

                end += delta;
            }
        }
    }

    /**
     * Add non-capturing check moves from the move delta of the chessman.
     *
     * @param piece        the piece.
     * @param moveDelta    the move delta list.
     * @param kingPosition the position of the enemy king.
     * @param isPinned     whether the chessman is pinned.
     */
    private static void addDefaultNonCaptureCheckMovesTo(int piece, int chessman, int chessmanColor, int chessmanPosition, int[] moveDelta, int kingPosition, boolean isPinned) {
        assert _myPosition != null;
        assert moveList != null;
        assert moveDelta != null;

        boolean sliding = Piece.isSliding(piece);
        int attackDeltaStart = Attack.deltas[kingPosition - chessmanPosition + 127];
        int moveTemplate = Move.createMove(MoveType.NORMAL, chessmanPosition, chessmanPosition, piece, Piece.NOPIECE, Piece.NOPIECE);

        for (int delta : moveDelta) {
            int end = chessmanPosition + delta;

            // Get moves to empty squares
            while ((end & 0x88) == 0 && _myPosition.board[end] == Piece.NOPIECE) {
                if (isPinned) {
                    // We are pinned. Test if we move on the line.
                    int attackDeltaEnd = Attack.deltas[kingPosition - end + 127];
                    if (attackDeltaStart != attackDeltaEnd) {
                        int move = Move.setEndPosition(moveTemplate, end);
                        moveList.moves[moveList.tail++] = move;
                    }
                } else if (_myPosition.canAttack(chessman, chessmanColor, end, kingPosition)) {
                    int move = Move.setEndPosition(moveTemplate, end);
                    moveList.moves[moveList.tail++] = move;
                }

                if (!sliding) {
                    break;
                }

                end += delta;
            }
        }
    }

    /**
     * Add capturing moves from the move delta of the chessman.
     *
     * @param piece          the piece.
     * @param moveDelta      the move delta list.
     * @param targetPosition the target position.
     */
    private static void addDefaultCaptureMovesTo(int piece, int position, int[] moveDelta, int targetPosition) {
        assert piece != Piece.NOPIECE;
        assert moveDelta != null;
        assert _myPosition != null;
        assert moveList != null;

        boolean sliding = Piece.isSliding(piece);
        int oppositeColor = Piece.getColorOpposite(piece);
        int moveTemplate = Move.createMove(MoveType.NORMAL, position, position, piece, Piece.NOPIECE, Piece.NOPIECE);

        for (int delta : moveDelta) {
            int end = position + delta;

            // Get moves to empty squares
            while ((end & 0x88) == 0) {
                int target = _myPosition.board[end];
                if (target == Piece.NOPIECE) {
                    if (targetPosition == Square.NOPOSITION || end == targetPosition) {
                        int move = Move.setEndPosition(moveTemplate, end);
                        nonCaptureMoveList.moves[nonCaptureMoveList.tail++] = move;
                    }

                    if (!sliding) {
                        break;
                    }

                    end += delta;
                } else {
                    if (targetPosition == Square.NOPOSITION || end == targetPosition) {
                        // Get the move to the square the next chessman is standing on
                        if (Piece.getColor(target) == oppositeColor
                                && Piece.getChessman(target) != PieceType.KING) {
                            int move = Move.setEndPositionAndTarget(moveTemplate, end, target);
                            moveList.moves[moveList.tail++] = move;
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Add non-capturing moves of the pawn.
     *
     * @param pawn         the pawn.
     * @param pawnColor    the pawn color.
     * @param pawnPosition the pawn position.
     */
    private static void addPawnNonCaptureMovesTo(int pawn, int pawnColor, int pawnPosition) {
        assert pawn != Piece.NOPIECE;
        assert Piece.getChessman(pawn) == PieceType.PAWN;
        assert Piece.getColor(pawn) == pawnColor;
        assert (pawnPosition & 0x88) == 0;
        assert _myPosition != null;
        assert _myPosition.board[pawnPosition] == pawn;
        assert moveList != null;

        int delta = Square.pawnDirections[0];
        if (pawnColor == Color.BLACK) {
            delta *= -1;
        }

        // Move one square forward
        int end = pawnPosition + delta;
        if ((end & 0x88) == 0 && _myPosition.board[end] == Piece.NOPIECE) {
            // GenericRank.R8 = position > 111
            // GenericRank.R1 = position < 8
            if ((end > 111 && pawnColor == Color.WHITE)
                    || (end < 8 && pawnColor == Color.BLACK)) {
                int moveTemplate = Move.createMove(MoveType.PAWNPROMOTION, pawnPosition, end, pawn, Piece.NOPIECE, Piece.NOPIECE);
                int move = Move.setPromotion(moveTemplate, PieceType.QUEEN);
                moveList.moves[moveList.tail++] = move;
                move = Move.setPromotion(moveTemplate, PieceType.ROOK);
                moveList.moves[moveList.tail++] = move;
                move = Move.setPromotion(moveTemplate, PieceType.BISHOP);
                moveList.moves[moveList.tail++] = move;
                move = Move.setPromotion(moveTemplate, PieceType.KNIGHT);
                moveList.moves[moveList.tail++] = move;
            } else {
                int move = Move.createMove(MoveType.NORMAL, pawnPosition, end, pawn, Piece.NOPIECE, Piece.NOPIECE);
                moveList.moves[moveList.tail++] = move;

                // Move two squares forward
                end += delta;
                if ((end & 0x88) == 0 && _myPosition.board[end] == Piece.NOPIECE) {
                    // GenericRank.R4 = end >>> 4 == 3
                    // GenericRank.R5 = end >>> 4 == 4
                    if (((end >>> 4) == 3 && pawnColor == Color.WHITE)
                            || ((end >>> 4) == 4 && pawnColor == Color.BLACK)) {
                        assert ((pawnPosition >>> 4) == 1 && (end >>> 4) == 3 && pawnColor == Color.WHITE) || ((pawnPosition >>> 4) == 6 && (end >>> 4) == 4 && pawnColor == Color.BLACK);

                        move = Move.createMove(MoveType.PAWNDOUBLE, pawnPosition, end, pawn, Piece.NOPIECE, Piece.NOPIECE);
                        moveList.moves[moveList.tail++] = move;
                    }
                }
            }
        }
    }

    /**
     * Add non-capturing moves of pawns to the target position.
     *
     * @param pawnColor      the pawn color.
     * @param targetPosition the target position.
     */
    private static void addPawnNonCaptureMovesToTarget(int pawnColor, int targetPosition) {
        assert pawnColor == Color.WHITE || pawnColor == Color.BLACK;
        assert (targetPosition & 0x88) == 0;
        assert _myPosition != null;
        assert moveList != null;
        assert _myPosition.board[targetPosition] == Piece.NOPIECE;

        int delta = Square.pawnDirections[0];
        int pawnPiece = Piece.BLACK_PAWN;
        if (pawnColor == Color.WHITE) {
            delta *= -1;
            pawnPiece = Piece.WHITE_PAWN;
        }

        // Move one square backward
        int pawnPosition = targetPosition + delta;
        if ((pawnPosition & 0x88) == 0) {
            int pawn = _myPosition.board[pawnPosition];
            if (pawn != Piece.NOPIECE) {
                if (pawn == pawnPiece) {
                    // We found a valid pawn

                    if (!_myPosition.isPinned(pawnPosition, pawnColor)) {
                        // GenericRank.R8 = position > 111
                        // GenericRank.R1 = position < 8
                        if ((targetPosition > 111 && pawnColor == Color.WHITE)
                                || (targetPosition < 8 && pawnColor == Color.BLACK)) {
                            int moveTemplate = Move.createMove(MoveType.PAWNPROMOTION, pawnPosition, targetPosition, pawn, Piece.NOPIECE, Piece.NOPIECE);
                            int move = Move.setPromotion(moveTemplate, PieceType.QUEEN);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.ROOK);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.BISHOP);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.KNIGHT);
                            moveList.moves[moveList.tail++] = move;
                        } else {
                            int move = Move.createMove(MoveType.NORMAL, pawnPosition, targetPosition, pawn, Piece.NOPIECE, Piece.NOPIECE);
                            moveList.moves[moveList.tail++] = move;
                        }
                    }
                }
            } else {
                // Move two squares backward
                pawnPosition += delta;
                if ((pawnPosition & 0x88) == 0) {
                    // GenericRank.R4 = end >>> 4 == 3
                    // GenericRank.R5 = end >>> 4 == 4
                    if (((pawnPosition >>> 4) == 1 && pawnColor == Color.WHITE)
                            || ((pawnPosition >>> 4) == 6 && pawnColor == Color.BLACK)) {
                        assert ((pawnPosition >>> 4) == 1 && (targetPosition >>> 4) == 3 && pawnColor == Color.WHITE) || ((pawnPosition >>> 4) == 6 && (targetPosition >>> 4) == 4 && pawnColor == Color.BLACK);

                        pawn = _myPosition.board[pawnPosition];
                        if (pawn != Piece.NOPIECE && pawn == pawnPiece) {
                            if (!_myPosition.isPinned(pawnPosition, pawnColor)) {
                                int move = Move.createMove(MoveType.PAWNDOUBLE, pawnPosition, targetPosition, pawn, Piece.NOPIECE, Piece.NOPIECE);
                                moveList.moves[moveList.tail++] = move;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Add non-capturing check moves of the pawn to the target position.
     *
     * @param pawn         the IntChessman.
     * @param kingPosition the enemy king position.
     * @param isPinned     whether the pawn is pinned.
     */
    private static void addPawnNonCaptureCheckMovesTo(int pawn, int color, int pawnPosition, int kingPosition, boolean isPinned) {
        assert pawn != Piece.NOPIECE;
        assert (kingPosition & 0x88) == 0;
        assert _myPosition != null;
        assert moveList != null;

        int delta = Square.pawnDirections[0];
        if (color == Color.BLACK) {
            delta *= -1;
        }

        // Move one square forward
        int end = pawnPosition + delta;
        if ((end & 0x88) == 0 && _myPosition.board[end] == Piece.NOPIECE) {
            // GenericRank.R8 = position > 111
            // GenericRank.R1 = position < 8
            if ((end > 111 && color == Color.WHITE)
                    || (end < 8 && color == Color.BLACK)) {
                int moveTemplate = Move.createMove(MoveType.PAWNPROMOTION, pawnPosition, end, pawn, Piece.NOPIECE, Piece.NOPIECE);
                int move = Move.setPromotion(moveTemplate, PieceType.QUEEN);
                _myPosition.makeMove(move);
                boolean isCheck = _myPosition.isAttacked(kingPosition, color);
                _myPosition.undoMove(move);
                if (isCheck) {
                    moveList.moves[moveList.tail++] = move;
                }
                move = Move.setPromotion(moveTemplate, PieceType.ROOK);
                _myPosition.makeMove(move);
                isCheck = _myPosition.isAttacked(kingPosition, color);
                _myPosition.undoMove(move);
                if (isCheck) {
                    moveList.moves[moveList.tail++] = move;
                }
                move = Move.setPromotion(moveTemplate, PieceType.BISHOP);
                _myPosition.makeMove(move);
                isCheck = _myPosition.isAttacked(kingPosition, color);
                _myPosition.undoMove(move);
                if (isCheck) {
                    moveList.moves[moveList.tail++] = move;
                }
                move = Move.setPromotion(moveTemplate, PieceType.KNIGHT);
                _myPosition.makeMove(move);
                isCheck = _myPosition.isAttacked(kingPosition, color);
                _myPosition.undoMove(move);
                if (isCheck) {
                    moveList.moves[moveList.tail++] = move;
                }
            } else {
                if (isPinned) {
                    // We are pinned. Test if we move on the line.
                    int attackDeltaStart = Attack.deltas[kingPosition - pawnPosition + 127];
                    int attackDeltaEnd = Attack.deltas[kingPosition - end + 127];
                    if (attackDeltaStart != attackDeltaEnd) {
                        int move = Move.createMove(MoveType.NORMAL, pawnPosition, end, pawn, Piece.NOPIECE, Piece.NOPIECE);
                        moveList.moves[moveList.tail++] = move;
                    }
                } else if (_myPosition.canAttack(PieceType.PAWN, color, end, kingPosition)) {
                    int move = Move.createMove(MoveType.NORMAL, pawnPosition, end, pawn, Piece.NOPIECE, Piece.NOPIECE);
                    moveList.moves[moveList.tail++] = move;
                }

                // Move two squares forward
                end += delta;
                if ((end & 0x88) == 0 && _myPosition.board[end] == Piece.NOPIECE) {
                    // GenericRank.R4 = end >>> 4 == 3
                    // GenericRank.R5 = end >>> 4 == 4
                    if (((end >>> 4) == 3 && color == Color.WHITE)
                            || ((end >>> 4) == 4 && color == Color.BLACK)) {
                        assert ((pawnPosition >>> 4) == 1 && (end >>> 4) == 3 && color == Color.WHITE) || ((pawnPosition >>> 4) == 6 && (end >>> 4) == 4 && color == Color.BLACK);

                        if (isPinned) {
                            // We are pinned. Test if we move on the line.
                            int attackDeltaStart = Attack.deltas[kingPosition - pawnPosition + 127];
                            int attackDeltaEnd = Attack.deltas[kingPosition - end + 127];
                            if (attackDeltaStart != attackDeltaEnd) {
                                int move = Move.createMove(MoveType.PAWNDOUBLE, pawnPosition, end, pawn, Piece.NOPIECE, Piece.NOPIECE);
                                moveList.moves[moveList.tail++] = move;
                            }
                        } else if (_myPosition.canAttack(PieceType.PAWN, color, end, kingPosition)) {
                            int move = Move.createMove(MoveType.PAWNDOUBLE, pawnPosition, end, pawn, Piece.NOPIECE, Piece.NOPIECE);
                            moveList.moves[moveList.tail++] = move;
                        }
                    }
                }
            }
        }
    }

    /**
     * Add capturing moves of the pawn.
     *
     * @param pawn         the pawn.
     * @param pawnColor    the pawn color.
     * @param pawnPosition the pawn position.
     */
    private static void addPawnCaptureMovesTo(int pawn, int pawnColor, int pawnPosition) {
        assert pawn != Piece.NOPIECE;
        assert Piece.getChessman(pawn) == PieceType.PAWN;
        assert Piece.getColor(pawn) == pawnColor;
        assert (pawnPosition & 0x88) == 0;
        assert _myPosition != null;
        assert _myPosition.board[pawnPosition] == pawn;
        assert moveList != null;

        for (int i = 1; i < Square.pawnDirections.length; i++) {
            int delta = Square.pawnDirections[i];
            if (pawnColor == Color.BLACK) {
                delta *= -1;
            }

            int end = pawnPosition + delta;
            if ((end & 0x88) == 0) {
                int target = _myPosition.board[end];
                if (target != Piece.NOPIECE) {
                    if (Piece.getColorOpposite(target) == pawnColor
                            && Piece.getChessman(target) != PieceType.KING) {
                        // Capturing move

                        // GenericRank.R8 = position > 111
                        // GenericRank.R1 = position < 8
                        if ((end > 111 && pawnColor == Color.WHITE)
                                || (end < 8 && pawnColor == Color.BLACK)) {
                            int moveTemplate = Move.createMove(MoveType.PAWNPROMOTION, pawnPosition, end, pawn, target, Piece.NOPIECE);
                            int move = Move.setPromotion(moveTemplate, PieceType.QUEEN);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.ROOK);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.BISHOP);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.KNIGHT);
                            moveList.moves[moveList.tail++] = move;
                        } else {
                            int move = Move.createMove(MoveType.NORMAL, pawnPosition, end, pawn, target, Piece.NOPIECE);
                            moveList.moves[moveList.tail++] = move;
                        }
                    }
                } else if (end == _myPosition.enPassantSquare) {
                    // En passant move
                    assert _myPosition.enPassantSquare != Square.NOPOSITION;
                    assert ((end >>> 4) == 2 && pawnColor == Color.BLACK) || ((end >>> 4) == 5 && pawnColor == Color.WHITE);

                    // Calculate the en passant position
                    int enPassantTargetPosition;
                    if (pawnColor == Color.WHITE) {
                        enPassantTargetPosition = end - 16;
                    } else {
                        enPassantTargetPosition = end + 16;
                    }
                    target = _myPosition.board[enPassantTargetPosition];
                    assert Piece.getChessman(target) == PieceType.PAWN;
                    assert Piece.getColor(target) == Color.switchColor(pawnColor);

                    int move = Move.createMove(MoveType.ENPASSANT, pawnPosition, end, pawn, target, Piece.NOPIECE);
                    moveList.moves[moveList.tail++] = move;
                }
            }
        }
    }

    /**
     * Add capturing moves of pawns to the target position.
     *
     * @param pawnColor      the color of the attacking pawn.
     * @param target         the target chessman.
     * @param targetPosition the target position.
     */
    private static void addPawnCaptureMovesToTarget(int pawnColor, int target, int targetPosition) {
        assert pawnColor == Color.WHITE || pawnColor == Color.BLACK;
        assert target != Piece.NOPIECE;
        assert Piece.getColor(target) == Color.switchColor(pawnColor);
        assert (targetPosition & 0x88) == 0;
        assert _myPosition != null;
        assert moveList != null;
        assert _myPosition.board[targetPosition] != Piece.NOPIECE;
        assert _myPosition.board[targetPosition] == target;
        assert Piece.getChessman(_myPosition.board[targetPosition]) != PieceType.KING;
        assert Piece.getColorOpposite(_myPosition.board[targetPosition]) == pawnColor;

        int pawnPiece = Piece.BLACK_PAWN;
        int enPassantDelta = -16;
        if (pawnColor == Color.WHITE) {
            pawnPiece = Piece.WHITE_PAWN;
            enPassantDelta = 16;
        }
        int enPassantPosition = targetPosition + enPassantDelta;

        for (int i = 1; i < Square.pawnDirections.length; i++) {
            int delta = Square.pawnDirections[i];
            if (pawnColor == Color.WHITE) {
                delta *= -1;
            }

            int pawnPosition = targetPosition + delta;
            if ((pawnPosition & 0x88) == 0) {
                int pawn = _myPosition.board[pawnPosition];
                if (pawn != Piece.NOPIECE && pawn == pawnPiece) {
                    // We found a valid pawn

                    if (!_myPosition.isPinned(pawnPosition, pawnColor)) {
                        // GenericRank.R8 = position > 111
                        // GenericRank.R1 = position < 8
                        if ((targetPosition > 111 && pawnColor == Color.WHITE)
                                || (targetPosition < 8 && pawnColor == Color.BLACK)) {
                            int moveTemplate = Move.createMove(MoveType.PAWNPROMOTION, pawnPosition, targetPosition, pawn, target, Piece.NOPIECE);
                            int move = Move.setPromotion(moveTemplate, PieceType.QUEEN);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.ROOK);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.BISHOP);
                            moveList.moves[moveList.tail++] = move;
                            move = Move.setPromotion(moveTemplate, PieceType.KNIGHT);
                            moveList.moves[moveList.tail++] = move;
                        } else {
                            int move = Move.createMove(MoveType.NORMAL, pawnPosition, targetPosition, pawn, target, Piece.NOPIECE);
                            moveList.moves[moveList.tail++] = move;
                        }
                    }
                }
                if (enPassantPosition == _myPosition.enPassantSquare) {
                    // En passant move
                    pawnPosition = pawnPosition + enPassantDelta;
                    assert (enPassantPosition & 0x88) == 0;
                    assert (pawnPosition & 0x88) == 0;

                    pawn = _myPosition.board[pawnPosition];
                    if (pawn != Piece.NOPIECE && pawn == pawnPiece) {
                        // We found a valid pawn which can do a en passant move

                        if (!_myPosition.isPinned(pawnPosition, pawnColor)) {
                            assert ((enPassantPosition >>> 4) == 2 && pawnColor == Color.BLACK) || ((enPassantPosition >>> 4) == 5 && pawnColor == Color.WHITE);
                            assert Piece.getChessman(target) == PieceType.PAWN;

                            int move = Move.createMove(MoveType.ENPASSANT, pawnPosition, enPassantPosition, pawn, target, Piece.NOPIECE);
                            moveList.moves[moveList.tail++] = move;
                        }
                    }
                }
            }
        }
    }

    /**
     * Add the castling moves to the move list.
     *
     * @param king the king.
     */
    private static void addCastlingMoveIfAllowed(int king, int kingPosition, int color) {
        assert king != Piece.NOPIECE;
        assert (kingPosition & 0x88) == 0;
        assert _myPosition != null;
        assert moveList != null;

        if (color == Color.WHITE) {
            // Do not test g1 whether it is attacked as we will test it in isLegal()
            if ((_myPosition.castling & Castling.WHITE_KINGSIDE) != 0
                    && _myPosition.board[Square.f1] == Piece.NOPIECE
                    && _myPosition.board[Square.g1] == Piece.NOPIECE
                    && !_myPosition.isAttacked(Square.f1, Color.BLACK)) {
                assert _myPosition.board[Square.e1] == Piece.WHITE_KING;
                assert _myPosition.board[Square.h1] == Piece.WHITE_ROOK;

                int move = Move.createMove(MoveType.CASTLING, kingPosition, Square.g1, king, Piece.NOPIECE, Piece.NOPIECE);
                moveList.moves[moveList.tail++] = move;
            }
            // Do not test c1 whether it is attacked as we will test it in isLegal()
            if ((_myPosition.castling & Castling.WHITE_QUEENSIDE) != 0
                    && _myPosition.board[Square.b1] == Piece.NOPIECE
                    && _myPosition.board[Square.c1] == Piece.NOPIECE
                    && _myPosition.board[Square.d1] == Piece.NOPIECE
                    && !_myPosition.isAttacked(Square.d1, Color.BLACK)) {
                assert _myPosition.board[Square.e1] == Piece.WHITE_KING;
                assert _myPosition.board[Square.a1] == Piece.WHITE_ROOK;

                int move = Move.createMove(MoveType.CASTLING, kingPosition, Square.c1, king, Piece.NOPIECE, Piece.NOPIECE);
                moveList.moves[moveList.tail++] = move;
            }
        } else {
            assert color == Color.BLACK;

            // Do not test g8 whether it is attacked as we will test it in isLegal()
            if ((_myPosition.castling & Castling.BLACK_KINGSIDE) != 0
                    && _myPosition.board[Square.f8] == Piece.NOPIECE
                    && _myPosition.board[Square.g8] == Piece.NOPIECE
                    && !_myPosition.isAttacked(Square.f8, Color.WHITE)) {
                assert _myPosition.board[Square.e8] == Piece.BLACK_KING;
                assert _myPosition.board[Square.h8] == Piece.BLACK_ROOK;

                int move = Move.createMove(MoveType.CASTLING, kingPosition, Square.g8, king, Piece.NOPIECE, Piece.NOPIECE);
                moveList.moves[moveList.tail++] = move;
            }
            // Do not test c8 whether it is attacked as we will test it in isLegal()
            if ((_myPosition.castling & Castling.BLACK_QUEENSIDE) != 0
                    && _myPosition.board[Square.b8] == Piece.NOPIECE
                    && _myPosition.board[Square.c8] == Piece.NOPIECE
                    && _myPosition.board[Square.d8] == Piece.NOPIECE
                    && !_myPosition.isAttacked(Square.d8, Color.WHITE)) {
                assert _myPosition.board[Square.e8] == Piece.BLACK_KING;
                assert _myPosition.board[Square.a8] == Piece.BLACK_ROOK;

                int move = Move.createMove(MoveType.CASTLING, kingPosition, Square.c8, king, Piece.NOPIECE, Piece.NOPIECE);
                moveList.moves[moveList.tail++] = move;
            }
        }
    }

    /**
     * Add the castling check moves to the move list.
     *
     * @param king           the king.
     * @param targetPosition the position of the enemy king.
     */
    private static void addCastlingCheckMoveIfAllowed(int king, int kingPosition, int color, int targetPosition) {
        assert king != Piece.NOPIECE;
        assert (kingPosition & 0x88) == 0;
        assert _myPosition != null;
        assert moveList != null;

        if (color == Color.WHITE) {
            // Do not test g1 whether it is attacked as we will test it in isLegal()
            if ((_myPosition.castling & Castling.WHITE_KINGSIDE) != 0
                    && _myPosition.board[Square.f1] == Piece.NOPIECE
                    && _myPosition.board[Square.g1] == Piece.NOPIECE
                    && !_myPosition.isAttacked(Square.f1, Color.BLACK)) {
                assert _myPosition.board[Square.e1] == Piece.WHITE_KING;
                assert _myPosition.board[Square.h1] == Piece.WHITE_ROOK;

                if (_myPosition.canAttack(PieceType.ROOK, color, Square.f1, targetPosition)) {
                    int move = Move.createMove(MoveType.CASTLING, kingPosition, Square.g1, king, Piece.NOPIECE, Piece.NOPIECE);
                    moveList.moves[moveList.tail++] = move;
                }
            }
            // Do not test c1 whether it is attacked as we will test it in isLegal()
            if ((_myPosition.castling & Castling.WHITE_QUEENSIDE) != 0
                    && _myPosition.board[Square.b1] == Piece.NOPIECE
                    && _myPosition.board[Square.c1] == Piece.NOPIECE
                    && _myPosition.board[Square.d1] == Piece.NOPIECE
                    && !_myPosition.isAttacked(Square.d1, Color.BLACK)) {
                assert _myPosition.board[Square.e1] == Piece.WHITE_KING;
                assert _myPosition.board[Square.a1] == Piece.WHITE_ROOK;

                if (_myPosition.canAttack(PieceType.ROOK, color, Square.d1, targetPosition)) {
                    int move = Move.createMove(MoveType.CASTLING, kingPosition, Square.c1, king, Piece.NOPIECE, Piece.NOPIECE);
                    moveList.moves[moveList.tail++] = move;
                }
            }
        } else {
            assert color == Color.BLACK;

            // Do not test g8 whether it is attacked as we will test it in isLegal()
            if ((_myPosition.castling & Castling.BLACK_KINGSIDE) != 0
                    && _myPosition.board[Square.f8] == Piece.NOPIECE
                    && _myPosition.board[Square.g8] == Piece.NOPIECE
                    && !_myPosition.isAttacked(Square.f8, Color.WHITE)) {
                assert _myPosition.board[Square.e8] == Piece.BLACK_KING;
                assert _myPosition.board[Square.h8] == Piece.BLACK_ROOK;

                if (_myPosition.canAttack(PieceType.ROOK, color, Square.f8, targetPosition)) {
                    int move = Move.createMove(MoveType.CASTLING, kingPosition, Square.g8, king, Piece.NOPIECE, Piece.NOPIECE);
                    moveList.moves[moveList.tail++] = move;
                }
            }
            // Do not test c8 whether it is attacked as we will test it in isLegal()
            if ((_myPosition.castling & Castling.BLACK_QUEENSIDE) != 0
                    && _myPosition.board[Square.b8] == Piece.NOPIECE
                    && _myPosition.board[Square.c8] == Piece.NOPIECE
                    && _myPosition.board[Square.d8] == Piece.NOPIECE
                    && !_myPosition.isAttacked(Square.d8, Color.WHITE)) {
                assert _myPosition.board[Square.e8] == Piece.BLACK_KING;
                assert _myPosition.board[Square.a8] == Piece.BLACK_ROOK;

                if (_myPosition.canAttack(PieceType.ROOK, color, Square.d8, targetPosition)) {
                    int move = Move.createMove(MoveType.CASTLING, kingPosition, Square.c8, king, Piece.NOPIECE, Piece.NOPIECE);
                    moveList.moves[moveList.tail++] = move;
                }
            }
        }
    }

}
