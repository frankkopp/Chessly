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

import fko.chessly.game.GameMove;

import java.util.List;

final class PrincipalVariation implements Comparable<PrincipalVariation> {

    final int moveNumber;
    final int value;
    final int type;
    final int sortValue;
    final List<GameMove> pv;
    final int depth;
    final int maxDepth;
    final long nps;
    final long time;
    final long totalNodes;

    PrincipalVariation(int moveNumber, int value, int type, int sortValue, List<GameMove> pv, int depth, int maxDepth, long nps, long time, long totalNodes) {
        this.moveNumber = moveNumber;
        this.value = value;
        this.type = type;
        this.sortValue = sortValue;
        this.pv = pv;
        this.depth = depth;
        this.maxDepth = maxDepth;
        this.nps = nps;
        this.time = time;
        this.totalNodes = totalNodes;
    }

    @Override
    public int compareTo(PrincipalVariation o) {
        int result;
        if (this.depth > o.depth) {
            result = -1;
        } else if (this.depth == o.depth) {
            if (this.sortValue > o.sortValue) {
                result = -1;
            } else if (this.sortValue == o.sortValue) {
                if (this.moveNumber < o.moveNumber) {
                    result = -1;
                } else if (this.moveNumber == o.moveNumber) {
                    result = 0;
                } else {
                    result = 1;
                }
            } else {
                result = 1;
            }
        } else {
            result = 1;
        }

        return result;
    }

}
