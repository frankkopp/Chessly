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

import static java.lang.Integer.parseInt;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.player.computer.Omega.OmegaTranspositionTable.TT_Entry;
import fko.chessly.player.computer.Omega.OmegaTranspositionTable.TT_EntryType;
import fko.chessly.util.ChesslyLogger;

/**
 * This is the actual search implementation class for the Omega Engine.<br/>
 * It runs in a separate thread and needs to be started through <code>startSearch(position)</code>().
 * If stopped with stop() it stops gracefully returning the best move at that time.
 *
 * Features:
 *      DONE: Thread control
 *      DONE: Move Generation
 *      DONE: Book (in the engine class)
 *      DONE: Basic iterative MiniMax search
 *      DONE: Basic Evaluation
 *      DONE: DRAW 50-moves rule / repetition rule / insufficient material
 *      DONE: Basic Time Control
 *      DONE: Engine Watcher
 *      DONE: Pondering
 *      DONE: Evaluation Table
 *      DONE: Basic Transposition Table
 *      DONE: Iterative AlphaBeta search
 *      DONE: Pruning: AlphaBeta_Pruning
 *      DONE: AlphaBeta Transposition Table
 *      DONE: Simple Quiescence
 *      DONE: reuse move list
 *      DONE: reuse MoveGenerator
 *      DONE: use on demand MoveGen - is slower - not used
 *      TODO: Pruning: PV, Minor Promotion Pruning
 *      TODO: EXTENSIONS: Search Extension for active positions - extra value as fraction of depth
 *      TODO: EXTENSIONS: SingleReplyExtension, RecaptureExtension, CheckExtension, Pawn Extension, MateThreatExtension
 *      TODO: SEE
 *      TODO: Advanced Quiescence
 *      TODO: Advanced Evaluation
 *      TODO: Advanced Time Control
 *      TODO: PRUNING: AspirationWindows, NullMove, Futility, LateMove, Delta, MinorPromotion, See
 *      TODO: KillerTable, HistoryTable, PawnTable
 *      TODO: Extend UI for Time Per Move
 */
public class OmegaSearch implements Runnable {

    private static final int MAX_SEARCH_DEPTH = 99;

    private Logger _log = ChesslyLogger.getLogger();

    // back reference to the engine
    private OmegaEngine _omegaEngine;

    // field for current position
    private OmegaBoardPosition _currentPosition;

    // Move Generators - each depth in search gets it own to avoid object creation during search
    private final OmegaMoveGenerator[] _omegaMoveGenerator = new OmegaMoveGenerator[MAX_SEARCH_DEPTH];

    // Position Evaluator
    private final OmegaEvaluation _omegaEvaluation;

    // the thread in which we will do the actual search
    Thread _searchThread = null;

    // used to wait for move from search
    private CountDownLatch _waitForInitializaitonLatch = new CountDownLatch(1);

    // flag to indicate if engine has been configured sine the last start
    private boolean _isConfigured = false;

    // flag to indicate to stop the search - can be called externally or via the timer clock.
    private boolean _stopSearch = true;

    // flag to indicate that the search is currently pondering
    private boolean _isPondering = false;

    /*
     * Search configuration (with defaults)
     *
     * If remaining time is set to >0 then time per move is ignored.
     * If time per move is set then level is set to max.
     * if neither remaining time nor time per move is set then we use level only.
     */
    private TimeControlMode _timedControlMode = TimeControlMode.TIME_PER_MOVE;
    private Duration _remainingTime = Duration.ofSeconds(0);
    private Duration _timePerMove = Duration.ofSeconds(5);
    private int _currentEngineLevel = 0;

    private boolean _softTimeLimitReached = false;
    private boolean _hardTimeLimitReached = false;
    TimeKeeper _timer = null;

    /*
     * The following fields are package wide to allow the engine to access these fields directly.
     */

    // time control
    Instant _startTime = Instant.now();
    // remembers the time pondering started after we had a ponderhit
    Instant _ponderStartTime = Instant.now();

    // root move evaluation fields
    OmegaRootMoveList _rootMoves = new OmegaRootMoveList();

    // current variation of the search
    OmegaMoveList _currentVariation = new OmegaMoveList(MAX_SEARCH_DEPTH);
    OmegaMoveList[] _principalVariation = new OmegaMoveList[MAX_SEARCH_DEPTH];

    /* max depth for search iteration - will be set with values from the UI */
    int _maxIterativeDepth = MAX_SEARCH_DEPTH;

