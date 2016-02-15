/*
 * Copyright (C) 2007-2014 Phokham Nonava
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package fko.chessly.player.computer.FluxEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveList;

final class Search implements Runnable {

    /**
     * The maximum number of moves.
     */
    static final int MAX_MOVES = 4096;

    private static final int ASPIRATIONWINDOW = 20;
    private static final int ASPIRATIONWINDOW_ADJUSTMENT = 200;

    private static final int TIMEEXTENSION_MARGIN = 30;

    private static final int NULLMOVE_DEPTH = 2;
    private static final int NULLMOVE_REDUCTION;
    private static final int NULLMOVE_VERIFICATIONREDUCTION = 3;

    private static final int IID_DEPTH = 2;

    private static final int LMR_DEPTH = 3;
    private static final int LMR_MOVENUMBER_MINIMUM = 3;

    private static final int FUTILITY_FRONTIERMARGIN = 2 * Piece.VALUE_PAWN;
    private static final int FUTILITY_PREFRONTIERMARGIN = Piece.VALUE_ROOK;
    private static final int FUTILITY_QUIESCENTMARGIN = Piece.VALUE_PAWN;

    // Objects
    //private final IProtocol protocol;
    private final Thread _thread = new Thread(this, "FluxEngine");
    private final Semaphore _semaphore = new Semaphore(0);

    // Search control
    private Timer _timer = null;
    private boolean _canStop = false;
    private boolean _stopped = true;
    private boolean _stopFlag = false;
    private boolean _doTimeManagement = true;
    private boolean _analyzeMode = false;

    // Search parameters
    private int _searchDepth = 0;
    private long _searchNodes = 0;
    private long _searchTime = 0;
    private long _searchTimeHard = 0;
    private long _searchTimeStart = 0;
    private final long[] _searchClock = new long[Color.ARRAY_DIMENSION];
    private final long[] _searchClockIncrement = new long[Color.ARRAY_DIMENSION];
    private int _searchMovesToGo = 0;
    private final MoveList _searchMoveList = new MoveList();

    // Analyze parameters
    private int _showPvNumber = 1;

    // Search logic
    private Evaluation _evaluation = new Evaluation();
    private static Position _board;
    private final int _myColor;

    // Search tables
    private TranspositionTable _transpositionTable;
    private static KillerTable _killerTable;
    private static HistoryTable _historyTable;

    // Search information
    private static final MoveList[] _pvList = new MoveList[Depth.MAX_PLY + 1];
    private static final HashMap<Integer, PrincipalVariation> _multiPvMap = new HashMap<>(MAX_MOVES);
    private Result _bestResult = null;
    private final int[] _timeTable;

    private int _currentDepth = 1;
    private int _currentMaxDepth = 0;
    private long _totalTimeStart = 0;
    private long _currentTimeStart = 0;
    private int _currentMoveNumber = 0;
    private GameMove _currentMove = null;
    private long _totalNodes = 0;
    private long _totalBoards = 0;
    private int _totalNonQuietBoards = 0;

    // back reference to FluxEngine to send move to
    private FluxEngine _fluxengine;

    // Cache statistics
    private final AtomicLong _nodeCacheHits = new AtomicLong(0);
    private final AtomicLong _nodeCacheMisses = new AtomicLong(0);

    static final class Result {
        int bestMove = Move.NOMOVE;
        int ponderMove = Move.NOMOVE;
        int value = Bound.NOBOUND;
        int resultValue = -Value.INFINITY;
        long time = -1;
        int moveNumber = 0;
        int depth = 0;

        /**
         * Creates a new Result.
         */
        Result() {
        }
    }

    // Static initialization
    static {
        if (Configuration.useVerifiedNullMovePruning) {
            NULLMOVE_REDUCTION = 3;
        } else {
            NULLMOVE_REDUCTION = 2;
        }

        for (int i = 0; i < _pvList.length; i++) {
            _pvList[i] = new MoveList();
        }
    }

    Search(FluxEngine backreference, Position newBoard, TranspositionTable newTranspositionTable, int[] timeTable) {
        assert newBoard != null;
        assert newTranspositionTable != null;

        _fluxengine = backreference;

        _thread.setName("FluxEngineSearch "+_fluxengine.getActiveColor().toString());

        this._analyzeMode = Configuration.analyzeMode;

        _board = newBoard;
        this._myColor = _board.activeColor;

        this._transpositionTable = newTranspositionTable;
        if (this._analyzeMode) {
            this._transpositionTable.increaseAge();
        }
        _killerTable = new KillerTable();
        _historyTable = new HistoryTable();

        new MoveGenerator(_board, _killerTable, _historyTable);
        new See(_board);

        this._timeTable = timeTable;

        _multiPvMap.clear();
    }

    @Override
    public void run() {
        this._stopped = false;
        this._canStop = false;
        this._bestResult = new Result();

        // Set the time management
        if (this._doTimeManagement) {
            setTimeManagement();
            this._searchTimeStart = System.currentTimeMillis();
        }
        if (this._searchTime > 0) {
            startTimer();
        }

        // Go...
        this._semaphore.release();

        _totalTimeStart = System.currentTimeMillis();
        _currentTimeStart = _totalTimeStart;
        Result moveResult = getBestMove();

        // Cancel the timer
        if (this._timer != null) {
            this._timer.cancel();
        }

        // Send the result
        if (moveResult.bestMove != Move.NOMOVE) {
            if (moveResult.ponderMove != Move.NOMOVE) {
                _fluxengine.storeResult(moveResult);
            } else {
                _fluxengine.storeResult(moveResult);
            }
        } else {
            _fluxengine.storeResult(null);
        }

        // Cleanup manually
        this._transpositionTable = null;
        this._evaluation = null;
    }

    void start() {
        this._thread.start();
        try {
            // Wait for initialization
            this._semaphore.acquire();
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    void stop() {
        this._stopped = true;
        this._canStop = true;
        try {
            // Wait for the thread to die
            this._thread.join();
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    void ponderhit() {
        // Enable time management
        this._doTimeManagement = true;

        // Set time management parameters
        setTimeManagement();
        this._searchTimeStart = System.currentTimeMillis();

        // Start our hard stop timer
        startTimer();

        // Check whether we have already a result
        assert this._bestResult != null;
        if (this._bestResult.bestMove != Move.NOMOVE) {
            this._canStop = true;

            // Check if we have a checkmate
            if (Math.abs(this._bestResult.resultValue) > Value.CHECKMATE_THRESHOLD
                    && this._bestResult.depth >= (Value.CHECKMATE - Math
                            .abs(this._bestResult.resultValue))) {
                this._stopped = true;
            }

            // Check if we have only one move to make
            else if (this._bestResult.moveNumber == 1) {
                this._stopped = true;
            }
        }
    }

    boolean isStopped() {
        return !this._thread.isAlive();
    }

    void setSearchDepth(int searchDepth) {
        assert searchDepth > 0;

        this._searchDepth = searchDepth;
        if (this._searchDepth > Depth.MAX_DEPTH) {
            this._searchDepth = Depth.MAX_DEPTH;
        }
        this._doTimeManagement = false;
    }

    void setSearchNodes(long searchNodes) {
        assert searchNodes > 0;

        this._searchNodes = searchNodes;
        this._searchDepth = Depth.MAX_DEPTH;
        this._doTimeManagement = false;
    }

    void setSearchTime(long searchTime) {
        assert searchTime > 0;

        this._searchTime = searchTime;
        this._searchTimeHard = this._searchTime;
        this._searchDepth = Depth.MAX_DEPTH;
        this._doTimeManagement = false;
    }

    void setSearchClock(int side, long timeLeft) {
        assert timeLeft > 0;

        this._searchClock[side] = timeLeft;
    }

    void setSearchClockIncrement(int side, long timeIncrement) {
        assert timeIncrement > 0;

        this._searchClockIncrement[side] = timeIncrement;
    }

    void setSearchMovesToGo(int searchMovesToGo) {
        assert searchMovesToGo > 0;

        this._searchMovesToGo = searchMovesToGo;
    }

    void setSearchInfinite() {
        this._searchDepth = Depth.MAX_DEPTH;
        this._doTimeManagement = false;
        this._analyzeMode = true;
    }

    void setSearchPonder() {
        this._searchDepth = Depth.MAX_DEPTH;
        this._doTimeManagement = false;
    }

    void setSearchMoveList(List<GameMove> moveList) {
        for (GameMove move : moveList) {
            this._searchMoveList.moves[this._searchMoveList.tail++] = Move
                    .convertMove(move, _board);
        }
    }

    private void startTimer() {
        // Only start timer if we have a hard time limit
        if (this._searchTimeHard > 0) {
            this._timer = new Timer(_thread.getName()+" Timer hard:"+_searchTimeHard, true);
            this._timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    stop();
                }
            }, this._searchTimeHard);
        }
    }

    private void setTimeManagement() {
        // Dynamic time allocation
        this._searchDepth = Depth.MAX_DEPTH;

        if (this._searchClock[this._myColor] > 0) {
            // We received a time control.

            // Check the moves to go
            if (this._searchMovesToGo < 1 || this._searchMovesToGo > 40) {
                this._searchMovesToGo = 40;
            }

            // Check the increment
            if (this._searchClockIncrement[this._myColor] < 1) {
                this._searchClockIncrement[this._myColor] = 0;
            }

            // Set the maximum search time
            long maxSearchTime = (long) (this._searchClock[this._myColor] * 0.95) - 1000L;
            if (maxSearchTime < 0) {
                maxSearchTime = 0;
            }

            // Set the search time
            this._searchTime = (maxSearchTime + (this._searchMovesToGo - 1)
                    * this._searchClockIncrement[this._myColor])
                    / this._searchMovesToGo;
            if (this._searchTime > maxSearchTime) {
                this._searchTime = maxSearchTime;
            }

            // Set the hard limit search time
            this._searchTimeHard = (maxSearchTime + (this._searchMovesToGo - 1)
                    * this._searchClockIncrement[this._myColor]) / 8;
            if (this._searchTimeHard < this._searchTime) {
                this._searchTimeHard = this._searchTime;
            }
            if (this._searchTimeHard > maxSearchTime) {
                this._searchTimeHard = maxSearchTime;
            }
        } else {
            // We received no time control. Search for 2 seconds.
            this._searchTime = 2000L;

            // Stop hard after +50% of the allocated time
            this._searchTimeHard = this._searchTime + this._searchTime / 2;
        }
    }

    private Result getBestMove() {
        // ## BEGIN Root Move List
        MoveList rootMoveList = new MoveList();

        PrincipalVariation pv = null;
        int transpositionMove = Move.NOMOVE;
        int transpositionDepth = -1;
        int transpositionValue = 0;
        int transpositionType = Bound.NOBOUND;
        if (Configuration.useTranspositionTable) {
            TranspositionTable.TranspositionTableEntry entry = this._transpositionTable.get(_board.zobristCode);
            if (entry != null) {
                _nodeCacheHits.getAndIncrement();
                List<GameMove> moveList = this._transpositionTable.getMoveList(_board, entry.depth, new ArrayList<GameMove>());
                if (moveList.size() != 0) {
                    pv = new PrincipalVariation(1, entry.getValue(0),
                            entry.type, entry.getValue(0), moveList,
                            entry.depth, entry.depth, 0, 0, 0);
                }
                transpositionMove = entry.move;
                transpositionDepth = entry.depth;
                transpositionValue = entry.getValue(0);
                transpositionType = entry.type;
            } else {
                _nodeCacheMisses.getAndIncrement();
            }
        }

        Attack attack = _board.getAttack(_board.activeColor);
        boolean isCheck = attack.isCheck();

        if (this._searchMoveList.getLength() == 0) {
            MoveGenerator.initializeMain(attack, 0, transpositionMove);

            int move;
            while ((move = MoveGenerator.getNextMove()) != Move.NOMOVE) {
                rootMoveList.moves[rootMoveList.tail++] = move;
            }

            MoveGenerator.destroy();
        } else {
            for (int i = this._searchMoveList.head; i < this._searchMoveList.tail; i++) {
                rootMoveList.moves[rootMoveList.tail++] = this._searchMoveList.moves[i];
            }
        }

        // Check if we cannot move
        if (rootMoveList.getLength() == 0) {
            // This position is a checkmate or stalemate
            return this._bestResult;
        }

        // Adjust pv number
        this._showPvNumber = Configuration.showPvNumber;
        if (Configuration.showPvNumber > rootMoveList.getLength()) {
            this._showPvNumber = rootMoveList.getLength();
        }
        // ## ENDOF Root Move List

        int alpha = -Value.CHECKMATE;
        int beta = Value.CHECKMATE;

        int initialDepth = 1;
        int equalResults = 0;
        if (!this._analyzeMode && transpositionDepth > 1
                && transpositionType == Bound.EXACT
                && Math.abs(transpositionValue) < Value.CHECKMATE_THRESHOLD
                && pv != null) {
            this._bestResult.bestMove = transpositionMove;
            this._bestResult.resultValue = transpositionValue;
            this._bestResult.value = transpositionType;
            this._bestResult.time = 0;
            this._bestResult.moveNumber = rootMoveList.getLength();

            initialDepth = transpositionDepth;
            equalResults = transpositionDepth - 2;
        }

        // ## BEGIN Iterative Deepening
        for (_currentDepth = initialDepth; _currentDepth <= this._searchDepth; _currentDepth++) {
            _currentMaxDepth = 0;
            sendInformationDepth();

            // Create a new result
            Result moveResult = new Result();

            // Set the start time
            long startTime = System.currentTimeMillis();

            int value;
            if (_currentDepth == initialDepth && initialDepth > 1) {
                value = transpositionValue;
                _pvList[0].resetList();
                sendInformation(pv, 1);

                moveResult.bestMove = transpositionMove;
                moveResult.resultValue = transpositionValue;
                moveResult.value = transpositionType;
                moveResult.moveNumber = rootMoveList.getLength();
            } else {
                // Do the Alpha-Beta search
                value = alphaBetaRoot(_currentDepth, alpha, beta, 0, rootMoveList, isCheck, moveResult);
            }

            // ## BEGIN Aspiration Windows
            // Notes: Ideas from Ed Schröder. We open the aspiration window
            // twice, as the first adjustment should be wide enough.
            if (!(this._stopped && this._canStop)
                    && Configuration.useAspirationWindows
                    && this._showPvNumber <= 1
                    && _currentDepth >= transpositionDepth) {
                // First adjustment
                if (value <= alpha || value >= beta) {
                    if (value <= alpha) {
                        alpha -= ASPIRATIONWINDOW_ADJUSTMENT;
                    } else if (value >= beta) {
                        beta += ASPIRATIONWINDOW_ADJUSTMENT;
                    } else {
                        assert false : "Alpha: " + alpha + ", Beta: " + beta
                        + ", Value: " + value;
                    }
                    if (alpha < -Value.CHECKMATE) {
                        alpha = -Value.CHECKMATE;
                    }
                    if (beta > Value.CHECKMATE) {
                        beta = Value.CHECKMATE;
                    }

                    // Do the Alpha-Beta search again
                    value = alphaBetaRoot(_currentDepth, alpha, beta, 0,
                            rootMoveList, isCheck, moveResult);

                    if (!(this._stopped && this._canStop)) {
                        // Second adjustment
                        // Open window to full width
                        if (value <= alpha || value >= beta) {
                            alpha = -Value.CHECKMATE;
                            beta = Value.CHECKMATE;

                            // Do the Alpha-Beta search again
                            value = alphaBetaRoot(_currentDepth, alpha, beta, 0,
                                    rootMoveList, isCheck, moveResult);
                        }
                    }
                }

                // Adjust aspiration window
                alpha = value - ASPIRATIONWINDOW;
                beta = value + ASPIRATIONWINDOW;
                if (alpha < -Value.CHECKMATE) {
                    alpha = -Value.CHECKMATE;
                }
                if (beta > Value.CHECKMATE) {
                    beta = Value.CHECKMATE;
                }
            }
            // ## ENDOF Aspiration Windows

            // Set the end time
            long endTime = System.currentTimeMillis();
            moveResult.time = endTime - startTime;
            moveResult.depth = _currentDepth;

            // Set the used time
            if (_currentDepth > initialDepth) {
                if (_timeTable[_currentDepth] == 0) {
                    _timeTable[_currentDepth] += moveResult.time;
                } else {
                    _timeTable[_currentDepth] += moveResult.time;
                    _timeTable[_currentDepth] /= 2;
                }
            }

            // Prepare the move result
            if (moveResult.bestMove != Move.NOMOVE) {
                // Count all equal results
                if (moveResult.bestMove == this._bestResult.bestMove) {
                    equalResults++;
                } else {
                    equalResults = 0;
                }

                if (this._doTimeManagement) {
                    // ## BEGIN Time Control
                    boolean timeExtended = false;

                    // Check value change
                    if (moveResult.resultValue + TIMEEXTENSION_MARGIN < this._bestResult.resultValue
                            || moveResult.resultValue - TIMEEXTENSION_MARGIN > this._bestResult.resultValue) {
                        timeExtended = true;
                    }

                    // Check equal results
                    else if (equalResults < 1) {
                        timeExtended = true;
                    }

                    // Set the needed time for the next iteration
                    long nextIterationTime = _timeTable[_currentDepth + 1];
                    if (_timeTable[_currentDepth + 1] == 0) {
                        nextIterationTime = (moveResult.time * 2);
                    }

                    // Check if we cannot finish the next iteration on time
                    if (this._searchTimeStart + this._searchTimeHard < System
                            .currentTimeMillis() + nextIterationTime) {
                        // Clear table
                        if (_currentDepth == initialDepth) {
                            for (int i = _currentDepth + 1; i < this._timeTable.length; i++) {
                                this._timeTable[i] = 0;
                            }
                        } else {
                            for (int i = _currentDepth + 1; i < this._timeTable.length; i++) {
                                this._timeTable[i] += this._timeTable[i - 1] * 2;
                                this._timeTable[i] /= 2;
                            }
                        }
                        this._stopFlag = true;
                    }

                    // Check time limit
                    else if (!timeExtended
                            && this._searchTimeStart + this._searchTime < System
                            .currentTimeMillis() + nextIterationTime) {
                        // Clear table
                        if (_currentDepth == initialDepth) {
                            for (int i = _currentDepth + 1; i < this._timeTable.length; i++) {
                                this._timeTable[i] = 0;
                            }
                        } else {
                            for (int i = _currentDepth + 1; i < this._timeTable.length; i++) {
                                this._timeTable[i] += this._timeTable[i - 1] * 2;
                                this._timeTable[i] /= 2;
                            }
                        }
                        this._stopFlag = true;
                    }

                    // Check if this is an easy recapture
                    else if (!timeExtended
                            && Move.getEnd(moveResult.bestMove) == _board.captureSquare
                            && Piece.getValueFromChessman(Move
                                    .getTarget(moveResult.bestMove)) >= Piece.VALUE_KNIGHT
                                    && equalResults > 4) {
                        this._stopFlag = true;
                    }

                    // Check if we have a checkmate
                    else if (Math.abs(value) > Value.CHECKMATE_THRESHOLD
                            && _currentDepth >= (Value.CHECKMATE - Math
                                    .abs(value))) {
                        this._stopFlag = true;
                    }

                    // Check if we have only one move to make
                    else if (moveResult.moveNumber == 1) {
                        this._stopFlag = true;
                    }
                    // ## ENDOF Time Control
                }

                // Update the best result.
                this._bestResult = moveResult;

                if (_pvList[0].tail > 1) {
                    // We found a line. Set the ponder move.
                    this._bestResult.ponderMove = _pvList[0].moves[1];
                }
            } else {
                // We found no best move.
                // Perhaps we have a checkmate or we got a stop request?
                break;
            }

            // Check if we can stop the search
            if (this._stopFlag) {
                break;
            }

            this._canStop = true;

            if (this._stopped) {
                break;
            }
        }
        // ## ENDOF Iterative Deepening

        // Update all stats
        sendInformationSummary();

        return this._bestResult;
    }

    private void updateSearch(int height) {
        _totalNodes++;
        if (height > _currentMaxDepth) {
            _currentMaxDepth = height;
        }
        sendInformationStatus();

        if (this._searchNodes > 0 && this._searchNodes <= _totalNodes) {
            // Hard stop on number of nodes
            this._stopped = true;
        }

        // Reset
        _pvList[height].resetList();
    }

    private int alphaBetaRoot(int depth, int alpha, int beta, int height,
            MoveList rootMoveList, boolean isCheck, Result moveResult) {

        updateSearch(height);

        // Abort conditions
        if ((this._stopped && this._canStop) || height == Depth.MAX_PLY) {
            return evaluateBoard();
        }

        // Initialize
        int hashType = Bound.UPPER;
        int bestValue = -Value.INFINITY;
        int bestMove = Move.NOMOVE;
        int oldAlpha = alpha;
        PrincipalVariation lastMultiPv = null;
        PrincipalVariation bestPv = null;
        PrincipalVariation firstPv = null;

        // Initialize the move number
        int currentMoveNumber = 0;

        // Initialize Single-Response Extension
        boolean isSingleReply;
        isSingleReply = isCheck && rootMoveList.getLength() == 1;

        for (int j = rootMoveList.head; j < rootMoveList.tail; j++) {
            int move = rootMoveList.moves[j];

            // Update the information if we evaluate a new move.
            currentMoveNumber++;
            sendInformationMove(Move.toGameMove(move), currentMoveNumber, rootMoveList.getLength());

            // Extension
            int newDepth = getNewDepth(depth, move, isSingleReply, false);

            // Do move
            _board.makeMove(move);

            // ## BEGIN Principal Variation Search
            int value;
            if (bestValue == -Value.INFINITY) {
                // First move
                value = -alphaBeta(newDepth, -beta, -alpha, height + 1, true,
                        true);
            } else {
                value = -alphaBeta(newDepth, -alpha - 1, -alpha, height + 1,
                        false, true);
                if (value > alpha && value < beta) {
                    // Research again
                    value = -alphaBeta(newDepth, -beta, -alpha, height + 1,
                            true, true);
                }
            }
            // ## ENDOF Principal Variation Search

            // Undo move
            _board.undoMove(move);

            if (this._stopped && this._canStop) {
                break;
            }

            // Store value
            int sortValue;
            int moveType;
            if (value <= alpha) {
                value = alpha;
                moveType = Bound.UPPER;
                rootMoveList.values[j] = oldAlpha;
                sortValue = -Value.INFINITY;
            } else if (value >= beta) {
                value = beta;
                moveType = Bound.LOWER;
                rootMoveList.values[j] = beta;
                sortValue = Value.INFINITY;
            } else {
                moveType = Bound.EXACT;
                rootMoveList.values[j] = value;
                sortValue = value;
            }

            // Add pv to list
            List<GameMove> refutationMoveList = new ArrayList<>();
            refutationMoveList.add(Move.toGameMove(move));
            for (int i = _pvList[height + 1].head; i < _pvList[height + 1].tail; i++) {
                refutationMoveList.add(Move.toGameMove(_pvList[height + 1].moves[i]));
            }

            PrincipalVariation pv = new PrincipalVariation(currentMoveNumber,
                    value, moveType, sortValue, refutationMoveList, _currentDepth,
                    _currentMaxDepth, getCurrentNps(),
                    System.currentTimeMillis() - _totalTimeStart, _totalNodes);

            _multiPvMap.put(move, pv);

            // Save first pv
            if (currentMoveNumber == 1) {
                firstPv = pv;
            }

            // Show refutations
            if (Configuration.showRefutations) {
                sendInformationRefutations(refutationMoveList);
            }

            // Show multi pv
            if (this._showPvNumber > 1) {
                assert currentMoveNumber <= this._showPvNumber || lastMultiPv != null;

                if (currentMoveNumber <= this._showPvNumber || pv.compareTo(lastMultiPv) < 0) {
                    PriorityQueue<PrincipalVariation> tempPvList = new PriorityQueue<>(_multiPvMap.values());
                    for (int i = 1; i <= this._showPvNumber && !tempPvList.isEmpty(); i++) {
                        lastMultiPv = tempPvList.remove();
                        sendInformation(lastMultiPv, i);
                    }
                }
            }

            // Pruning
            if (value > bestValue) {
                bestValue = value;
                addPv(_pvList[height], _pvList[height + 1], move);

                // Do we have a better value?
                if (value > alpha) {
                    bestMove = move;
                    bestPv = pv;
                    hashType = Bound.EXACT;
                    alpha = value;

                    if (depth > 1 && this._showPvNumber <= 1) {
                        // Send pv information for depth > 1
                        // Print the best move as soon as we get a new one
                        // This is really an optimistic assumption
                        sendInformation(bestPv, 1);
                    }

                    // Is the value higher than beta?
                    if (value >= beta) {
                        // Cut-off

                        hashType = Bound.LOWER;
                        break;
                    }
                }
            }

            if (this._showPvNumber > 1) {
                // Reset alpha to get the real value of the next move
                assert oldAlpha == -Value.CHECKMATE;
                alpha = oldAlpha;
            }
        }

        if (!(this._stopped && this._canStop)) {
            this._transpositionTable.put(_board.zobristCode, depth, bestValue,
                    hashType, bestMove, false, height);
        }

        if (depth == 1 && this._showPvNumber <= 1 && bestPv != null) {
            // Send pv information for depth 1
            // On depth 1 we have no move ordering available
            // To reduce the output we only print the best move here
            sendInformation(bestPv, 1);
        }

        if (this._showPvNumber <= 1 && bestPv == null && firstPv != null) {
            // We have a fail low
            assert oldAlpha == alpha;

            PrincipalVariation resultPv = new PrincipalVariation(
                    firstPv.moveNumber, firstPv.value, firstPv.type,
                    firstPv.sortValue, firstPv.pv, firstPv.depth,
                    _currentMaxDepth, getCurrentNps(),
                    System.currentTimeMillis() - _totalTimeStart, _totalNodes);
            sendInformation(resultPv, 1);
        }

        moveResult.bestMove = bestMove;
        moveResult.resultValue = bestValue;
        moveResult.value = hashType;
        moveResult.moveNumber = currentMoveNumber;

        if (Configuration.useTranspositionTable) {
            TranspositionTable.TranspositionTableEntry entry = this._transpositionTable.get(_board.zobristCode);
            if (entry != null) {
                _nodeCacheHits.getAndIncrement();
                for (int i = rootMoveList.head; i < rootMoveList.tail; i++) {
                    if (rootMoveList.moves[i] == entry.move) {
                        rootMoveList.values[i] = Value.INFINITY;
                        break;
                    }
                }
            } else {
                _nodeCacheMisses.getAndIncrement();
            }
        }

        rootMoveList.sort();

        return bestValue;
    }

    private int alphaBeta(int depth, int alpha, int beta, int ply,
            boolean pvNode, boolean doNull) {

        // We are at a leaf/horizon. So calculate that value.
        if (depth <= 0) {
            // Descend into quiescent
            return quiescent(0, alpha, beta, ply, pvNode, true);
        }

        updateSearch(ply);

        // Abort conditions
        if ((this._stopped && this._canStop) || ply == Depth.MAX_PLY) {
            return evaluateBoard();
        }

        // Check the repetition table and fifty move rule
        if (_board.isRepetition() || _board.halfMoveClock >= 100) {
            return Value.DRAW;
        }

        // ## BEGIN Mate Distance Pruning
        if (Configuration.useMateDistancePruning) {
            int value = -Value.CHECKMATE + ply;
            if (value > alpha) {
                alpha = value;
                if (value >= beta) {
                    return value;
                }
            }
            value = -(-Value.CHECKMATE + ply + 1);
            if (value < beta) {
                beta = value;
                if (value <= alpha) {
                    return value;
                }
            }
        }
        // ## ENDOF Mate Distance Pruning

        // Check the transposition table first
        int transpositionMove = Move.NOMOVE;
        boolean mateThreat = false;
        if (Configuration.useTranspositionTable) {
            TranspositionTable.TranspositionTableEntry entry = this._transpositionTable.get(_board.zobristCode);
            if (entry != null) {
                _nodeCacheHits.getAndIncrement();
                transpositionMove = entry.move;
                mateThreat = entry.mateThreat;

                if (!pvNode && entry.depth >= depth) {
                    int value = entry.getValue(ply);
                    int type = entry.type;

                    switch (type) {
                        case Bound.LOWER:
                            if (value >= beta) {
                                return value;
                            }
                            break;
                        case Bound.UPPER:
                            if (value <= alpha) {
                                return value;
                            }
                            break;
                        case Bound.EXACT:
                            return value;
                        default:
                            assert false;
                            break;
                    }
                }
            } else {
                _nodeCacheMisses.getAndIncrement();
            }
        }

        // Get the attack
        // Notes: Ideas from Fruit. Storing all attacks here seems to be a good
        // idea.
        Attack attack = _board.getAttack(_board.activeColor);
        boolean isCheck = attack.isCheck();

        // ## BEGIN Null-Move Pruning
        // Notes: Ideas from
        // http://www.cs.biu.ac.il/~davoudo/pubs/vrfd_null.html
        int evalValue = Value.INFINITY;
        if (Configuration.useNullMovePruning) {
            if (!pvNode && depth >= NULLMOVE_DEPTH && doNull && !isCheck
                    && !mateThreat && _board.getGamePhase() != GamePhase.ENDGAME
                    && (evalValue = evaluateBoard()) >= beta) {
                // Depth reduction
                int newDepth = depth - 1 - NULLMOVE_REDUCTION;

                // Make the null move
                _board.makeMoveNull();
                int value = -alphaBeta(newDepth, -beta, -beta + 1, ply + 1,
                        false, false);
                _board.undoMoveNull();

                // Verify on beta exceeding
                if (Configuration.useVerifiedNullMovePruning) {
                    if (depth > NULLMOVE_VERIFICATIONREDUCTION) {
                        if (value >= beta) {
                            newDepth = depth - NULLMOVE_VERIFICATIONREDUCTION;

                            // Verify
                            value = alphaBeta(newDepth, alpha, beta, ply,
                                    true, false);

                            if (value >= beta) {
                                // Cut-off

                                return value;
                            }
                        }
                    }
                }

                // Check for mate threat
                if (value < -Value.CHECKMATE_THRESHOLD) {
                    mateThreat = true;
                }

                if (value >= beta) {
                    // Do not return unproven mate values
                    if (value > Value.CHECKMATE_THRESHOLD) {
                        value = Value.CHECKMATE_THRESHOLD;
                    }

                    if (!(this._stopped && this._canStop)) {
                        // Store the value into the transposition table
                        this._transpositionTable.put(_board.zobristCode, depth,
                                value, Bound.LOWER, Move.NOMOVE, mateThreat,
                                ply);
                    }

                    return value;
                }
            }
        }
        // ## ENDOF Null-Move Forward Pruning

        // Initialize
        int hashType = Bound.UPPER;
        int bestValue = -Value.INFINITY;
        int bestMove = Move.NOMOVE;
        int searchedMoves = 0;

        // ## BEGIN Internal Iterative Deepening
        if (Configuration.useInternalIterativeDeepening) {
            if (pvNode && depth >= IID_DEPTH
                    && transpositionMove == Move.NOMOVE) {
                int oldAlpha = alpha;
                int oldBeta = beta;
                alpha = -Value.CHECKMATE;
                beta = Value.CHECKMATE;

                for (int newDepth = 1; newDepth < depth; newDepth++) {
                    int value = alphaBeta(newDepth, alpha, beta, ply, true,
                            false);

                    // ## BEGIN Aspiration Windows
                    if (!(this._stopped && this._canStop)
                            && Configuration.useAspirationWindows) {
                        // First adjustment
                        if (value <= alpha || value >= beta) {
                            if (value <= alpha) {
                                alpha -= ASPIRATIONWINDOW_ADJUSTMENT;
                            } else if (value >= beta) {
                                beta += ASPIRATIONWINDOW_ADJUSTMENT;
                            } else {
                                assert false : "Alpha: " + alpha + ", Beta: "
                                        + beta + ", Value: " + value;
                            }
                            if (alpha < -Value.CHECKMATE) {
                                alpha = -Value.CHECKMATE;
                            }
                            if (beta > Value.CHECKMATE) {
                                beta = Value.CHECKMATE;
                            }

                            // Do the Alpha-Beta search again
                            value = alphaBeta(newDepth, alpha, beta, ply,
                                    true, false);

                            if (!(this._stopped && this._canStop)) {
                                // Second adjustment
                                // Open window to full width
                                if (value <= alpha || value >= beta) {
                                    alpha = -Value.CHECKMATE;
                                    beta = Value.CHECKMATE;

                                    // Do the Alpha-Beta search again
                                    value = alphaBeta(newDepth, alpha, beta,
                                            ply, true, false);
                                }
                            }
                        }

                        // Adjust aspiration window
                        alpha = value - ASPIRATIONWINDOW;
                        beta = value + ASPIRATIONWINDOW;
                        if (alpha < -Value.CHECKMATE) {
                            alpha = -Value.CHECKMATE;
                        }
                        if (beta > Value.CHECKMATE) {
                            beta = Value.CHECKMATE;
                        }
                    }
                    // ## ENDOF Aspiration Windows

                    if (this._stopped && this._canStop) {
                        return oldAlpha;
                    }
                }

                alpha = oldAlpha;
                beta = oldBeta;

                if (_pvList[ply].getLength() > 0) {
                    // Hopefully we have a transposition move now
                    transpositionMove = _pvList[ply].moves[_pvList[ply].head];
                }
            }
        }
        // ## ENDOF Internal Iterative Deepening

        // Initialize the move generator
        MoveGenerator.initializeMain(attack, ply, transpositionMove);

        // Initialize Single-Response Extension
        boolean isSingleReply;
        isSingleReply = isCheck && attack.numberOfMoves == 1;

        int move;
        while ((move = MoveGenerator.getNextMove()) != Move.NOMOVE) {
            // ## BEGIN Minor Promotion Pruning
            if (Configuration.useMinorPromotionPruning && !this._analyzeMode
                    && Move.getType(move) == MoveType.PAWNPROMOTION
                    && Move.getPromotion(move) != PieceType.QUEEN) {
                assert Move.getPromotion(move) == PieceType.ROOK
                        || Move.getPromotion(move) == PieceType.BISHOP
                        || Move.getPromotion(move) == PieceType.KNIGHT;
                continue;
            }
            // ## ENDOF Minor Promotion Pruning

            // Extension
            int newDepth = getNewDepth(depth, move, isSingleReply, mateThreat);

            // ## BEGIN Extended Futility Pruning
            // Notes: Ideas from
            // http://supertech.lcs.mit.edu/~heinz/dt/node18.html
            if (Configuration.useExtendedFutilityPruning) {
                if (!pvNode
                        && depth == 2
                        && newDepth == 1
                        && !isCheck
                        && (Configuration.useCheckExtension || !_board
                                .isCheckingMove(move))
                        && !isDangerousMove(move)) {
                    assert !_board.isCheckingMove(move);
                    assert Move.getType(move) != MoveType.PAWNPROMOTION
                            : _board.convertToGameBoard() + ", " + Move.toString(move);

                    if (evalValue == Value.INFINITY) {
                        // Store evaluation
                        evalValue = evaluateBoard();
                    }
                    int value = evalValue + FUTILITY_PREFRONTIERMARGIN;

                    // Add the target value to the eval
                    int target = Move.getTarget(move);
                    if (target != Piece.NOPIECE) {
                        value += Piece.getValueFromChessman(target);
                    }

                    // If we cannot reach alpha do not look at the move
                    if (value <= alpha) {
                        if (value > bestValue) {
                            bestValue = value;
                        }
                        continue;
                    }
                }
            }
            // ## ENDOF Extended Futility Pruning

            // ## BEGIN Futility Pruning
            // Notes: Ideas from
            // http://supertech.lcs.mit.edu/~heinz/dt/node18.html
            if (Configuration.useFutilityPruning) {
                if (!pvNode
                        && depth == 1
                        && newDepth == 0
                        && !isCheck
                        && (Configuration.useCheckExtension || !_board
                                .isCheckingMove(move))
                        && !isDangerousMove(move)) {
                    assert !_board.isCheckingMove(move);
                    assert Move.getType(move) != MoveType.PAWNPROMOTION
                            : _board.convertToGameBoard() + ", " + Move.toString(move);

                    if (evalValue == Value.INFINITY) {
                        // Store evaluation
                        evalValue = evaluateBoard();
                    }
                    int value = evalValue + FUTILITY_FRONTIERMARGIN;

                    // Add the target value to the eval
                    int target = Move.getTarget(move);
                    if (target != Piece.NOPIECE) {
                        value += Piece.getValueFromChessman(target);
                    }

                    // If we cannot reach alpha do not look at the move
                    if (value <= alpha) {
                        if (value > bestValue) {
                            bestValue = value;
                        }
                        continue;
                    }
                }
            }
            // ## ENDOF Futility Pruning

            // ## BEGIN Late Move Reduction
            // Notes: Ideas from: http://www.glaurungchess.com/lmr.html
            boolean reduced = false;
            if (Configuration.useLateMoveReduction) {
                if (!pvNode
                        && searchedMoves >= LMR_MOVENUMBER_MINIMUM
                        && depth >= LMR_DEPTH
                        && newDepth < depth
                        && !isCheck
                        && (Configuration.useCheckExtension || !_board
                                .isCheckingMove(move))
                        && Move.getTarget(move) == Piece.NOPIECE
                        && !isDangerousMove(move)) {
                    assert !_board.isCheckingMove(move);
                    assert Move.getType(move) != MoveType.PAWNPROMOTION
                            : _board.convertToGameBoard() + ", " + Move.toString(move);

                    newDepth--;
                    reduced = true;
                }
            }
            // ## ENDOF Late Move Reduction

            // Do move
            _board.makeMove(move);

            // ## BEGIN Principal Variation Search
            int value;
            if (!pvNode || bestValue == -Value.INFINITY) {
                // First move
                value = -alphaBeta(newDepth, -beta, -alpha, ply + 1, pvNode,
                        true);
            } else {
                if (newDepth >= depth) {
                    value = -alphaBeta(depth - 1, -alpha - 1, -alpha,
                            ply + 1, false, true);
                } else {
                    value = -alphaBeta(newDepth, -alpha - 1, -alpha,
                            ply + 1, false, true);
                }
                if (value > alpha && value < beta) {
                    // Research again
                    value = -alphaBeta(newDepth, -beta, -alpha, ply + 1,
                            true, true);
                }
            }
            // ## ENDOF Principal Variation Search

            // ## BEGIN Late Move Reduction Research
            if (Configuration.useLateMoveReductionResearch) {
                if (reduced && value >= beta) {
                    // Research with original depth
                    newDepth++;
                    value = -alphaBeta(newDepth, -beta, -alpha, ply + 1,
                            pvNode, true);
                }
            }
            // ## ENDOF Late Move Reduction Research

            // Undo move
            _board.undoMove(move);

            if (this._stopped && this._canStop) {
                break;
            }

            // Update
            searchedMoves++;

            // Pruning
            if (value > bestValue) {
                bestValue = value;
                addPv(_pvList[ply], _pvList[ply + 1], move);

                // Do we have a better value?
                if (value > alpha) {
                    bestMove = move;
                    hashType = Bound.EXACT;
                    alpha = value;

                    // Is the value higher than beta?
                    if (value >= beta) {
                        // Cut-off

                        hashType = Bound.LOWER;
                        break;
                    }
                }
            }
        }

        MoveGenerator.destroy();

        // If we cannot move, check for checkmate and stalemate.
        if (bestValue == -Value.INFINITY) {
            if (isCheck) {
                // We have a check mate. This is bad for us, so return a
                // -CHECKMATE.
                hashType = Bound.EXACT;
                bestValue = -Value.CHECKMATE + ply;
            } else {
                // We have a stale mate. Return the draw value.
                hashType = Bound.EXACT;
                bestValue = Value.DRAW;
            }
        }

        if (!(this._stopped && this._canStop)) {
            if (bestMove != Move.NOMOVE) {
                addGoodMove(bestMove, depth, ply);
            }
            this._transpositionTable.put(_board.zobristCode, depth, bestValue,
                    hashType, bestMove, mateThreat, ply);
        }

        return bestValue;
    }

    private int quiescent(int checkingDepth, int alpha, int beta, int height,
            boolean pvNode, boolean useTranspositionTable) {

        updateSearch(height);

        // Abort conditions
        if ((this._stopped && this._canStop) || height == Depth.MAX_PLY) {
            return evaluateBoard();
        }

        // Check the repetition table and fifty move rule
        if (_board.isRepetition() || _board.halfMoveClock >= 100) {
            return Value.DRAW;
        }

        // ## BEGIN Mate Distance Pruning
        if (Configuration.useMateDistancePruning) {
            int value = -Value.CHECKMATE + height;
            if (value > alpha) {
                alpha = value;
                if (value >= beta) {
                    return value;
                }
            }
            value = -(-Value.CHECKMATE + height + 1);
            if (value < beta) {
                beta = value;
                if (value <= alpha) {
                    return value;
                }
            }
        }
        // ## ENDOF Mate Distance Pruning

        // Check the transposition table first
        if (Configuration.useTranspositionTable && useTranspositionTable) {
            TranspositionTable.TranspositionTableEntry entry = this._transpositionTable.get(_board.zobristCode);
            if (entry != null) {
                _nodeCacheHits.getAndIncrement();
                assert entry.depth >= checkingDepth;
                int value = entry.getValue(height);
                int type = entry.type;

                switch (type) {
                    case Bound.LOWER:
                        if (value >= beta) {
                            return value;
                        }
                        break;
                    case Bound.UPPER:
                        if (value <= alpha) {
                            return value;
                        }
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
        }

        // Get the attack
        // Notes: Ideas from Fruit. Storing all attacks here seems to be a good
        // idea.
        Attack attack = _board.getAttack(_board.activeColor);
        boolean isCheck = attack.isCheck();

        // Initialize
        int hashType = Bound.UPPER;
        int bestValue = -Value.INFINITY;
        int evalValue = Value.INFINITY;

        if (!isCheck) {
            // Stand pat
            int value = evaluateBoard();

            // Store evaluation
            evalValue = value;

            // Pruning
            bestValue = value;

            // Do we have a better value?
            if (value > alpha) {
                hashType = Bound.EXACT;
                alpha = value;

                // Is the value higher than beta?
                if (value >= beta) {
                    // Cut-off

                    hashType = Bound.LOWER;

                    if (useTranspositionTable) {
                        assert checkingDepth == 0;
                        this._transpositionTable
                        .put(_board.zobristCode, 0, bestValue, hashType,
                                Move.NOMOVE, false, height);
                    }

                    return bestValue;
                }
            }
        } else {
            // Check Extension
            checkingDepth++;
        }

        // Initialize the move generator
        MoveGenerator.initializeQuiescent(attack, checkingDepth >= 0);

        int move;
        while ((move = MoveGenerator.getNextMove()) != Move.NOMOVE) {

            // ## BEGIN Futility Pruning
            if (Configuration.useDeltaPruning) {
                if (!pvNode && !isCheck && !_board.isCheckingMove(move)
                        && !isDangerousMove(move)) {
                    assert Move.getTarget(move) != Piece.NOPIECE;
                    assert Move.getType(move) != MoveType.PAWNPROMOTION
                            : _board.convertToGameBoard() + ", " + Move.toString(move);

                    int value = evalValue + FUTILITY_QUIESCENTMARGIN;

                    // Add the target value to the eval
                    value += Piece.getValueFromChessman(Move.getTarget(move));

                    // If we cannot reach alpha do not look at the move
                    if (value <= alpha) {
                        if (value > bestValue) {
                            bestValue = value;
                        }
                        continue;
                    }
                }
            }
            // ## ENDOF Futility Pruning

            // Do move
            _board.makeMove(move);

            // count non quiet boards
            _totalNonQuietBoards++;

            // Recurse into Quiescent
            int value = -quiescent(checkingDepth - 1, -beta, -alpha,
                    height + 1, pvNode, false);

            // Undo move
            _board.undoMove(move);

            if (this._stopped && this._canStop) {
                break;
            }

            // Pruning
            if (value > bestValue) {
                bestValue = value;
                addPv(_pvList[height], _pvList[height + 1], move);

                // Do we have a better value?
                if (value > alpha) {
                    hashType = Bound.EXACT;
                    alpha = value;

                    // Is the value higher than beta?
                    if (value >= beta) {
                        // Cut-off

                        hashType = Bound.LOWER;
                        break;
                    }
                }
            }
        }

        MoveGenerator.destroy();

        if (bestValue == -Value.INFINITY) {
            assert isCheck;

            // We have a check mate. This is bad for us, so return a -CHECKMATE.
            bestValue = -Value.CHECKMATE + height;
        }

        if (useTranspositionTable) {
            if (!(this._stopped && this._canStop)) {
                this._transpositionTable.put(_board.zobristCode, 0, bestValue,
                        hashType, Move.NOMOVE, false, height);
            }
        }

        return bestValue;
    }

    /**
     * @return evaluation of current board
     */
    private int evaluateBoard() {
        _totalBoards++;
        return this._evaluation.evaluate(_board);
    }

    /**
     * Returns the new possibly extended search depth.
     *
     * @param depth the current depth.
     * @param move the current move.
     * @param isSingleReply whether we are in check and have only one way out.
     * @param mateThreat whether we have a mate threat.
     * @return the new possibly extended search depth.
     */
    @SuppressWarnings("static-method")
    private int getNewDepth(int depth, int move, boolean isSingleReply, boolean mateThreat) {
        int newDepth = depth - 1;

        assert (Move.getEnd(move) != _board.captureSquare)
        || (Move.getTarget(move) != Piece.NOPIECE);

        // ## Recapture Extension
        if (Configuration.useRecaptureExtension
                && Move.getEnd(move) == _board.captureSquare
                && See.seeMove(move, Move.getChessmanColor(move)) > 0) {
            newDepth++;
        }

        // ## Check Extension
        else if (Configuration.useCheckExtension && _board.isCheckingMove(move)) {
            newDepth++;
        }

        // ## Pawn Extension
        else if (Configuration.usePawnExtension
                && Move.getChessman(move) == PieceType.PAWN
                && Square.getRelativeRank(Move.getEnd(move), _board.activeColor) == Rank.r7) {
            newDepth++;
        }

        // ## Single-Reply Extension
        else if (Configuration.useSingleReplyExtension && isSingleReply) {
            newDepth++;
        }

        // ## Mate Threat Extension
        else if (Configuration.useMateThreatExtension && mateThreat) {
            newDepth++;
        }

        // Extend another ply if we enter a pawn endgame
        if (Position.materialCount[_board.activeColor] == 0
                && Position.materialCount[Color.switchColor(_board.activeColor)] == 1
                && Move.getTarget(move) != Piece.NOPIECE
                && Move.getTarget(move) != PieceType.PAWN) {
            newDepth++;
        }

        return newDepth;
    }

    private static boolean isDangerousMove(int move) {
        int chessman = Move.getChessman(move);
        int relativeRank = Square.getRelativeRank(Move.getEnd(move),
                _board.activeColor);
        if (chessman == PieceType.PAWN && relativeRank >= Rank.r7) {
            return true;
        }

        int target = Move.getTarget(move);
        if (target == PieceType.QUEEN) {
            return true;
        }

        return false;
    }

    private static void addPv(MoveList destination, MoveList source, int move) {
        assert destination != null;
        assert source != null;
        assert move != Move.NOMOVE;

        destination.resetList();

        destination.moves[destination.tail++] = move;

        for (int i = source.head; i < source.tail; i++) {
            destination.moves[destination.tail++] = source.moves[i];
        }
    }

    private static void addGoodMove(int move, int depth, int height) {
        assert move != Move.NOMOVE;

        if (Move.getTarget(move) != Piece.NOPIECE) {
            return;
        }

        int type = Move.getType(move);
        if (type == MoveType.PAWNPROMOTION) {
            return;
        }

        assert type != MoveType.ENPASSANT;

        _killerTable.add(move, height);
        _historyTable.add(move, depth);
    }

    /**********************************
     * ObservableEngine support
     **********************************/

    private void sendInformation(PrincipalVariation pv, int pvNumber) {
        if (Math.abs(pv.value) > Value.CHECKMATE_THRESHOLD) {
            // Calculate the mate distance
            int mateDepth = Value.CHECKMATE - Math.abs(pv.value);
            sendInformationMate(pv, Integer.signum(pv.value) * (mateDepth + 1) / 2, pvNumber);
        } else {
            sendInformationCentipawns(pv, pvNumber);
        }
    }

    /**
     * Sends the current move and current move number.
     *
     * @param currentMove the current move.
     * @param currentMoveNumber the current move number.
     * @param numberOfMoves
     */
    private void sendInformationMove(GameMove currentMove, int currentMoveNumber, int numberOfMoves) {
        assert currentMove != null;
        assert currentMoveNumber >= 0;

        this._currentMove = currentMove;
        this._currentMoveNumber = currentMoveNumber;

        _fluxengine.setCurrentMove(_currentMove);
        _fluxengine.setCurrentMoveNumber(_currentMoveNumber);
        _fluxengine.setNumberOfMoves(numberOfMoves);
    }

    /**
     * Sends the refutation information.
     *
     * @param refutationList the current refutation move list.
     */
    private void sendInformationRefutations(List<GameMove> refutationList) {
        assert refutationList != null;
        // show refutation list in info text area of the ui or sysout
        GameMoveList gml = new GameMoveList(refutationList);
        _fluxengine.printVerboseInfo(String.format("%s%n", gml.toString()));
    }

    /**
     * Sends the current depth.
     */
    private void sendInformationDepth() {
        _fluxengine.setCurrentSearchDepth(_currentDepth);
        _fluxengine.setCurrentMaxSearchDepth(_currentMaxDepth);
    }

    /**
     * Sends the current status.
     */
    private void sendInformationStatus() {
        _fluxengine.setCurrentSearchDepth(_currentDepth);
        _fluxengine.setCurrentMaxSearchDepth(_currentMaxDepth);
        _fluxengine.setCurrentNodesPerSecond(getCurrentNps());
        _fluxengine.setCurrentUsedTime(System.currentTimeMillis() - _totalTimeStart);
        _fluxengine.setTotalNodes(_totalNodes);
        _fluxengine.setTotalBoards(_totalBoards);
        _fluxengine.setTotalNonQuietBoards(_totalNonQuietBoards);
        if (_currentMove != null) {
            _fluxengine.setCurrentMove(_currentMove);
            _fluxengine.setCurrentMoveNumber(_currentMoveNumber);
        }
    }

    /**
     * Sends the current status.
     */
    private void sendInformationSummary() {
        _fluxengine.setCurrentSearchDepth(_currentDepth);
        _fluxengine.setCurrentMaxSearchDepth(_currentMaxDepth);
        _fluxengine.setCurrentNodesPerSecond(getCurrentNps());
        _fluxengine.setCurrentUsedTime(System.currentTimeMillis() - _totalTimeStart);
        _fluxengine.setTotalNodes(_totalNodes);
        _fluxengine.setTotalBoards(_totalBoards);
        _fluxengine.setTotalNonQuietBoards(_totalNonQuietBoards);
    }

    /**
     * Sends the centipawn information.
     */
    private void sendInformationCentipawns(PrincipalVariation pv, int pvNumber) {
        assert pv != null;
        assert pvNumber >= 1;
        if (pvNumber <= Configuration.showPvNumber) {
            _fluxengine.setCurrentSearchDepth(_currentDepth);
            _fluxengine.setCurrentMaxSearchDepth(_currentMaxDepth);
            _fluxengine.setCurrentNodesPerSecond(getCurrentNps());
            _fluxengine.setCurrentUsedTime(System.currentTimeMillis() - _totalTimeStart);
            _fluxengine.setTotalNodes(_totalNodes);
            _fluxengine.setTotalBoards(_totalBoards);
            _fluxengine.setTotalNonQuietBoards(_totalNonQuietBoards);
            _fluxengine.setCurrentPV(pv.pv);
            final GameMove currentBestMove = pv.pv.get(0);
            currentBestMove.setValue(pv.value);
            _fluxengine.setCurrentMaxValueMove(currentBestMove);
        }
    }

    /**
     * Sends the mate information.
     *
     * @param currentMateDepth the current mate depth.
     */
    private void sendInformationMate(PrincipalVariation pv,
            int currentMateDepth, int pvNumber) {
        assert pv != null;
        assert pvNumber >= 1;

        if (pvNumber <= Configuration.showPvNumber) {
            _fluxengine.setCurrentSearchDepth(_currentDepth);
            _fluxengine.setCurrentMaxSearchDepth(_currentMaxDepth);
            _fluxengine.setCurrentNodesPerSecond(getCurrentNps());
            _fluxengine.setCurrentUsedTime(System.currentTimeMillis() - _totalTimeStart);
            _fluxengine.setTotalNodes(_totalNodes);
            _fluxengine.setTotalBoards(_totalBoards);
            _fluxengine.setTotalNonQuietBoards(_totalNonQuietBoards);
            _fluxengine.setCurrentPV(pv.pv);
            final GameMove currentBestMove = pv.pv.get(0);
            currentBestMove.setValue(pv.value);
            _fluxengine.setCurrentMaxValueMove(currentBestMove);
        }
    }

    /**
     * Returns the current nps.
     *
     * @return the current nps.
     */
    private long getCurrentNps() {
        long currentNps = 0;
        long currentTimeDelta = System.currentTimeMillis() - this._totalTimeStart;
        currentNps = (this._totalNodes * 1000) / (currentTimeDelta+1);  // +1 to avoid div 0
        return currentNps;
    }

    public long getNodeCacheHits() {
        return _nodeCacheHits.get();
    }

    public long getNodeCacheMisses() {
        return _nodeCacheMisses.get();
    }

    public int getCurrentBoardCacheSize() {
        if (_evaluation == null || _evaluation.getEvaluationTable() == null) return 0;
        return _evaluation.getEvaluationTable().getSize();
    }

    public int getCurBoardsInCache() {
        if (_evaluation == null || _evaluation.getEvaluationTable() == null) return 0;
        return _evaluation.getEvaluationTable().getNumberOfEntries();
    }

    public long getBoardCacheHits() {
        if (_evaluation == null) return 0;
        return _evaluation.getBoardCacheHits();
    }

    public long getBoardCacheMisses() {
        if (_evaluation == null) return 0;
        return _evaluation.getBoardCacheMisses();
    }
}
