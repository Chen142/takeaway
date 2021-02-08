package gameofthree.game;

import gameofthree.game.exceptions.GamePlayException;
import gameofthree.game.exceptions.InvalidStepException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * The essential logic of the game.
 * Use callbacks to output numbers and game status changes.
 */
@Getter
public class Game {


  private enum Operation {
    SEND, RECEIVE
  }

  @AllArgsConstructor
  @Data
  public static class GameStep {
    private int number;
    private Operation operation;
  }


  public enum GameResult {
    WIN, LOSS, EXCEPTION
  }

  // game info:
  private final String id;
  private final int firstNumber;
  private boolean isGameStarter;

  //game lifecycle
  private boolean gameRunning;

  // game logs:
  private final List<GameStep> gameSteps = new ArrayList<>();
  private GameResult result;
  private String gameException;

  // game events:
  private Consumer<Game> onGameStarts;
  private Consumer<Integer> onNumberPlayer;
  private Consumer<Game> onGameEnds;

  public Game(String id, int firstNumber) {
    this.id = id;
    this.firstNumber = firstNumber;
    validateFirstNumber(firstNumber);
  }

  private void validateFirstNumber(int firstNumber) {
    if (firstNumber < 2) {
      throw new GamePlayException("Invalid first number");
    }
  }

  public void pushStep(int number) throws InvalidStepException {
    validate(number);
    gameSteps.add(new GameStep(number, Operation.RECEIVE));
    if (number == 1) {
      endGame(GameResult.LOSS);
      return;
    }

    final int nextNumber = playCore(number);
    this.gameSteps.add(new GameStep(nextNumber, Operation.SEND));

    if (onNumberPlayer != null) {
      onNumberPlayer.accept(nextNumber);
    }

    // it can run before sending out the number? It should be fine..
    if (nextNumber == 1) {
      endGame(GameResult.WIN);
    }
  }

  private void endGame(GameResult result) {
    this.result = result;
    this.gameRunning = false;
    if (onGameEnds != null) {
      onGameEnds.accept(this);
    }
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
    // the starter will send the 1st number, which is the same as the number there.
    if (!isGameStarter && gameSteps.isEmpty()) {
      if (firstNumber == number) {
        return;
      } else {
        throw new InvalidStepException("The opposite side is trying to cheat.");
      }
    }
    int lastNumber = gameSteps.get(gameSteps.size() - 1).getNumber();
    if (playCore(lastNumber) != number) {
      throw new InvalidStepException("The opposite side is trying to cheat.");
    }
  }

  /**
   * Start the game
   */
  public synchronized void startGame(boolean asStarter) {
    if (gameRunning) {
      return;
    }
    gameRunning = true;

    this.isGameStarter = asStarter;
    if (onGameStarts != null) {
      onGameStarts.accept(this);
    }
    if (asStarter) {
      gameSteps.add(new GameStep(firstNumber, Operation.SEND));
      if (onNumberPlayer != null) {
        onNumberPlayer.accept(firstNumber);
      }
    }
  }

  /**
   *  End the game abnormally, e.g. when the other player drops
   * @param gameException reason why the game is ended with exception, can be used to print
   */
  public void endGameExceptionally(String gameException) {
    this.gameRunning = false;
    this.gameException = gameException;
    this.result = GameResult.EXCEPTION;
    if (onGameEnds != null) {
      onGameEnds.accept(this);
    }
  }

  /**
   * Set a consumer will be called when a number is played by the player
   * @param onNumberPlayed consumer
   */
  public void setOnNumberPlayed(Consumer<Integer> onNumberPlayed) {
    this.onNumberPlayer = onNumberPlayed;
  }

  /**
   * Set a consumer will be called when the game ends
   * @param onGameEnd consumer
   */
  public void setOnGameEndsCallback(Consumer<Game> onGameEnd) {
    this.onGameEnds = onGameEnd;
  }

  /**
   * Set a consumer will be called when the game starts.
   * @param onGameStarts consumer
   */
  public void setOnGameStartsCallback(Consumer<Game> onGameStarts) {
    this.onGameStarts = onGameStarts;
  }
}
