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

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * This class just provides some helper utilities and cannot be instanciated.
 */
public final class HelperTools {

    private final static Format digitFormat = new DecimalFormat("00");
    private final static Format milliFormat = new DecimalFormat("000");

    private HelperTools () {}

    /**
     * get a MByte String from a byte input
     * @param bytes
     * @return String
     */
    public static String getDigit(long digit) {
        Locale.setDefault(new Locale("de", "DE"));
        NumberFormat f = NumberFormat.getInstance();
        if (f instanceof DecimalFormat) {
            f.setMaximumFractionDigits(1);
        }
        return f.format(digit);
    }

    /**
     * get a MByte String from a byte input
     * @param bytes
     * @return String
     */
    public static String getMBytes(long bytes) {
        double d = (Long.valueOf(bytes)).doubleValue() / (1024.0 * 1024.0);
        NumberFormat f = NumberFormat.getInstance();
        if (f instanceof DecimalFormat) {
            f.setMaximumFractionDigits(1);
        }
        return f.format(d);
    }

    /**
     * format a given time into 00:00:00
     * @param time
     * @param milliseconds
     * @return formatted string
     */
    public static String formatTime(long time, boolean milliseconds) {
        StringBuilder sb = new StringBuilder(digitFormat.format((time / 1000 / 60 / 60)));
        sb.append(':');
        sb.append(digitFormat.format((time / 1000 / 60) % 60));
        sb.append(':');
        sb.append(digitFormat.format((time / 1000) % 60));
        if (milliseconds) {
            sb.append('.');
            sb.append(milliFormat.format(time % 1000));
        }
        return sb.toString();
    }
}
