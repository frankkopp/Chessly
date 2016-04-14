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

import com.fluxchess.jcpi.models.GenericBoard;
import com.fluxchess.jcpi.models.IllegalNotationException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MoveGeneratorTest {

    @Test
    public void testSpecialPerft() {
        // Setup a new board from fen
        GenericBoard board;
        try {
            board = new GenericBoard("1k6/8/8/5pP1/4K1P1/8/8/8 w - f6");
            Position testBoard = new Position(board);
            new See(testBoard);

            //			testBoard.makeMove(IntMove.createMove(IntMove.NORMAL, IntPosition.d2, IntPosition.c1, IntChessman.NOCHESSMAN, IntChessman.NOCHESSMAN, IntChessman.NOCHESSMAN));
            //			testBoard.makeMove(IntMove.createMove(IntMove.NORMAL, IntPosition.e7, IntPosition.d6, IntChessman.NOCHESSMAN, IntChessman.NOCHESSMAN, IntChessman.NOCHESSMAN));
            //			testBoard.makeMove(IntMove.createMove(IntMove.NORMAL, IntPosition.e1, IntPosition.d1, IntChessman.NOCHESSMAN, IntChessman.NOCHESSMAN, IntChessman.NOCHESSMAN));
            //			testBoard.makeMove(IntMove.createMove(IntMove.PAWNDOUBLE, IntPosition.c7, IntPosition.c5, IntChessman.NOCHESSMAN, IntChessman.NOCHESSMAN, IntChessman.NOCHESSMAN));
            miniMax(testBoard, new MoveGenerator(testBoard, new KillerTable(), new HistoryTable()), 5, 5);
        } catch (IllegalNotationException e) {
            fail();
        }
    }

    @Test
    public void testPerft() {
        for (int i = 1; i < 4; i++) {
            //		for (int i = 1; i < 7; i++) {
            BufferedReader file;
            try {
                file = new BufferedReader(new InputStreamReader(MoveGeneratorTest.class.getResourceAsStream("/perftsuite.epd")));

                String line = file.readLine();
                while (line != null) {
                    String[] tokens = line.split(";");

                    // Setup a new board from fen
                    GenericBoard board = new GenericBoard(tokens[0].trim());

                    if (tokens.length > i) {
                        String[] data = tokens[i].trim().split(" ");
                        int depth = Integer.parseInt(data[0].substring(1));
                        int nodesNumber = Integer.parseInt(data[1]);

                        Position testBoard = new Position(board);
                        new See(testBoard);

                        int result = miniMax(testBoard, new MoveGenerator(testBoard, new KillerTable(), new HistoryTable()), depth, depth);
                        assertEquals(tokens[0].trim(), nodesNumber, result);
                    }

                    line = file.readLine();
                }
            } catch (IOException | IllegalNotationException e) {
                fail();
            }
        }
    }

    private int miniMax(Position board, MoveGenerator generator, int depth, int maxDepth) {
        if (depth == 0) {
            return 1;
        }

        int totalNodes = 0;

        Attack attack = board.getAttack(board.activeColor);
        MoveGenerator.initializeMain(attack, 0, Move.NOMOVE);

        int nodes;
        int move = MoveGenerator.getNextMove();
        while (move != Move.NOMOVE) {
            boolean isCheckingMove = board.isCheckingMove(move);
            GenericBoard oldBoard = board.getBoard();

            int captureSquare = board.captureSquare;
            board.makeMove(move);
            boolean isCheckingMoveReal = board.getAttack(board.activeColor).isCheck();
            assertEquals(oldBoard.toString() + ", " + Move.toCommandMove(move).toString(), isCheckingMoveReal, isCheckingMove);
            nodes = miniMax(board, generator, depth - 1, maxDepth);
            board.undoMove(move);
            assert captureSquare == board.captureSquare;

            //			if (depth == maxDepth) {
            //				System.out.println(IntMove.toCommandMove(move).toLongAlgebraicNotation() + ": " + nodes);
            //			}
            totalNodes += nodes;
            move = MoveGenerator.getNextMove();
        }

        MoveGenerator.destroy();

        return totalNodes;
    }

    @Test
    public void testSpecialQuiescentCheckingMoves() {
        // Setup a new board from fen
        GenericBoard board;
        try {
            board = new GenericBoard("8/8/3K4/3Nn3/3nN3/4k3/8/8 b - - 0 1");
            Position testBoard = new Position(board);

            new MoveGenerator(testBoard, new KillerTable(), new HistoryTable());
            miniMaxQuiescentCheckingMoves(testBoard, 3, 3);
        } catch (IllegalNotationException e) {
            fail();
        }
    }

    @Test
    public void testQuiescentCheckingMoves() {
        for (int i = 1; i < 3; i++) {
            //		for (int i = 1; i < 7; i++) {
            BufferedReader file;
            try {
                file = new BufferedReader(new InputStreamReader(MoveGeneratorTest.class.getResourceAsStream("/perftsuite.epd")));

                String line = file.readLine();
                while (line != null) {
                    String[] tokens = line.split(";");

                    // Setup a new board from fen
                    GenericBoard board = new GenericBoard(tokens[0].trim());

                    if (tokens.length > i) {
                        String[] data = tokens[i].trim().split(" ");
                        int depth = Integer.parseInt(data[0].substring(1));

                        Position testBoard = new Position(board);
                        new See(testBoard);

                        new MoveGenerator(testBoard, new KillerTable(), new HistoryTable());
                        miniMaxQuiescentCheckingMoves(testBoard, depth, depth);
                    }

                    line = file.readLine();
                }
            } catch (IOException | IllegalNotationException e) {
                fail();
            }
        }
    }

    private void miniMaxQuiescentCheckingMoves(Position board, int depth, int maxDepth) {
        if (depth == 0) {
            return;
        }

        Attack attack = board.getAttack(board.activeColor);

        // Get quiescent move list
        MoveList quiescentMoveList = new MoveList();
        MoveGenerator.initializeQuiescent(attack, true);
        int move = MoveGenerator.getNextMove();
        while (move != Move.NOMOVE) {
            quiescentMoveList.moves[quiescentMoveList.tail++] = move;
            move = MoveGenerator.getNextMove();
        }
        MoveGenerator.destroy();

        // Do main moves and count
        MoveList mainMoveList = new MoveList();
        MoveGenerator.initializeMain(attack, 0, Move.NOMOVE);
        move = MoveGenerator.getNextMove();
        while (move != Move.NOMOVE) {
            if (!attack.isCheck()) {
                if ((Move.getTarget(move) != Piece.NOPIECE && isGoodCapture(move, board)) || (Move.getTarget(move) == Piece.NOPIECE && board.isCheckingMove(move)) && See.seeMove(move, Move.getChessmanColor(move)) >= 0) {
                    board.makeMove(move);
                    miniMaxQuiescentCheckingMoves(board, depth - 1, maxDepth);
                    board.undoMove(move);
                    mainMoveList.moves[mainMoveList.tail++] = move;
                }
            } else {
                board.makeMove(move);
                miniMaxQuiescentCheckingMoves(board, depth - 1, maxDepth);
                board.undoMove(move);
                mainMoveList.moves[mainMoveList.tail++] = move;
            }
            move = MoveGenerator.getNextMove();
        }
        MoveGenerator.destroy();

        assertEquals(printDifference(board, mainMoveList, quiescentMoveList), mainMoveList.getLength(), quiescentMoveList.getLength());
    }

    private String printDifference(Position board, MoveList main, MoveList quiescent) {
        String result = board.getBoard().toString() + "\n";

        main.rateFromMVVLVA();
        quiescent.rateFromMVVLVA();

        result += "     Main:";
        for (int i = 0; i < main.tail; i++) {
            result += " " + Move.toCommandMove(main.moves[i]).toString();
        }
        result += "\n";

        result += "Quiescent:";
        for (int i = 0; i < quiescent.tail; i++) {
            result += " " + Move.toCommandMove(quiescent.moves[i]).toString();
        }
        result += "\n";

        return result;
    }

    private static boolean isGoodCapture(int move, Position board) {
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

}
