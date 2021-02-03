package gameofthree.game.exceptions;

/**
 * Exception when trying to start another game while the existing game is running.
 */
public class GameRunningException extends Exception {

  public GameRunningException(String gameId) {
    super(String.format("An existing game %s is running.", gameId));
  }
}
