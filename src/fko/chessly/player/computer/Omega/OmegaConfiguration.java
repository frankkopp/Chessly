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

import fko.chessly.openingbook.OpeningBookImpl.Mode;

/**
 * This is the engines configuration. All fields are package visible
 * and can be change during runtime. They are deliberately not static
 * so we can change white and black differently when engine vs. engine.
 */
public class OmegaConfiguration {

    // Verbose
    /** If set to true this object will produce info output to System.out */
    boolean VERBOSE_TO_SYSOUT = true;

    /** If set to true we will use the opening book */
    boolean _USE_BOOK = true;

    /** Use Ponderer while waiting for opponents move - fills node_cache */
    boolean _USE_PONDERER = true;

    /** default value for folder to books */
    String _OB_FolderPath = "./book/";
    /** default opening book file */
    //public String _OB_fileNamePlain = "8moves_GM_LB.pgn";
    String _OB_fileNamePlain = "book_graham.txt";
    /** default opening book value */
    //public Mode _OB_Mode = Mode.PGN;
    Mode _OB_Mode = Mode.SAN;

}
