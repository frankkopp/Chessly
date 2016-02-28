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

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameMoveList;
import fko.chessly.ui.SwingGUI.MoveList;

/**
 * The move generator for Omega Engine.<br/>
 * It generates pseudo legal and legal moves for a given position.<br/>
 * As long as the position does not change for consecutive calls the generated
 * move lists are caches.<br/>
 * <b>This class is not thread safe as it uses static variables to avoid generating them
 * during each object creation.</b><br/>
 * @author Frank
 *
 */
public class OmegaMoveGenerator {

    private long _zobristLastPosition = 0;

    private OmegaBoardPosition _position = null;
    private OmegaColor _activePlayer;

    // cache the generated move list for repeated call to generateMoves
    private OmegaMoveList _cachedPseudoLegalMoveList = null;
    private boolean _cachedPseudoLegalMoveListValid = false;
    private OmegaMoveList _cachedLegalMoveList = null;
    private boolean _cachedLegalMoveListValid = false;

    // these are are working lists as static fields to avoid to have to
    // create them every time. Instead of creating the need to be cleared before use.
    private static final OmegaMoveList _legalMoves = new OmegaMoveList();
    // these are all pseudo legal
    private static final OmegaMoveList _pseudoLegalMoves = new OmegaMoveList(); // all moves
    private static final OmegaMoveList _capturingMoves = new OmegaMoveList(); // only capturing moves
    private static final OmegaMoveList _checkingMoves = new OmegaMoveList(); // only checking moves
    private static final OmegaMoveList _nonCapturingMoves = new OmegaMoveList(); // only non capturing moves
    private static final OmegaMoveList _evasionMoves = new OmegaMoveList(); // only evasion moves



    /**
     * Constructor
     */
    public OmegaMoveGenerator() {

    }

    /**
     * Generates all legal moves for a position.
     * Legal moves have been checked if they leave the king in check or not.
     * Repeated calls to this will return a cached list as long the position has
     * not changed in between.
     *
     * @param position
     * @param capturingOnly if only capturing moves should be generated for quiescence moves
     * @return legal moves
     */
    public OmegaMoveList getLegalMoves(OmegaBoardPosition position, boolean capturingOnly) {
        if (position==null) throw new IllegalArgumentException("position may not be null to generate moves");

        if (_cachedLegalMoveListValid && position.getZobristKey() == _zobristLastPosition) {
            System.out.println("PseudoLegalMoves form cache");
            return _cachedLegalMoveList;
        }

        _position = position;
        _activePlayer = _position._nextPlayer;
        // position has changed - cache is invalid
        _cachedLegalMoveListValid = false;
        _cachedPseudoLegalMoveListValid = false;

        // remember the last position to see when it has changed
        // if changed the cache is always invalid
        this._zobristLastPosition = position.getZobristKey();

        // clear all lists
        clearLists();

        // call the move generators
        if (position.hasCheck()) {
            generateEvasionMoves();
        } else {
            generatePseudoLegaMoves();
            filterLegalMovesOnly(_pseudoLegalMoves, _legalMoves);
            sortMoves(_legalMoves);
        }

        // DEBUG - temporary code until we actually can create moves
        if (_legalMoves.empty()) {
            GameBoard board = new GameBoardImpl(position.toFENString());
            GameMoveList moves = board.generateMoves();
            moves.stream().forEach(c -> _legalMoves.add(OmegaMove.convertFromGameMove(c)));
        }

        // cache the list of legal moves
        _cachedLegalMoveList = _legalMoves;
        _cachedLegalMoveListValid = true;

        // return a clone of the list as we will continue to use the list as a static list
        return _legalMoves.clone();
    }

