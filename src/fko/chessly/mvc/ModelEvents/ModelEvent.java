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
package fko.chessly.mvc.ModelEvents;

/**
 * <p/>
 * The ModelEvent class is the base class for Events send from the ModelObservable
 * to the ModelObservers.<p/>
 * It can be extended to contain more specific information about the event.<p/>
 * The ModelEvent's signal and name are immutable meaning once instantiated they cannot be changed.</p>
 * Therefore a ModelEvent instance itself is immutable as it does not contain any other fields. Subclasses
 * can add mutable fields but the signal and name of the ModelEvent will still be immutable.
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class ModelEvent {

    /**
     * To create a unique name over all instances of this class
     */
    private static int __nameCounter = 0;

    private final String _myName;
    /**
     * A String representing the name of the Event. Is used for toString().<br/>
     * Usually only needed when the signals must be human readable e.g. during
     * debugging.
     */
    public String getName() { return _myName; }

    private final int _mySignal;
    /**
     * Each Event carries a signal. Default is 0.
     * @return The Event's signal
     */
    public int getSignal() { return _mySignal; }
    /**
     * Each Event carries a signal. Default is 0.
     * @return true - if aSignal is identical to the Event'a signal, false otherwise
     */
    public boolean signals(int aSignal) { return _mySignal == aSignal; }

    /**
     * Creates a ModelEvent with a unique name and the default signal (0).
     */
    public ModelEvent() {
        _myName=uniqueName();
        _mySignal=0;
    }

    /**
     * Creates a ModelEvent with the given name and the default signal (0).
     * @param name
     */
    public ModelEvent(String name) {
        this(name, 0);
    }

    /**
     * Creates a ModelEvent with a unique name and the given signal.
     * @param aSignal
     */
    public ModelEvent(int aSignal) {
        _myName=uniqueName();
        _mySignal=aSignal;
    }

    /**
     * Creates a ModelEvent with the given name and the given signal.
     * @param name
     * @param aSignal
     */
    public ModelEvent(String name, int aSignal) {
        _myName=name;
        _mySignal=aSignal;
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return _myName;
    }

    /**
     * Indicates that this ModelEvent equals the given ModelEvent.<br/>
     * This is true exactly when they have the same signal (getSignal()). They
     * do not need to have the same name.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this ModelEvent has the same signal as the obj
     *         argument; <code>false</code> otherwise.
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        return !(obj == null || !(obj instanceof ModelEvent)) && _mySignal == ((ModelEvent) obj).getSignal();
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * <p/>
     * ModeEvents simply return their signal value.
     *
     * @see Object#equals
     * @see Object#hashCode
     * @see java.util.Hashtable
     */
    @Override
    public int hashCode() {
        return _mySignal;
    }

    /**
     * Creates a unique String over all instances of this class.
     *
     * @return unique string
     */
    private static synchronized String uniqueName() {
        return new StringBuilder(20).append("aModelEvent").append(++__nameCounter).toString();
    }

}
