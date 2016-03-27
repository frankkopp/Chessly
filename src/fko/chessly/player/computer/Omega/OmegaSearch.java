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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

/**
 * This is the actual search implementation class for the Omega Engine.<br/>
 * It runs in a separate thread and needs to be started through <code>startSearch(position)</code>().
 * If stopped with stop() it stops gracefully returning the best move at that time.
 *
 * Features:
 *      DONE: Thread control
 *      DONE: Move Generation
 *      DONE: Book (in the engine class)
 *      TODO: Basic iterative MiniMax search
 *      TODO: Basic Evaluation
 *      TODO: Basic Time Control
 *      TODO: Pondering
 *      TODO: Transposition Table
 *      TODO: Evaluation Table
 *      TODO: Quiescence
 *      TODO: Advanced iterative AlphaBeta search
 *      TODO: Advanced Evaluation
 *      TODO: Advanced Time Control
 *      TODO: AspirationWindows
 *      TODO: Pruning: AlphaBeta_Pruning, PV, MDP,
 *      TODO: NullMove, Futility, LateMove, Delta, MinorPromotion
 *      TODO: KillerTable. HistoryTable, PawnTable
 *      TODO: SingleReplyExtension, RecaptureExtension, CheckExtension, Pawn Extension, MateThreatExtension
 *
 * @author Frank
 */
public class OmegaSearch implements Runnable {

    private static final int MAX_MOVES = 128;
    private static final int MAX_SEARCH_DEPTH = 99;

    private OmegaEngine _omegaEngine;
    private OmegaBoardPosition _omegaBoard;

    // Move Generator
    private final OmegaMoveGenerator _omegaMoveGenerator = new OmegaMoveGenerator();

    // the thread in which we will do the actual search
    private Thread _searchThread = null;

    // used to wait for move from search
    private CountDownLatch _waitForInitializaitonLatch = new CountDownLatch(1);

    // flag to indicate if engine has been configured sine the last start
    private boolean _isConfigured = false;

    // flag to indicate to stop the search - can be called externally or via the timer clock.
    private boolean _stopSearch = true;

    // flag to indicate that the search is currently pondering
    private boolean _isPondering = false;

    // search configuration (with defaults)
    private boolean _isTimedGame = true;
    private Duration _remainingTimeWhite = Duration.ofSeconds(300);
    private Duration _remainingTimeBlack = Duration.ofSeconds(300);
    private int _currentEngineLevelWhite = 99;
    private int _currentEngineLevelBlack = 99;

    // root move evaluation fields
    OmegaMoveValueList _rootMoves = new OmegaMoveValueList();
    OmegaMoveValueList[] _principalVariation = new OmegaMoveValueList[MAX_SEARCH_DEPTH];
    int _currentBestRootMove= OmegaMove.NOMOVE;

    /* max depth for search iteration - will be set with values from the UI */
    int _maxIterativeDepth = MAX_SEARCH_DEPTH;

    // current variation of the search
    OmegaMoveList _currentVariation = new OmegaMoveList(64);

    // engine watcher fields - not private for easy acces from engine
    int _currentIterationDepth = 0; // how deep will the search go in the current iteration
    int _currentSearchDepth = 0; // how deep did the search go this iteration
    int _currentExtraSearchDepth = 0; // how deep did we search including quiescence depth this iteration
    int _currentMove = 0;
    int _currentMoveNumber = 0;
    int _nodesVisited = 0;
    int _boardsEvaluated = 0;

    // time control
    Instant _startTime = null;

    // convenience fields
    private OmegaColor _activePlayer;
    private OmegaColor _passivePlayer;

    /**
     * Creates a search object and stores a back reference to the engine object.<br/>
     * Before using the search you need to configure it through <code>configure(...)</code><br/>
     *
     *
     * @param omegaEngine
     */
    public OmegaSearch(OmegaEngine omegaEngine) {
        _omegaEngine = omegaEngine;
    }

