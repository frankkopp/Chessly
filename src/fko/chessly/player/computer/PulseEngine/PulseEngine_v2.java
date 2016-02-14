/**
 * =============================================================================
 * Pulse
 *
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 *
 * ============================================================================
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

import static java.lang.Integer.parseInt;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import fko.chessly.Chessly;
import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameCastling;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveImpl;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePosition;
import fko.chessly.game.pieces.Bishop;
import fko.chessly.game.pieces.King;
import fko.chessly.game.pieces.Knight;
import fko.chessly.game.pieces.Pawn;
import fko.chessly.game.pieces.Queen;
import fko.chessly.game.pieces.Rook;
import fko.chessly.mvc.ModelObservable;
import fko.chessly.mvc.ModelEvents.PlayerDependendModelEvent;
import fko.chessly.openingbook.OpeningBook;
import fko.chessly.openingbook.OpeningBookImpl;
import fko.chessly.player.AbstractPlayer.PlayerStatusController;
import fko.chessly.player.ComputerPlayer;
import fko.chessly.player.Player;
import fko.chessly.player.computer.Engine;
import fko.chessly.player.computer.ObservableEngine;
import fko.chessly.player.computer.PulseEngine.MoveList.Entry;
import fko.chessly.player.computer.PulseEngine.MoveList.MoveVariation;
import fko.chessly.util.HelperTools;

/**
 * Implementation of a chess engine originally written by Phokham Nonava.<br/>
 * It uses pseudo classes and int values to represent types instead of proper
 * Java classes - this approach is much faster than Java classes!<br/>
 * <br/>
 * Features:<br/>
 * <ul>
 * <li>time control</li>
 * <li>nega max</li>
 * <li>AlphaBeta Pruni</li>
 * <li>Principal Variation</li>
 * <li>Mate Distance Pruning</li>
 * <li>Quiescence</li>
 * <li>Transposition tables</li>
 * <li>Evaluation tables ( only good idea if eval is expensive)</li>
 * <li>Opening book</li>
 * <li>Pondering</li>
 * </ul>
 *
 * <ul>
 * <li>FIXME: TestFix - Board cannot see move repetition if the move has been received by Game
 * <li>FIXME: Improve Quiescence (Stalemate a.o.)</li>
 * <li>TODO: End Game</li>
 * <li>TODO: Better position evaluatoin
 * <li>TODO: Idea: Delay Move Generation http://www.madchess.net/post/madchess-2-0-beta-build-23</li>
 * <li>TODO: End Game Solver</li>
 * </li>
 * </ul>
 * <br/>
 *
 * @author Frank Kopp, Phokham Nonava
 */
public class PulseEngine_v2 extends ModelObservable implements ObservableEngine {

    /**
     * read in the default configuration - change the public fields if necessary
     */
    public Configuration _config = new Configuration();

    /** Will store the VERBOSE info until the EngineWatcher collects it. */
    private static final int _engineInfoTextMaxSize = 10000;
    private final StringBuilder _engineInfoText = new StringBuilder(_engineInfoTextMaxSize);

    // A back reference to the current Game object this engine is used in
    private Game _game = null;

    // A back reference to the current Player object this engine is used in
    private ComputerPlayer _player = null;

    // My color (Max Player)
    private GameColor _activeColor = GameColor.NONE;

    // The boardAnalyser is used to analyze the board and return a value
    private final Evaluation _evaluation = new Evaluation();

    // We will store a MoveGenerator in Worker for each ply so we don't have to create
    private final MoveGenerator _rootMoveGenerator = new MoveGenerator();

    // Depth search - set by UI
    private int _maxIterativeDepth = Depth.MAX_DEPTH;

    // Time & Clock & Ponder search
    private long _startTime = 0L;
    private Timer _timer = null;
    private boolean _doTimeManagement = false;

    // Time control
    private boolean _softTimeLimitReached = false;
    private boolean _hardTimeLimitReached = false;
    private long _approxTime;

    // move stats
    private final MoveList _rootMoves = new MoveList();
    private int _initialIterativeDepth = 1;
    private int _currentSearchDepth = _initialIterativeDepth;
    private int _curSearchDepth = _initialIterativeDepth;
    private int _currentMove = Move.NOMOVE;
    private int _currentMoveNumber = 0;
    private int _currrentExtraSearchDepth = 0;

    // is set when abort conditions are met - stops recursions at certain points
    private boolean _abort = false;

    // some counters
    private final AtomicLong _boardsEvaluated = new AtomicLong(0);
    private final AtomicLong _boardsNonQuiet = new AtomicLong(0);
    private final AtomicLong _nodesVisited = new AtomicLong(0);

    // to have a value when the engine is not thinking
    private long _lastUsedTime = 0;
    private int _lastNodesPerSecond = 0;

    // Holds the currently best move in the current search
    private Entry _currentBestRootMove = new Entry();

    // Transposition Table Cache
    private boolean _cacheEnabled;
    private TranspositionTable _transpositionTable = null;
    private EvaluationTable    _evaluationTable = null;

    private final AtomicLong _nodeCacheHits = new AtomicLong(0);
    private final AtomicLong _nodeCacheMisses = new AtomicLong(0);
    private final AtomicLong _boardsCacheHit = new AtomicLong(0);
    private final AtomicLong _boardsCacheMiss = new AtomicLong(0);

    // pre-generated MoveGenerators
    private final MoveGenerator[] _searchMoveGenerators;

    // hold calculated variations
    private final MoveVariation[] _pv = new MoveVariation[Depth.MAX_PLY + 1];
    private final MoveVariation _cv = new MoveVariation(); // current variation

    // Opening Book
    private OpeningBook _openingBook = null;

    // Ponderer
    private Ponderer _ponderer = null;
    private boolean _timeToPonder = false;
    private final Semaphore _ponderSemaphore = new Semaphore(1);
    private Board _ponderBoard = null;
    private boolean _isPondering;
    private GameMove _ponderMove = null;

    /*
     * A string holding the current status of the engine for the ui.
     * E.g. "thinking", "waiting", "pondering on e2-e4", "book move"
     */
    private String _statusInfo;

    /*
     * Status of the engine for engine observers
     */
    private AtomicInteger _status = new AtomicInteger(ObservableEngine.IDLE);

    // Constructor ----------------------------------------------

    /**
     * Generates an instance of this Engine implementation.
     * It initializes caches, move generators and the opening book if these features
     * are enabled by properties, options or code.
     */
    public PulseEngine_v2() {
        super();

        // cache setup
        _cacheEnabled = Boolean.valueOf(Chessly.getProperties().getProperty("engine.cacheEnabled"));
        if (_cacheEnabled) {
            initializeCacheTables(); // create a cache
        }

        // init move generators
        _searchMoveGenerators = new MoveGenerator[Depth.MAX_PLY];

    }

    // EngineInterface ------------------------------------------

