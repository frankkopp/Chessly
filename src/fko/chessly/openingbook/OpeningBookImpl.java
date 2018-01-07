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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.ClosedByInterruptException;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * result in serialized bin files. Implemented are PGN, SAN, SIMPLE and SER.
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
	private String _path;
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
		_path = _config._folderPath + _config._fileNamePlain;
	}

	/**
	 * Constructor for ObservableEngine
	 * @param engine
	 */
	public OpeningBookImpl(ObservableEngine engine) {
		_engine = engine;
		_path = _config._folderPath + _config._fileNamePlain;
	}

	/**
	 * Constructor for non ObservableEngine
	 * @param engine
	 * @param pathToOpeningBook
	 * @param mode
	 */
	public OpeningBookImpl(Engine engine, String pathToOpeningBook, Mode mode) {
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
	public OpeningBookImpl(ObservableEngine engine, String pathToOpeningBook, Mode mode) {
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
	 * 
	 * TODO: trigger regeneration not only when missing
	 * and when file data of original file is newer than the cache file If not
	 * uses _mode flag to determine which format to use.<br/>
	 * TODO: determine format automatically.
	 *
	 * @param pathString
	 */
	private void readBookfromFile(String pathString) {

		if (_config.VERBOSE) {
			printInfo(String.format("Opening Book: %s%n", pathString));
		}
		
		if (tryFromCache(pathString)) return; 

		// NON CACHE

		// book files might be in the JAR file so we can't use normal File mechanics but
		// need to use Class.getResource or Class.getResourceAsStream which uses URL or InputStream as 
		// return classes
		
		InputStream bookFileInputStream = null;

		bookFileInputStream = Chessly.class.getResourceAsStream(pathString.toString());

		if (bookFileInputStream == null) {
			Chessly.criticalError("Book File not found: " + pathString);
			return;
		} else {
			if (_config.VERBOSE)
				printInfo(String.format("Found Opening Book...: %s%n",pathString));
		}

		switch (_config._mode) {
		case PGN:
			processBookfromPGNFile(bookFileInputStream);
			saveOpeningBooktoSERFile(pathString);
			break;
		case SAN:
			processAllLines(bookFileInputStream);
			saveOpeningBooktoSERFile(pathString);
			break;
		case SIMPLE:
			processAllLines(bookFileInputStream);
			saveOpeningBooktoSERFile(pathString);
			break;
		default:
			break;
		}
	}

	/**
	 * Reads all lines from a file into list and returns it as a List<String>.
	 *
	 * @param bookFileInputStream.
	 * @return List<String> allLines
	 */
	private List<String> readAllLinesFromFile(InputStream bookFileInputStream) {

		long start = System.currentTimeMillis();
		Charset charset = Charset.forName("ISO-8859-1");
		List<String> lines = null;
		
		if (_config.VERBOSE)
			printInfo(String.format("Reading Opening Book...%n"));
		
		InputStreamReader isr = new InputStreamReader(bookFileInputStream, charset);
		BufferedReader br = new BufferedReader(isr);
		lines = br.lines().collect(Collectors.toList());
		
		long time = System.currentTimeMillis() - start;

		if (_config.VERBOSE)
			printInfo(String.format("Finished reading %d lines. (%f sec)%n", lines.size(), (time / 1000f)));

		return lines;
	}

	/**
	 * Reads all lines from a file into list and returns it as a List<String>.
	 *
	 * @param bookFileInputStream
	 * @return List<String> allLines
	 */
	private Stream<String> getStreamOfLinesFromFile(InputStream bookFileInputStream) {

		long start = System.currentTimeMillis();
		Charset charset = Charset.forName("ISO-8859-1");
		Stream<String> lines = null;
		
		if (_config.VERBOSE)
			printInfo(String.format("Reading Opening Book.%n"));

		InputStreamReader isr = new InputStreamReader(bookFileInputStream, charset);
		BufferedReader br = new BufferedReader(isr);
		lines = br.lines();
					
		long time = System.currentTimeMillis() - start;
		if (_config.VERBOSE)
			printInfo(String.format("Finished creating stream of lines. (%f sec)%n",(time / 1000f)));

		return lines;
	}

	private void processBookfromPGNFile(InputStream bookFileInputStream) {

		List<String> lines = readAllLinesFromFile(bookFileInputStream);

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
			//gameList.stream().forEach(game -> {
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
	 * Reads the opening book from a plain file and generates the internal data
	 * structure.
	 *
	 * @param bookFileInputStream
	 */
	private void processAllLines(InputStream bookFileInputStream) {

		Stream<String> lines = getStreamOfLinesFromFile(bookFileInputStream);

		long start = System.currentTimeMillis();
		if (_config.VERBOSE) {
			printInfo(String.format("Creating internal book...%n"));
		}

		synchronized (_counterLock) { _counter = 0; }
		// parallel lambda expression - very fast and cool - needs some synchronizing though
		lines.parallel().forEach(line -> {
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
		final int c;
		synchronized (_counterLock) {
			c = ++_counter;
		}
		if (_config.VERBOSE && c % 1000 == 0) {
			printInfo(String.format("%7d ", c));
			if (_config.VERBOSE && c % 10000 == 0) {
				printInfo(String.format("%n"));
			}
		}

		OpeningBook_Entry rootEntry = bookMap.get(NotationHelper.StandardBoardFEN);
		rootEntry.occurenceCounter.getAndIncrement();

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
				//System.out.println("Invalid Move ");
				//System.out.println();
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

		ArrayList<String> lineItems = new ArrayList<>();
		while (matcher.find()) {
			lineItems.add(matcher.group());
		}

		// board position
		GameBoard currentPosition = new GameBoardImpl(NotationHelper.StandardBoardFEN);

		// iterate over line items - not parallel / actually slower as we would have to copy the board.
		lineItems.stream().forEach(item -> {
			if (!processSIMPLELineItem(item, currentPosition)) return;
		});

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
			// System.out.println("Invalid Move ");
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
			currentEntry.occurenceCounter.getAndIncrement();
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
	 * Tries to find and read an existing cache file. If one exists <code>readBookfromSERFile</code>
	 * will be called and bookMap will be filled with cached entries.<br>
	 * If book cache folder does not exist it will be created.<br> 
	 * @param pathString
	 * @return true if cache file exists and has been read, false otherwise 
	 */
	protected boolean tryFromCache(String pathString) {
	
		// path of cache files always is external from JAR so we can use Files.exist()
		InputStream openingBookInputStream = null;
		Path cacheFolder = FileSystems.getDefault().getPath(_config._serPath);
	
		// Check if folder exists and if not try to create it.
		if (!Files.exists(cacheFolder)) {
			Chessly.minorError(String.format(
					"While reading book cache file: Path %s could not be found. Trying to create it."
					,cacheFolder.toString()
					));
			try {
				Files.createDirectories(cacheFolder);
			} catch (IOException e) {
				Chessly.fatalError(String.format(
						"While reading book cache file: Path %s could not be found. Couldn't create it."
						,cacheFolder.toString()
						));
			}
		}
		
		Path cacheFile = createCacheFileName(pathString);
		
		// read from cache file and return if not configured otherwise
		if (cacheFile.toFile().exists()) {
			if (!_config.FORCE_CREATE) {
				_config._mode = Mode.SER;
				try {
					openingBookInputStream = Files.newInputStream(cacheFile);
					readBookfromSERFile(openingBookInputStream);
					return true;
				} catch (IOException e) {
					Chessly.criticalError(String.format(
							"While reading book cache file: File %s could not read."
							,cacheFolder.toString()
							));
				}
			} else {
				if (_config.VERBOSE)
					printInfo(String.format("Cache file exists but ignored as FORCE_CREATE is set.%n"));
			}
		}
		return false;
	}

	/**
	 * @param openingBookInputStream
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean readBookfromSERFile(InputStream openingBookInputStream) {
	
		long start = System.currentTimeMillis();
		try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(openingBookInputStream))) {
			if (_config.VERBOSE) {
				printInfo(String.format("Reading Opening Book from cachefile.%n"));
			}
	
			bookMap = (Map<String, OpeningBook_Entry>) ois.readObject();
	
			ois.close();
		} catch (FileNotFoundException x) {
			Chessly.criticalError("file does not exists: " + openingBookInputStream);
			return false;
		} catch (ClosedByInterruptException x) {
			// ignore - we probably closed the game
			return false;
		} catch (IOException e) {
			Chessly.criticalError("reading file error: " + openingBookInputStream + " " + e.toString());
			return false;
		} catch (ClassCastException e) {
			Chessly.criticalError("file format error: " + openingBookInputStream + " " + e.toString());
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
	 * @return
	 */
	protected boolean saveOpeningBooktoSERFile(String pathString) {

		boolean result = false;
		long start = 0, time = 0;

		if (_config.VERBOSE) {
			start = System.currentTimeMillis();
			printInfo(String.format("Saving Open Book to cache file..."));
		}

		Path cacheFile = createCacheFileName(pathString);
		
		try {
			final OutputStream newOutputStream = Files.newOutputStream(cacheFile);
			final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(newOutputStream));
			oos.writeObject(bookMap);
			oos.close();
			
			result=true;
		
		} catch (FileAlreadyExistsException x) {
			System.err.format("file named %s" + " already exists: ", cacheFile);
			try {
				Files.delete(cacheFile);
			} catch (IOException e1) {
				// ignore
			}
			result = false;
		} catch (IOException e) {
			System.err.format("%ncreateFile error: %s%n%s%n", cacheFile, e.toString());
			try {
				Files.delete(cacheFile);
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
				System.err.format("Saving Opening Book to cache file failed. (%s)", cacheFile);
			}
		}

		return result;

	}

	/**
	 * @param pathString
	 * @return
	 */
	private Path createCacheFileName(String pathString) {
		// remove folder structure and replace by "-" in filename
		pathString = pathString.substring(0,1) + pathString.substring(1).replaceAll("/", "-");
		pathString = _config._serPath + pathString + ".ser";
		Path cacheFile = FileSystems.getDefault().getPath(pathString);
		return cacheFile;
	}

	/**
	 * @param info
	 */
	public void printInfo(String info) {
		if (_engine != null) {
			_engine.printVerboseInfo(info);
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
		AtomicInteger occurenceCounter = new AtomicInteger(0);
		// list of moves to next positions
		ArrayList<GameMove> moves = new ArrayList<>(5);

		// Constructor
		OpeningBook_Entry(String fen) {
			this.position = fen;
			this.occurenceCounter.set(1);
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
			return b.occurenceCounter.get() - this.occurenceCounter.get();
		}

		/*
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(OpeningBook_Entry o1, OpeningBook_Entry o2) {
			return o2.occurenceCounter.get() - o1.occurenceCounter.get();
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