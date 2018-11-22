package fko.chessly.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Match
 *
 * Stores a match to export it as PGN.
 *
 * <p>The seven tag names of the STR are (in order):
 * <p>Event (the name of the tournament or match event)
 * <p>Site (the location of the event)
 * <p>Date (the starting date of the game) (YYYY.MM.DD)
 * <p>Round (the playing round ordinal of the game)
 * <p>White (the player of the white pieces)
 * <p>Black (the player of the black pieces)
 * <p>Result (the result of the game)
 *
 * http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm
 */
public class Match {

  private static final Logger LOG = LoggerFactory.getLogger(Match.class);

  // Seven Tag Roster
  private String event;
  private String site;
  private LocalDateTime date;
  private String round;
  private String white;
  private String black;
  private Result result;

  // moves
  private GameMoveList moveList;

  public Match(Game game) {
    event = "Game played in Chessly by Frank Kopp";
    site = "?";
    date = LocalDateTime.now();
    round = "-";
    white = game.getPlayerWhite().getName();
    black = game.getPlayerBlack().getName();

    switch (game.getGameWinnerStatus()) {
      case Game.WINNER_WHITE:
        result = Result.WHITE;
        break;
      case Game.WINNER_BLACK:
        result = Result.BLACK;
        break;
      case Game.WINNER_DRAW:
        result = Result.DRAW;
        break;
      case Game.WINNER_NONE_YET:
        // fall through
      default:
        result = Result.UNKNOWN;
    }

    moveList = game.getCurBoard().getMoveHistory();
  }

  /**
   * @return PGN notation of match
   */
  @Override
  public String toString() {

    // prepare some strings
    String dateString = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.ENGLISH).format(date);
    String resultString;
    switch (this.result) {
      case WHITE:
        resultString = "1-0";
        break;
      case BLACK:
        resultString = "0-1";
        break;
      case DRAW:
        resultString = "1/2-1/2";
        break;
      case UNKNOWN:
        // fall through
      default:
        resultString = "*";
        break;
    }

    // build meta data block
    StringBuilder sb = new StringBuilder();
    sb.append("[Event \"").append(event).append("\"]\n");
    sb.append("[Site \"").append(site).append("\"]\n");
    sb.append("[Date \"").append(dateString).append("\"]\n");
    sb.append("[Round \"").append(round).append("\"]\n");
    sb.append("[White \"").append(white).append("\"]\n");
    sb.append("[Black \"").append(black).append("\"]\n");
    sb.append("[Result \"").append(resultString).append("\"]\n");
    sb.append("\n");

    // build lines of moves - new line before line length reaches 80
    StringBuilder line = new StringBuilder();
    int index = 0;
    int moveCounter = 1;
    for (GameMove move : moveList) {
      StringBuilder sbMove = new StringBuilder();
      if (index++ % 2 == 0) { // Move number every second move
        sbMove.append(moveCounter++).append(". ");
      }
      sbMove.append(move.toString()).append(" ");
      if (line.length() + sbMove.length() >= 80) { // cut line before length 80
        sb.append(line).append("\n");
        line = new StringBuilder();
      }
      line.append(sbMove);
    }

    sb.append(line);
    sb.append(" ").append(resultString).append("\n\n");

    return sb.toString();
  }

  public enum Result {
    WHITE,
    BLACK,
    DRAW,
    UNKNOWN;
  }

  public String getEvent() {
    return event;
  }

  public String getSite() {
    return site;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public String getRound() {
    return round;
  }

  public String getWhite() {
    return white;
  }

  public String getBlack() {
    return black;
  }

  public Result getResult() {
    return result;
  }

  public GameMoveList getMoveList() {
    return moveList;
  }
}
