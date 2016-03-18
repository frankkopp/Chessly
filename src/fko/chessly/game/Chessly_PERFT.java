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
package fko.chessly.game;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import fko.chessly.game.pieces.Pawn;
import fko.chessly.player.computer.Omega.OmegaMove;

/**
 * <table class="wiki_table">
 * <tr>
 * <td style="text-align: right;"><strong>Depth</strong><br />
 * </td>
 * <td style="text-align: right;"><strong>Nodes</strong><br />
 * </td>
 * <td style="text-align: right;"><strong>Captures</strong><br />
 * </td>
 * <td style="text-align: right;"><strong>E.p.</strong><br />
 * </td>
 * <td style="text-align: right;"><strong>Castles</strong><br />
 * </td>
 * <td style="text-align: right;"><strong>Promotions</strong><br />
 * </td>
 * <td style="text-align: right;"><strong>Checks</strong><br />
 * </td>
 * <td style="text-align: right;"><strong>Checkmates</strong><br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">0<br />
 * </td>
 * <td style="text-align: right;">1<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">1<br />
 * </td>
 * <td style="text-align: right;">20<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">2<br />
 * </td>
 * <td style="text-align: right;">400<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">3<br />
 * </td>
 * <td style="text-align: right;">8,902<br />
 * </td>
 * <td style="text-align: right;">34<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">12<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">4<br />
 * </td>
 * <td style="text-align: right;">197,281<br />
 * </td>
 * <td style="text-align: right;">1576<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">469<br />
 * </td>
 * <td style="text-align: right;">8<br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">5<br />
 * </td>
 * <td style="text-align: right;">4,865,609<br />
 * </td>
 * <td style="text-align: right;">82719<br />
 * </td>
 * <td style="text-align: right;">258<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">27351<br />
 * </td>
 * <td style="text-align: right;">347<br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">6<br />
 * </td>
 * <td style="text-align: right;">119,060,324<br />
 * </td>
 * <td style="text-align: right;">2812008<br />
 * </td>
 * <td style="text-align: right;">5248<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">0<br />
 * </td>
 * <td style="text-align: right;">809099<br />
 * </td>
 * <td style="text-align: right;">10828<br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">7<br />
 * </td>
 * <td style="text-align: right;">3,195,901,860<br />
 * </td>
 * <td colspan="6"><br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">8<br />
 * </td>
 * <td style="text-align: right;">84,998,978,956<br />
 * </td>
 * <td colspan="6"><br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">9<br />
 * </td>
 * <td style="text-align: right;">2,439,530,234,167<br />
 * </td>
 * <td colspan="6"><br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">10<br />
 * </td>
 * <td style="text-align: right;">69,352,859,712,417<br />
 * </td>
 * <td colspan="6"><br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">11<br />
 * </td>
 * <td style="text-align: right;">2,097,651,003,696,806<br />
 * </td>
 * <td colspan="6"><br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">12<br />
 * </td>
 * <td style="text-align: right;">62,854,969,236,701,747<br />
 * </td>
 * <td colspan="6"><!-- ws:start:WikiTextRefRule:7:&amp;lt;ref&amp;gt;&lt;a
 * class=&quot;wiki_link_ext&quot;
 * href=&quot;http://www.talkchess.com/forum/viewtopic.php?t=38862&quot;
 * rel=&quot;nofollow&quot;&gt;Perft(12) count confirmed&lt;/a&gt; by &lt;a
 * class=&quot;wiki_link&quot; href=&quot;/Steven+Edwards&quot;&gt;Steven
 * Edwards&lt;/a&gt;, &lt;a class=&quot;wiki_link&quot;
 * href=&quot;/Computer+Chess+Forums&quot;&gt;CCC&lt;/a&gt;, April 25,
 * 2011&amp;lt;/ref&amp;gt; --><sup id="cite_ref-2" class="reference"><a
 * href="#cite_note-2">[2]</a></sup><!-- ws:end:WikiTextRefRule:7 --><br />
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">13<br />
 * </td>
 * <td style="text-align: right;">1,981,066,775,000,396,239<br />
 * </td>
 * <td colspan="6"><br />
 * </td>
 * </tr>
 * </table>
 *
 * @author Frank
 *
 */
public final class Chessly_PERFT {

    private static final boolean DIVIDE=false;

    private LinkedBlockingQueue<Job> fifo = new LinkedBlockingQueue<Job>();
    private final Object _fifoLock = new Object();

    private long _nodes = 0;
    private long _checkCounter = 0;
    private long _checkMateCounter = 0;
    private long _captureCounter = 0;
    private long _enpassantCounter = 0;
    private final Object _checkCounterLock = new Object();
    private final Object _mateCounterLock = new Object();
    private final Object _captureCounterLock = new Object();
    private final Object _epCounterLock = new Object();