    // engine watcher fields - not private for easy access from engine
    int _currentBestRootMove= OmegaMove.NOMOVE; // current best move found by search
    int _currentBestRootValue = OmegaEvaluation.Value.NOVALUE; // value of the current best move
    int _currentIterationDepth = 0; // how deep will the search go in the current iteration
    int _currentSearchDepth = 0; // how deep did the search go this iteration
    int _currentExtraSearchDepth = 0; // how deep did we search including quiescence depth this iteration
    int _currentRootMove = 0; // current root move that is searched
    int _currentRootMoveNumber = 0; // number of the current root move in the list of root moves
    int _nodesVisited = 0; // how many times a node has been visited (negamax calls)
    int _boardsEvaluated = 0; // how many times a node has been visited (= boards evaluated)
    int _prunings = 0;
    int _pv_researches = 0;
    long _evalCache_Hits = 0;
    long _evalCache_Misses = 0;
    long _nodeCache_Hits = 0;
    long _nodeCache_Misses = 0;
    int  _MovesFromCache=0;
    int  _MovesGenerated=0;
    private void resetCounter() {
        _currentIterationDepth = 0;
        _currentSearchDepth = 0;
        _currentExtraSearchDepth = 0;
        _currentRootMove = 0;
        _currentRootMoveNumber = 0;
        _nodesVisited = 0;
        _boardsEvaluated = 0;
        _prunings = 0;
        _pv_researches = 0;
        _evalCache_Hits = 0;
        _evalCache_Misses = 0;
        _nodeCache_Hits = 0;
        _nodeCache_Misses = 0;
        _MovesFromCache=0;
        _MovesGenerated=0;
    }

    /*
     * Caches
     */
    Boolean _cacheEnabled;
    OmegaEvaluationCache _evalCache;
    OmegaTranspositionTable _transpositionTable;


    /**
     * Creates a search object and stores a back reference to the engine object.<br/>
     * Before using the search you need to configure it through <code>configure(...)</code><br/>
     *
     * @param omegaEngine
     */
    public OmegaSearch(OmegaEngine omegaEngine) {
        _omegaEngine = omegaEngine;

        _log.setLevel(Level.OFF);

        // Move Generators - each depth in search gets it own
        // to avoid object creation during search
        for (int i=0; i<MAX_SEARCH_DEPTH; i++ ) {
            _omegaMoveGenerator[i] = new OmegaMoveGenerator();
        }

        _omegaEvaluation = new OmegaEvaluation(_omegaEngine, new OmegaMoveGenerator());

        // cache setup
        _cacheEnabled = Boolean.valueOf(Chessly.getProperties().getProperty("engine.cacheEnabled"));
        if (_cacheEnabled) {
            initializeCacheTables(); // create a cache
        }
    }

    /**
     * Setup the Search for time based game with remaining time per player.
     *
     * @param remainingTime in sec
     * @param maxDepth
     */
    public void configureRemainingTime(long remainingTime, int maxDepth) {
        _timedControlMode = TimeControlMode.REMAINING_TIME;
        _remainingTime = Duration.ofSeconds(remainingTime);
        updateSearchDepth();
        _isConfigured = true;
    }

    /**
     * Setup the Search for time based game with time per move per player.
     * @param time time for white in seconds
     */
    public void configureTimePerMove(long time) {
        _timedControlMode = TimeControlMode.TIME_PER_MOVE;
        _timePerMove = Duration.ofSeconds(time);
        _currentEngineLevel = MAX_SEARCH_DEPTH;
        _isConfigured = true;
    }

    /**
     * Setup the Search for depth based level
     * @param currentEngineLevel
     */
    public void configureMaxDepth(int currentEngineLevel) {
        _timedControlMode = TimeControlMode.NO_TIMECONTROL;
        _remainingTime = Duration.ofSeconds(0);
        _currentEngineLevel = currentEngineLevel;
        _isConfigured = true;
    }

    /**
     * Setup the Search for pondering
     */
    public void configurePondering() {
        _timedControlMode = TimeControlMode.PONDERING;
        _currentEngineLevel = MAX_SEARCH_DEPTH;
        _isConfigured = true;
    }

    /**
     * Signals the search to continue after a ponder hit.
     * It is important to configure the search before this call!
     */
    public void ponderHit() {

        // remember the start of the search
        _startTime = Instant.now();

        // setup Time Control
        setupTimeControl();

    }

    /**
     * Start the search in a separate thread.<br/>
     * Calls <code>_omegaEngine.storeResult(searchResult);</code> to
     * store the result is it has found one. After storing the result
     * the search is ended and the thread terminated.<br/>
     * The search will stop when it has reach the configured conditions. Either
     * reached a certain depth oder used up the time or found a move.<br/>
     * The search also can be stopped by calling stop at any time. The
     * search will stop gracefully by storing the best move so far via
     * <code>_omegaEngine.storeResult(searchResult);</code>.
     *
     * @param position
     */
    public void startSearch(OmegaBoardPosition position) {
        assert position != null : "omegaBoard must not be null";

        if (_searchThread!=null)
            throw new IllegalStateException("OmegaSearch already running - can only be started once");

        // make copy of the board to avoid concurrent access
        _currentPosition = new OmegaBoardPosition(position);

        // has OmegaSearch.configure been called?
        if (!_isConfigured) {
            System.err.println("Search started without configuration - using defaults");
        }

        // setup latch
        _waitForInitializaitonLatch = new CountDownLatch(1);

        // reset the stop search flag
        _stopSearch = false;

        // create new search thread
        String threadName = "OmegaEngine: "+position._nextPlayer.toString();
        if (_timedControlMode == TimeControlMode.PONDERING) threadName += " (Pondering)";
        _searchThread = new Thread(this, threadName);
        _searchThread.setDaemon(true);

        // start the search thread
        this._searchThread.start();

        // Wait for initialization in run() before returning from call
        try { _waitForInitializaitonLatch.await();
        } catch (InterruptedException e) {/* empty*/}

    }

