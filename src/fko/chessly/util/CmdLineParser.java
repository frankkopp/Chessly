/*
 * Copyright (c) 2001-2003 Steve Purcell.
 * Copyright (c) 2002      Vidar Holen.
 * Copyright (c) 2002      Michal Ceresna.
 * Copyright (c) 2005      Ewan Mellor.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met: Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. Neither the name of the copyright
 * holder nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package fko.chessly.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Largely GNU-compatible command-line options parser. Has short (-v) and
 * long-form (--verbose) option support, and also allows options with
 * associated values (-d 2, --debug 2, --debug=2). Option processing
 * can be explicitly terminated by the argument '--'.
 *
 * Minor modification to avoid warnings in Java 1.5 (Vector->List, Hashtable->Map, etc.)
 *
 * @author Steve Purcell
 * @version $Revision: 1.10 $
 */
public class CmdLineParser {

    /**
     * Base class for exceptions that may be thrown when options are parsed
     */
    public static abstract class OptionException extends Exception {
		private static final long serialVersionUID = 1L;
		protected OptionException(String msg) { super(msg); }
    }

    /**
     * Thrown when the parsed command-line contains an option that is not
     * recognised. <code>getMessage()</code> returns
     * an error string suitable for reporting the error to the user (in
     * English).
     */
    public static class UnknownOptionException extends OptionException {
		private static final long serialVersionUID = 1L;
		UnknownOptionException( String optionName ) {
            this(optionName, "Unknown option '" + optionName + '\'');
        }

        UnknownOptionException( String optionName, String msg ) {
            super(msg);
            this._optionName = optionName;
        }

        /**
         * @return the name of the option that was unknown (e.g. "-u")
         */
        public String getOptionName() { return this._optionName; }
        private String _optionName = null;
    }

    /**
     * Thrown when the parsed commandline contains multiple concatenated
     * short options, such as -abcd, where one is unknown.
     * <code>getMessage()</code> returns an english human-readable error
     * string.
     * @author Vidar Holen
     */
    public static class UnknownSuboptionException
        extends UnknownOptionException {
		private static final long serialVersionUID = 1L;
		private char _suboption;

        UnknownSuboptionException( String option, char suboption ) {
            super(option, "Illegal option: '"+suboption+"' in '"+option+ '\'');
            this._suboption =suboption;
        }
        public char getSuboption() { return _suboption; }
    }

    /**
     * Thrown when the parsed commandline contains multiple concatenated
     * short options, such as -abcd, where one or more requires a value.
     * <code>getMessage()</code> returns an english human-readable error
     * string.
     * @author Vidar Holen
     */
    public static class NotFlagException extends UnknownOptionException {
		private static final long serialVersionUID = 1L;
		private char notflag;

        NotFlagException( String option, char unflaggish ) {
            super(option, "Illegal option: '"+option+"', '"+
                  unflaggish+"' requires a value");
            notflag=unflaggish;
        }

        /**
         * @return the first character which wasn't a boolean (e.g 'c')
         */
        public char getOptionChar() { return notflag; }
    }

    /**
     * Thrown when an illegal or missing value is given by the user for
     * an option that takes a value. <code>getMessage()</code> returns
     * an error string suitable for reporting the error to the user (in
     * English).
     */
    public static class IllegalOptionValueException extends OptionException {
		private static final long serialVersionUID = 1L;
		public IllegalOptionValueException( Option opt, String value ) {
            super("Illegal value '" + value + "' for option " +
                  (opt.shortForm() != null ? '-' + opt.shortForm() + '/' : "") +
                  "--" + opt.longForm());
            this._option = opt;
            this._value = value;
        }

        /**
         * @return the name of the option whose value was illegal (e.g. "-u")
         */
        public Option getOption() { return this._option; }

        /**
         * @return the illegal value
         */
        public String getValue() { return this._value; }
        private Option _option;
        private String _value;
    }

    /**
     * Representation of a command-line option
     */
    public static abstract class Option {

        protected Option( String longForm, boolean wantsValue ) {
            this(null, longForm, wantsValue);
        }

        protected Option( char shortForm, String longForm,
                          boolean wantsValue ) {
            this(new String(new char[]{shortForm}), longForm, wantsValue);
        }

        private Option( String shortForm, String longForm, boolean wantsValue ) {
            if ( longForm == null ) {
                throw new IllegalArgumentException("Null longForm not allowed");
            }
            this._shortForm = shortForm;
            this._longForm = longForm;
            this._wantsValue = wantsValue;
        }

        public String shortForm() { return this._shortForm; }

        public String longForm() { return this._longForm; }

