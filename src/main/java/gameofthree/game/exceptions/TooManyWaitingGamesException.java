package gameofthree.game.exceptions;

/**
 * Exception for too many games in the queue while manually creating more games.
 */
public class TooManyWaitingGamesException extends Exception {

  public TooManyWaitingGamesException(String msg) {
    super(msg);
  }


}
