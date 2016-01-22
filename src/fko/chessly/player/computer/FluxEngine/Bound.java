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
package com.fluxchess.flux;

import com.fluxchess.jcpi.models.GenericScore;

final class Bound {

  static final int EXACT = 0;
  static final int UPPER = 1;
  static final int LOWER = 2;
  static final int NOBOUND = 3;

  static final int[] values = {
      EXACT, UPPER, LOWER
  };

  private static final GenericScore[] toGenericScore = {
      GenericScore.EXACT, GenericScore.ALPHA, GenericScore.BETA
  };

  private Bound() {
  }

  static boolean isValid(int bound) {
    switch (bound) {
      case EXACT:
      case UPPER:
      case LOWER:
        return true;
      default:
        return false;
    }
  }

  static GenericScore toGenericScore(int bound) {
    assert isValid(bound);

    return toGenericScore[bound];
  }

}