    /**
     * Stops a current search. If no search is running it does nothing.<br/>
     * The search will stop gracefully by storing the best move so far via
     * <code>_omegaEngine.storeResult(searchResult);</code>.
     */
    public void stop() {
        // set stop flag - search needs to check regularly and stop accordingly
        _stopSearch = true;

        // return if no search is running
        if (_searchThread == null) return;

        // Wait for the thread to die
        try { this._searchThread.join();
        } catch (InterruptedException e) { /* empty*/ }

        // clear thread
        _searchThread=null;
    }

    /**
     * The start of the actual search after the Thread has been started.</br>
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        //System.out.print("Run()...");

        if (Thread.currentThread() != _searchThread)
            throw new java.lang.UnsupportedOperationException("run() cannot be called directly!");

        // reset counter
        resetCounter();

        // release latch so the caller can continue
        _waitForInitializaitonLatch.countDown();

        // run the search itself
        SearchResult searchResult = iterativeSearch(_currentPosition);

        if (_omegaEngine._CONFIGURATION.VERBOSE_STATS) {
            _omegaEngine.printVerboseInfo(String.format("Evaluations in total  : %,12d ", _boardsEvaluated));
            _omegaEngine.printVerboseInfo(String.format("Duration: %9s", Duration.between(_startTime, Instant.now()).toString()));
            _omegaEngine.printVerboseInfo(String.format("\tEvaluations/sec: %,10d   ",
                    (_boardsEvaluated*1000L)/(Duration.between(_startTime,Instant.now()).toMillis()+1)));
            _omegaEngine.printVerboseInfo(String.format("\tNodes/sec: %,10d",
                    (_nodesVisited*1000L)/(Duration.between(_startTime,Instant.now()).toMillis()+1)));
            _omegaEngine.printVerboseInfo("\tMove: "+OmegaMove.toString(searchResult.bestMove)+" ("+searchResult.resultValue+")  ");
            _omegaEngine.printVerboseInfo("\tPV: "+_principalVariation[0].toNotationString()+"\n");
        }

        // send the result
        _omegaEngine.storeResult(searchResult);

        // reset configuration flag
        _isConfigured = false;

        // System.out.println("...end");

    }

    /**
     * This starts the actual iterative search.
     * @param position
     * @return the best move
     */
    private SearchResult iterativeSearch(OmegaBoardPosition position) {

        // remember the start of the search
        _startTime = Instant.now();
        _ponderStartTime = Instant.now();

        // generate all root moves
        OmegaMoveList rootMoves = _omegaMoveGenerator[0].getLegalMoves(position, false);

        //assert !rootMoves.empty() : "no legal root moves - game already ended!";
        if (rootMoves.size() == 0)
            return new SearchResult();

        // prepare principal variation lists
        for (int i=0; i< MAX_SEARCH_DEPTH; i++) {
            _principalVariation[i]= new OmegaMoveList(MAX_SEARCH_DEPTH);
        }

        // create _rootMoves list
        _rootMoves.clear();
        for (int i=0; i< rootMoves.size(); i++) {
            _rootMoves.add(rootMoves.get(i), OmegaEvaluation.Value.NOVALUE);
        }

        // temporary best move - take the first move available
        _currentBestRootMove = _rootMoves.getMove(0);
        _currentBestRootValue = OmegaEvaluation.Value.NOVALUE;

        // prepare search result
        SearchResult searchResult = new SearchResult();

        // setup Time Control
        int startIterativeDepth = setupTimeControl();

        // ### BEGIN Iterative Deepening
        int depth = startIterativeDepth;
        do {
            _currentIterationDepth = depth;

            // check for game paused
            if (_omegaEngine.getGame().isPresent())
                _omegaEngine.getGame().get().waitWhileGamePaused();

            // do search
            rootMovesSearch(position, depth);

            // check if we need to stop search - could be external or time.
            if (_stopSearch || _softTimeLimitReached || _hardTimeLimitReached) break;

        } while (++depth <= _maxIterativeDepth);
        // ### ENDOF Iterative Deepening

        if (_timer != null) {
            _timer.stop();
            _timer=null;
        }

        // we should have a sorted _rootMoves list here
        // first move is best move so far
        // create searchRestult here
        searchResult.bestMove = _currentBestRootMove;
        searchResult.resultValue = _currentBestRootValue;
        searchResult.depth = _currentIterationDepth;
        int p_move;
        if (_principalVariation[0].size()>1 && (p_move = _principalVariation[0].get(1))!=OmegaMove.NOMOVE) {
            //System.out.println("Best Move: "+OmegaMove.toString(searchResult.bestMove)+" Ponder Move: "+OmegaMove.toString(p_move)+" ("+_principalVariation[0].toNotationString()+")");
            searchResult.ponderMove = p_move;
        } else {
            searchResult.ponderMove = OmegaMove.NOMOVE;
        }

        return searchResult;
    }

