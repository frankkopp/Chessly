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

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fko.chessly.Chessly;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameMove;
import fko.chessly.game.IllegalMoveException;
import fko.chessly.game.InvalidMoveException;
import fko.chessly.game.NotationHelper;
import fko.chessly.player.computer.Engine;
import fko.chessly.player.computer.ObservableEngine;
import fko.chessly.util.HelperTools;

/**
 * Implements an Opening Book for Chessly. Reads different formats and caches
 * result in serialized bin files. Implemented are SAN, SIMPLE and SER. ToDo:
 * PGN.
 *
 * @author Frank Kopp
 */
public class OpeningBookImpl implements OpeningBook, Serializable {

    private static final long serialVersionUID = -6462934049609479248L;

    protected Openingbook_Configuration _config = new Openingbook_Configuration();

    /**
     * this is the book mapping itself - Key, Value
     * Key is FEN notation
     * Value is BookEntry object
     * Trying different Collection implementations for it.
     */
    private Map<String, OpeningBook_Entry> bookMap = Collections.synchronizedMap(new HashMap<String, OpeningBook_Entry>(10000));

    private ObservableEngine _engine;
    private Path _path;
    private boolean _isInitialized = false;

    // -- helps with process output
    private int _counter = 0;
    private Object _counterLock = new Object();

    /**
     * Constructor for non ObservableEngine
     * @param engine
     */
    public OpeningBookImpl(Engine engine) {
        _engine = null;
        _path = FileSystems.getDefault().getPath(_config._folderPath, _config._fileNamePlain);
    }

    /**
     * Constructor for ObservableEngine
     * @param engine
     */
    public OpeningBookImpl(ObservableEngine engine) {
        _engine = engine;
        _path = FileSystems.getDefault().getPath(_config._folderPath, _config._fileNamePlain);
    }

    /**
     * Constructor for non ObservableEngine
     * @param engine
     * @param pathToOpeningBook
     * @param mode
     */
    public OpeningBookImpl(Engine engine, Path pathToOpeningBook, Mode mode) {
        _engine = null;
        _path = pathToOpeningBook;
        _config._mode = mode;
    }

    /**
     * Constructor for ObservableEngine
     * @param engine
     * @param pathToOpeningBook
     * @param mode
     */
    public OpeningBookImpl(ObservableEngine engine, Path pathToOpeningBook, Mode mode) {
        _engine = engine;
        _path = pathToOpeningBook;
        _config._mode = mode;
    }

    // --- START OF INTERFACE METHODS

    /**
     * Selects a random move from available opening book moves of a position.
     *
     * @see fko.chessly.openingbook.OpeningBook#getBookMove(java.lang.String)
     */
    @Override
    public GameMove getBookMove(String fen) {

        GameMove move = null;

        if (bookMap.containsKey(fen)) {
            ArrayList<GameMove> moveList;
            moveList = bookMap.get(fen).moves;
            if (moveList.isEmpty())
                return move;
            Collections.shuffle(moveList);
            move = moveList.get(0);
        }

        return move;
    }

    /**
     * @see fko.chessly.openingbook.OpeningBook#initialize()
     */
    @Override
    public void initialize() {

        if (_isInitialized) return;

        if (_config.VERBOSE) {
            printInfo(String.format("Opening Book initialization...%n"));
        }

        System.gc();
        long memStart = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        long start = System.currentTimeMillis();

        // setup the root node
        bookMap.put(NotationHelper.StandardBoardFEN, new OpeningBook_Entry(NotationHelper.StandardBoardFEN));

        readBookfromFile(_path);

        long time = System.currentTimeMillis() - start;

        System.gc();
        long memEnd = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        if (_config.VERBOSE) {
            printInfo(String.format("Memory used at Start: %s MB %n", HelperTools.getMBytes(memStart)));
            printInfo(String.format("Memory used at End: %s MB%n", HelperTools.getMBytes(memEnd)));
            printInfo(String.format("Memory used by Opening Book: ~%s MB%n", HelperTools.getMBytes(memEnd - memStart)));
            printInfo(String.format("Opening Book initialization took %f sec.%n%n", (time / 1000f)));
        }

        _isInitialized = true;
    }



    // --- END OF INTERFACE METHODS

