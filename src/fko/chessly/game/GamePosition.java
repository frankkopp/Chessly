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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with "Chessly by Frank Kopp"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * </p>
 */
package fko.chessly.game;

import java.io.Serializable;

/**
 * Simply and lightwight class for x and Y coordinates.
 * Coordinates are int value.
 * Immutable.
 * 
 * @author Frank Kopp
 */
public class GamePosition implements Serializable {

    private static final long serialVersionUID = -5250164431718958510L;

    // immutable
    /** */
    public final int x;
    /** */
    public final int y;
    /** */
    public final String name;

    /**
     * An aray with all 64 possible chess position plus on e extra [0] to indicate invalid positions     
     */
    public final static GamePosition[] positions = { 
            new GamePosition(0, 0, ""),   // non-valid
            new GamePosition(1, 1, "a1"), // 1
            new GamePosition(2, 1, "b1"), // 2
            new GamePosition(3, 1, "c1"), // 3
            new GamePosition(4, 1, "d1"), // 4
            new GamePosition(5, 1, "e1"), // 5
            new GamePosition(6, 1, "f1"), // 6
            new GamePosition(7, 1, "g1"), // 7
            new GamePosition(8, 1, "h1"), // 8
            new GamePosition(1, 2, "a2"), // 9
            new GamePosition(2, 2, "b2"), // 10
            new GamePosition(3, 2, "c2"), // 11
            new GamePosition(4, 2, "d2"), // 12
            new GamePosition(5, 2, "e2"), // 13
            new GamePosition(6, 2, "f2"), // 14
            new GamePosition(7, 2, "g2"), // 15
            new GamePosition(8, 2, "h2"), // 16
            new GamePosition(1, 3, "a3"), // 17
            new GamePosition(2, 3, "b3"), // 18
            new GamePosition(3, 3, "c3"), // 19
            new GamePosition(4, 3, "d3"), // 20
            new GamePosition(5, 3, "e3"), // 21
            new GamePosition(6, 3, "f3"), // 22
            new GamePosition(7, 3, "g3"), // 23
            new GamePosition(8, 3, "h3"), // 24
            new GamePosition(1, 4, "a4"), // 25
            new GamePosition(2, 4, "b4"), // 26
            new GamePosition(3, 4, "c4"), // 27
            new GamePosition(4, 4, "d4"), // 28
            new GamePosition(5, 4, "e4"), // 29
            new GamePosition(6, 4, "f4"), // 30
            new GamePosition(7, 4, "g4"), // 31
            new GamePosition(8, 4, "h4"), // 32
            new GamePosition(1, 5, "a5"), // 33
            new GamePosition(2, 5, "b5"), // 34
            new GamePosition(3, 5, "c5"), // 35
            new GamePosition(4, 5, "d5"), // 36
            new GamePosition(5, 5, "e5"), // 37
            new GamePosition(6, 5, "f5"), // 38
            new GamePosition(7, 5, "g5"), // 39
            new GamePosition(8, 5, "h5"), // 40
            new GamePosition(1, 6, "a6"), // 41
            new GamePosition(2, 6, "b6"), // 42
            new GamePosition(3, 6, "c6"), // 43
            new GamePosition(4, 6, "d6"), // 44
            new GamePosition(5, 6, "e6"), // 45
            new GamePosition(6, 6, "f6"), // 46
            new GamePosition(7, 6, "g6"), // 47
            new GamePosition(8, 6, "h6"), // 48
            new GamePosition(1, 7, "a7"), // 49
            new GamePosition(2, 7, "b7"), // 50
            new GamePosition(3, 7, "c7"), // 51
            new GamePosition(4, 7, "d7"), // 52
            new GamePosition(5, 7, "e7"), // 53
            new GamePosition(6, 7, "f7"), // 54
            new GamePosition(7, 7, "g7"), // 55
            new GamePosition(8, 7, "h7"), // 56
            new GamePosition(1, 8, "a8"), // 57
            new GamePosition(2, 8, "b8"), // 58
            new GamePosition(3, 8, "c8"), // 59
            new GamePosition(4, 8, "d8"), // 60
            new GamePosition(5, 8, "e8"), // 61
            new GamePosition(6, 8, "f8"), // 62
            new GamePosition(7, 8, "g8"), // 63
            new GamePosition(8, 8, "h8"), // 64
    };

    /**
     * @param x
     *            (must be between 1 and 8)
     * @param y
     *            (must be between 1 and 8)
     */
    private GamePosition(int x, int y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    /**
     * Returns GamePostion for given x,y - will null if not a valid
     * chess position.
     * 
     * @param x
     * @param y
     * @return GamePosition for x,y or null for invalid position
     */
    public static GamePosition getGamePosition(int x, int y) {
        if (x<1 || x>8 || y<1 || y>8) return positions[0];
        return positions[getIndex(x, y)];
    }

    /**
     * @param x
     * @param y
     * @return
     */
    private static int getIndex(int x, int y) {
        return (y-1)*8 + x;
    }

    /**
     * Factory method for GamePosition - so we can return one 64
     * pregenerated GamePosition objects.
     * 
     * @param s
     * @return
     */
    public static GamePosition getGamePosition(String s) {
        if (!s.matches("[a-h][1-8]")) throw new IllegalArgumentException("Not a valid move string: " + s);
        int y = Integer.valueOf(s.substring(1, 2));
        int x;
        switch (s.substring(0, 1)) {
            case "a":
                x = 1;
                break;
            case "b":
                x = 2;
                break;
            case "c":
                x = 3;
                break;
            case "d":
                x = 4;
                break;
            case "e":
                x = 5;
                break;
            case "f":
                x = 6;
                break;
            case "g":
                x = 7;
                break;
            case "h":
                x = 8;
                break;
            default:
                throw new IllegalArgumentException("Not a valid position: " + s);
        }
        if (getIndex(x, y) < 1 || getIndex(x, y) > 64) return positions[0];
        return positions[getIndex(x, y)];
    }

    /**
     * @param c
     * @return
     */
    @SuppressWarnings("fallthrough")
    public boolean isPromotionRow(GameColor c) {
        switch (c) {
            case WHITE:
                if (this.x == 8) return true;
            case BLACK:
                if (this.x == 1) return true;
            default:
                throw new IllegalArgumentException("Invalid Color");
        }
    }

    /**
     * @return
     */
    public String toNotationString() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Translate 1..8 to a..h
     * 
     * @param col
     *            1..8
     * @return returns a-h
     */
    public static String getColString(int col) {
        switch (col) {
            case 1:
                return "a";
            case 2:
                return "b";
            case 3:
                return "c";
            case 4:
                return "d";
            case 5:
                return "e";
            case 6:
                return "f";
            case 7:
                return "g";
            case 8:
                return "h";
            default:
                throw new IllegalArgumentException("Not a valid position: " + col);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = getIndex(prime, result) + x;
        result = getIndex(prime, result) + y;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof GamePosition)) { return false; }
        GamePosition other = (GamePosition) obj;
        if (x != other.x) { return false; }
        if (y != other.y) { return false; }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return this; // Immutable object
    }

}