    /**
     * @param position
     * @param depth
     * @param omegaBoard
     */
    private void rootMovesSearch(OmegaBoardPosition position, int depth) {

        final int rootply = 0;

        // some stats for iteration
        int boardsCounter = -_boardsEvaluated;
        Instant iterationStart = Instant.now();

        int bestValue = OmegaEvaluation.Value.NOVALUE;

        final int alpha = -OmegaEvaluation.Value.INFINITE;
        final int beta  = OmegaEvaluation.Value.INFINITE;

        // ##### Iterate through all available root moves
        for (int i = 0; i < _rootMoves.size(); i++) {
            int move = _rootMoves.getMove(i);

            // check for game paused
            if (_omegaEngine.getGame().isPresent())
                _omegaEngine.getGame().get().waitWhileGamePaused();

            // store the current move for Engine Watcher
            _currentRootMove = move;
            _currentRootMoveNumber = i+1;

            // #### START - Commit move and go deeper into recursion
            position.makeMove(move);
            _currentVariation.add(move);

            int value = OmegaEvaluation.Value.NOVALUE;
            if (OmegaConfiguration.PERFT || !_omegaEngine._CONFIGURATION._USE_PVS) {
                value = -negamax(position, depth-1, rootply+1, -beta, -alpha);
            }
            // ### START PVS ###
            else {
                if (bestValue == OmegaEvaluation.Value.NOVALUE) { // no PV yet
                    value = -negamax(position, depth-1, rootply+1, -beta, -alpha);
                } else { // we have a PV - do Null Window Search
                    value = -negamax(position, depth-1, rootply+1, -alpha-1, -alpha);
                    if (value > alpha && value < beta) { // no fail - research
                        _pv_researches++;
                        value = -negamax(position, depth-1, rootply+1, -beta, -alpha);
                    }
                }
            } // ### END PVS ###

            // write the value back to the root moves list
            _rootMoves.set(i, move, value);

            // Evaluate the calculated value and compare to current best move
            if (value > bestValue && value != -OmegaEvaluation.Value.NOVALUE) {
                bestValue = value;
                _currentBestRootValue = value;
                _currentBestRootMove = move;
                OmegaMoveList.savePV(move,  _principalVariation[rootply+1], _principalVariation[rootply]);
            }

            position.undoMove();
            printCurrentVariation(i, 0, _rootMoves.size(), value);
            _currentVariation.removeLast();
            // #### END - Commit move and go deeper into recursion

            // check if we need to stop search - could be external or time.
            // we should have any best move here
            if (_stopSearch || _hardTimeLimitReached) break;

        } // ##### Iterate through all available moves

        // sort root moves - higher values first
        // best move is not necessarily at index 0
        // best move is in _currentBestMove or _principalVariation[0].get(0)
        _rootMoves.sort();
        // push PV move to head of list
        _rootMoves.pushToHead(_principalVariation[0].get(0));

        boardsCounter += _boardsEvaluated;

        if (_omegaEngine._CONFIGURATION.VERBOSE_STATS) {
            _omegaEngine.printVerboseInfo(String.format("Evaluations in depth %d: %,12d ", depth, boardsCounter));
            _omegaEngine.printVerboseInfo(String.format("Duration: %9s ", Duration.between(iterationStart, Instant.now()).toString()));
            _omegaEngine.printVerboseInfo(String.format("\tEvaluations/sec: %,10d   ",
                    (_boardsEvaluated*1000L)/(Duration.between(_ponderStartTime,Instant.now()).toMillis()+1L)));
            _omegaEngine.printVerboseInfo(String.format("\tNodes/sec: %,10d ",
                    (_nodesVisited*1000L)/(Duration.between(_ponderStartTime,Instant.now()).toMillis()+1L)));
            _omegaEngine.printVerboseInfo("\tMove: "+OmegaMove.toString(_rootMoves.getMove(0))+" ("+_rootMoves.getValue(0)+")  ");
            _omegaEngine.printVerboseInfo("\tPV: "+_principalVariation[0].toNotationString()+"\n");
        }

    }