    /**
     * Initializes the engine by giving it the back reference to the player iteslf.
     *
     * @param player back reference to the player.
     */
    @Override
    public void init(Player player) {
        if (!(player instanceof ComputerPlayer)) {
            Chessly.fatalError("Engine objext can only be used with an instance of ComputerPlayer!");
        }

        this._player = (ComputerPlayer) player;
        _activeColor = player.getColor();

        // initialize opening book
        if (_config._USE_BOOK && !_config.PERF_TEST) {
            Path path = FileSystems.getDefault().getPath(_config._OB_FolderPath, _config._OB_fileNamePlain);
            _openingBook =   new OpeningBookImpl(this,path,_config._OB_Mode);
        }

        // only useful when we have a node cache
        if (!_config._USE_NODE_CACHE || _config.PERF_TEST)
            _config._USE_PONDERER = false;

        // initialize the ponderer if we use transposition tables
        if (_config._USE_PONDERER) {
            _ponderer = new Ponderer(_player.getName());
        }

        _statusInfo = "Engine initialized.";

    }

    /**
     * Sets the current game.
     *
     * @param game
     */
    @Override
    public void setGame(Game game) {
        this._game = game;
    }

    /**
     * Starts calculation and returns next move.
     *
     * @param board
     * @return best legal move
     */
    @Override
    public GameMove getNextMove(GameBoard board) {

        // waits until pondering has finished
        if (_config._USE_PONDERER) stopPondering();

        _status.set(ObservableEngine.THINKING);

        // notify ui
        setChanged();
        notifyObservers(new PlayerDependendModelEvent("ENGINE "+_activeColor+" start calculating", _player, SIG_ENGINE_START_CALCULATING));

        // Start timer
        _startTime = System.currentTimeMillis();

        // we initialize here to be fair and have this run against
        // the players clock.
        if (_config._USE_BOOK && !_config.PERF_TEST)
            _openingBook.initialize();

        // Reset all the counters used for the TreeSearchEngineWatcher
        resetCounters();

        _abort = false;

        // Translate fko.chessly.game.Board to Board
        Board convertedBoard = new Board(board);

        // Make sure the board is playable
        // FIXME: Repetition!
        if (board.hasInsufficientMaterial() || convertedBoard.isRepetition() || _game.countRepetitions() >= 3 || convertedBoard.halfmoveClock >= 100)
            throw new AssertionError("board is not playable - should not happen");

        // Timed game?
        boolean timedGame = _game.isTimedGame();

        // Update maximal search depth so we can change
        // the level for the engine in the UI each move
        _maxIterativeDepth = updateSearchDepth();

        if (timedGame) {
            _doTimeManagement = true;
            _approxTime = approxTime(convertedBoard);
            configureTimeControl(_approxTime);
        } else {
            // If we do not have a timed game we immediately search
            // with the maximum search depth
            _initialIterativeDepth = _maxIterativeDepth;
        }

        GameMove move = null;

        if (_config._USE_BOOK && !_config.PERF_TEST) {
            final GameMove bookMove = _openingBook.getBookMove(board.toFENString());
            if (bookMove != null) {
                move = bookMove;
            }
        }

        // no book move found
        if (move == null) {
            _statusInfo = "Engine calculating... ";
            // Search
            prepareSearch();
            int bestMove = iterativeSearch(convertedBoard);
            // Translate best Move to fko.chessly.game.Move
            move = convertMove(bestMove);
            move.setValue(_rootMoves.entries[0].value);
            // prepare pondering
            _ponderBoard = convertedBoard;
            _ponderBoard.makeMove(bestMove);
        } else {
            _statusInfo = "Engine opening book move "+move;
        }

        if (_timer != null) {
            _timer.cancel();
        }

        // notify ui
        setChanged();
        notifyObservers(new PlayerDependendModelEvent("ENGINE "+_activeColor+" finished calculating", _player, SIG_ENGINE_FINISHED_CALCULATING));

        _status.set(ObservableEngine.IDLE);

        if (_config._USE_PONDERER) startPondering();

        return move;
    }
    //
    // EngineInterface END------------------------------------------
    //


    /**
     * Generates moves at root and starts iterative search deepening.
     *
     * @param board
     * @return
     */
    private int iterativeSearch(Board board) {

        // Populate root move list
        boolean isCheck = board.isCheck();
        MoveGenerator moveGenerator = _rootMoveGenerator;
        MoveList moves = moveGenerator.getLegalMoves(board, 1, isCheck);
        // list is pre sorted in moveGenerator

        // reset values on the move list
        _rootMoves.size = 0;
        for (int i = 0; i < moves.size; ++i) {
            int move = moves.entries[i].move;
            _rootMoves.entries[_rootMoves.size].move = move;
            _rootMoves.entries[_rootMoves.size].value = -Value.INFINITE;
            _rootMoves.size++;
        }

        // The root position is a checkmate or stalemate.
        // Should not happen!
        // We cannot search further.
        if (_rootMoves.size == 0) {
            //assert false;
            // send NOMOVE if we got this board
            // let the outside deal with this
            return Move.NOMOVE;
        }

        // When not doing a timed game and using PV we should do at least
        // one search 2-level deep to better sort the move list
        int startIterativeDepth = _maxIterativeDepth;
        boolean preIteration = false;
        if (_doTimeManagement || _isPondering) {
            startIterativeDepth = 1;
        } else if (_config._USE_PV && !_config.PERF_TEST) {
            startIterativeDepth = _maxIterativeDepth <= 2 ? _maxIterativeDepth : 2;
            preIteration = true;
        }

        if (_config.PERF_TEST) {
            startIterativeDepth = _maxIterativeDepth;
        }

        // reset the principal variation move list
        _pv[0].size = 0;

        // set a temporary best move
        _currentBestRootMove = _rootMoves.entries[0];
        savePV(_currentBestRootMove.move, _pv[1], _pv[0]);

        // ### BEGIN Iterative Deepening
        int depth = startIterativeDepth;
        do {

            // check for game paused
            _game.waitWhileGamePaused();

            // update depth value
            _currentSearchDepth = depth;
            // for UI
            _curSearchDepth = 0;

            // Do the search on the _rootMoves list and store values in
            // the elements of _rootMoves.
            searchRoot(board, _currentSearchDepth);

            if (_config.VERBOSE_PV) {
                for (int i = 0; i < _currentSearchDepth;i++) {
                    String info = String.format("PV(%d) (size=%d): %s%n", i, _pv[i].size, _pv[i]);
                    getVerboseInfo(info);
                }
                printInfoln();
            }

            // sort moves - best first
            _rootMoves.sort();

            checkStopConditions(false);
            if (_abort) {
                break;
            }

            // When not doing a timed game and using PV we should do at least
            // one search 2-level deep to better sort the move list for PV
            if ((_config._USE_PV && preIteration)) {
                depth = _maxIterativeDepth - 1; // -1 because depth is increased in while statement
                preIteration = false;
            }

        } while (++depth <= _maxIterativeDepth);
        // ### ENDOF Iterative Deepening

        return _currentBestRootMove.move;

    }

