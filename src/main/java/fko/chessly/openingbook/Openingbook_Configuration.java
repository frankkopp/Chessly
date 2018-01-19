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
package fko.chessly.openingbook;

import fko.chessly.openingbook.OpeningBookImpl.Mode;

/**
 * @author fkopp
 */
public class Openingbook_Configuration {

    /**
     * If set to true this object will produce info output to System.out
     */
    public boolean VERBOSE = true;

    /**
     * If set to true will ignore an existing cache file and recreate it
     */
    public boolean FORCE_CREATE = false;

    /** default value for folder to books */
    public String _folderPath = "/book/";
    /** default value for serialization folder */
    public String _serPath = "./var/book_cache";
    /** default opening book */
    public String _fileNamePlain = "Test_PGN/perle.pgn";
    /** default mode */
    public Mode _mode = Mode.PGN;

    /** Creates opening book configuration object */
    public Openingbook_Configuration() {}
}