    /**
     * A simple NegaMax search for the beginning.
     * @param position
     *
     * @param depthLeft
     * @param ply
     * @return value of the search
     */
    private int negamax(OmegaBoardPosition position, int depthLeft, int ply, int alpha, int beta) {

        // prepare hash type
        TT_EntryType tt_Type = TT_EntryType.ALPHA;

        // nodes counter - not fully accurate here as we have counted the
        // first call to this the previous alpha beta.
        _nodesVisited++;

        // current search depth
        if (_currentSearchDepth < ply) _currentSearchDepth = ply;
        if (_currentExtraSearchDepth < ply) _currentExtraSearchDepth = ply;

        // clear principal Variation for this depth
        _principalVariation[ply].clear();

        // check draw through 50-moves-rule, 3-fold-repetition, insufficient material
        if (!OmegaConfiguration.PERFT) {
            if (position.check50Moves()
                    || position.check3Repetitions()
                    || position.checkInsufficientMaterial()) {
                return OmegaEvaluation.Value.DRAW;
            }
        }

        // on leaf node evaluate the position from the view of the active player
        if (depthLeft == 0) {
            if (OmegaConfiguration.PERFT) {
                return evaluate(position);
            }
            return quiescence(position, ply, alpha, beta);
        }

        // ## BEGIN Mate Distance Pruning
        if (_omegaEngine._CONFIGURATION._USE_MDP && !OmegaConfiguration.PERFT) {
            int mate_value = -OmegaEvaluation.Value.CHECKMATE + ply;
            // lower bound
            if (mate_value > alpha) {
                alpha = mate_value;
                if (mate_value >= beta) {
                    _prunings++;
                    return mate_value;
                }
            }
            // upper bound
            mate_value = OmegaEvaluation.Value.CHECKMATE - ply;
            if (mate_value < beta) {
                beta = mate_value;
                if (mate_value <= alpha) {
                    _prunings++;
                    return mate_value;
                }
            }
        }
        // ## ENDOF Mate Distance Pruning

        // check for game paused
        if (_omegaEngine.getGame().isPresent())
            _omegaEngine.getGame().get().waitWhileGamePaused();

        // *****************************************************
        // TT Lookup
        OmegaMoveList tt_moves = null;
        if (_cacheEnabled
                && _omegaEngine._CONFIGURATION._USE_NODE_CACHE
                && !OmegaConfiguration.PERFT) {

            final TT_Entry entry = _transpositionTable.get(position._zobristKey);

            if (entry != null) { // possible TT Hit
                if (entry.depth >= depthLeft) { // only if tt depth was equal or deeper
                    switch (entry.type) {
                        case EXACT:
                            _nodeCache_Hits++;
                            return entry.value;
                        case ALPHA:
                            if (entry.value <= alpha) {
                                _nodeCache_Hits++;
                                return alpha;
                            }
                            break;
                        case BETA:
                            if (entry.value >= beta) {
                                _nodeCache_Hits++;
                                return beta;
                            }
                            break;
                        default:
                            break;
                    }
                }
                // move list is independent from depth
                tt_moves = entry.move_list;
            } else {
                _nodeCache_Misses++;
            }
        }
        // End TT Lookup
        // *****************************************************

        // Initialize best values
        int bestMove = OmegaMove.NOMOVE;
        int bestValue = OmegaEvaluation.Value.NOVALUE;

        // Generate all PseudoLegalMoves
        boolean hadLegaMove = false;

        // generate moves or get them from cache
        OmegaMoveList moves;
        if (_omegaEngine._CONFIGURATION._USE_MOVE_CACHE && tt_moves != null) {
            moves = tt_moves;
            _MovesFromCache++;
            //assert(moves.equals(_omegaMoveGenerator[ply].getPseudoLegalMoves(position, false)));
        } else {
            moves = _omegaMoveGenerator[ply].getPseudoLegalMoves(position, false);
            _MovesGenerated++;
        }

        // moves to search recursively
        for(int i = 0; i < moves.size(); i++) {
            int move = moves.get(i);
            int value = bestValue;

            position.makeMove(move);

            // is legal move?
            if (!position.isAttacked(position._nextPlayer,
                    position._kingSquares[position._nextPlayer.getInverseColor().ordinal()])) {

                // needed to remember if we even had a legal move
                hadLegaMove = true;

                // keep track of current variation
                _currentVariation.add(move);

                // go one ply deeper into the search tree
                //value = -negamax(position, depthLeft-1, ply+1, -beta, -alpha);
                if (!_omegaEngine._CONFIGURATION._USE_PVS || OmegaConfiguration.PERFT) {
                    value = -negamax(position, depthLeft-1, ply+1, -beta, -alpha);
                }
                // ### START PVS ###
                else {
                    if (bestValue == OmegaEvaluation.Value.NOVALUE) { // no PV yet
                        value = -negamax(position, depthLeft-1, ply+1, -beta, -alpha);
                    } else { // we have a PV - do Null Window Search
                        value = -negamax(position, depthLeft-1, ply+1, -alpha-1, -alpha);
                        if (value > alpha && value < beta) { // no fail - research
                            _pv_researches++;
                            value = -negamax(position, depthLeft-1, ply+1, -beta, -alpha);
                        }
                    }
                } // ### END PVS ###


                // PRUNING START
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;

                    if (value > alpha) {
                        alpha = value;
                        tt_Type = TT_EntryType.EXACT;
                        OmegaMoveList.savePV(bestMove, _principalVariation[ply+1], _principalVariation[ply]);

                        if (value >= beta) {
                            if (_omegaEngine._CONFIGURATION._USE_PRUNING && !OmegaConfiguration.PERFT) {
                                tt_Type = TT_EntryType.BETA;
                                bestValue = beta; // same as return beta
                                printCurrentVariation(i, ply, moves.size(), value);
                                _currentVariation.removeLast();
                                position.undoMove();
                                _prunings++;
                                break;
                            }
                        }
                    }
                }
                // PRUNING END

                printCurrentVariation(i, ply, moves.size(), value);
                _currentVariation.removeLast();
            }

            position.undoMove();

            // check if we need to stop search - could be external or time.
            // we should have any best move here
            if (_stopSearch || _hardTimeLimitReached) break;

        }