    /**
     * Root Search
     *
     * @param board
     * @param depth
     */
    private void searchRoot(Board board, int depth) {

        // at least one ply needs to be searched
        if (depth < 1) depth = 1;

        int ply = 0;

        // nodes counter
        _nodesVisited.getAndIncrement();

        // Initialize
        int alpha = -Value.INFINITE;
        int beta = Value.INFINITE;

        // Verbose code
        long startNodes = _nodesVisited.get();
        long start = System.currentTimeMillis();
        if (_config.VERBOSE_ITERATIVE_SEARCH) {
            String info = String.format("%nSearching depth (%d)%n", depth);
            getVerboseInfo(info);
        }

        // ### Iterate through all available root moves
        for (int i = 0; i < _rootMoves.size; ++i) {
            int move = _rootMoves.entries[i].move;

            // store the current move for Engine Watcher
            _currentMove = move;
            _currentMoveNumber = i + 1;

            // ### START - Commit move and go deeper into recursion
            board.makeMove(move);
            cvAdd(ply, move);

            final boolean check = board.isCheck();
            int value;
            if (_config._USE_PV && !_config.PERF_TEST && i > 0) {
                // search with null window after the first loop
                value = -negaMax(board, depth - 1, 0, -alpha - 1, -alpha, ply + 1, check, _searchMoveGenerators);
                if (_config.VERBOSE_ITERATIVE_SEARCH) {
                    String info = String.format("%2d/%2d depth:%d %2d/%2d (%d) PVS %s - current best: %s", _currentMoveNumber, _rootMoves.size, ply + 1, i + 1,
                            _rootMoves.size, value, _cv.toString(), _currentBestRootMove);
                    getVerboseInfo(info);
                }
                // value > beta - is checked below after undoMove

                // alpha < value < beta - we do not have an exact alpha value, but know all nodes are <beta
                if (value > alpha && value < beta) {
                    // full re-search to get real alpha value
                    value = -negaMax(board, depth - 1, 0, -beta, -value, ply + 1, check, _searchMoveGenerators);
                    if (_config.VERBOSE_ITERATIVE_SEARCH) {
                        String info = String.format("%n%2d/%2d depth:%d %2d/%2d (%d) PVSre %s - current best: %s", _currentMoveNumber, _rootMoves.size, ply + 1, i + 1,
                                _rootMoves.size, value, _cv.toString(), _currentBestRootMove);
                        getVerboseInfo(info);
                    }
                }
            } else {
                // no PV null window search
                value = -negaMax(board, depth - 1, 0, -beta, -alpha, ply + 1, check, _searchMoveGenerators);
                if (_config.VERBOSE_ITERATIVE_SEARCH) {
                    if (depth > 1) getVerboseInfo(String.format("%n"));
                    String info = String.format("%2d/%2d root  (%d) %s - current best: %s", i + 1, _rootMoves.size, value, Move.toString(move), _currentBestRootMove);
                    getVerboseInfo(info);

                }
            }

            board.undoMove();
            cvRemove(ply);
            // ###END - Commit move and go deeper into recursion

            // store the value in the move
            _rootMoves.entries[i].value = value;

            // Do we have a better value?
            if (value > alpha) {
                if (_config.VERBOSE_ITERATIVE_SEARCH) {
                    String info = String.format("   NEW BEST PV: value(%d) > alpha(%d) %s", value, alpha, _pv[0]);
                    getVerboseInfo(info);
                }
                alpha = value;

                savePV(move, _pv[1], _pv[0]); // ply+1 to ply

                if (_config.VERBOSE_PV) {
                    printInfoln();
                    for (int j = 0; j < depth;j++) {
                        String info = String.format("   PV(%d) (size=%d): %s%n", j, _pv[j].size, _pv[j]);
                        getVerboseInfo(info);
                    }
                    printInfoln();
                }

                // for the UI
                _currentBestRootMove = _rootMoves.entries[i];
            }

            if (_config.VERBOSE_ITERATIVE_SEARCH) {
                printInfoln();
                if (depth > 1) getVerboseInfo(String.format("%n"));
            }

            // Check for time when time enabled management
            checkStopConditions(false);
            if (_abort) {
                break;
            }

        } // ### Iterate through all available moves

        if (_config.VERBOSE_ITERATIVE_SEARCH) {
            long nodes = _nodesVisited.get() - startNodes;
            long time = System.currentTimeMillis() - start;
            long nps = nodes / (time + 1); // to avoid div by zero
            String info = String.format(Locale.GERMANY, "%nBest Move: %s PV: %s", _currentBestRootMove, _pv[0]);
            info += String.format(Locale.GERMANY, "%nDepth %d/%d done. %s nodes (%.4f sec) (%s nps) %n", depth, _currrentExtraSearchDepth,
                    HelperTools.getDigit(nodes), (time / 1000f), HelperTools.getDigit(nps * 1000));
            getVerboseInfo(info);
        }

    }

