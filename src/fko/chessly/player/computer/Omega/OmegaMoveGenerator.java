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
 * @author Frank
 *
 */
public class OmegaMoveGenerator {

    private OmegaBoardPosition _position;

    private OmegaMoveList _moveList = null;

    /**
     * Constructor
     */
    public OmegaMoveGenerator(OmegaBoardPosition position) {
        this._position = position;
    }

    /**
     * @return generated moves as OmegaMoveList
     */
    public OmegaMoveList getLegalMoves() {
        // DEBUG - temporary code
        _moveList=new OmegaMoveList();
        GameBoard board = new GameBoardImpl(_position.toFENString());
        GameMoveList moves = board.generateMoves();
        moves.stream().forEach(c -> _moveList.add(OmegaMove.convertFromGameMove(c)));
        return _moveList;
    }







}
