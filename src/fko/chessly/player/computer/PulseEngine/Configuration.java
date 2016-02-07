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
package fko.chessly.player.computer.PulseEngine;

import fko.chessly.openingbook.OpeningBookImpl.Mode;


/**
 * Engine Configuration.
 *
 * @author fkopp
 */
public class Configuration {

    /** If set to true this object will produce info output to System.out */
    public boolean VERBOSE_TO_SYSOUT = false;

    /** verbose output at the root level of the iterative search */
    public boolean VERBOSE_ITERATIVE_SEARCH = true;
    /** verbose output in the nega-max recursion */
    public boolean VERBOSE_ALPHABETA = true;
    /** verbose output when pondering */
    public boolean VERBOSE_PONDERER = true;
    /** verbose output for Principal Variation development */
    public boolean VERBOSE_PV = true;

    /**
     * set to true for correct move generation according to PERF tests
     * Turns off all optimizations.
     */
    public boolean PERF_TEST = false;

    /** AlphaBeta pruning */
    public boolean _USE_PRUNING = true;
    /** Principle Variation null window search */
    public boolean _USE_PV = true;
    /** Mate Distance Pruning */
    public boolean _USE_MDP = true;
    /** Use quiescnet search after depth has been reached */
    public boolean _USE_QUIESCENCE = true;
    /** Use Transposition Tables for visited nodes */
    public boolean _USE_NODE_CACHE = true;
    /** Use Ponderer while waiting for opponents move - fills node_cache */
    public boolean _USE_PONDERER = true;
    /** Use Cache for Board evaluations - very expensive, only worth with expensie eval */
    public boolean _USE_BOARD_CACHE = _USE_QUIESCENCE ? true : false;
    /** Use preconfigured opening book. @see OpeningBookImpl.java */
    public boolean _USE_BOOK = true;

    /** default value for folder to books */
    public String _OB_FolderPath = "./book/";
    /** default opening book file */
    //public String _OB_fileNamePlain = "8moves_GM_LB.pgn";
    public String _OB_fileNamePlain = "book_graham.txt";
    /** default opening book value */
    //public Mode _OB_Mode = Mode.PGN;
    public Mode _OB_Mode = Mode.SAN;

    // private final static String _fileNamePlain = "book_graham.txt";
    // private static Mode _mode = Mode.SAN;
    // private final static String _fileNamePlain = "book.txt";
    // private static Mode _mode = Mode.SIMPLE;
    // private final static String _fileNamePlain = "book_smalltest.txt";
    // private static Mode _mode = Mode.SIMPLE;


    /**
     * Creates an instance of the configuration for the engine.
     */
    public Configuration() {}
}