    /**
     * Reads the opening book from file. Determines if a cache file
     * (serialization) of the book is already present. If present calls
     * readBookfromSERFile().<br/>
     * TODO: trigger regeneration not only when missing
     * and when file data of original file is newer than the cache file If not
     * uses _mode flag to determine which format to use.<br/>
     * TODO: determine format automatically.
     *
     * @param path
     */
    private void readBookfromFile(Path path) {

        Path serPath = FileSystems.getDefault().getPath(_config._serPath, path.getFileName().toString() + ".ser");

        if (serPath.toFile().exists()) {
            if (!_config.FORCE_CREATE) {
                _config._mode = Mode.SER;
                path = serPath;
            } else {
                if (_config.VERBOSE)
                    printInfo(String.format("Cache file exists but ignored as FORCE_CREATE is set.%n"));
            }

        } else if (path.toFile().exists()) {
            // leave mode as is for now
            // Later we could try to determine the mode automatically
        } else {
            Chessly.criticalError("Book File not found: " + FileSystems.getDefault().getPath(_config._folderPath, _config._fileNamePlain));
            return;
        }

        switch (_config._mode) {
            case SER:
                readBookfromSERFile(path);
                break;
            case PGN:
                processBookfromPGNFile(path);
                saveOpeningBooktoSERFile(path);
                break;
            case SAN:
                processAllLines(path);
                saveOpeningBooktoSERFile(path);
                break;
            case SIMPLE:
                processAllLines(path);
                saveOpeningBooktoSERFile(path);
                break;
            default:
                break;
        }
    }

    /**
     * Reads all lines from a file into list and returns it as a List<String>.
     *
     * @param path
     * @return List<String> allLines
     */
    private List<String> readAllLinesFromFile(Path path) {

        long start = System.currentTimeMillis();
        Charset charset = Charset.forName("ISO-8859-1");
        List<String> lines = null;
        try {
            if (_config.VERBOSE)
                printInfo(String.format("Reading Opening Book...: %s%n",path));
            lines = Files.readAllLines(path, charset);
            long time = System.currentTimeMillis() - start;
            if (_config.VERBOSE)
                printInfo(String.format("Finished reading %d lines. (%f sec)%n", lines.size(), (time / 1000f)));
        } catch (CharacterCodingException e) {
            Chessly.criticalError("Opening Book file '" + path + "' has wrong charset (needs to be ISO-8859-1) - not loaded!");
        } catch (IOException e) {
            Chessly.criticalError("Opening Book file '" + path + "' could not be loaded!");
        }

        return lines;
    }

    private void processBookfromPGNFile(Path path) {

        List<String> lines = readAllLinesFromFile(path);

        PGN_Reader pgnReader = new PGN_Reader(lines);

        if (!pgnReader.startProcessing()) {
            Chessly.criticalError("Could not process lines from PGN file: " + FileSystems.getDefault().getPath(_config._folderPath, _config._fileNamePlain));
            return;
        }

        List<PGN_Reader.pgnGame> gameList = pgnReader.getGames();

        // give the gc a chance to delete the lines
        lines = null;
        pgnReader = null;

        long start = System.currentTimeMillis();
        if (_config.VERBOSE) {
            printInfo(String.format("Creating internal book...%n"));
        }

        synchronized (_counterLock) { _counter = 0; }
        // parallel lambda expression - very fast and cool - needs some synchronizing though
        gameList.parallelStream().forEach(game -> {
            String moves = game.getMoves().toString();
            moves = moves.replaceAll("[\\[\\],]", "");
            processLine(moves);
        });

        long time = System.currentTimeMillis() - start;
        if (_config.VERBOSE) {
            printInfo(String.format("%nOpening Book ready! %d Positions (%f sec) %n", bookMap.size(), (time / 1000f)));
        }
    }

    /**
     * Reads the opening book from a plain file and generates the internal date
     * structure.
     *
     * @param path
     */
    private void processAllLines(Path path) {

        List<String> lines = readAllLinesFromFile(path);

        long start = System.currentTimeMillis();
        if (_config.VERBOSE) {
            printInfo(String.format("Creating internal book...%n"));
        }

        synchronized (_counterLock) { _counter = 0; }
        // parallel lambda expression - very fast and cool - needs some synchronizing though
        lines.parallelStream().forEach(line -> {
            processLine(line);
        });

        if (_config.VERBOSE)
            printInfo(String.format("%n"));

        long time = System.currentTimeMillis() - start;
        if (_config.VERBOSE) {
            printInfo(String.format("Opening Book ready! %d Positions (%f sec) %n", bookMap.size(), (time / 1000f)));
        }
    }

    /**
     * Generates the data structure from one line of input form the plain book
     * file.
     *
     * @param line
     */
    private void processLine(String line) {
        synchronized (_counterLock) {
            _counter++;
            if (_config.VERBOSE && _counter % 1000 == 0) {
                printInfo(String.format("%7d ", _counter));
                if (_config.VERBOSE && _counter % 10000 == 0) {
                    printInfo(String.format("%n"));
                }
            }
        }

        OpeningBook_Entry rootEntry = bookMap.get(NotationHelper.StandardBoardFEN);
        rootEntry.occurenceCounter++;

        switch (_config._mode) {
            case SAN:
                processSANLine(line);
                break;
            case SIMPLE:
                processSIMPLELine(line);
                break;
            case PGN:
                processSANLine(line);
                break;
            default:
                break; // NYI
        }

    }

