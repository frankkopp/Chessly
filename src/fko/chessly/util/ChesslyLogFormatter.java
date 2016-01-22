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

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.text.MessageFormat;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * <p/>
 * The ReversiLogFormatter class ...
 * </p>
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class ChesslyLogFormatter extends Formatter {

    private final Date dat = new Date();
    private final static String format = "{0,time}"; // {0,date}
    private MessageFormat formatter = null;

    private Object[] args = new Object[1];

    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Override
	public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        // Minimize memory allocations here.
        dat.setTime(record.getMillis());
        args[0] = dat;
        StringBuffer text = new StringBuffer();
        if (formatter == null) {
            formatter = new MessageFormat(format);
        }
        formatter.format(args, text, null);
        sb.append(text);
        sb.append(' ');
        if (record.getSourceClassName() != null) {
            String t = record.getSourceClassName();
            t=t.substring(t.lastIndexOf('.')+1);
            sb.append(t);
        } else {
            sb.append(record.getLoggerName());
        }
        if (record.getSourceMethodName() != null) {
            sb.append(' ');
            sb.append(record.getSourceMethodName());
        }
        sb.append(": ");//lineSeparator);
        String message = formatMessage(record);
        sb.append(record.getLevel().getName());
        sb.append(": ");
        sb.append(message);
        sb.append(Character.LINE_SEPARATOR);
        if (record.getThrown() != null) {
            PrintWriter pw = null;
            try {
                StringWriter sw = new StringWriter();
                pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
            sb.append(sw.toString());
            } catch (Exception ex) {
                //ignore
            } finally {
                if (pw!=null) {
                    pw.close();
                }
            }
        }
        return sb.toString();
    }
}