        /**
         * Tells whether or not this option wants a value
         */
        public boolean wantsValue() { return this._wantsValue; }

        public final Object getValue( String arg, Locale locale )
            throws IllegalOptionValueException {
            if ( this._wantsValue ) {
                if ( arg == null ) {
                    throw new IllegalOptionValueException(this, "");
                }
                return this.parseValue(arg, locale);
            }
            else {
                return Boolean.TRUE;
            }
        }

        /**
         * Override to extract and convert an option value passed on the
         * command-line
         */
        protected Object parseValue( String arg, Locale locale )
            throws IllegalOptionValueException {
            return null;
        }

        private String _shortForm = null;
        private String _longForm = null;
        private boolean _wantsValue = false;

        public static class BooleanOption extends Option {
            public BooleanOption( char shortForm, String longForm ) {
                super(shortForm, longForm, false);
            }
            public BooleanOption( String longForm ) {
                super(longForm, false);
            }
        }

        /**
         * An option that expects an integer value
         */
        public static class IntegerOption extends Option {
            public IntegerOption( char shortForm, String longForm ) {
                super(shortForm, longForm, true);
            }
            public IntegerOption( String longForm ) {
                super(longForm, true);
            }
            @Override
			protected Object parseValue( String arg, Locale locale )
                throws IllegalOptionValueException {
                try {
                    return new Integer(arg);
                }
                catch (NumberFormatException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects a long integer value
         */
        public static class LongOption extends Option {
            public LongOption( char shortForm, String longForm ) {
                super(shortForm, longForm, true);
            }
            public LongOption( String longForm ) {
                super(longForm, true);
            }
            @Override
			protected Object parseValue( String arg, Locale locale )
                throws IllegalOptionValueException {
                try {
                    return new Long(arg);
                }
                catch (NumberFormatException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects a floating-point value
         */
        public static class DoubleOption extends Option {
            public DoubleOption( char shortForm, String longForm ) {
                super(shortForm, longForm, true);
            }
            public DoubleOption( String longForm ) {
                super(longForm, true);
            }
            @Override
			protected Object parseValue( String arg, Locale locale )
                throws IllegalOptionValueException {
                try {
                    NumberFormat format = NumberFormat.getNumberInstance(locale);
                    Number num = format.parse(arg);
                    return num.doubleValue();
                }
                catch (ParseException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects a string value
         */
        public static class StringOption extends Option {
            public StringOption( char shortForm, String longForm ) {
                super(shortForm, longForm, true);
            }
            public StringOption( String longForm ) {
                super(longForm, true);
            }
            @Override
			protected Object parseValue( String arg, Locale locale ) {
                return arg;
            }
        }
    }

    /**
     * Add the specified Option to the list of accepted options
     */
    @SuppressWarnings("unchecked")
	public final Option addOption( Option opt ) {
        if ( opt.shortForm() != null ) {
            this.options.put('-' + opt.shortForm(), opt);
        }
        this.options.put("--" + opt.longForm(), opt);
        return opt;
    }

    /**
     * Convenience method for adding a string option.
     * @return the new Option
     */
    public final Option addStringOption( char shortForm, String longForm ) {
        return addOption(new Option.StringOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a string option.
     * @return the new Option
     */
    public final Option addStringOption( String longForm ) {
        return addOption(new Option.StringOption(longForm));
    }

    /**
     * Convenience method for adding an integer option.
     * @return the new Option
     */
    public final Option addIntegerOption( char shortForm, String longForm ) {
        return addOption(new Option.IntegerOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding an integer option.
     * @return the new Option
     */
    public final Option addIntegerOption( String longForm ) {
        return addOption(new Option.IntegerOption(longForm));
    }

    /**
     * Convenience method for adding a long integer option.
     * @return the new Option
     */
    public final Option addLongOption( char shortForm, String longForm ) {
        return addOption(new Option.LongOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a long integer option.
     * @return the new Option
     */
    public final Option addLongOption( String longForm ) {
        return addOption(new Option.LongOption(longForm));
    }

    /**
     * Convenience method for adding a double option.
     * @return the new Option
     */
    public final Option addDoubleOption( char shortForm, String longForm ) {
        return addOption(new Option.DoubleOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a double option.
     * @return the new Option
     */
    public final Option addDoubleOption( String longForm ) {
        return addOption(new Option.DoubleOption(longForm));
    }

    /**
     * Convenience method for adding a boolean option.
     * @return the new Option
     */
    public final Option addBooleanOption( char shortForm, String longForm ) {
        return addOption(new Option.BooleanOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a boolean option.
     * @return the new Option
     */
    public final Option addBooleanOption( String longForm ) {
        return addOption(new Option.BooleanOption(longForm));
    }

    /**
     * Equivalent to {@link #getOptionValue(Option, Object) getOptionValue(o,
     * null)}.
     */
    public final Object getOptionValue( Option o ) {
        return getOptionValue(o, null);
    }


    /**
     * @return the parsed value of the given Option, or null if the
     * option was not set
     */
    @SuppressWarnings("rawtypes")
	public final Object getOptionValue(Option o, Object def ) {
        List v = (List)values.get(o.longForm());

        if (v == null) {
            // Edited by Frank Kopp to avoid null pointer exeptions
            if (o instanceof Option.BooleanOption && def==null) {
                return false;
            }
            return def;
        }
        else if (v.isEmpty()) {
            return null;
        }
        else {
            Object result = v.get(0);
            v.remove(0);
            return result;
        }
    }


    /**
     * @return A List giving the parsed values of all the occurrences of the
     * given Option, or an empty List if the option was not set.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public final List getOptionValues( Option option ) {
        List result = new ArrayList(10);

        while (true) {
            Object o = getOptionValue(option, null);

            if (o == null) {
                return result;
            }
            else {
                result.add(o);
            }
        }
    }


    /**
     * @return the non-option arguments
     */
    public final String[] getRemainingArgs() {
        return this.remainingArgs;
    }

    /**
     * Extract the options and non-option arguments from the given
     * list of command-line arguments. The default locale is used for
     * parsing options whose values might be locale-specific.
     */
    public final void parse( String[] argv )
        throws IllegalOptionValueException, UnknownOptionException {

        // It would be best if this method only threw OptionException, but for
        // backwards compatibility with old user code we throw the two
        // exceptions above instead.

        parse(argv, Locale.getDefault());
    }

    /**
     * Extract the options and non-option arguments from the given
     * list of command-line arguments. The specified locale is used for
     * parsing options whose values might be locale-specific.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public final void parse( String[] argv, Locale locale )
        throws IllegalOptionValueException, UnknownOptionException {

        // It would be best if this method only threw OptionException, but for
        // backwards compatibility with old user code we throw the two
        // exceptions above instead.

        List otherArgs = new ArrayList(10);
        int position = 0;
        this.values = new HashMap(10);
        while ( position < argv.length ) {
            String curArg = argv[position];
            if ( curArg.startsWith("-") ) {
                if ( curArg.equals("--") ) { // end of options
                    position += 1;
                    break;
                }
                String valueArg = null;
                if ( curArg.startsWith("--") ) { // handle --arg=value
                    //noinspection SingleCharacterStringConcatenation
                    int equalsPos = curArg.indexOf("=");
                    if ( equalsPos != -1 ) {
                        valueArg = curArg.substring(equalsPos+1);
                        curArg = curArg.substring(0,equalsPos);
                    }
                } else if(curArg.length() > 2) {  // handle -abcd
                    for(int i=1; i<curArg.length(); i++) {
                        Option opt=(Option)this.options.get
                            ("-"+curArg.charAt(i));
                        if(opt==null) {
                            throw new
                                    UnknownSuboptionException(curArg, curArg.charAt(i));
                        }
                        if(opt.wantsValue()) {
                            throw new
                                    NotFlagException(curArg, curArg.charAt(i));
                        }
                        addValue(opt, opt.getValue(null,locale));

                    }
                    position++;
                    continue;
                }

                Option opt = (Option)this.options.get(curArg);
                if ( opt == null ) {
                    throw new UnknownOptionException(curArg);
                }
                Object value;
                if ( opt.wantsValue() ) {
                    if ( valueArg == null ) {
                        position += 1;
                        if ( position < argv.length ) {
                            valueArg = argv[position];
                        }
                    }
                    value = opt.getValue(valueArg, locale);
                }
                else {
                    value = opt.getValue(null, locale);
                }

                addValue(opt, value);

                position += 1;
            }
            else {
                otherArgs.add(curArg);
                position += 1;
            }
        }
        for ( ; position < argv.length; ++position ) {
            otherArgs.add(argv[position]);
        }

        this.remainingArgs = new String[otherArgs.size()];
        otherArgs.addAll(Arrays.asList(remainingArgs));
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void addValue(Option opt, Object value) {
        String lf = opt.longForm();

        List v = (List)values.get(lf);

        if (v == null) {
            v = new ArrayList(10);
            values.put(lf, v);
        }

        v.add(value);
    }


    private String[] remainingArgs = null;
    @SuppressWarnings("rawtypes")
	private Map options = new HashMap(10);
    @SuppressWarnings("rawtypes")
	private Map values = new HashMap(10);
}