    private String _fen;

    /**
     * @param fen
     */
    public Chessly_PERFT(String fen) {
        _fen = fen;
    }

    /**
     *
     */
    public Chessly_PERFT() {
        _fen = NotationHelper.StandardBoardFEN;
    }

    /**
     * @param maxDepth
     */
    public void testSingleThreaded(int maxDepth) {

        resetCounters();

        int depth = maxDepth;

        System.out.format("Testing single threaded at depth %d%n", depth);

        GameBoardImpl board = new GameBoardImpl(_fen);
        //        board.makeMove(NotationHelper.createNewMoveFromSimpleNotation(board, "a2a4"));
        //        board.makeMove(NotationHelper.createNewMoveFromSimpleNotation(board, "e6d5"));
        //        board.makeMove(NotationHelper.createNewMoveFromSimpleNotation(board, "e4d5"));

        long result = 0;

        long startTime = System.currentTimeMillis();
        List<GameMove> moves = board.generateMoves();
        for (GameMove move : moves) {
            if (DIVIDE) System.out.print(move.toSimpleString()+" ");
            board.makeMove(move);
            long r = miniMax(depth - 1, board, 1);
            if (DIVIDE) System.out.println(r);
            result += r;
            board.undoMove();
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        if (DIVIDE) System.out.println("Moves: "+moves.size());

        _nodes = result;
        printResult(result, duration);
    }

    /**
     * @param maxDepth
     * @param threads
     */
    public void testMultiThreaded(int maxDepth, int threads) {

        resetCounters();

        int depth = maxDepth;

        System.out.format("Testing multi threaded at depth %d%n", depth);

        GameBoardImpl board = new GameBoardImpl(_fen);

        long result = 0;

        long startTime = System.currentTimeMillis();

        List<GameMove> moves = board.generateMoves();
        for (GameMove move : moves) {
            GameBoardImpl b = new GameBoardImpl(board);
            try {
                fifo.put(new Job(b, move, depth));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Worker thread[] = new Worker[threads];
        for (int t = 0; t < threads; t++) {
            thread[t] = new Worker();
            thread[t].start();
        }

        for (int j = 0; j < thread.length; j++) {
            try {
                thread[j].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result += thread[j].getResult();
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        _nodes = result;
        printResult(result, duration);

    }

    private long miniMax(int depthleft, GameBoard board, int ply) {

        // PERFT only looks at leaf nodes
        if (depthleft == 0) {
            updateCounter(board);
            return 1;
        }

        // Iterate over moves
        long totalNodes = 0L;
        List<GameMove> moves = board.generateMoves();
        for (GameMove move : moves) {
            board.makeMove(move);
            totalNodes += miniMax(depthleft - 1, board, ply + 1);
            board.undoMove();
        }

        return totalNodes;
    }


    public class Worker extends Thread {
        long result = 0;

        public long getResult() {
            return result;
        }

        public Worker() {
        }

        @Override
        public void run() {
            Job myJob = null;
            GameBoard b_copy = null;
            while (!fifo.isEmpty()) {
                try {
                    synchronized (_fifoLock) {
                        if (!fifo.isEmpty())
                            myJob = fifo.take();
                        else
                            break;
                    }
                    // System.out.println("Got Job: "+myJob.move);
                    b_copy = myJob.board;
                    b_copy.makeMove(myJob.move);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IllegalMoveException e) {
                    e.printStackTrace();
                }
                long tmp = miniMax(myJob.depth - 1, b_copy, 1);
                //System.out.println(myJob.move+": "+tmp);
                result += tmp;
            }
        }
    }

    public class Job {
        public final GameBoard board;
        public final GameMove move;
        public final int depth;

        public Job(GameBoard b, GameMove m, int depth) {
            this.board = b;
            this.move = m;
            this.depth = depth;
        }
    }

    /**
     * @param board
     */
    private void updateCounter(GameBoard board) {
        if (board.hasCheck()) {
            synchronized (_checkCounterLock) {
                _checkCounter++;
            }
            if (board.hasCheckMate()) {
                synchronized (_mateCounterLock) {
                    _checkMateCounter++;
                }
            }
        }
        GameMove lastMove = board.getMoveHistory().getLast();
        if (lastMove.getCapturedPiece() != null) {
            synchronized (_captureCounterLock) {
                _captureCounter++;
            }
        }
        if (lastMove.getWasEnPassantCapture()) {
            synchronized (_epCounterLock) {
                _enpassantCounter++;
            }
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
        System.out.format("Leaf Nodes: %d Captures: %d EnPassant: %d Checks: %d Checkmates: %d %n",
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

        System.out.format("n/s: %d%n", result*1000/(duration+1));
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
