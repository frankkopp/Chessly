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
    public String _folderPath = "./book/";
    /** default value for serialization folder */
    public String _serPath = "./book/ser/";
    /** default opening book */
    public String _fileNamePlain = "Test_PGN/perle.pgn";
    /** default mode */
    public Mode _mode = Mode.PGN;

    /** Creates opening book configuration object */
    public Openingbook_Configuration() {}
}