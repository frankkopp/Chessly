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

package fko.chessly.player.computer.FluxEngine;

import static org.junit.Assert.*;

import org.junit.Test;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;

/**
 * @author fkopp
 *
 */
public class PositionFromGameBoard {

    @Test
    public void testPosition() {

        GameBoard gameBoard = new GameBoardImpl();
        Position position = new Position(gameBoard);
        assertTrue(position.toFENString().equals(gameBoard.toFENString()));
        GameBoard convertedBoard = position.convertToGameBoard();
        assertTrue(position.toFENString().equals(convertedBoard.toFENString()));

        String testFen = "r3k2r/1ppn3p/2q1q1n1/8/2q1Pp2/6R1/p1p2PPP/1R4K1 b kq e4 0 113";

        gameBoard = new GameBoardImpl(testFen);
        System.out.println(gameBoard.toFENString());

        position = new Position(gameBoard);
        System.out.println(position.toFENString());

        convertedBoard = position.convertToGameBoard();
        System.out.println(convertedBoard.toFENString());

        assertTrue(position.toFENString().equals(gameBoard.toFENString()));
        assertTrue(position.toFENString().equals(convertedBoard.toFENString()));

    }

}
