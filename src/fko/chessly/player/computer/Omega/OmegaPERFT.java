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

import java.util.concurrent.TimeUnit;

/**
 * @author Frank
 *
 */
public class OmegaPERFT {

    private long _nodes = 0;
    private long _checkCounter = 0;
    private long _checkMateCounter = 0;
    private long _captureCounter = 0;
    private long _enpassantCounter = 0;
    private final Object _checkCounterLock = new Object();
    private final Object _mateCounterLock = new Object();
    private final Object _captureCounterLock = new Object();
    private final Object _epCounterLock = new Object();

    /**
     * @param maxDepth
     */
    public void testPerft(int maxDepth) {

        resetCounters();

        int depth = maxDepth;

        System.out.format("Testing single threaded at depth %d%n", depth);

        OmegaBoardPosition board = new OmegaBoardPosition();
        OmegaMoveGenerator mg = new OmegaMoveGenerator();

        long result = 0;

        long startTime = System.currentTimeMillis();
        OmegaMoveList moves = mg.getLegalMoves(board, false);
        for (int move : moves.toArray()) {
            board.makeMove(move);
            result += miniMax(depth - 1, board, mg, 1);
            board.undoMove();
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        _nodes = result;
        printResult(result, duration);
    }

    private long miniMax(int depthleft, OmegaBoardPosition board, OmegaMoveGenerator mg, int ply) {

        // PERFT only looks at leaf nodes
        if (depthleft == 0) {
            updateCounter(board);
            return 1;
        }

        // Iterate over moves
        long totalNodes = 0L;
        OmegaMoveList moves = mg.getLegalMoves(board, false);
        for (int move : moves.toArray()) {
            board.makeMove(move);
            totalNodes += miniMax(depthleft - 1, board, mg, ply + 1);
            board.undoMove();
        }

        return totalNodes;
    }

    /**
     * @param board
     */
    private void updateCounter(OmegaBoardPosition board) {
        if (board.hasCheck()) {
            _checkCounter++;
            //            if (board.hasCheckMate()) {
            //                _checkMateCounter++;
            //            }
        }

        int lastMove = board.getLastMove();

        if (OmegaMove.getTarget(lastMove) != OmegaPiece.NOPIECE) {
            _captureCounter++;
        }

        if (OmegaMove.getMoveType(lastMove) == OmegaMoveType.ENPASSANT) {
            _enpassantCounter++;
        }
    }

    /**
     * Reset the counters
     */
    private void resetCounters() {
        _nodes = 0;
        _checkCounter = 0;
        _checkMateCounter = 0;
        _captureCounter = 0;
        _enpassantCounter = 0;
    }

    /**
     * @param result
     * @param duration
     */
    private void printResult(long result, long duration) {
        System.out.format("Leaf Nodes: %,d Captures: %,d EnPassant: %,d Checks: %,d Checkmates: %,d %n",
                result, _captureCounter, _enpassantCounter, _checkCounter, _checkMateCounter);
        System.out.format("Duration: %02d:%02d:%02d.%03d%n",
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                        .toHours(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                        .toMinutes(duration)),
                duration
                - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS
                        .toSeconds(duration)));

        System.out.format("n/s: %,d%n", result*1000/(duration+1));
        System.out.println();
    }

    public long get_nodes() {
        return _nodes;
    }

    public long get_checkCounter() {
        return _checkCounter;
    }

    public long get_checkMateCounter() {
        return _checkMateCounter;
    }

    public long get_captureCounter() {
        return _captureCounter;
    }

    public long get_enpassantCounter() {
        return _enpassantCounter;
    }

}
