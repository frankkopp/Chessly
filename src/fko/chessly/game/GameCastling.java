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
 */
package fko.chessly.game;

/**
 * @author fkopp
 */
enum GameCastling {

    WHITE_KINGSIDE(0),
    WHITE_QUEENSIDE(1),
    BLACK_KINGSIDE(2),
    BLACK_QUEENSIDE(3),
    NOCASTLING(4);

    private final int index;

    static final GameCastling[] values = {
        WHITE_KINGSIDE, WHITE_QUEENSIDE,
        BLACK_KINGSIDE, BLACK_QUEENSIDE
    };

    private GameCastling(int i) {
        this.index = i;
    }

    public int getIndex() {
        return index;
    }

    boolean isValid() {
        switch (this) {
            case WHITE_KINGSIDE:
            case WHITE_QUEENSIDE:
            case BLACK_KINGSIDE:
            case BLACK_QUEENSIDE:
                return true;
            case NOCASTLING:
            default:
                return false;
        }
    }

    static GameCastling valueOf(GameColor color, GameCastlingType castlingType) {
        switch (color) {
            case WHITE:
                switch (castlingType) {
                    case KINGSIDE:
                        return WHITE_KINGSIDE;
                    case QUEENSIDE:
                        return WHITE_QUEENSIDE;
                    case NOCASTLINGTYPE:
                    default:
                        throw new IllegalArgumentException();
                }
            case BLACK:
                switch (castlingType) {
                    case KINGSIDE:
                        return BLACK_KINGSIDE;
                    case QUEENSIDE:
                        return BLACK_QUEENSIDE;
                    case NOCASTLINGTYPE:
                    default:
                        throw new IllegalArgumentException();
                }
            case NONE:
            default:
                throw new IllegalArgumentException();
        }
    }

    public String toChar() {
        switch (this) {
            case WHITE_KINGSIDE: return "K";
            case WHITE_QUEENSIDE: return "Q";
            case BLACK_KINGSIDE: return "k";
            case BLACK_QUEENSIDE: return "q";
            case NOCASTLING: 
            default:
                return "-";
        }
    }

}