    /**
     * negaMax Search after the root level
     *
     * @param board
     * @param depth
     * @param extra
     * @param alpha
     * @param beta
     * @param ply
     * @param isCheck
     * @param moveGenerators
     * @return
     */
    private int negaMax(Board board, int depth, int extra, int alpha, int beta, int ply, boolean isCheck, MoveGenerator[] moveGenerators) {

        // nodes counter
        _nodesVisited.getAndIncrement();

        // search depth counter
        updateSearchDepthCounter(ply, extra);

        _pv[ply].size = 0;

        // Check the repetition table and fifty move rule
        // FIXME: Repetition
        if (board.hasInsufficientMaterial() || board.halfmoveClock >= 100) { // board.isRepetition() ||
            if (!_config.PERF_TEST) { // influences the counting of nodes according to PERF tests
                // nodes counter - this is a board evaluation
                _boardsEvaluated.getAndIncrement();
                return Value.DRAW;
            }
        }

        // Board Evaluation
        if (depth <= 0) {
            final int value = evaluate(board, ply, isCheck, moveGenerators);
            return value;
        }

        // ## BEGIN Mate Distance Pruning
        if (_config._USE_MDP && !_config.PERF_TEST) {
            int value = -Value.CHECKMATE + ply;
            if (value > alpha) {
                alpha = value;
                if (value >= beta) return value;
            }
            value = -(-Value.CHECKMATE + ply + 1);
            if (value < beta) {
                beta = value;
                if (value <= alpha) return value;
            }
        }
        // ## ENDOF Mate Distance Pruning

        // START Transposition Table Lookup
        // if cache is enabled - influences PERF counter test
        if (_config._USE_NODE_CACHE && _cacheEnabled && !_config.PERF_TEST) {

            TranspositionTable.TranspositionTableEntry entry = this._transpositionTable.get(board.zobristKey);
            if (entry != null) { // possible cache hit

                // transpositionMove = entry.move;
                if (entry.depth >= depth) { // cache hit

                    _nodeCacheHits.getAndIncrement();

                    int value = entry.getValue(ply);
                    int type = entry.type;

                    switch (type) {
                        case Bound.LOWER:
                            if (value >= beta) return value;
                            break;
                        case Bound.UPPER:
                            if (value <= alpha) return value;
                            break;
                        case Bound.EXACT:
                            return value;
                        default:
                            assert false;
                            break;
                    }
                } else {
                    _nodeCacheMisses.getAndIncrement();
                }
            } else { // cache miss
                _nodeCacheMisses.getAndIncrement();
            }
        }
        // END Transposition Table Lookup

        // Initialize
        int bestValue = -Value.INFINITE;
        int bestMove = Move.NOMOVE;
        int hashType = Bound.UPPER;
        int searchedMoves = 0;

        // Generate available pseudo legal moves
        MoveGenerator moveGenerator = moveGenerators[ply];
        MoveList moves = moveGenerator.getPseudoLegalMoves(board, depth, isCheck);
        // Moves are pre sorted in moveGenerator

        // ### Iterate through all available moves
        for (int i = 0; i < moves.size; ++i) {
            int move = moves.entries[i].move;
            int value = bestValue;

            // ### START - Commit move and go deeper into recursion
            board.makeMove(move);
            cvAdd(ply, move);

            boolean wasLegal = false;
            // is it a legal move? Moves only contains pseudolegal moves
            if (!board.isAttacked(Bitboard.next(board.kings[Color.opposite(board.activeColor)].squares), board.activeColor)) {
                wasLegal = true;
                searchedMoves++;
                final boolean check = board.isCheck();

                int new_depth = depth - 1;
                int new_extra = extra;

                // Check for quiescent search extension
                // if board has check or last move was capture extend the search depth by 1
                if (_config._USE_QUIESCENCE && !_config.PERF_TEST && new_depth == 0) { // would lead to evaluation in next recursion
                    // was last move check or capture
                    // TODO: only extend for valuable moves
                    // TODO: Static Exchange Evaluation
                    // TODO: Delta Pruning or Futility Pruning
                    if (!isQuiet(board, check)) {
                        // board was non quite
                        new_depth++;
                        new_extra++;
                        _boardsNonQuiet.getAndIncrement();
                        if (_config.VERBOSE_ALPHABETA) {
                            String info = String.format("%2d/%2d depth:%d %2d/%2d (%d) -> ", _currentMoveNumber, _rootMoves.size, ply + 1, i + 1, moves.size, value);
                            info += String.format("quiescence extension: Current Search is %d, new extra is %d%n", _currentSearchDepth, new_extra);
                            getVerboseInfo(info);
                        }
                    }
                }

                if (_config._USE_PV && !_config.PERF_TEST && i > 0) {
                    // search with null window after the first loop
                    value = -negaMax(board, new_depth, new_extra, -alpha - 1, -alpha, ply + 1, check, moveGenerators);
                    if (_config.VERBOSE_ALPHABETA) {
                        String info = String.format("%2d/%2d depth:%d %2d/%2d (%d) PVS %s - current best: %s (%d)", _currentMoveNumber, _rootMoves.size, ply + 1, i + 1,
                                moves.size, value, _cv.toString(), Move.toString(bestMove), bestValue);
                        getVerboseInfo(info);
                    }
                    // value > beta - is checked below after undoMove

                    // alpha < value < beta - we do not have an exact alpha value, but know all nodes are <beta
                    if (value > alpha && value < beta) {
                        // full re-search to get real alpha value
                        value = -negaMax(board, new_depth, new_extra, -beta, -value, ply + 1, check, moveGenerators);
                        if (_config.VERBOSE_ALPHABETA) {
                            String info = String.format("%n%2d/%2d depth:%d %2d/%2d (%d) PVSre %s - current best: %s (%d)", _currentMoveNumber, _rootMoves.size, ply + 1,
                                    i + 1, moves.size, value, _cv.toString(), Move.toString(bestMove), bestValue);
                            getVerboseInfo(info);
                        }
                    }
                } else {
                    value = -negaMax(board, new_depth, new_extra, -beta, -alpha, ply + 1, check, moveGenerators);
                    if (_config.VERBOSE_ALPHABETA) {
                        String info = String.format("%2d/%2d depth:%d %2d/%2d (%d) %s - current best: %s (%d)", _currentMoveNumber, _rootMoves.size, ply + 1, i + 1,
                                moves.size, value, _cv.toString(), Move.toString(bestMove), bestValue);
                        getVerboseInfo(info);
                    }
                }
            }

            board.undoMove();
            cvRemove(ply);

            // Pruning - START
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;

                if (value > alpha) {
                    if (_config.VERBOSE_ALPHABETA) {
                        String info = String.format(" value (%d) >  alpha(%d) @ Depth: %d ", value, alpha, ply + 1);
                        getVerboseInfo(info);
                    }
                    alpha = value;
                    savePV(move, _pv[ply + 1], _pv[ply]);
                    hashType = Bound.EXACT;

                    if (value >= beta) {
                        if (_config.VERBOSE_ALPHABETA) {
                            String info = String.format("%n ==> CUT value (%d) >=  beta(%d) @ Depth: %d ", value, beta, ply);
                            getVerboseInfo(info);
                        }
                        // Extremly influences PERF counter test
                        if (_config._USE_PRUNING && !_config.PERF_TEST) {
                            // Cut-off
                            hashType = Bound.LOWER;
                            break;
                        }
                    }

                }
            }
            // Pruning - END

            if (_config.VERBOSE_ALPHABETA) {
                if (wasLegal) printInfoln();
            }

            // Check if game has been stopped or the hard time limit has been reached
            // and return the best move so far
            checkStopConditions(extra > 0);
            if (_abort) {
                return bestValue;
            }

        } // ### Iterate through all available moves

        // If we cannot move, check for checkmate and stalemate.
        if (searchedMoves == 0) {
            if (isCheck) {
                // We have a check mate. Return a -CHECKMATE.
                hashType = Bound.EXACT;
                bestValue = -Value.CHECKMATE + ply;
            } else {
                // We have a stalemate. Return the draw value.
                hashType = Bound.EXACT;
                bestValue = Value.DRAW;
            }
        }

        // Update Transposition Table
        if (_config._USE_NODE_CACHE && _cacheEnabled && !_config.PERF_TEST) {
            _transpositionTable.put(board.zobristKey, depth, bestValue, hashType, bestMove, ply);
        }