    /**
     * Generates the data structure from one line of SAN input form the plain
     * book file.
     *
     * @param line
     */
    private void processSANLine(String line) {

        // separate each move
        String[] lineItems = line.split(" ");

        // board position
        GameBoard currentPosition = new GameBoardImpl(NotationHelper.StandardBoardFEN);

        // iterate over line items
        for (String item : lineItems) {
            // trim whitespaces
            item = item.trim();

            // ignore numbering
            if (item.matches("\\d+\\."))
                continue;

            GameMove bookMove = null;
            // try to create a move from it
            try {
                bookMove = NotationHelper.createNewMoveFromSANNotation(currentPosition, item);
            } catch (InvalidMoveException e) {
                // Jump back if the last move from file was not valid.
                // We will then skip the rest of the line and try the next line.
                // System.out.println("Invalid Move ");
                // System.out.println();
                return;
            }
            // System.out.print(m+" ");

            // remember last position
            GameBoard lastPosition = new GameBoardImpl(currentPosition);
            String lastFen = lastPosition.toFENString();

            // try to make move
            try {
                currentPosition.makeMove(bookMove);
            } catch (IllegalMoveException e) {
                // Jump back if the last move from file was not valid.
                // We will then skip the rest of the line and try the next line.
                // System.out.println("Illegal Move "+item+Character.LINE_SEPARATOR);
                // System.out.println();
                return;
            }

            // we have successfully made the move
            // get fen notation from position
            String currentFen = currentPosition.toFENString();

            addToBook(item, bookMove, lastFen, currentFen);

        }
        // System.out.println();
    }

    private void processSIMPLELine(String line) {

        // ignore lines start with a digit
        if (line.matches("^\\d+"))
            return;

        // separate each move
        Pattern pattern = Pattern.compile("([a-h][1-8][a-h][1-8])");
        Matcher matcher = pattern.matcher(line);

        ArrayList<String> lineItems = new ArrayList<String>();
        while (matcher.find()) {
            lineItems.add(matcher.group());
        }

        // board position
        GameBoard currentPosition = new GameBoardImpl(NotationHelper.StandardBoardFEN);

        // iterate over line items - not parallel / actually slower as we would have to copy the board.
        lineItems.stream().forEach(item -> {
            if (!processSIMPLELineItem(item, currentPosition)) return;
        });

        /*for (String item : lineItems) {
            if (!processSIMPLELineItem(item, currentPosition)) break;
        }*/

    }

    /**
     * @param item
     * @param currentPosition
     * @return
     */
    private boolean processSIMPLELineItem(String item, GameBoard currentPosition) {
        // trim whitespaces
        item = item.trim();

        GameMove bookMove = null;
        // try to create a move from it
        try {
            bookMove = NotationHelper.createNewMoveFromSimpleNotation(currentPosition, item);
        } catch (InvalidMoveException e) {
            // Jump back if the last move from file was not valid.
            // We will then skip the rest of the line and try the next line.
            //System.out.println("Invalid Move ");
            // System.out.println();
            return false;
        }

        // remember last position
        GameBoard lastPosition = new GameBoardImpl(currentPosition);
        String lastFen = lastPosition.toFENString();

        // try to make move
        try {
            currentPosition.makeMove(bookMove);
        } catch (IllegalMoveException e) {
            // Jump back if the last move from file was not valid.
            // We will then skip the rest of the line and try the next line.
            //System.out.println("Illegal Move "+item+Character.LINE_SEPARATOR);
            // System.out.println();
            return false;
        }

        // we have successfully made the move
        // get fen notation from position
        String currentFen = currentPosition.toFENString();

        addToBook(item, bookMove, lastFen, currentFen);
        return true;
    }

    /**
     * @param item
     * @param bookMove
     * @param lastFen
     * @param currentFen
     */
    private synchronized void addToBook(String item, GameMove bookMove, String lastFen, String currentFen) {

        // add the new entry to map or increase occurrence counter to existing
        // entry
        if (bookMap.containsKey(currentFen)) {
            OpeningBook_Entry currentEntry = bookMap.get(currentFen);
            // Collision detection
            if (!currentFen.equals(currentEntry.position)) {
                throw new RuntimeException("Hashtable Collision!");
            }
            currentEntry.occurenceCounter++;
        } else {
            String key = new String(currentFen);
            bookMap.put(key, new OpeningBook_Entry(currentFen));
        }

        // add move to last book entry (lastPosition)
        OpeningBook_Entry lastEntry = bookMap.get(lastFen);
        if (!lastEntry.moves.contains(bookMove)) {
            lastEntry.moves.add(bookMove);
        }
    }

