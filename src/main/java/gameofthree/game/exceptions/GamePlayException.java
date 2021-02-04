package gameofthree.game.exceptions;

/**
 * Exception communicating with the other player. End the game when it happens.
 */
public class GamePlayException extends RuntimeException {
  public GamePlayException(String msg) {
    super(msg);
  }
}
