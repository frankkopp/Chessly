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

import java.util.concurrent.TimeUnit;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;

/**
 * <table class="wiki_table">
 * <tr>
 * <td style="text-align: right;"><strong>Depth</strong><br>
 * </td>
 * <td style="text-align: right;"><strong>Nodes</strong><br>
 * </td>
 * <td style="text-align: right;"><strong>Captures</strong><br>
 * </td>
 * <td style="text-align: right;"><strong>E.p.</strong><br>
 * </td>
 * <td style="text-align: right;"><strong>Castles</strong><br>
 * </td>
 * <td style="text-align: right;"><strong>Promotions</strong><br>
 * </td>
 * <td style="text-align: right;"><strong>Checks</strong><br>
 * </td>
 * <td style="text-align: right;"><strong>Checkmates</strong><br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">0<br>
 * </td>
 * <td style="text-align: right;">1<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">1<br>
 * </td>
 * <td style="text-align: right;">20<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">2<br>
 * </td>
 * <td style="text-align: right;">400<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">3<br>
 * </td>
 * <td style="text-align: right;">8,902<br>
 * </td>
 * <td style="text-align: right;">34<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">12<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">4<br>
 * </td>
 * <td style="text-align: right;">197,281<br>
 * </td>
 * <td style="text-align: right;">1576<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">469<br>
 * </td>
 * <td style="text-align: right;">8<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">5<br>
 * </td>
 * <td style="text-align: right;">4,865,609<br>
 * </td>
 * <td style="text-align: right;">82719<br>
 * </td>
 * <td style="text-align: right;">258<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">27351<br>
 * </td>
 * <td style="text-align: right;">347<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">6<br>
 * </td>
 * <td style="text-align: right;">119,060,324<br>
 * </td>
 * <td style="text-align: right;">2812008<br>
 * </td>
 * <td style="text-align: right;">5248<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">0<br>
 * </td>
 * <td style="text-align: right;">809099<br>
 * </td>
 * <td style="text-align: right;">10828<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">7<br>
 * </td>
 * <td style="text-align: right;">3,195,901,860<br>
 * </td>
 * <td colspan="6"><br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">8<br>
 * </td>
 * <td style="text-align: right;">84,998,978,956<br>
 * </td>
 * <td colspan="6"><br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">9<br>
 * </td>
 * <td style="text-align: right;">2,439,530,234,167<br>
 * </td>
 * <td colspan="6"><br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">10<br>
 * </td>
 * <td style="text-align: right;">69,352,859,712,417<br>
 * </td>
 * <td colspan="6"><br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">11<br>
 * </td>
 * <td style="text-align: right;">2,097,651,003,696,806<br>
 * </td>
 * <td colspan="6"><br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">12<br>
 * </td>
 * <td style="text-align: right;">62,854,969,236,701,747<br>
 * </td>
 * <td colspan="6"><!-- ws:start:WikiTextRefRule:7:&amp;lt;ref&amp;gt;&lt;a
 * class=&quot;wiki_link_ext&quot;
 * href=&quot;http://www.talkchess.com/forum/viewtopic.php?t=38862&quot;
 * rel=&quot;nofollow&quot;&gt;Perft(12) count confirmed&lt;/a&gt; by &lt;a
 * class=&quot;wiki_link&quot; href=&quot;/Steven+Edwards&quot;&gt;Steven
 * Edwards&lt;/a&gt;, &lt;a class=&quot;wiki_link&quot;
 * href=&quot;/Computer+Chess+Forums&quot;&gt;CCC&lt;/a&gt;, April 25,
 * 2011&amp;lt;/ref&amp;gt; --><sup id="cite_ref-2" class="reference"><a
 * href="#cite_note-2">[2]</a></sup><!-- ws:end:WikiTextRefRule:7 --><br>
 * </td>
 * </tr>
 * <tr>
 * <td style="text-align: center;">13<br>
 * </td>
 * <td style="text-align: right;">1,981,066,775,000,396,239<br>
 * </td>
 * <td colspan="6"><br>
 * </td>
 * </tr>
 * </table>
 *
 * @author Frank
 *
 */
final class Perft {

    private static final int MAX_DEPTH = 4;

    private long captures = 0;
    private long enpassant= 0;
    private long castles  = 0;
    private long checks	  = 0;
    private long mates	  = 0;


    private final MoveGenerator[] moveGenerators = new MoveGenerator[MAX_DEPTH+1];

    void run() {

        //GameBoard boardG = new GameBoardImpl();
        GameBoard boardG = new GameBoardImpl("r3qb1k/1b4p1/p2pr2p/3n4/Pnp1N1N1/6RP/1B3PP1/1B1QR1K1 w - - 0 1");

        Board board = new Board(boardG);
        int depth = MAX_DEPTH;

        for (int i = 0; i < MAX_DEPTH+1; ++i) {
            moveGenerators[i] = new MoveGenerator();
        }

        System.out.format("Testing %s at depth %d%n", board.toString(), depth);

        long startTime = System.currentTimeMillis();
        long result = miniMax(depth, board, 0);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        System.out.format(
                "Duration: %02d:%02d:%02d.%03d%n",
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

        System.out.format("n/ms: %d%n", result / duration);
        System.out.format("Nodes: %d checks=%d mates=%d captures=%d enpassant=%d castlings=%d %n", result, checks, mates, captures, enpassant, castles);
    }

    private long miniMax(int depth, Board board, int ply) {

        if (depth == 0) {

            // count checks
            if (board.isCheck()) {
                checks++;
                // count checkmates
                MoveGenerator mG = moveGenerators[ply];
                MoveList      mL = mG.getLegalMoves(board, 1, true);
                if (mL.size==0) mates++;
            }

            // check for special move types
            final int moveType = Move.getType(board.moveHistory.lastMove());
            if (moveType == MoveType.ENPASSANT) {
                enpassant++;
                captures++;
            } else if (moveType == MoveType.CASTLING) {
                castles++;
            } else if (moveType == MoveType.CAPTURE) {
                captures++;
            }

            return 1;
        }

        boolean isCheck = board.isCheck();
        int totalNodes = 0;
        MoveGenerator moveGenerator = moveGenerators[ply];
        MoveList moves = moveGenerator.getPseudoLegalMoves(board, depth, isCheck);
        for (int i = 0; i < moves.size; ++i) {
            long nodes = 0;
            int move = moves.entries[i].move;
            board.makeMove(move);
            if (!board.isAttacked(Bitboard.next(board.kings[Color.opposite(board.activeColor)].squares), board.activeColor)) {
                nodes = miniMax(depth-1, board, ply+1);
            }
            board.undoMove();
            if (ply==0) System.out.println(Move.toString(move)+": "+nodes);
            totalNodes += nodes;
        }

        return totalNodes;
    }

}
