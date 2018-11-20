package fko.chessly.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

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
    event = "Game played in Chessly by Frank Kopp program";
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
    // TODO
    return moveList.toString();
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
