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

import java.util.EnumSet;

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

    static private final boolean CACHE = false;

    // remember the last position to control cache validity
    private long _zobristLastPosition = 0;

    // the current position we generate the move for
    // is set in the getMoves methods
    private OmegaBoardPosition _position = null;

    // which color do we generate moves for
    private OmegaColor _activePlayer;

    // cache the generated move list for repeated call to generateMoves
    private OmegaMoveList _cachedPseudoLegalMoveList = null;
    private boolean _cachedPseudoLegalMoveListValid = false;
    private OmegaMoveList _cachedLegalMoveList = null;
    private boolean _cachedLegalMoveListValid = false;

    // these are are working lists as fields to avoid to have to
    // create them every time. Instead of creating the need to be cleared before use.
    private final OmegaMoveList _legalMoves = new OmegaMoveList();
    // these are all pseudo legal
    private final OmegaMoveList _pseudoLegalMoves = new OmegaMoveList(); // all moves
    private final OmegaMoveList _capturingMoves = new OmegaMoveList(); // only capturing moves
    private final OmegaMoveList _nonCapturingMoves = new OmegaMoveList(); // only non capturing moves
    private final OmegaMoveList _castlingMoves = new OmegaMoveList(); // only non castling moves
    private final OmegaMoveList _evasionMoves = new OmegaMoveList(); // only evasion moves

    /**
     * Creates a new {@link OmegaMoveGenerator}
     */
    public OmegaMoveGenerator() {

    }

    /**
     * Generates all legal moves for a position.
     * Legal moves have been checked if they leave the king in check or not.
     * Repeated calls to this will return a cached list as long the position has
     * not changed in between.
     * This method basically calls <code>getPseudoLegalMoves</code> and the filters the
     * non legal moves out of the provided list by checking each move if it leaves
     * the king in check.
     *
     * @param position
     * @param capturingOnly if only capturing moves should be generated for quiescence moves
     * @return legal moves
     */
    public OmegaMoveList getLegalMoves(OmegaBoardPosition position, boolean capturingOnly) {
        if (position==null) throw new IllegalArgumentException("position may not be null to generate moves");

        // TODO zobrist is could collide - then this will break.
        if (_cachedLegalMoveListValid && position.getZobristKey() == _zobristLastPosition) {
            if (CACHE) return _cachedLegalMoveList;
        }

        // update position
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
            // DEBUG temporary -- only generate check evasion moves - not yet implemented
            generatePseudoLegaMoves();
            filterLegalMovesOnly();
            sortMoves(_legalMoves);
        } else {
            generatePseudoLegaMoves();
            filterLegalMovesOnly();
            sortMoves(_legalMoves);
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
            if (CACHE) return _cachedPseudoLegalMoveList;
        }

        // update position
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
            // DEBUG temporary -- only generate check evasion moves - not yet implemented
            generatePseudoLegaMoves();
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
     * Generates all pseudo legal moves from the given position.
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
         * Too expensive to create several lists every call?
         * Make them static and clear them instead of creating!!
         */

        generatePawnMoves();
        generateKnightMoves();
        generateBishopMoves();
        generateRookMoves();
        generateQueenMoves();
        generateKingMoves();
        generateCastlingMoves();

        // TODO sort moves
        // sort captureList - according to value diff
        // sort non capturing - via better piece/position/game phase value

        _pseudoLegalMoves.add(_capturingMoves);
        _pseudoLegalMoves.add(_castlingMoves);
        _pseudoLegalMoves.add(_nonCapturingMoves);
    }

    private void generatePawnMoves() {
        // reverse direction of pawns for black
        final int pawnDir = _activePlayer.isBlack() ? -1 : 1;

        // iterate over all squares where we have a pawn
        for (OmegaSquare square : _position._pawnSquares[_activePlayer.ordinal()]) {
            assert _position._x88Board[square.ordinal()].getType() == OmegaPieceType.PAWN;

            // get all possible x88 index values for pawn moves
            // these are basically int values to add or subtract from the
            // current square index. Very efficient with a x88 board.
            int[] directions = OmegaSquare.pawnDirections;
            for (int d : directions) {

                // calculate the to square
                final int to = square.ordinal() + d * pawnDir;

                if ((to & 0x88) == 0) { // valid square

                    final OmegaMoveType type = OmegaMoveType.NORMAL;
                    final OmegaSquare fromSquare = OmegaSquare.getSquare(square.ordinal());
                    final OmegaSquare toSquare = OmegaSquare.getSquare(to);
                    final OmegaPiece piece = OmegaPiece.getPiece(OmegaPieceType.PAWN, _activePlayer);
                    final OmegaPiece target = _position._x88Board[to];
                    final OmegaPiece promotion = OmegaPiece.NOPIECE;

                    // capture
                    if (d != OmegaSquare.N) { // not straight
                        if (target != OmegaPiece.NOPIECE // not empty
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
                            } else { // normal capture
                                _capturingMoves.add(OmegaMove.createMove(type,fromSquare,toSquare,piece,target,promotion));
                            }
                        } else { // empty but maybe en passant
                            if (toSquare == _position._enPassantSquare) { //  en passant capture
                                // which target?
                                final int t = _activePlayer.isWhite() ? _position._enPassantSquare.getSouth().ordinal() : _position._enPassantSquare.getNorth().ordinal();
                                _capturingMoves.add(OmegaMove.createMove(OmegaMoveType.ENPASSANT,fromSquare,toSquare,piece,_position._x88Board[t],promotion));
                            }
                        }
                    }
                    // no capture
                    else if (d == OmegaSquare.N) { // straight
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
                            } else {
                                // pawndouble
                                if (_activePlayer.isWhite()
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
    }

    private void generateKnightMoves() {
        generateNonSlidingMoves(OmegaPieceType.KNIGHT, _position._knightSquares[_activePlayer.ordinal()], OmegaSquare.knightDirections);
    }

    private void generateBishopMoves() {
        generateSlidingMoves(OmegaPieceType.BISHOP, _position._bishopSquares[_activePlayer.ordinal()], OmegaSquare.bishopDirections);
    }

    private void generateRookMoves() {
        generateSlidingMoves(OmegaPieceType.ROOK, _position._rookSquares[_activePlayer.ordinal()], OmegaSquare.rookDirections);
    }

    private void generateQueenMoves() {
        generateSlidingMoves(OmegaPieceType.QUEEN, _position._queenSquares[_activePlayer.ordinal()], OmegaSquare.queenDirections);
    }

    private void generateKingMoves() {
        generateNonSlidingMoves(OmegaPieceType.KING, _position._kingSquares[_activePlayer.ordinal()], OmegaSquare.kingDirections);
    }

    private void generateCastlingMoves() {
        if (_position.hasCheck()) return; // no castling if we are in check
        // iterate over all available castlings at this position
        for (OmegaCastling castling : _position._castlingRights) {
            if (_activePlayer.isWhite()) {
                if (castling == OmegaCastling.WHITE_KINGSIDE) {
                    // f1 free, g1 free and f1 not attacked
                    // we will not check if g1 is attacked as this is a pseudo legal move
                    // and this to be checked separately e.g. when filtering for legal moves
                    if (_position._x88Board[OmegaSquare.f1.ordinal()] == OmegaPiece.NOPIECE // passing square free
                            && !_position.isAttacked(_activePlayer.getInverseColor(), OmegaSquare.f1) // passing square not attacked
                            && _position._x88Board[OmegaSquare.g1.ordinal()] == OmegaPiece.NOPIECE)  // to square free
                    {
                        _castlingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.CASTLING,
                                OmegaSquare.e1,OmegaSquare.g1,
                                OmegaPiece.WHITE_KING,
                                OmegaPiece.NOPIECE,OmegaPiece.NOPIECE));
                    }
                } else if (castling == OmegaCastling.WHITE_QUEENSIDE) {
                    // d1 free, c1 free and d1 not attacked
                    // we will not check if d1 is attacked as this is a pseudo legal move
                    // and this to be checked separately e.g. when filtering for legal moves
                    if (_position._x88Board[OmegaSquare.d1.ordinal()] == OmegaPiece.NOPIECE // passing square free
                            && _position._x88Board[OmegaSquare.b1.ordinal()] == OmegaPiece.NOPIECE // rook passing square free
                            && !_position.isAttacked(_activePlayer.getInverseColor(), OmegaSquare.d1) // passing square not attacked
                            && _position._x88Board[OmegaSquare.c1.ordinal()] == OmegaPiece.NOPIECE)  // to square free
                    {
                        _castlingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.CASTLING,
                                OmegaSquare.e1,OmegaSquare.c1,
                                OmegaPiece.WHITE_KING,
                                OmegaPiece.NOPIECE,OmegaPiece.NOPIECE));
                    }
                }
            } else {
                if (castling == OmegaCastling.BLACK_KINGSIDE) {
                    // f8 free, g8 free and f8 not attacked
                    // we will not check if g8 is attacked as this is a pseudo legal move
                    // and this to be checked separately e.g. when filtering for legal moves
                    if (_position._x88Board[OmegaSquare.f8.ordinal()] == OmegaPiece.NOPIECE // passing square free
                            && !_position.isAttacked(_activePlayer.getInverseColor(), OmegaSquare.f8) // passing square not attacked
                            && _position._x88Board[OmegaSquare.g8.ordinal()] == OmegaPiece.NOPIECE)  // to square free
                    {
                        _castlingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.CASTLING,
                                OmegaSquare.e8,OmegaSquare.g8,
                                OmegaPiece.BLACK_KING,
                                OmegaPiece.NOPIECE,OmegaPiece.NOPIECE));
                    }
                } else if (castling == OmegaCastling.BLACK_QUEENSIDE) {
                    // d8 free, c8 free and d8 not attacked
                    // we will not check if d8 is attacked as this is a pseudo legal move
                    // and this to be checked separately e.g. when filtering for legal moves
                    if (_position._x88Board[OmegaSquare.d8.ordinal()] == OmegaPiece.NOPIECE // passing square free
                            && _position._x88Board[OmegaSquare.b8.ordinal()] == OmegaPiece.NOPIECE // rook passing square free
                            && !_position.isAttacked(_activePlayer.getInverseColor(), OmegaSquare.d8) // passing square not attacked
                            && _position._x88Board[OmegaSquare.c8.ordinal()] == OmegaPiece.NOPIECE)  // to square free
                    {
                        _castlingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.CASTLING,
                                OmegaSquare.e8,OmegaSquare.c8,
                                OmegaPiece.BLACK_KING,
                                OmegaPiece.NOPIECE,OmegaPiece.NOPIECE));
                    }
                }
            }
        }
    }

    /**
     * For Bishop, Rook, Queen
     *
     * @param type
     * @param pieceSquares
     * @param pieceDirections
     */
    private void generateSlidingMoves(OmegaPieceType type, EnumSet<OmegaSquare> pieceSquares, int[] pieceDirections) {
        // iterate over all squares where we have this piece type
        for (OmegaSquare square : pieceSquares) {
            assert _position._x88Board[square.ordinal()].getType() == type;

            // get all possible x88 index values for piece's moves
            // these are basically int values to add or subtract from the
            // current square index. Very efficient with a x88 board.
            int[] directions = pieceDirections;
            for (int d : directions) {
                int to = square.ordinal() + d;

                while ((to & 0x88) == 0) { // slide while valid square
                    final OmegaPiece target = _position._x88Board[to];
                    // free square - non capture
                    if (target == OmegaPiece.NOPIECE) { // empty
                        _nonCapturingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.NORMAL,
                                OmegaSquare.getSquare(square.ordinal()),OmegaSquare.getSquare(to),
                                OmegaPiece.getPiece(type, _activePlayer),target,OmegaPiece.NOPIECE));
                    }
                    // occupied square - capture if opponent and stop sliding
                    else {
                        if (target.getColor() == _activePlayer.getInverseColor()) { // opponents color
                            assert target.getType() != OmegaPieceType.KING; // did we miss a check?
                            _capturingMoves.add(OmegaMove.createMove(
                                    OmegaMoveType.NORMAL,
                                    OmegaSquare.getSquare(square.ordinal()),OmegaSquare.getSquare(to),
                                    OmegaPiece.getPiece(type, _activePlayer),target,OmegaPiece.NOPIECE));
                        }
                        break; // stop sliding;
                    }
                    to += d; // next sliding field in this direction
                }
            }
        }
    }

    /**
     * For King and Knight
     *
     * @param type
     * @param pieceSquares
     * @param pieceDirections
     */
    private void generateNonSlidingMoves(OmegaPieceType type, EnumSet<OmegaSquare> pieceSquares, int[] pieceDirections) {
        // iterate over all squares where we have a piece
        for (OmegaSquare square : pieceSquares) {
            assert _position._x88Board[square.ordinal()].getType() == type;

            // get all possible x88 index values for piece moves
            // these are basically int values to add or subtract from the
            // current square index. Very efficient with a x88 board.
            int[] directions = pieceDirections;
            for (int d : directions) {
                int to = square.ordinal() + d;

                if ((to & 0x88) == 0) { // valid square
                    final OmegaPiece target = _position._x88Board[to];

                    // free square - non capture
                    if (target == OmegaPiece.NOPIECE) { // empty
                        _nonCapturingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.NORMAL,
                                OmegaSquare.getSquare(square.ordinal()),OmegaSquare.getSquare(to),
                                OmegaPiece.getPiece(type, _activePlayer),target,OmegaPiece.NOPIECE));
                    }
                    // occupied square - capture or ignore
                    else if (target.getColor() == _activePlayer.getInverseColor()) { // opponents color
                        assert target.getType() != OmegaPieceType.KING; // did we miss a check?
                        _capturingMoves.add(OmegaMove.createMove(
                                OmegaMoveType.NORMAL,
                                OmegaSquare.getSquare(square.ordinal()),OmegaSquare.getSquare(to),
                                OmegaPiece.getPiece(type, _activePlayer),target,OmegaPiece.NOPIECE));
                    }
                }
            }
        }
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
     * Filters the _pseudoLegalMove list into the _legalMove list.
     * Very expensive as i has to make and unmake the move to test
     * if king is left in check.
     * TODO: Improve - is there a way to avoid make/unmake?
     *
     * @param pseudolegalmoves
     * @param legalMoves
     */
    private void filterLegalMovesOnly() {
        int size = _pseudoLegalMoves.size();
        assert _legalMoves.size() == 0;
        for (int i = 0; i < size; ++i) {
            int move = _pseudoLegalMoves.get(i);
            _position.makeMove(move);
            if (!_position.isAttacked(_activePlayer.getInverseColor(), (OmegaSquare) _position._kingSquares[_activePlayer.ordinal()].toArray()[0])) {
                _legalMoves.add(move);
            }
            _position.undoMove();
        }
    }

    /**
     * Clears all lists
     */
    private void clearLists() {
        _legalMoves.clear();
        _pseudoLegalMoves.clear();
        _evasionMoves.clear();
        _capturingMoves.clear();
        _nonCapturingMoves.clear();
        _castlingMoves.clear();
    }








}
