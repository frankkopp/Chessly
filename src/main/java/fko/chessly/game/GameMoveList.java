/**
 * The MIT License (MIT)
 *
 * <p>"Chessly by Frank Kopp"
 *
 * <p>mail-to:frank@familie-kopp.de
 *
 * <p>Copyright (c) 2016 Frank Kopp
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fko.chessly.game;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Extends ArrayList&lt;GameMove&gt; to add a few convenience methods.
 *
 * @author Frank Kopp
 */
public class GameMoveList extends ArrayList<GameMove> {

  /** Constructor */
  public GameMoveList() {
    super();
  }

  /** @param c */
  public GameMoveList(Collection<? extends GameMove> c) {
    super(c);
  }

  /** @param initialCapacity */
  public GameMoveList(final int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Returns a string representing the GameMoveList.
   *
   * @return String with each GameMove
   */
  @Override
  public String toString() {
    String line = "";
    int index = 0;
    int moveCounter = 1;
    for (GameMove move : this) {
      String sbMove = "";
      if (index++ % 2 == 0) { // Move number every second move
        sbMove += moveCounter++ + ". ";
      }
      sbMove += move.toString() + " ";
      line += sbMove;
    }
    return line.toString();
  }

  /**
   * Returns a string representing the GameMoveList including value per GameMove.
   *
   * @return String with each GameMove and its value
   */
  public String toStringWithValues() {
    String line = "";
    int index = 0;
    int moveCounter = 1;
    for (GameMove move : this) {
      String sbMove = "";
      if (index++ % 2 == 0) { // Move number every second move
        sbMove += moveCounter++ + ". ";
      }
      sbMove += move.toString() + " ";
      sbMove += " (" + move.getValue() + ") ";

     line += sbMove;
    }
    return line;
  }

  /** @return return last item in list */
  public GameMove getLast() {
    if (this.size() <= 0) return null;
    return this.get(this.size() - 1);
  }

  /** @return removes the last item */
  public GameMove removeLast() {
    if (this.size() <= 0) return null;
    return this.remove(this.size() - 1);
  }
}
