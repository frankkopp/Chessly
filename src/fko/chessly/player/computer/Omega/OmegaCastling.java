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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Enumeration for chess castlings
 */
@SuppressWarnings("javadoc")
public enum OmegaCastling {

    // order has influence on FEN notation
    WHITE_KINGSIDE  ("K", "O-O"),   // 0
    WHITE_QUEENSIDE ("Q", "O-O-O"), // 1
    BLACK_KINGSIDE  ("k", "o-o"),   // 2
    BLACK_QUEENSIDE ("q", "o-o-o"); // 3

    private final String _shortName;
    private final String _notation;

    // this list holds all combinations of castlings - it will be used to create zobrist keys
    private final static List<EnumSet<OmegaCastling>> _permutationList = new ArrayList<EnumSet<OmegaCastling>>(16);

    static {
        _permutationList.add(EnumSet.noneOf(OmegaCastling.class));                              // 0
        _permutationList.add(EnumSet.of(BLACK_QUEENSIDE));                                      // 1
        _permutationList.add(EnumSet.of(BLACK_KINGSIDE));                                       // 2
        _permutationList.add(EnumSet.of(BLACK_QUEENSIDE, BLACK_KINGSIDE));                      // 3

        _permutationList.add(EnumSet.of(WHITE_QUEENSIDE));                                      // 4
        _permutationList.add(EnumSet.of(WHITE_QUEENSIDE, BLACK_QUEENSIDE));                     // 5
        _permutationList.add(EnumSet.of(WHITE_QUEENSIDE, BLACK_KINGSIDE));                      // 6
        _permutationList.add(EnumSet.of(WHITE_QUEENSIDE, BLACK_KINGSIDE, BLACK_QUEENSIDE));     // 7

        _permutationList.add(EnumSet.of(WHITE_KINGSIDE));                                       // 8
        _permutationList.add(EnumSet.of(WHITE_KINGSIDE, BLACK_QUEENSIDE));                      // 9
        _permutationList.add(EnumSet.of(WHITE_KINGSIDE, BLACK_KINGSIDE));                       // 10
        _permutationList.add(EnumSet.of(WHITE_KINGSIDE, BLACK_KINGSIDE, BLACK_QUEENSIDE));      // 11

        _permutationList.add(EnumSet.of(WHITE_QUEENSIDE, WHITE_KINGSIDE));                      // 12
        _permutationList.add(EnumSet.of(WHITE_QUEENSIDE, WHITE_KINGSIDE, BLACK_QUEENSIDE));     // 13
        _permutationList.add(EnumSet.of(WHITE_QUEENSIDE, WHITE_KINGSIDE, BLACK_KINGSIDE));      // 14
        _permutationList.add(EnumSet.allOf(OmegaCastling.class));                               // 15
    }

    private OmegaCastling(String shortname, String notation) {
        _shortName = shortname;
        _notation = notation;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return _shortName;
    }

    @Override
    public String toString() {
        return _shortName;
    }

    public String getNotation() {
        return _notation;
    }

    /**
     * Returns a list of all the combinations of castlings
     * to easily create a zobrist key for it
     * @return a list with the combination of castlings
     */
    public static List<EnumSet<OmegaCastling>> getCombinationList() {
        return Collections.unmodifiableList(_permutationList);
    }

    /**
     * Returns an index between 0-15 for all the combinations of castlings
     * to easily create a zobrist key for it
     * @return an index between 0 and 15 representing the combination of castlings
     */
    public static int getCombinationIndex(EnumSet<OmegaCastling> set) {
        return _permutationList.indexOf(set);
    }



}
