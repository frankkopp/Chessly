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
     * Before committing them to a board they need to be checked if they leave the king in check.
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
            //System.out.println("PseudoLegalMoves form cache");
            //return _cachedPseudoLegalMoveList;
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
         * Then non capturing - lower to higher pieces
         *      - ideally:
         *      - moves to better positions first
         *      - e.g. Knights in the middle
         *      - sliding pieces middle border position with much control over board
         *      - King at the beginning in castle or corners, at the end in middle
         *      - middle pawns forward in the beginning
         * Use different lists to add moves to avoid repeated looping
         * Too expensive to create several lists? Make them static and clear them instead of creating?
         */

        generatePawnMoves();
        generateKnightMoves();
        generateBishopMoves();
        generateRookMoves();
        generateQueenMoves();
        generateKingMoves();

        // TODO
        // sort captureList - according to value diff
        // sort checking moves - lowest ranking first - should already have this order?
        // sort non capturing - via better piece/position/game phase value

        _pseudoLegalMoves.add(_capturingMoves);
        _pseudoLegalMoves.add(_nonCapturingMoves);
    }

    /**
     * @param position
     *
     */
    private void generatePawnMoves() {
        int pawnDir = _activePlayer.isBlack() ? -1 : 1;
        for (OmegaSquare os : _position._pawnSquares[_activePlayer.ordinal()]) {
            assert _position._x88Board[os.ordinal()].getType() == OmegaPieceType.PAWN;

            final int from = os.ordinal();
            int[] directions = OmegaSquare.pawnDirections;

            for (int d : directions) {
                int to = from + d * pawnDir;

                if ((to & 0x88) == 0) { // valid square

                    final OmegaMoveType type = OmegaMoveType.NORMAL;
                    final OmegaSquare fromSquare = OmegaSquare.getSquare(from);
                    final OmegaSquare toSquare = OmegaSquare.getSquare(to);
                    final OmegaPiece piece = OmegaPiece.getPiece(OmegaPieceType.PAWN, _activePlayer);
                    final OmegaPiece target = _position._x88Board[to];
                    final OmegaPiece promotion = OmegaPiece.NOPIECE;

                    // capture
                    if (d != OmegaSquare.N // not straight
                            && target != OmegaPiece.NOPIECE // not empty
                            && (target.getColor() == _activePlayer.getInverseColor())) { // opponents color

                        assert target.getType() != OmegaPieceType.KING; // did we miss a check?

                        // capture & promotion
                        if (to > 111) { // rank 8
                            assert _activePlayer.isWhite(); // checking for  color is probably redundant
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.WHITE_QUEEN));
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.WHITE_ROOK));
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.WHITE_BISHOP));
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.WHITE_KNIGHT));
                        } else if (to < 8) { // rank 1
                            assert _activePlayer.isBlack(); // checking for  color is probably redundant
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.BLACK_QUEEN));
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.BLACK_ROOK));
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.BLACK_BISHOP));
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.BLACK_KNIGHT));
                        } else if (toSquare == _position._enPassantSquare) { //  en passant capture
                            // which target?
                            final int t = _activePlayer.isWhite() ? _position._enPassantSquare.getSouth().ordinal() : _position._enPassantSquare.getNorth().ordinal();
                            _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.ENPASSANT,fromSquare,toSquare,piece,_position._x88Board[t],promotion));
                        } else { // normal capture
                            _capturingMoves.add(OmegaMove.createMove(type,fromSquare,toSquare,piece,target,promotion));
                        }
                    }
                    // no capture
                    else if (d == OmegaSquare.N) {
                        if (target == OmegaPiece.NOPIECE){ // way needs to be free
                            // promotion
                            if (to > 111) { // rank 8
                                assert _activePlayer.isWhite(); // checking for  color is probably redundant
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.WHITE_QUEEN));
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.WHITE_ROOK));
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.WHITE_BISHOP));
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.WHITE_KNIGHT));
                            } else if (to < 8) { // rank 1
                                assert _activePlayer.isBlack(); // checking for  color is probably redundant
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.BLACK_QUEEN));
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.BLACK_ROOK));
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.BLACK_BISHOP));
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PROMOTION,fromSquare,toSquare,piece,target,OmegaPiece.BLACK_KNIGHT));
                            }
                            // pawndouble
                            else if (_activePlayer.isWhite()
                                    && fromSquare.isWhitePawnBaseRow()
                                    && (_position._x88Board[fromSquare.ordinal()+(2*OmegaSquare.N)]) == OmegaPiece.NOPIECE) {
                                // on rank 2 && rank 4 is free(rank 3 already checked via target)
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PAWNDOUBLE,fromSquare,toSquare.getNorth(),piece,target,promotion));
                            }
                            else if (_activePlayer.isBlack()
                                    && fromSquare.isBlackPawnBaseRow()
                                    && _position._x88Board[fromSquare.ordinal()+(2*OmegaSquare.S)] == OmegaPiece.NOPIECE) {
                                // on rank 7 && rank 5 is free(rank 6 already checked via target)
                                _nonCapturingMoves.add(OmegaMove.createMove(OmegaMoveType.PAWNDOUBLE,fromSquare,toSquare.getSouth(),piece,target,promotion));
                            }
                            // normal pawn move
                            _nonCapturingMoves.add(OmegaMove.createMove(type,fromSquare,toSquare,piece,target,promotion));
                        }
                    }
                }
            }
        }
    }

    /**
     * @param position
     *
     */
    private void generateKnightMoves() {
        for (OmegaSquare os : _position._knightSquares[_activePlayer.ordinal()]) {
            assert _position._x88Board[os.ordinal()].getType() == OmegaPieceType.KNIGHT;

            final int from = os.ordinal();
            int[] directions = OmegaSquare.knightDirections;

            for (int d : directions) {
                int to = from + d;

                if ((to & 0x88) == 0) { // valid square
                    final OmegaPiece target = _position._x88Board[to];

                    // free square - non capture
                    if (target == OmegaPiece.NOPIECE) { // empty
                        _nonCapturingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.NORMAL,
                                OmegaSquare.getSquare(from),OmegaSquare.getSquare(to),
                                OmegaPiece.getPiece(OmegaPieceType.KNIGHT, _activePlayer),target,OmegaPiece.NOPIECE));
                    }
                    // occupied square - capture or ignore
                    else if (target.getColor() == _activePlayer.getInverseColor()) { // opponents color
                        assert target.getType() != OmegaPieceType.KING; // did we miss a check?
                        _capturingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.NORMAL,
                                OmegaSquare.getSquare(from),OmegaSquare.getSquare(to),
                                OmegaPiece.getPiece(OmegaPieceType.KNIGHT, _activePlayer),target,OmegaPiece.NOPIECE));
                    }
                }
            }
        }
    }

    /**
     * @param position
     *
     */
    private void generateBishopMoves() {

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
    private static void clearLists() {
        _legalMoves.clear();
        _pseudoLegalMoves.clear();
        _evasionMoves.clear();
        _capturingMoves.clear();
        _nonCapturingMoves.clear();
    }








}