        return bestValue;
    }

    /**
     * This is called regularly during the AlphaBeta recursion to manage
     * time and other abortion criteria like hardTimeLimit, softTimeLimit,
     * single moves.
     * TODO: Improve: Mates
     * @param checkHardTimeLimit
     */
    private void checkStopConditions(boolean checkHardTimeLimit) {

        if (checkHardTimeLimit && _hardTimeLimitReached) {
            _abort=true;
            return;
        }

        // game is over or has been stopped
        if (_game.isOverOrStopped()) {
            _abort = true;
            return;
        }

        // no sense to waste time
        // only reason would be to get a good value
        if (_rootMoves.size == 1) {
            _abort = true;
            return;
        }

        // if pondering don't care about time - return
        if (_isPondering) {
            if (_timeToPonder) {
                _abort = false;
                return;
            }
            _abort = true;
            return;
        }

        // Check if we have a checkmate to avoid further
        // iterative deepening.
        if ( Math.abs(_rootMoves.entries[0].value) >= Value.CHECKMATE_THRESHOLD
                && Math.abs(_rootMoves.entries[0].value) <= Value.CHECKMATE
                && _curSearchDepth >= (Value.CHECKMATE - Math.abs(_rootMoves.entries[0].value))) {
            _abort = true;
            return;
        }

        // We will check the stop conditions only if we are using time
        // management, that is if our timer != null.
        if (_timer != null && _doTimeManagement) {
            if (_softTimeLimitReached) {
                _abort = true;
                return;
            }
        }

    }

    /**
     * Checks if we have a quite position.
     * If we currently have a check this is not the case, as we might have a mate in the
     * next move.
     *
     * FIXME: This does not cover stalemates as the would not be recognized
     * TODO: Improve isQuiet()
     *
     * @param board
     * @param check
     * @return
     */
    private static boolean isQuiet(Board board, final boolean check) {

        // check?
        // only way to determine mates is to check one ply deeper
        if (check) { return false; }

        // Possible improvements
        // Stale mate Check
        // Winning Capture (weaker Piece captures stronger piece)

        // Any Capture
        // if (Move.getTargetPiece(board.moveHistory.lastMove()) != Piece.NOPIECE) {
        // return false;
        // };

        return true;
    }

    /**
     * Evaluation is always done from the view of the next player as positive
     * number
     *
     * @param board
     * @param ply
     * @param moveGenerators
     * @return value of the given board from the next player's perspective
     */
    private int evaluate(Board board, int ply, boolean isCheck, MoveGenerator[] moveGenerators) {

        _boardsEvaluated.getAndIncrement();

        // Check the evaluation table
        if (_config._USE_BOARD_CACHE && _cacheEnabled && !_config.PERF_TEST) {
            EvaluationTable.EvaluationTableEntry entry = this._evaluationTable.get(board.zobristKey);
            if (entry != null) {
                _boardsCacheHit.getAndIncrement();
                // System.out.println("Times: Retrieve="+(System.nanoTime() - start1));
                return entry.evaluation;
            }
            _boardsCacheMiss.getAndIncrement();
        }

        // Check for game paused
        _game.waitWhileGamePaused();

        MoveList moves = null;

        // When not using quiescence extension checks it is necessary to check for moves
        // here otherwise we will overlook checkmates and stalemates at the last depth
        if (!_config._USE_QUIESCENCE) {
            // check for check, mate and stalemate
            MoveGenerator moveGenerator = moveGenerators[ply];
            moves = moveGenerator.getLegalMoves(board, 1, isCheck);
            // If we cannot move, check for checkmate and stalemate.
            if (moves.size == 0) {
                if (isCheck) { // checkmate
                    return -(Value.CHECKMATE - ply);
                }
                // stalemate
                return Value.DRAW;
            }
        }

        int value = _evaluation.evaluate(board);

        if (moves != null && moves.size == 1) { // Zugzwang
            value -= 200;
        }

        // Store the result and return
        if (_config._USE_BOARD_CACHE && _cacheEnabled && !_config.PERF_TEST) {
            this._evaluationTable.put(board.zobristKey, value);
        }

        return value;
    }

    /**
     * Signals the ponderer to start pondering. This thread releases the
     * semaphore that prevents thinking and pondering to interfere with each other.
     */
    private void startPondering() {

        _statusInfo = "Engine pondering... ";

        // Signals the ponderer to start pondering
        _timeToPonder=true;

        // allow pondering again
        _ponderSemaphore.release();

        // start ponderer
        _ponderer.startPondering();
    }

    /**
     * Signals the ponderer to stop pondering. This thread wait until
     * it aqcuires the semaphore that prevents thinking and pondering
     * to interfere with each other.
     */
    private void stopPondering() {

        _statusInfo = "Engine waiting";

        // Signals the ponderer to stop pondering
        _timeToPonder=false;

        _ponderer.stopPondering();

        // wait until ponderer has finished
        try {
            _ponderSemaphore.acquire();
        } catch (InterruptedException e) {
            // ignore
        }

    }

    private static void savePV(int move, MoveVariation src, MoveVariation dest) {
        dest.moves[0] = move;
        System.arraycopy(src.moves, 0, dest.moves, 1, src.size);
        dest.size = src.size + 1;
    }

    /**
     * Adds a move to the list of the moves of the current variation.
     * @param ply
     * @param move
     */
    private void cvAdd(int ply, int move) {
        _cv.moves[ply] = move;
        _cv.size++;
    }

    /**
     * Removes a move from the list of the moves of the current variation.
     * @param ply
     */
    private void cvRemove(int ply) {
        _cv.moves[ply] = Move.NOMOVE;
        _cv.size--;
    }

    /**
     * Helps to keep the UI informed about the current search depth.
     * @param ply
     * @param extra
     */
    private void updateSearchDepthCounter(int ply, int extra) {
        if (ply - extra > _curSearchDepth) {
            _curSearchDepth = ply - extra;
        }
        if (extra + _curSearchDepth > _currrentExtraSearchDepth) {
            _currrentExtraSearchDepth = extra + _curSearchDepth;
        }
    }

    /**
     * Called to update the current search depth for the player.
     * This makes sure we can change the maximum search depth of a player
     * from the UI during the search.
     *
     * @return current search depth
     */
    private int updateSearchDepth() {
        int maxDepth;
        if (_player.getColor().isBlack()) {
            maxDepth = Chessly.getPlayroom().getCurrentEngineLevelBlack();
        } else if (_player.getColor().isWhite()) {
            maxDepth = Chessly.getPlayroom().getCurrentEngineLevelWhite();
        } else
            throw new RuntimeException("Invalid next player color. Was " + _player.getColor());
        return maxDepth;
    }

    /**
     * Approximates the time available for the next move.
     */
    private long approxTime(Board board) {

        // reset flags
        _approxTime = 0;
        _softTimeLimitReached = false;
        _hardTimeLimitReached = false;

        long timeLeft;
        long totalTime;
        long timeUsed;
        long timeThisMove;

        if (_activeColor.isBlack()) {
            totalTime = _game.getBlackTime();
            timeUsed = _game.getBlackClock().getTime();
            timeLeft = totalTime - timeUsed;
        } else {
            totalTime = _game.getWhiteTime();
            timeUsed = _game.getWhiteClock().getTime();
            timeLeft = totalTime - timeUsed;
        }

        // Give some overhead time so that in games with very low available time we do not run out of time
        timeLeft -= 1000; // this should do

        // First 30 moves in 50% of the time
        // next 25 moves in 30% of the time
        // next 25 moves in 15% of the time
        // last moves 5% of the time.
        final int moveNumber = board.getFullmoveNumber(); // (int) Math.ceil(nextHalfMoveNumber/2.0);
        if (moveNumber <= 30) {
            long segmentTime = (long) (totalTime * 0.50);
            long timePerMove = segmentTime / 30;
            long overusage = moveNumber > 1 ? timeUsed - (moveNumber * timePerMove) : 0; // compensate for first move
            long correction = -(overusage / moveNumber);
            timeThisMove = timePerMove + correction;

            /*
             * System.out.format("ApproxTime(%d) ===========================%n"
             * + "Used Time: %d Segment -30 time: %d Time per Move: %d Overusage: %d Correction: %d Time for Move %d%n",
             * moveNumber,
             * timeUsed,
             * segmentTime,
             * timePerMove,
             * overusage,
             * correction,
             * timeThisMove
             * );
             */

        } else if (moveNumber > 30 && moveNumber <= 55) {
            long segmentTime = (long) (totalTime * 0.30);
            long timeUsedThisSegment = (long) (timeUsed - (totalTime * 0.50));
            long timePerMove = segmentTime / 25;
            long overusage = timeUsedThisSegment - ((moveNumber - 30) * timePerMove);
            long correction = -(overusage / moveNumber);
            timeThisMove = timePerMove + correction;

            /*
             * System.out.format("ApproxTime(%d) ===========================%n"
             * + "Used Time: %d Time Used this Segment: %d Segment 41-55 time: %d Time per Move: %d Overusage: %d Correction: %d Time for Move %d%n",
             * moveNumber,
             * timeUsed,
             * timeUsedThisSegment,
             * segmentTime,
             * timePerMove,
             * overusage,
             * correction,
             * timeThisMove
             * );
             */

        } else if (moveNumber > 55 && moveNumber <= 80) {
            long segmentTime = (long) (totalTime * 0.15);
            long timeUsedThisSegment = (long) (timeUsed - (totalTime * 0.80));
            long timePerMove = segmentTime / 25;
            long overusage = timeUsedThisSegment - ((moveNumber - 55) * timePerMove);
            long correction = -(overusage / moveNumber);
            timeThisMove = timePerMove + correction;

            /*
             * System.out.format("ApproxTime(%d) ===========================%n"
             * + "Used Time: %d Time Used this Segment: %d Segment 55-80 time: %d Time per Move: %d Overusage: %d Correction: %d Time for Move %d%n",
             * moveNumber,
             * timeUsed,
             * timeUsedThisSegment,
             * segmentTime,
             * timePerMove,
             * overusage,
             * correction,
             * timeThisMove
             * );
             */

        } else {
            timeThisMove = timeLeft / 25;
        }
        return Math.max(timeThisMove, 0);
    }

    /**
     * Configure and start time keepers
     * @param approxTime
     * FIXME: Bug - TimeKeeper does not Pause when game paused!
     */
    private void configureTimeControl(long approxTime) {
        // standard limits
        float soft = 0.9f;
        float hard = 1.1f;
        // limits for very short available time
        if (approxTime < 100) {
            soft = 0.8f;
            hard = 0.9f;
        }
        // limits for higher available time
        if (approxTime > 1000) {
            soft = 1.0f;
            hard = 1.2f;
        }
        _timer = new Timer("TimeKeeper " + _activeColor.toString() + " " + " ApproxTime: " + approxTime + " Soft:" + soft + " Hard:" + hard);
        _timer.schedule(new TimeKeeper(1), (long) (approxTime * soft));
        _timer.schedule(new TimeKeeper(2), (long) (approxTime * hard));
    }

    /**
     * Resets the counter used for the TreeSearchEngineWatcher (UI)
     */
    private void resetCounters() {
        // -- reset counters --
        _boardsEvaluated.set(0);
        _nodesVisited.set(0);
        _boardsNonQuiet.set(0);
        _curSearchDepth = 0;
        _currrentExtraSearchDepth = 0;
        _lastNodesPerSecond = 0;
        _lastUsedTime = 0;
        _nodeCacheHits.set(0);
        _nodeCacheMisses.set(0);
        _boardsCacheHit.set(0);
        _boardsCacheMiss.set(0);
    }

    /**
     * Prepare a MoveGenerator and MoveVariation for each depth to avoid creating objects
     * during search (MoveGenerator contains a MoveList)
     */
    private void prepareSearch() {
        for (int i = 0; i < Depth.MAX_PLY; ++i) {
            _searchMoveGenerators[i] = new MoveGenerator();
        }
        for (int i = 0; i < _pv.length; ++i) {
            _pv[i] = new MoveVariation();
        }
    }

    /**
     * Convert an engine move to a GameMove
     * @param move
     * @return converted Move as GameMove
     */
    private static GameMove convertMove(int move) {
        if (move == Move.NOMOVE) return null;

        int origin = Move.getOriginSquare(move);
        int target = Move.getTargetSquare(move);
        int oPiece = Move.getOriginPiece(move);
        int tPiece = Move.getTargetPiece(move);

        GamePosition _fromField = GamePosition.getGamePosition(Square.getFile(origin) + 1, Square.getRank(origin) + 1);
        GamePosition _toField = GamePosition.getGamePosition(Square.getFile(target) + 1, Square.getRank(target) + 1);
        GamePiece _pieceMoved = convertPieceToGamePiece(oPiece);

        GameMove gameMove = new GameMoveImpl(_fromField, _toField, _pieceMoved);

        int type = Move.getType(move);

        switch (type) {
            case MoveType.NORMAL:
                break;

            case MoveType.CAPTURE:
                gameMove.setCapturedPiece(convertPieceToGamePiece(tPiece));
                break;

            case MoveType.PAWNDOUBLE:
                break;

            case MoveType.PAWNPROMOTION:
                int promotionPiece = Move.getPromotion(move);
                int promotionType = Move.getType(promotionPiece);
                assert (PieceType.isValidPromotion(promotionType));
                GameColor color = Piece.getColor(oPiece) == Color.WHITE ? GameColor.WHITE : GameColor.BLACK;
                GamePiece pp;
                switch (promotionType) {
                    case PieceType.QUEEN:
                        pp = Queen.create(color);
                        break;
                    case PieceType.ROOK:
                        pp = Rook.create(color);
                        break;
                    case PieceType.BISHOP:
                        pp = Bishop.create(color);
                        break;
                    case PieceType.KNIGHT:
                        pp = Knight.create(color);
                        break;
                    default:
                        throw new RuntimeException("Invalid PieceType for Promotion during convert from Move to GameMove");
                }
                gameMove.setPromotedTo(pp);
                break;

            case MoveType.ENPASSANT:
                gameMove.setCapturedPiece(convertPieceToGamePiece(tPiece));
                break;

            case MoveType.CASTLING:
                if (oPiece == Piece.WHITE_KING) {
                    if (Square.getFile(target) == 7) {
                        // king side
                        gameMove.setCastlingType(GameCastling.WHITE_KINGSIDE);
                    } else if (Square.getFile(target) == 3) {
                        // queen side
                        gameMove.setCastlingType(GameCastling.WHITE_QUEENSIDE);
                    }
                } else if (oPiece == Piece.BLACK_KING) {
                    if (Square.getFile(target) == 7) {
                        // king side
                        gameMove.setCastlingType(GameCastling.BLACK_KINGSIDE);
                    } else if (Square.getFile(target) == 3) {
                        // queen side
                        gameMove.setCastlingType(GameCastling.BLACK_QUEENSIDE);
                    }
                }
                break;

            default:
                break;
        }

        return gameMove;
    }

    /**
     * @param oPiece
     * @return
     */
    private static GamePiece convertPieceToGamePiece(int oPiece) {
        GamePiece _convertedPiece;
        switch (oPiece) {
            case Piece.WHITE_PAWN:
                _convertedPiece = Pawn.create(GameColor.WHITE);
                break;
            case Piece.WHITE_KNIGHT:
                _convertedPiece = Knight.create(GameColor.WHITE);
                break;
            case Piece.WHITE_BISHOP:
                _convertedPiece = Bishop.create(GameColor.WHITE);
                break;
            case Piece.WHITE_ROOK:
                _convertedPiece = Rook.create(GameColor.WHITE);
                break;
            case Piece.WHITE_QUEEN:
                _convertedPiece = Queen.create(GameColor.WHITE);
                break;
            case Piece.WHITE_KING:
                _convertedPiece = King.create(GameColor.WHITE);
                break;
            case Piece.BLACK_PAWN:
                _convertedPiece = Pawn.create(GameColor.BLACK);
                break;
            case Piece.BLACK_KNIGHT:
                _convertedPiece = Knight.create(GameColor.BLACK);
                break;
            case Piece.BLACK_BISHOP:
                _convertedPiece = Bishop.create(GameColor.BLACK);
                break;
            case Piece.BLACK_ROOK:
                _convertedPiece = Rook.create(GameColor.BLACK);
                break;
            case Piece.BLACK_QUEEN:
                _convertedPiece = Queen.create(GameColor.BLACK);
                break;
            case Piece.BLACK_KING:
                _convertedPiece = King.create(GameColor.BLACK);
                break;
            case Piece.NOPIECE:
            default:
                throw new RuntimeException("Can't convert from Move to GameMove. Invalid oPiece");
        }
        return _convertedPiece;
    }

    /**
     * Initialize the transposition table so that we do not need to create
     * new objects in the recursion.
     */
    private void initializeCacheTables() {
        if (_config._USE_NODE_CACHE) {
            int numberOfEntries = parseInt(Chessly.getProperties().getProperty("engine.nodesCacheSize", "4")) * 1024 * 1024 / TranspositionTable.ENTRYSIZE;
            _transpositionTable = new TranspositionTable(numberOfEntries);
        }
        if (_config._USE_BOARD_CACHE) {
            int numberOfEntries = parseInt(Chessly.getProperties().getProperty("engine.boardsCacheSize", "2")) * 1024 * 1024 / EvaluationTable.ENTRYSIZE;
            _evaluationTable = new EvaluationTable(numberOfEntries);
        }
        Runtime.getRuntime().gc();
    }

    /**
     * Prints the current Principle Variation in human readable form.
     * @param convertedBoard
     * @return
     */
    @SuppressWarnings("unused")
    private String printPV(Board convertedBoard) {
        String s = "";
        int mn = convertedBoard.halfmoveNumber;
        MoveVariation mv = _pv[0];
        for (int i = 0; i < mv.size; ++i) {
            if (i == 0 || (mn + i) % 2 == 0) {
                s += (int) (Math.ceil((mn + i) / 2)) + ". ";
            }
            s += Move.toString(mv.moves[i]) + " ";
        }
        s += "(" + _rootMoves.entries[0].value + ")";
        return s;
    }

    /**
     * Provide additional information for the UI to collect.
     * E.g. verbose information etc.
     * Size is limit to avoid out of memory.
     * @param info
     */
    @Override
    public void getVerboseInfo(String info) {
        synchronized (_engineInfoText) {
            _engineInfoText.append(info);
            // out of memory protection if the info is not retrieved
            int oversize = _engineInfoText.length() - _engineInfoTextMaxSize;
            if (oversize > 0) _engineInfoText.delete(0, oversize);
        }
        if (_config.VERBOSE_TO_SYSOUT) System.out.print(info);
    }

    private void printInfoln() {
        getVerboseInfo(String.format("%n"));
    }

    /**
     * The UI can collect additional text to display. E.g. verboae output.
     * The info will be deleted after collection.
     * The info buffer is limited and older entries will be deleted every time
     * new info is added an the maximum size is exceeded.
     */
    @Override
    public String getInfoText() {
        String s;
        synchronized (_engineInfoText) {
            s = _engineInfoText.toString();
            _engineInfoText.setLength(0);
        }
        return s;
    }


    /**
     * Not used in this engine.
     * @see fko.chessly.player.computer.Engine#setNumberOfThreads(int)
     */
    @Override
    public void setNumberOfThreads(int n) {
        // ignore
    }

    /**
     * @return PlayerStatusController of the durrent player
     */
    public PlayerStatusController getPlayerStatus() {
        return _player.getPlayerStatus();
    }

    // EngineWatcher ----------------------------------------

    @Override
    public int getNumberOfMoves() {
        return _rootMoves.size;
    }

    @Override
    public int getCurMoveNumber() {
        return _currentMoveNumber;
    }

    @Override
    public GameMove getCurMove() {
        return convertMove(_currentMove);
    }

    @Override
    public GameMove getMaxValueMove() {
        GameMove m = convertMove(_currentBestRootMove.move);
        if (m != null) {
            m.setValue(_currentBestRootMove.value);
        }
        return m;
    }

    @Override
    public List<GameMove> getPV() {
        int size = _pv[0] != null ? _pv[0].size : 0;
        List<GameMove> l = new ArrayList<GameMove>(size);
        if (size == 0) return l;
        for (int i = 0; i < size; i++) {
            if (_pv[0] != null) {
                l.add(convertMove(_pv[0].moves[i]));
            }
        }
        return l;
    }

    @Override
    public int getCurSearchDepth() {
        return _curSearchDepth;
    }

    @Override
    public int getCurExtraSearchDepth() {
        return _currrentExtraSearchDepth;
    }

    @Override
    public long getNodesChecked() {
        return _nodesVisited.get();
    }

    @Override
    public int getCurNodesPerSecond() {
        if (_player.isThinking()) {
            _lastNodesPerSecond = (int) (1000.0F * (_nodesVisited.get() / (float) getCurUsedTime()));
            return _lastNodesPerSecond;
        }
        return _lastNodesPerSecond;
    }

    @Override
    public long getCurUsedTime() {
        if (_player.isThinking()) {
            _lastUsedTime = System.currentTimeMillis() - _startTime;
            return _lastUsedTime;
        }
        return _lastUsedTime;
    }

    @Override
    public long getBoardsChecked() {
        return _boardsEvaluated.get();
    }

    @Override
    public long getBoardsNonQuiet() {
        return _boardsNonQuiet.get();
    }

    @Override
    public long getNodeCacheHits() {
        return _nodeCacheHits.get();
    }

    @Override
    public long getNodeCacheMisses() {
        return _nodeCacheMisses.get();
    }

    @Override
    public int getCurNodeCacheSize() {
        return _cacheEnabled && _config._USE_NODE_CACHE ? _transpositionTable.getSize() : 0;
    }

    @Override
    public int getCurNodesInCache() {
        return _cacheEnabled && _config._USE_NODE_CACHE ? _transpositionTable.getNumberOfEntries() : 0;
    }

    @Override
    public long getBoardCacheHits() {
        return _boardsCacheHit.get();
    }

    @Override
    public long getBoardCacheMisses() {
        return _boardsCacheMiss.get();
    }

    @Override
    public int getCurBoardCacheSize() {
        return _cacheEnabled && _config._USE_BOARD_CACHE ? _evaluationTable.getSize() : 0;
    }

    @Override
    public int getCurBoardsInCache() {
        return _cacheEnabled && _config._USE_BOARD_CACHE ? _evaluationTable.getNumberOfEntries() : 0;
    }

    @Override
    public int getCurNumberOfThreads() {
        return 1;
    }

    @Override
    public String getCurConfig() {
        String s = "";
        if (_config._USE_PRUNING) {
            s += "P,";
        }
        if (_config._USE_MDP) {
            s += "MDP,";
        }
        if (_config._USE_PV) {
            s += "PV,";
        }
        if (_config._USE_QUIESCENCE) {
            s += "Q,";
        }
        if (_config._USE_NODE_CACHE && _cacheEnabled) {
            s += "NC,";
        }
        if (_config._USE_BOARD_CACHE && _cacheEnabled) {
            s += "BC,";
        }
        if (_config._USE_BOOK) {
            s += "OB,";
        }
        if (_config.PERF_TEST) {
            s = "PERF TEST";
        }
        return s;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getStatus()
     */
    @Override
    public String getStatusText() {
        return _statusInfo;
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getState()
     */
    @Override
    public int getState() {
        return _status.get();
    }

    /* (non-Javadoc)
     * @see fko.chessly.player.computer.ObservableEngine#getPonderMove()
     */
    @Override
    public GameMove getPonderMove() {
        return _ponderMove;
    }

    /**
     * This TimeKeeper class is used to implement a Timer that calls this Timekeeper when a time limit has been
     * reached. This TimeKeeper then sets the time limit reached flags.
     * FIXME: Does not pause when game is paused
     */
    private class TimeKeeper extends TimerTask {
        private final int _mode;

        private TimeKeeper(int mode) {
            _mode = mode;
        }

        @Override
        public void run() {
            switch (_mode) {
                case 1:
                    _softTimeLimitReached = true;
                    break;
                case 2:
                    _hardTimeLimitReached = true;
                    break;
                default:
                    throw new RuntimeException("TimeKeeper mode not set.");
            }
        }
    }

    /**
     * Will use the time while player is waiting for an opponent to move.
     * It searches using the oppenent's the most promising move from the Principle Variation.
     * It will basically fill the TransistionTable.
     * @author Frank
     */
    private class Ponderer implements Runnable {

        // Thread for the ponderer
        private Thread _pondererThread = null;
        private final Object _threadLock = new Object();

        // nmae for thread
        private String _name = "";

        Ponderer(String name) {
            this._name = name;
        }

        /*
         * Starts the ponderer in a new thread.
         */
        void startPondering() {
            synchronized(_threadLock) {
                if (_pondererThread == null) {
                    _pondererThread = new Thread(this, "Ponderer" +_name);
                    _pondererThread.setPriority(Thread.MIN_PRIORITY);
                    _pondererThread.setDaemon(true);
                    _pondererThread.start();
                }

            }
        }

        /**
         * Stop the player by interrupting the players thread and setting its status to Player.STOPPED
         */
        void stopPondering() {
            synchronized(_threadLock) {
                if (_pondererThread != null) {
                    _pondererThread.interrupt();
                }
            }
            try {
                if (_pondererThread != null)
                    _pondererThread.join();
            } catch (InterruptedException e) {
                // ignore
            }
            _pondererThread = null;
        }

        /**
         * The ponderers run method - makes sure that thinking and pondering
         * are well seperated by signal booleans and a semaphore.
         */
        @Override
        public void run() {

            if (Thread.currentThread() != _pondererThread) {
                throw new UnsupportedOperationException("Direct call of run() is not supported.");
            }

            if (_config.VERBOSE_PONDERER) {
                //String info = String.format("Start pondering %s.%n", _player.getName());
                //printInfo(info);
            }

            // wait for semaphore
            try {
                _ponderSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            _isPondering = true;

            doPondering();

            _isPondering = false;

            // release semaphore
            _ponderSemaphore.release();

            if (_config.VERBOSE_PONDERER) {
                //String info = String.format("Stop pondering %s.%n", _player.getName());
                //printInfo(info);
            }
        }

        /**
         * This method starts the actual pondering. It deactivates time management
         * and uses the best opponents move from the last Principal Variation.
         * It currently only fills the caches (node cache, board cache).
         * TODO: Improve this
         */
        private void doPondering() {

            // ponderer doesn't care about time
            // will be reset to correct value in getNextMove()
            _doTimeManagement = false;

            // the most likely white move from the last PV

            if (_pv[0] != null && _pv[0].size > 1 && _pv[0].moves[1] != Move.NOMOVE) {

                int ponderMove = _pv[0].moves[1];

                _ponderMove = convertMove(ponderMove);
                _status.set(ObservableEngine.PONDERING);
                _statusInfo = "Engine pondering on move " + Move.toString(ponderMove);
                // notify ui
                setChanged();
                notifyObservers(new PlayerDependendModelEvent("ENGINE start pondering", _player, SIG_ENGINE_START_PONDERING));
                if (_config.VERBOSE_PONDERER) {
                    String info = String.format("Pondering over move %s%n",Move.toString(ponderMove));
                    getVerboseInfo(info);
                }

                // Guess the opponents move from last PV
                _ponderBoard.makeMove(ponderMove);
                //Search
                prepareSearch();
                // we don't care about the return value (yet) - just filling up the node cache
                iterativeSearch(_ponderBoard);

                // notify ui
                setChanged();
                notifyObservers(new PlayerDependendModelEvent("ENGINE stopped pondering", _player, SIG_ENGINE_FINISHED_PONDERING));
                _status.set(ObservableEngine.IDLE);
                _ponderMove = null;

            } else {
                _statusInfo = "Engine waiting.";
                // this should be redundant
                _status.set(ObservableEngine.IDLE);
                _ponderMove = null;
                // notify ui
                setChanged();
                notifyObservers(new PlayerDependendModelEvent("ENGINE "+_activeColor+ " nothing to ponder - waiting", _player, SIG_ENGINE_NO_PONDERING));
                if (_config.VERBOSE_PONDERER) {
                    String info = String.format("Nothing to ponder%n");
                    getVerboseInfo(info);
                }
            }
        }
    }

    /** */
    public static final int SIG_ENGINE_START_CALCULATING = 6000;
    /** */
    public static final int SIG_ENGINE_FINISHED_CALCULATING = 6010;
    /** */
    public static final int SIG_ENGINE_START_PONDERING = 6020;
    /** */
    public static final int SIG_ENGINE_FINISHED_PONDERING = 6030;
    /** */
    public static final int SIG_ENGINE_NO_PONDERING = 6040;
}