    /**
     * Generates all  moves for a position. These moves may leave the king in check
     * and may be illegal.
     * Before committing the to a board they to be checked if they leave the king in check.
     * Repeated calls to this will return a cached list as long the position has
     * not changed in between.
     *
     * @param position
     * @param capturingOnly
     * @return list of moves which may leave the king in check
     */
    public OmegaMoveList getPseudoLegalMoves(OmegaBoardPosition position, boolean capturingOnly) {
        if (position==null) throw new IllegalArgumentException("position may not be null to generate moves");

        if (_cachedPseudoLegalMoveListValid && position.getZobristKey() == _zobristLastPosition) {
            System.out.println("PseudoLegalMoves form cache");
            return _cachedPseudoLegalMoveList;
        }

        _position = position;
        _activePlayer = _position._nextPlayer;
        // position has changed - cache is invalid
        _cachedLegalMoveListValid = false;
        _cachedPseudoLegalMoveListValid = false;

        // remember the last position to see when it has changed
        // if changed the cache is always invalid
        this._zobristLastPosition = position.getZobristKey();

        // clear all lists
        clearLists();

        // call the move generators
        if (position.hasCheck()) {
            generateEvasionMoves();
        } else {
            generatePseudoLegaMoves();
            sortMoves(_pseudoLegalMoves);
        }

        // DEBUG - temporary code until we actually can create moves
        if (_legalMoves.empty()) {
            GameBoard board = new GameBoardImpl(position.toFENString());
            GameMoveList moves = board.generateMoves();
            moves.stream().forEach(c -> _pseudoLegalMoves.add(OmegaMove.convertFromGameMove(c)));
        }

        // cache the list of legal moves
        _cachedPseudoLegalMoveList = _pseudoLegalMoves;
        _cachedPseudoLegalMoveListValid = true;

        // return a clone of the list as we will continue to use the list as a static list
        return _pseudoLegalMoves.clone();
    }

    /**
     * @param legalMoves
     */
    private void generatePseudoLegaMoves() {
        /*
         * Start with capturing move - lower pieces to higher pieces
         * Then checking moves
         * Then non capturing - lower to higher pieces
         *      moves to better positions first
         *      - e.g. Knights in the middle
         *      - sliding pieces middle border position with much control over board
         *      - King at the beginning in castle or corners, at the end in middle
         *      - middle pawns forward in the beginning
         *
         * Use different lists to add moves to avoid repeated looping
         * Too expensive to create several lists? Make them static and clear them instead of creating?
         */

        generatePawnMoves();
        generateKnightMoves();
        generateBishopMoves();
        generateRookMoves();
        generateQueenMoves();
        generateKingMoves();

    }

    /**
     * @param position
     *
     */
    private void generatePawnMoves() {
        int pawnDir = -1 *_activePlayer.ordinal();
        for (OmegaSquare os : _position._pawnSquares[_activePlayer.ordinal()]) {
            assert _position._x88Board[os.ordinal()].getType() == OmegaPieceType.PAWN;

        }
    }

    /**
     * @param position
     *
     */
    private void generateKnightMoves() {
        // TODO Auto-generated method stub

    }

    /**
     * @param position
     *
     */
    private void generateBishopMoves() {
        // TODO Auto-generated method stub

    }

    /**
     * @param position
     *
     */
    private void generateRookMoves() {
        // TODO Auto-generated method stub

    }

    /**
     * @param position
     *
     */
    private void generateQueenMoves() {
        // TODO Auto-generated method stub

    }

    /**
     *
     */
    private void generateKingMoves() {
        // TODO Auto-generated method stub

    }

    /**
     * @param position
     * @param legalMoves
     */
    private void generateEvasionMoves() {
        // TODO Auto-generated method stub

    }

    /**
     * @param legalMoves
     */
    private void sortMoves(OmegaMoveList list) {
        // TODO Auto-generated method stub

    }

    /**
     * @param pseudolegalmoves
     * @param legalMoves
     */
    private void filterLegalMovesOnly(OmegaMoveList pseudolegalmoves, OmegaMoveList legalMoves) {
        // TODO Auto-generated method stub

    }

    /**
     * Clears all lists
     */
    private void clearLists() {
        _legalMoves.clear();
        _pseudoLegalMoves.clear();
        _evasionMoves.clear();
        _capturingMoves.clear();
        _checkingMoves.clear();
        _nonCapturingMoves.clear();
    }








}
