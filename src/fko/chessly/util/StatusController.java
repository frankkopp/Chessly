/*
 * <p>GPL Dislaimer</p>
 * <p>
 * "Chessly by Frank Kopp"
 * Copyright (c) 2003-2015 Frank Kopp
 * mail-to:frank@familie-kopp.de
 *
 * This file is part of "Chessly by Frank Kopp".
 *
 * "Chessly by Frank Kopp" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * "Chessly by Frank Kopp" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Chessly by Frank Kopp"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </p>
 *
 *
 */

package fko.chessly.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * <p>This class is a helper to support classes which rely on certain states.</p>
 * <p>It defines certain methods to act on the status like wait for a certain state
 * or get and set the state.</p>
 * <p/>
 * <p>If this class shall check transitions from one state to the other it should be extended and the
 * checkTransition() method should be overwritten accordingly</p>
 * <p/>
 * <i>Example:</i><br/>
 * <code>
 * private static class PlayerStatusController extends StatusController {
 * private PlayerStatusController(int initialState, String name) {
 * super(initialState, name);
 * this.setTransitionCheck(true);
 * }
 * <p/>
 * protected synchronized boolean checkTransition(int sourceState, int targetState) {
 * if (sourceState == targetState) return true;
 * switch (sourceState) {
 * <p/>
 * // Define which states are allowed when currently in a certain state.
 * case Player.WAITING:
 * switch (targetState) {
 * case Player.THINKING:
 * return true;
 * case Player.STOPPED:
 * return true;
 * default                 :
 * return false;
 * }
 * ...
 * </code>
 * <p/>
 * <p>This class is thread safe</p>
 */
public class StatusController {

    // The current state
    private final AtomicInteger state = new AtomicInteger(0);
    //private volatile int state; 

    // The state which will interrupt any waiting methods
    private final AtomicInteger interruptState = new AtomicInteger(-Integer.MAX_VALUE);

    // Defines if the transition from one state to the other shall be checked
    private final AtomicBoolean transitionCheck = new AtomicBoolean(false);

    private final Object _stateWatchLock = new Object();

    // A ReadWriteLock for better locking options than synchronize
    private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
    private final Lock 					 _readLock = _rwLock.readLock();
    private final Lock 					 _writeLock = _rwLock.writeLock();

    /**
     * Default constructor
     * Defaults to StatusController(0,null)
     */
    public StatusController() {
	this(0);
    }

    /**
     * Constructor setting an initial status and a
     * name for this status object
     *
     * @param initialState
     */
    public StatusController(int initialState) {
	this.state.set(initialState);
    }

    /**
     * Returns the WriteLock of this object.
     *
     * @see ReentrantReadWriteLock
     */
    public Lock writeLock() {
	return _writeLock;
    }

    /**
     * Returns the ReadLock of this object.
     *
     * @see ReentrantReadWriteLock
     */
    public Lock readLock() {
	return _readLock;
    }

    /**
     * Getter for the current state for interupting the wait methods
     * Default is -Interger.MAX_VALUE
     *
     * @return interrupt state
     */
    public int getInterruptState() {
	return interruptState.get();
    }

    /**
     * Sets a state for interrupting the wait methods
     * Default is -Interger.MAX_VALUE
     *
     * @param newInterruptState
     */
    public void setInterruptState(int newInterruptState) {
	synchronized (_stateWatchLock) {
	    this.interruptState.set(newInterruptState);
	    _stateWatchLock.notifyAll();
	}
    }

    /**
     * Getter for the actual status value of the status object
     *
     * @return state
     */
    public int getStatus() {
	return this.state.get();
    }

    /**
     * Setter for the actual status value of the status object.<br/>
     * Notifies all (this.notifyAll()) waiting objects.<br/>
     * If getTransitionCheck() is true the transition from the current state to the new state will be checked by
     * checkTransition().<br/>
     * If checkTransition() returns false an exception is thrown and a stack trace is printed to System.err.<br/>
     * If getTransitionFaultFatal() is true a fault transition will cause the application to terminate with exit(1).
     */
    public void setStatus(int newState) throws StateTransitionException {
	final int oldState = this.state.get();
	if (transitionCheck.get()) {
	    if (!checkTransition(this.state.get(), newState)) {
		throw new StatusController.StateTransitionException("Transition fault! Tried to transist from " + oldState + " to " + newState);
	    }
	}
	synchronized (_stateWatchLock) {
	    this.state.set(newState);
	    _stateWatchLock.notifyAll();
	}
    }

    /**
     * Test if status object is in a certain state
     *
     * @param status
     * @return true or false
     */
    public boolean inStatus(int status) {
	return this.state.get() == status;
    }

    /**
     * Waits until the status has hanged (oldState != currentState)
     */
    public void waitForStatusChange() {
	final int oldState = state.get();
	while (oldState == state.get()) {
	    try {
		synchronized (_stateWatchLock) {
		    if (oldState == state.get()) _stateWatchLock.wait();	
		}
	    } catch (InterruptedException e) {
		if (interruptState.get() == state.get()) {
		    return;
		}
	    }
	}
    }

    /**
     * Calls this.wait()
     *
     * @throws InterruptedException
     */
    public void waitForStateSet() throws InterruptedException {
	synchronized (_stateWatchLock) {
	    _stateWatchLock.wait();	
	}
    }

    /**
     * Waits for a certain state
     *
     * @param stateExpected
     */
    public void waitForState(int stateExpected) {
	while (stateExpected != state.get() && interruptState.get() != state.get()) {
	    try {
		synchronized (_stateWatchLock) {
		    if (stateExpected != state.get() && interruptState.get() != state.get()) _stateWatchLock.wait();	
		}
	    } catch (InterruptedException e) {
		// ignore
	    } finally {
		if (interruptState.get() == state.get()) { 
		    //noinspection ReturnInsideFinallyBlock
		    return;
		}
	    }
	}
    }

    /**
     * Waits in a certain state
     *
     * @param waitingState
     */
    public void waitWhileInState(int waitingState) {
	while (waitingState == state.get()) {
	    try {
		synchronized (_stateWatchLock) {
		    if (waitingState == state.get()) _stateWatchLock.wait();	
		}
	    } catch (InterruptedException e) {
		// ignore
	    } finally {
		if (interruptState.get() == state.get()) {
		    //noinspection ReturnInsideFinallyBlock
		    return;
		}
	    }
	}
    }

    /**
     * If transition check is turned on every transition via <code>setState(int state)</code>
     * will be checked by calling the <code>checkTransition</code> method. <br>
     * The <code>checkTransition</code> method is meant to be overwritten be sublasses.
     * Depending on the transitionFaultFatal value the program exits with exit
     * code 1 or it just prints a message to System.err.
     *
     * @param bool
     */
    public void setTransitionCheck(boolean bool) {
	this.transitionCheck.set(bool);
    }

    /**
     * If transition check is turned on every transition via <code>setState(int state)</code>
     * will be checked by calling the <code>checkTransition</code> method. <br>
     * The <code>checkTransition</code> method is meant to be overwritten be sublasses.
     * Depending on the transitionFaultFatal value the program exits with exit
     * code 1 or it just prints a message to System.err.
     *
     * @return true or false
     */
    public boolean getTransitionCheck() {
	return this.transitionCheck.get();
    }

    /**
     * This method checks for valid transition. It returns true for a valid transition
     * and false for a illegal transition.
     * This method is meant to be overwritten be a subclass as it returns true always
     * in this implementation.
     *
     * @param sourceState
     * @param targetState
     * @return true when transition is allowed, false otherwise
     */
    protected boolean checkTransition(int sourceState, int targetState) {
	return true;
    }

    /**
     * The StateTransitionException class is thrown when a transition within the StatusController is illegal.
     */
    public static class StateTransitionException extends IllegalStateException {

	private static final long serialVersionUID = 306467158237981330L;

	public StateTransitionException() {
	    super();
	}

	public StateTransitionException(String message) {
	    super(message);
	}

    }
}
