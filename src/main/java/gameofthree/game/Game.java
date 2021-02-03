package gameofthree.game;

import gameofthree.game.exceptions.InvalidateStepException;
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

  private final String id;
  private final int firstNumber;
  private boolean gameRunning;

  private final List<GameStep> gameSteps = new ArrayList<>();
  private GameResult result;
  private String gameException;

  private Consumer<Integer> playNumber;
  private Consumer<Game> onGameEnd;

  public Game(String id, int firstNumber) {
    this.id = id;
    this.firstNumber = firstNumber;
  }

  public void pushStep(int number) throws InvalidateStepException {
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
    onGameEnd.accept(this);
  }

  // return a structure containing description if we want to print how the number is calculated.
  private int playCore(int number) {
    if (number % 3 == 2) {
      return (number + 1) / 3;
    } else {
      return number / 3;
    }
  }

  private void validate(int number) throws InvalidateStepException {
    if (!gameRunning) {
      throw new InvalidateStepException("Game hasn't started.");
    }
    if (number < 0) {
      throw new InvalidateStepException("Negative number not allowed.");
    }
    //anti-cheating..
    if (!gameSteps.isEmpty()) {
      int lastNumber = gameSteps.get(gameSteps.size() - 1).getNumber();
      if (playCore(lastNumber) != number) {
        throw new InvalidateStepException("The opposite side is trying to cheat.");
      }
    }
  }

  /**
   * Start the game
   */
  public void startGame(boolean asStarter) {
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
  }

  public void setPlayNumber(Consumer<Integer> playNumber) {
    this.playNumber = playNumber;
  }

  public void setOnGameEnd(Consumer<Game> onGameEnd) {
    this.onGameEnd = onGameEnd;
  }
}