    /**
     * @return
     */
    private boolean saveOpeningBooktoSERFile(Path path) {

        boolean result = false;
        long start = 0, time = 0;

        if (_config.VERBOSE) {
            start = System.currentTimeMillis();
            printInfo(String.format("Saving Open Book to cache file..."));
        }

        path = FileSystems.getDefault().getPath(_config._serPath, path.getFileName().toString() + ".ser");
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING))) {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(bookMap);
            oos.close();
            result=true;
        } catch (FileAlreadyExistsException x) {
            System.err.format("file named %s" + " already exists: ", path);
            try {
                Files.delete(path);
            } catch (IOException e1) {
                // ignore
            }
            result = false;
        } catch (IOException e) {
            System.err.format("%ncreateFile error: %s%n%s%n", path, e.toString());
            try {
                Files.delete(path);
            } catch (IOException e1) {
                // ignore
            }
            result = false;
        }

        if (result) {
            if (_config.VERBOSE) {
                time = System.currentTimeMillis() - start;
                printInfo(String.format("successful.(%f sec) %n", (time / 1000f)));
            }
        } else {
            if (_config.VERBOSE) {
                System.out.format("failed.%n");
            } else {
                System.err.format("Saving Opening Book to cache file failed. (%s)", path);
            }
        }

        return result;

    }

    /**
     * @param path
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean readBookfromSERFile(Path path) {

        long start = System.currentTimeMillis();
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            if (_config.VERBOSE) {
                printInfo(String.format("Reading Opening Book as SER file: %s%n",path));
            }
            ObjectInputStream ois = new ObjectInputStream(in);

            bookMap = (Map<String, OpeningBook_Entry>) ois.readObject();

            ois.close();

        } catch (FileNotFoundException x) {
            Chessly.criticalError("file does not exists: " + path);
            return false;
        } catch (ClosedByInterruptException x) {
            // ignore - we probably closed the game
            return false;
        } catch (IOException e) {
            Chessly.criticalError("reading file error: " + path + " " + e.toString());
            return false;
        } catch (ClassCastException e) {
            Chessly.criticalError("file format error: " + path + " " + e.toString());
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        long time = System.currentTimeMillis() - start;

        if (_config.VERBOSE) {
            printInfo(String.format("Opening Book ready! %d Positions (%f sec)%n", bookMap.size(), (time / 1000f)));
        }

        return true;

    }

    /**
     * @param info
     */
    public void printInfo(String info) {
        if (_engine != null) {
            _engine.getVerboseInfo(info);
        } else {
            System.out.print(info);
        }
    }

    /**
     * Different possible mode for book files. BIN - will be serialization of
     * java SAN - uses a line by line listing of move lists in SAN notation PNG
     * - uses a file of at least one PNG game SIMLE - uses a line with full
     * from-to description without - or ' '
     *
     * @author fkopp
     */
    @SuppressWarnings("javadoc")
    public enum Mode {
        SER,
        SAN,
        PGN,
        SIMPLE
    }

    /**
     * Represents one book entry for our opening book. This is bacically a fen
     * notation of the position, the number of occurences of this position in
     * the book file and the moves as SAN notation to the subsequent position.
     *
     * @author fkopp
     */
    private static class OpeningBook_Entry implements Comparable<OpeningBook_Entry>, Comparator<OpeningBook_Entry>, Serializable {

        private static final long serialVersionUID = 1573629955690947725L;

        // as fen notation
        String position;
        // how often did this position occur in the opening book
        int occurenceCounter = 0;
        // list of moves to subsequential positions
        ArrayList<GameMove> moves = new ArrayList<GameMove>(5);

        // Constructor
        OpeningBook_Entry(String fen) {
            this.position = fen;
            this.occurenceCounter = 1;
        }

        @Override
        public String toString() {
            StringBuffer s = new StringBuffer(position);
            s.append(" (").append(occurenceCounter).append(") ");
            s.append(moves.toString());
            // s.append(System.lineSeparator());
            return s.toString();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(OpeningBook_Entry b) {
            return b.occurenceCounter - this.occurenceCounter;
        }

        /*
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(OpeningBook_Entry o1, OpeningBook_Entry o2) {
            return o2.occurenceCounter - o1.occurenceCounter;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.position == null) ? 0 : this.position.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof OpeningBook_Entry)) {
                return false;
            }
            OpeningBook_Entry other = (OpeningBook_Entry) obj;
            if (this.position == null) {
                if (other.position != null) {
                    return false;
                }
            } else if (!this.position.equals(other.position)) {
                return false;
            }
            return true;
        }
    }


}