        // if we did not have a legal move then we have a mate
        if (!hadLegaMove && !_stopSearch) {
            if (position.hasCheck()) {
                // We have a check mate. Return a -CHECKMATE.
                bestValue = -OmegaEvaluation.Value.CHECKMATE + ply;
            } else {
                // We have a stale mate. Return the draw value.
                bestValue = OmegaEvaluation.Value.DRAW;
            }
        }

        // TT Store
        if (_cacheEnabled
                && _omegaEngine._CONFIGURATION._USE_NODE_CACHE
                && !OmegaConfiguration.PERFT) {

            // Stores the list of moves for this node and pushes the bestMovee to the front.
            // We create a minimal list to not waste space - list will not change any more
            OmegaMoveList m = null;
            if (_omegaEngine._CONFIGURATION._USE_MOVE_CACHE) {
                m = new OmegaMoveList(moves.size());
                m.add(moves);
                m.pushToHead(bestMove);
            }

            // now store to tt
            _transpositionTable.put(
                    position,
                    bestValue,
                    tt_Type,
                    depthLeft,
                    m);
        }

        return bestValue;
    }

    /**
     * Evaluate if a position is quiet and we can safely call the evaluation function
     * or if a position is non-quiet (hanging capture, check, etc.) and should be search
     * deeper to avoid horizon problems.
     *
     * @param position
     * @param ply
     * @param alpha
     * @param beta
     * @return
     */
    private int quiescence(OmegaBoardPosition position, int ply, int alpha, int beta) {

        // prepare hash type
        TT_EntryType tt_Type = TT_EntryType.ALPHA;

        // if we have moves the do evaluation
        if (_omegaMoveGenerator[ply].hasLegalMove(position)) {

            if (!_omegaEngine._CONFIGURATION._USE_QUIESCENCE) {
                return evaluate(position);
            }

            // ##############################################################
            // START QUIESCENCE

            _nodesVisited++;

            // ## BEGIN Mate Distance Pruning
            if (_omegaEngine._CONFIGURATION._USE_MDP) {
                int mate_value = -OmegaEvaluation.Value.CHECKMATE + ply;
                // lower bound
                if (mate_value > alpha) {
                    alpha = mate_value;
                    if (mate_value >= beta) {
                        _prunings++;
                        return mate_value;
                    }
                }
                // upper bound
                mate_value = OmegaEvaluation.Value.CHECKMATE - ply;
                if (mate_value < beta) {
                    beta = mate_value;
                    if (mate_value <= alpha) {
                        _prunings++;
                        return mate_value;
                    }
                }
            }
            // ## ENDOF Mate Distance Pruning

            // check for game paused
            if (_omegaEngine.getGame().isPresent())
                _omegaEngine.getGame().get().waitWhileGamePaused();

            // *****************************************************
            // TT Lookup
            if (_cacheEnabled && _omegaEngine._CONFIGURATION._USE_NODE_CACHE) {

                final TT_Entry entry = _transpositionTable.get(position._zobristKey);

                if (entry != null) { // possible TT Hit
                    switch (entry.type) {
                        case EXACT:
                            _nodeCache_Hits++;
                            return entry.value;
                        case ALPHA:
                            if (entry.value <= alpha) {
                                _nodeCache_Hits++;
                                return alpha;
                            }
                            break;
                        case BETA:
                            if (entry.value >= beta) {
                                _nodeCache_Hits++;
                                return beta;
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    _nodeCache_Misses++;
                }
            }
            // End TT Lookup
            // *****************************************************

            // get a fall back evaluation value - called stand-pat
            int stand_pat = evaluate(position);
            if( stand_pat >= beta ) {
                tt_Type = TT_EntryType.BETA;
                // TT Store
                if (_cacheEnabled && _omegaEngine._CONFIGURATION._USE_NODE_CACHE) {
                    _transpositionTable.put(position, alpha, tt_Type, 0, null);
                }
                return beta;
            }
            if( alpha < stand_pat ) {
                tt_Type = TT_EntryType.EXACT;
                alpha = stand_pat;
            }

            int value;

            // Generate all capturing PseudoLegalMoves
            OmegaMoveList moves = _omegaMoveGenerator[ply].getPseudoLegalMoves(position, true);

            // moves to search recursively
            for(int i = 0; i < moves.size(); i++) {
                int move = moves.get(i);

                position.makeMove(move);
                if (!position.isAttacked(  // is this a legal move?
                        position._nextPlayer,
                        position._kingSquares[position._nextPlayer.getInverseColor().ordinal()])) {

                    // needed to remember if we even had a legal move
                    _currentVariation.add(move);

                    // in quiescence search we count modes and extra depth here
                    if (_currentExtraSearchDepth < ply) _currentExtraSearchDepth = ply;

                    // check draw through 50-moves-rule, 3-fold-repetition, insufficient material
                    if (position.check50Moves()
                            || position.check3Repetitions()
                            || position.checkInsufficientMaterial()) {
                        value = OmegaEvaluation.Value.DRAW;
                    } else {

                        // go one ply deeper into the search tree
                        value = -quiescence(position, ply+1, -beta, -alpha);

                    }

                    // PRUNING START
                    if (value >= alpha) {
                        alpha = value;
                        if (value >= beta) {
                            _prunings++;
                            i=moves.size(); // like break but executes the rest
                        }
                    }
                    // PRUNING END

                    printCurrentVariation(i, ply, moves.size(), value);
                    _currentVariation.removeLast();

                }
                position.undoMove();
            }

            // END QUIESCENCE
            // ##############################################################

        }
        // no moves - mate position?
        else {
            if (position.hasCheck()) {
                // We have a check mate. Return a -CHECKMATE.
                alpha = -OmegaEvaluation.Value.CHECKMATE + ply;
            } else {
                // We have a stale mate. Return the draw value.
                alpha = OmegaEvaluation.Value.DRAW;
            }
        }

        // TT Store
        if (_cacheEnabled && _omegaEngine._CONFIGURATION._USE_NODE_CACHE) {
            _transpositionTable.put(position, alpha, tt_Type, 0, null);
        }

        return alpha;
    }

    /**
     * Calls the evaluation function for the position.<br/>
     * Also the a board cache will be implemented here.
     *
     * @param position
     * @return
     */
    private int evaluate(OmegaBoardPosition position) {

        // count all leaf nodes evaluated
        _boardsEvaluated++;

        // special cases for testing
        if (_omegaEngine._CONFIGURATION.DO_NULL_EVALUATION) return 0;
        if (OmegaConfiguration.PERFT) return 1;

        // retrieve evaluation value from evaluation cache
        if (_cacheEnabled && _omegaEngine._CONFIGURATION._USE_BOARD_CACHE) {
            final int value = _evalCache.get(position.getZobristKey());
            if (value > Integer.MIN_VALUE) {
                _evalCache_Hits++;
                return value;
            }
            _evalCache_Misses++;
        }

        // call the evaluation
        final int value = _omegaEvaluation.evaluate(position);

        // store evaluation value in evaluation cache
        if (_cacheEnabled && _omegaEngine._CONFIGURATION._USE_BOARD_CACHE) {
            _evalCache.put(position.getZobristKey(), value);
        }

        return value;
    }

    /**
     * @return
     */
    private int setupTimeControl() {
        /*
         * Setup Time Control
         */

        // start with depth 1
        int startIterativeDepth = 1;

        // get latest level from UI;
        _maxIterativeDepth  = updateSearchDepth();

        // reset time limits
        _softTimeLimitReached = false;
        _hardTimeLimitReached = false;

        // no time control or PERFT test
        if (OmegaConfiguration.PERFT
                || _timedControlMode == TimeControlMode.NO_TIMECONTROL) {
            // directly start iteration with deepest depth
            //startIterativeDepth = _maxIterativeDepth = _currentEngineLevel;
        }
        // use remaining time to calculate time for move
        else if (_timedControlMode == TimeControlMode.PONDERING) {
            updateSearchDepth();
        }
        // use remaining time to calculate time for move
        else if (_timedControlMode == TimeControlMode.REMAINING_TIME) {
            calculateTimePerMove();
            configureTimeControl();
        }
        // use time per move as a hard limit
        else if (_timedControlMode == TimeControlMode.TIME_PER_MOVE) {
            configureTimeControl();
        }
        return startIterativeDepth;
    }

    /**
     * Called to update the current search depth for the player.
     * This makes sure we can change the maximum search depth of a player
     * from the UI during the search.
     *
     * @return current search depth
     */
    private int updateSearchDepth() {
        // for perft tests we use max depth
        if (_timedControlMode == TimeControlMode.PONDERING) return MAX_SEARCH_DEPTH;

        // did we set an explicit level for this search - then keep it
        if (_currentEngineLevel > 0) return _currentEngineLevel;

        // no explicit level set
        int maxDepth;
        if (_omegaEngine.getPlayer().getColor().isBlack()) {
            maxDepth = Playroom.getInstance().getCurrentEngineLevelBlack();
        } else if (_omegaEngine.getPlayer().getColor().isWhite()) {
            maxDepth = Playroom.getInstance().getCurrentEngineLevelWhite();
        } else
            throw new RuntimeException("Invalid next player color. Was " + _omegaEngine.getPlayer().getColor());
        return maxDepth;
    }

    /**
     * Approximates the time available for the next move.
     */
    private void calculateTimePerMove() {

        // reset flags
        long timeLeft = _remainingTime.toMillis();

        // Give some overhead time so that in games with very low available time we do not run out of time
        timeLeft -= 1000; // this should do

        // simple for now - assume 40 moves to go
        _timePerMove = Duration.ofMillis((long) ((timeLeft/40) * 1.0f));

    }

    /**
     * Configure and start time keepers
     * @param approxTime
     */
    private void configureTimeControl() {

        long hardLimit = _timePerMove.toMillis();
        long softLimit = (long) (hardLimit * 0.8f);

        // limits for very short available time
        if (hardLimit < 100) {
            hardLimit = (long) (hardLimit * 0.9f);
            softLimit = (long) (hardLimit * 0.8f);
        }
        // limits for higher available time
        else if (hardLimit > 10000) {
            softLimit = hardLimit;
        }

        _timer = new TimeKeeper(softLimit, hardLimit);
        _timer.start();
    }

    /**
     * Helper method for stat and debug output.
     * @param i
     * @param ply
     * @param size
     * @param value
     */
    private void printCurrentVariation(int i, int ply, int size, int value) {
        if (_omegaEngine._CONFIGURATION.VERBOSE_VARIATION) {
            //if (ply<1 || ply>2) return;
            String info = String.format("%2d/%2d depth:%d/%d %2d/%2d: CV: %s (%d) \t(PV-%3$d: %s)%n"
                    , _currentRootMoveNumber
                    , _rootMoves.size()
                    , ply+1
                    , _currentIterationDepth
                    , i+1
                    , size
                    , _currentVariation.toNotationString()
                    , value
                    , _principalVariation[ply].toNotationString());
            _omegaEngine.printVerboseInfo(info);
            _log.fine(info);
        }
    }

    /**
     * True if currently pondering or was pondering but search is finished.
     *
     * @return true if currently pondering or was pondering but search is finished
     */
    public boolean isPondering() {
        return _isPondering;

    }

    /**
     * Returns true if the search is still running.
     *
     * @return true is search thread is still running
     */
    public boolean isSearching() {
        return (_searchThread != null && _searchThread.isAlive());
    }

    /**
     * Initialize the transposition table so that we do not need to create
     * new objects in the recursion.
     */
    private void initializeCacheTables() {
        if (_omegaEngine._CONFIGURATION._USE_NODE_CACHE) {
            _transpositionTable = new OmegaTranspositionTable(
                    parseInt(Chessly.getProperties().getProperty("engine.nodesCacheSize", "2")));
        }
        if (_omegaEngine._CONFIGURATION._USE_BOARD_CACHE) {
            _evalCache = new OmegaEvaluationCache(
                    parseInt(Chessly.getProperties().getProperty("engine.boardsCacheSize", "2")));
        }
        System.gc();
    }

    /**
     * Parameter class for the search result
     */
    static final class SearchResult {
        int bestMove = OmegaMove.NOMOVE;
        int ponderMove = OmegaMove.NOMOVE;
        int bound = 0;
        int resultValue = 0;
        long time = -1;
        int moveNumber = 0;
        int depth = 0;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Best Move: "+OmegaMove.toString(bestMove));
            sb.append(" Ponder Move: "+OmegaMove.toString(ponderMove));
            return sb.toString();
        }
    }

    /**
     * Modes for different time or level controls.
     */
    static public enum TimeControlMode {
        /**
         * Search is configured with the time left for the player
         */
        REMAINING_TIME,
        /**
         * search is configured with a time per move for player
         */
        TIME_PER_MOVE,
        /**
         * Search is configured to not do time control but use depth setting for player
         */
        NO_TIMECONTROL,
        /**
         * Search is configured to not do time control but use max depth setting
         */
        PONDERING
    }

    class TimeKeeper implements Runnable {

        static private final int GRANULARITY = 10;

        private Thread myThread;
        private long soft;
        private long hard;

        volatile private long timeAccumulator = 0;
        private Instant lastStartTime = Instant.now();

        /**
         * @param softLimit in ms
         * @param hardLimit in ms
         */
        public TimeKeeper(long softLimit, long hardLimit) {
            this.soft = softLimit;
            this.hard = hardLimit;
        }

        /**
         * Starts the time
         */
        public void start() {
            // create new search thread
            myThread = new Thread(this, "TimeKeeper: "+_currentPosition._nextPlayer.toString()+" " + "ApproxTime Soft:" + soft + " Hard:" + hard);
            myThread.setDaemon(true);
            // start the search thread
            myThread.start();
        }

        /**
         * Stops the timer
         */
        public void stop() {
            myThread.interrupt();
        }

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {

            lastStartTime = _startTime;

            while (timeAccumulator + Duration.between(lastStartTime, Instant.now()).toMillis() <= hard
                    && !myThread.isInterrupted()) {


                if (timeAccumulator >= soft)
                    // signal that soft time limit was reached
                    _softTimeLimitReached = true;

                final Instant now = Instant.now();

                // check for game paused
                if (_omegaEngine.getGame().isPresent() && _omegaEngine.getGame().get().isPaused()) {
                    timeAccumulator += Duration.between(lastStartTime, now).toMillis();
                    _omegaEngine.getGame().get().waitWhileGamePaused();
                    lastStartTime = Instant.now();
                }

                try { Thread.sleep(GRANULARITY); }
                catch (InterruptedException e) { break;}
            }
            // signal that hard time limit was reached
            _hardTimeLimitReached = true;

        }

        /**
         * @return the timeAccumulator
         */
        public long getUsedTime() {
            return timeAccumulator + Duration.between(lastStartTime, Instant.now()).toMillis();
        }
    }

}
