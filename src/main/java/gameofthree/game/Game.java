package gameofthree.game;

import gameofthree.game.exceptions.InvalidStepException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
public class Game {


  private enum Operation {
    SEND, RECEIVE
  }

  @AllArgsConstructor
  @Data
  private static class GameStep {
    private int number;
    private Operation operation;
  }


  private enum GameResult {
    WIN, LOSS
  }

  // game info:
  private final String id;
  private final int firstNumber;

  private boolean gameRunning;

  // game logs:
  private final List<GameStep> gameSteps = new ArrayList<>();
  private GameResult result;
  private String gameException;

  // game events:
  private Consumer<Game> onGameStarts;
  private Consumer<Integer> playNumber;
  private Consumer<Game> onGameEnds;

  public Game(String id, int firstNumber) {
    this.id = id;
    this.firstNumber = firstNumber;
  }

  public void pushStep(int number) throws InvalidStepException {
    validate(number);
    gameSteps.add(new GameStep(number, Operation.RECEIVE));
    if (number == 1) {
      endGame(GameResult.LOSS);
    }

    final int nextNumber = playCore(number);
    this.gameSteps.add(new GameStep(nextNumber, Operation.SEND));

    if (nextNumber == 1) {
      endGame(GameResult.WIN);
    }
    playNumber.accept(nextNumber);
  }

  private void endGame(GameResult result) {
    this.result = result;
    this.gameRunning = false;
    onGameEnds.accept(this);
  }

  // return a structure containing description if we want to print how the number is calculated.
  private int playCore(int number) {
    if (number % 3 == 2) {
      return (number + 1) / 3;
    } else {
      return number / 3;
    }
  }

  private void validate(int number) throws InvalidStepException {
    if (!gameRunning) {
      throw new InvalidStepException("Game hasn't started.");
    }
    if (number < 0) {
      throw new InvalidStepException("Negative number not allowed.");
    }
    //anti-cheating..
    if (!gameSteps.isEmpty()) {
      int lastNumber = gameSteps.get(gameSteps.size() - 1).getNumber();
      if (playCore(lastNumber) != number) {
        throw new InvalidStepException("The opposite side is trying to cheat.");
      }
    }
  }

  /**
   * Start the game
   */
  public void startGame(boolean asStarter) {
    if (onGameStarts != null) {
      onGameStarts.accept(this);
    }
    gameRunning = true;
    gameSteps.add(new GameStep(firstNumber, Operation.SEND));
    if (asStarter) {
      playNumber.accept(firstNumber);
    }
  }

  /**
   *  End the game abnormally, e.g. when the other player drops
   * @param gameException reason why the game is ended with exception, can be used to print
   */
  public void endGameExceptionally(String gameException) {
    this.gameRunning = false;
    this.gameException = gameException;
    onGameEnds.accept(this);
  }

  /**
   * Set a consumer will be called when a number is played by the player
   * @param playNumber consumer
   */
  public void setPlayNumber(Consumer<Integer> playNumber) {
    this.playNumber = playNumber;
  }

  /**
   * Set a consumer will be called when the game ends
   * @param onGameEnd consumer
   */
  public void setOnGameEnds(Consumer<Game> onGameEnd) {
    this.onGameEnds = onGameEnd;
  }

  /**
   * Set a consumer will be called when the game starts.
   * @param onGameStarts consumer
   */
  public void setOnGameStarts(Consumer<Game> onGameStarts) {
    this.onGameStarts = onGameStarts;
  }
}
