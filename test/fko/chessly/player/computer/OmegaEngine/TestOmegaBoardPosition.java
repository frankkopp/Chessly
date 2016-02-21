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

package fko.chessly.player.computer.OmegaEngine;

import java.util.EnumSet;

import org.junit.Test;

import fko.chessly.player.computer.Omega.OmegaCastling;

/**
 * @author fkopp
 *
 */
public class TestOmegaBoardPosition {

    /**
     *
     */
    @Test
    public void testCastlingRights() {
        // Castling rights
        EnumSet<OmegaCastling> _castlingRights = EnumSet.allOf(OmegaCastling.class);
        _castlingRights.forEach(c -> System.out.print(c));
        System.out.println();
        _castlingRights.remove(OmegaCastling.WHITE_KINGSIDE);
        _castlingRights.forEach(c -> System.out.print(c));
        System.out.println();
        System.out.println(_castlingRights.contains(OmegaCastling.BLACK_QUEENSIDE));
    }

}
