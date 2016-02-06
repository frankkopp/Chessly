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
package fko.chessly.game;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple clock for timed reversi games<br/>
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class GameClock extends Observable {

    // Player Name
    private String _playerName = "Player";

    // For use in inner class
    private final GameClock _thisClock = this;

    private final static Format digitFormat = new DecimalFormat("00");

    private volatile boolean _isRunning = false;
    private volatile long    _timeSoFar = 0;
    private volatile long    _lastStartTime = 0;
    private volatile long    _alarmTime = 0;

    //private Game    _game = null;
    private Timer   _timer = null;

    /**
     * Creates  a clock.
     */
    public GameClock() {
        this._playerName = "Clock";
    }

    /**
     * Creates  a clock.
     * @param name
     */
    public GameClock(String name) {
        this._playerName = name;
    }

    /**
     * Creates a clock with given alarm settings.
     * @param name
     * @param alarmTime in milliseconds
     */
    public GameClock(String name, long alarmTime) {
        this._playerName = name;
        this._alarmTime = alarmTime;
    }

    /**
     * Creates a clock with given alarm settings.
     * @param name
     * @param alarmTime in milliseconds
     * @param o
     */
    public GameClock(String name, long alarmTime, Observer o) {
        this._playerName = name;
        this._alarmTime = alarmTime;
        this.addObserver(o);
    }

    /**
     * Sets an alarm and changes status object to given state
     * @param time in milliseconds
     * @param o
     */
    public synchronized void setAlarm(long time, Observer o) {
        this._alarmTime = time;
        this.addObserver(o);
    }


    /**
     * Starts the alarm
     */
    public synchronized void startAlarm() {
        if (_timer == null) {
            _timer = new Timer("Clock: "+_playerName);
            _timer.schedule(new AlarmTask(), 50, 50);
        }
    }

    /**
     * Stops the alarm
     */
    public synchronized void stopAlarm() {
        if (_timer != null) {
            _timer.cancel();
            _timer = null;
        }
    }

    /**
     * Starts the timer. Can be stopped through stopClock() and restarted.
     */
    public synchronized void startClock() {
        if (!_isRunning) {
            _lastStartTime = System.currentTimeMillis();
            _isRunning = true;
        }
    }

    /**
     * Stops the timer. Can be restarted to continue measuring time.
     */
    public synchronized void stopClock() {
        if (_isRunning) {
            _timeSoFar += System.currentTimeMillis() - _lastStartTime;
            _isRunning = false;
        }
    }

    /**
     * Gets the cumulated time so far.
     * @return long time in milliseconds
     */
    public synchronized long getTime() {
        if (_isRunning) {
            return _timeSoFar + (System.currentTimeMillis() - _lastStartTime);
        }
        return _timeSoFar;
    }

    /**
     * Resets the timer to 0
     */
    public synchronized void reset() {
        if (!_isRunning) {
            _timeSoFar = 0L;
            _lastStartTime = 0L;
        } else {
            throw new IllegalStateException("Tried to reset a running Clock");
        }
    }

    /**
     * Format the current clock time into hh:mm:ss
     * @return String
     */
    public String getFormattedTime() {
        return formatTime(getTime());
    }


    /**
     * Format a given time into hh:mm:ss
     * @param time
     * @return formatted string
     */
    private static String formatTime(long time) {
        StringBuilder sb = new StringBuilder(digitFormat.format((time / 1000 / 60 / 60)));
        sb.append(':');
        sb.append(digitFormat.format((time / 1000 / 60) % 60));
        sb.append(':');
        sb.append(digitFormat.format((time / 1000) % 60));
        return sb.toString();
    }

    /**
     * The TimerTask doing the incremental time accounting.
     */
    private class AlarmTask extends TimerTask {
        @Override
        public void run() {
            // -- time is up --
            if ((_isRunning && _timeSoFar + (System.currentTimeMillis() - _lastStartTime) >= _alarmTime)
                    || (!_isRunning && _timeSoFar >= _alarmTime)) {
                _thisClock.setChanged();
                _thisClock.notifyObservers();
                _thisClock.stopAlarm(); //Terminate the timer thread
            }
        }
    }

}