    /**
     * Setup the Search with all necessary level and time settings.
     *
     * @param timedGame
     * @param remainingTimeWhite in ms
     * @param remainingTimeBlack in ms
     * @param currentEngineLevelWhite max search depth white
     * @param currentEngineLevelBlack max search depth black
     */
    public void configure(boolean timedGame,
            long remainingTimeWhite, long remainingTimeBlack,
            int currentEngineLevelWhite, int currentEngineLevelBlack) {

        _isTimedGame = timedGame;
        _remainingTimeWhite = Duration.ofSeconds(remainingTimeWhite);
        _remainingTimeBlack = Duration.ofSeconds(remainingTimeBlack);
        _currentEngineLevelWhite = currentEngineLevelWhite;
        _currentEngineLevelBlack = currentEngineLevelBlack;

        _isConfigured = true;
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
     * @param omegaBoard
     */
    public void startSearch(OmegaBoardPosition omegaBoard) {
        assert omegaBoard != null : "omegaBoard must not be null";
        _omegaBoard = omegaBoard;

        _startTime = Instant.now();

        // some convenience fields
        _activePlayer = omegaBoard._nextPlayer;
        _passivePlayer = omegaBoard._nextPlayer.getInverseColor();


        // has OmegaSearch.configure been called?
        if (!_isConfigured) {
            System.err.println("Search started without configuration - using defaults");
        }

        // setup latch
        _waitForInitializaitonLatch = new CountDownLatch(1);

        // reset the stop search flag
        _stopSearch = false;

        // create new search thread
        _searchThread = new Thread(this, "OmegaEngine"+_omegaEngine.getActiveColor().toString());
        _searchThread.setDaemon(true);

        // start the search thread
        this._searchThread.start();

        // Wait for initialization in run() before returning from call
        try { _waitForInitializaitonLatch.await();
        } catch (InterruptedException e) { /* empty*/}
    }

    /**
     * Stops a current search. If no search is running it does nothing.
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

    @Override
    public void run() {

        // release latch so the caller can continue
        _waitForInitializaitonLatch.countDown();

        // run the search itself
        SearchResult searchResult = iterativeSearch();

        // send the result
        _omegaEngine.storeResult(searchResult);

        // reset configuration flag
        _isConfigured = false;

    }

    /**
     * This starts the actual search.
     * @return the best move
     */
    private SearchResult iterativeSearch() {

        // generate all root moves
        OmegaMoveList rootMoves = _omegaMoveGenerator.getLegalMoves(_omegaBoard, false);

        assert !rootMoves.empty() : "no legal root moves - game already ended!";

        // prepare principal variation lists
        IntStream.rangeClosed(0, MAX_SEARCH_DEPTH-1)
        .forEach((i) -> {
            _principalVariation[i]= new OmegaMoveValueList();
        });

        // create _rootMoves list
        _rootMoves.clear();
        IntStream.rangeClosed(0, rootMoves.size()-1)
        .forEach((i) -> {
            _rootMoves.add(rootMoves.get(i), OmegaEvaluation.Value.NOVALUE);
        });

        // temporary best move - take the first move available
        _currentBestRootMove = _rootMoves.getMove(0);
        _principalVariation[0].add(_rootMoves.getMove(0), _rootMoves.getValue(0));

        // prepare search result
        SearchResult searchResult = new SearchResult();

        // set start depth and max depth
        int startIterativeDepth = 1;
        _maxIterativeDepth  = updateSearchDepth();
        int depth = startIterativeDepth;
        // for testing of move generation and correct counting
        if (OmegaConfiguration.PERFT) depth = _maxIterativeDepth;

        // ### BEGIN Iterative Deepening
        do {
            _currentIterationDepth = depth;

            // check for game paused
            if (_omegaEngine.getGame().isPresent())
                _omegaEngine.getGame().get().waitWhileGamePaused();

            // do search
            rootMovesSearch(depth, _omegaBoard);

            // we should have a sorted _rootMoves list here
            // first move is best move so far
            // create searchRestult here
            searchResult.bestMove = _rootMoves.getMove(0);
            searchResult.resultValue = _rootMoves.getValue(0);
            searchResult.depth = _currentIterationDepth;
            searchResult.ponderMove = OmegaMove.NOMOVE; // Not yet implemented

            // Time control

            // check  for stop signal
            if (_stopSearch) break;

        } while (++depth <= _maxIterativeDepth);
        // ### ENDOF Iterative Deepening

        return searchResult;
    }

    /**
     * @param depth
     * @param omegaBoard
     */
    private void rootMovesSearch(int depth, OmegaBoardPosition board) {

        // ### Iterate through all available root moves
        for (int i = 0; i < _rootMoves.getSize(); i++) {
            int move = _rootMoves.getMove(i);

            // store the current move for Engine Watcher
            _currentMove = move;
            _currentMoveNumber = i + 1;

            // ### START - Commit move and go deeper into recursion
            board.makeMove(move);
            _currentVariation.add(move);
            printCurrentVariation(i, 0, _rootMoves.getSize());

            int value = miniMax(depth - 1, board, 1);

            board.undoMove();
            _currentVariation.removeLast();
            // ###END - Commit move and go deeper into recursion

            // Evaluate the calculated value and compare to current best move
            if (value > _principalVariation[0].getValue(0)) {
                OmegaMoveValueList.savePV(move, value, _principalVariation[1], _principalVariation[0]);
            }

            // Time control

            // check  for stop signal
            if (_stopSearch) break;

        } // ### Iterate through all available moves

    }

    private int miniMax(int depthLeft, OmegaBoardPosition board, int ply) {

        // nodes counter
        _nodesVisited++;

        // current search depth
        if (ply > _currentSearchDepth) _currentSearchDepth = ply;

        // on leaf node evaluate the position from the view of the active player
        if (depthLeft == 0) {
            return evaluate(board);
        }

        // Generate all PseudoLegalMoves
        int value = 0;
        boolean hadLegaMove = false;
        OmegaMoveList moves = _omegaMoveGenerator.getPseudoLegalMoves(board, false);

        // moves to search recursively
        for(int i = 0; i < moves.size(); i++) {
            int move = moves.get(i);
            board.makeMove(move);
            if (!board.isAttacked(_passivePlayer, board._kingSquares[_activePlayer.ordinal()])) {
                hadLegaMove = true; // needed to check if we even had a legal move
                _currentVariation.add(move);
                printCurrentVariation(i, ply, moves.size());
                value = miniMax(depthLeft-1, board, ply+1);
                _currentVariation.removeLast();
            }
            board.undoMove();
        }

        // if we did not have a legal move then we have a mate
        if (!hadLegaMove) {
            if (board.hasCheck()) {
                // We have a check mate. Return a -CHECKMATE.
                value = OmegaEvaluation.Value.CHECKMATE;
            } else {
                // We have a stale mate. Return the draw value.
                value = OmegaEvaluation.Value.DRAW;
            }
            _boardsEvaluated++;
        }

        return value;
    }

    /**
     * @param board
     * @return
     */
    private int evaluate(OmegaBoardPosition board) {
        // count all leaf nodes evaluated
        _boardsEvaluated++;
        int value;
        if (_omegaMoveGenerator.hasLegalMove(board)) {
            int sideFactor = _omegaEngine.getPlayer().getColor().isWhite() ? 1 : -1;
            value = sideFactor * board.getMaterial(OmegaColor.WHITE) - board.getMaterial(OmegaColor.BLACK);
        } else {
            // no moves - mate position?
            if (board.hasCheck()) {
                // We have a check mate. Return a -CHECKMATE.
                value = -OmegaEvaluation.Value.CHECKMATE;
            } else {
                // We have a stale mate. Return the draw value.
                value = OmegaEvaluation.Value.DRAW;
            }
        }
        return value;
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
        if (_omegaEngine.getPlayer().getColor().isBlack()) {
            maxDepth = _currentEngineLevelBlack;
        } else if (_omegaEngine.getPlayer().getColor().isWhite()) {
            maxDepth = _currentEngineLevelWhite;
        } else
            throw new RuntimeException("Invalid next player color. Was " + _omegaEngine.getPlayer().getColor());
        return maxDepth;
    }

    /**
     * @param i
     * @param ply
     * @param size
     */
    private void printCurrentVariation(int i, int ply, int size) {
        if (_omegaEngine._CONFIGURATION.VERBOSE_VARIATION) {
            String info = String.format("%2d/%2d depth:%d/%d %2d/%2d: %s (%s)%n"
                    , _currentMoveNumber
                    , _rootMoves.getSize()
                    , ply+1
                    , _currentIterationDepth
                    , i+1
                    , size
                    ,_currentVariation.toNotationString()
                    ,_principalVariation[0]);
            _omegaEngine.printVerboseInfo(info);
        }
    }

    /**
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
        return _searchThread.isAlive();
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
            sb.append("BestMove: "+OmegaMove.toString(bestMove));
            return sb.toString();
        }


    }

}
