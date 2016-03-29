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

/**
 * Omega Evaluation
 *
 * Features:
 *      DONE: Material
 *      TODO: Mobility
 *      TODO: Game Phase
 *      TODO: Tapered Eval
 *      TODO: Piece Tables
 *      TODO: Bishop Pair
 *      TODO: Bishop vs. Knight
 *      TODO: Center Control
 *      TODO: Center Distance
 *      TODO: Square Control
 */
public class OmegaEvaluation {

    static private final boolean MATERIAL = true;

    private final OmegaMoveGenerator _omegaMoveGenerator;
    private final OmegaEngine _omegaEngine;

    /**
     * @param omegaEngine
     * @param omegaMoveGenerator
     */
    public OmegaEvaluation(OmegaEngine omegaEngine, OmegaMoveGenerator omegaMoveGenerator) {
        super();
        this._omegaEngine = omegaEngine;
        this._omegaMoveGenerator = omegaMoveGenerator;
    }

    /**
     * Always from the view of the active (next) player.
     *
     * @param board
     * @return value of the position from active player's view.
     */
    public int evaluate(OmegaBoardPosition board) {
        int value = OmegaEvaluation.Value.MIN_VALUE;



        if (_omegaMoveGenerator.hasLegalMove(board)) {
            final OmegaColor activePlayer = board.getNextPlayer();
            final int sideFactor = activePlayer.isWhite() ? 1 : -1;

            // Material
            if (MATERIAL)
                value = material(board, sideFactor);



        }
        // no moves - mate position?
        else {
            if (board.hasCheck()) {
                // We have a check mate. Return a -CHECKMATE.
                value = -OmegaEvaluation.Value.CHECKMATE;
            } else {
                // We have a stale mate. Return the draw value.
                value = OmegaEvaluation.Value.DRAW;
            }
        }

        return value;
    }

    /**
     * @param board
     * @param sideFactor
     * @return
     */
    private int material(OmegaBoardPosition board, final int sideFactor) {
        int value;
        value = sideFactor * (board.getMaterial(OmegaColor.WHITE) - board.getMaterial(OmegaColor.BLACK));
        return value;
    }

    /**
     * Predefined values for Evaluation of positions.
     */
    @SuppressWarnings("javadoc")
    public static class Value {
        static public final int NOVALUE = Integer.MIN_VALUE;
        static public final int INFINITE = Integer.MAX_VALUE;
        static public final int MIN_VALUE = -200000;
        static public final int DRAW = 0;
        static public final int CHECKMATE = 100000;
    }

    /**
     * Game Phases
     *
     * OPENING: develop minor pieces, control center, center pawn structure,
     *          castling, penalize too early queen development
     * MIDGAME: material, mobility and pawn structure considerations, king safety
     * ENDGAME: king to center, pawn promotion, passed pawns
     */
    @SuppressWarnings("javadoc")
    public enum GamePhase {
        OPENING,
        MIDGAME,
        ENDGAME
    }

}
