package gameofthree.game;

import gameofthree.game.exceptions.GameRunningException;
import gameofthree.game.exceptions.TooManyWaitingGamesException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// store the queued games and history games.
@Component
public class GameStore {

  private final ConcurrentMap<String, Game> playedGames = new ConcurrentHashMap<>();// stores history games and ongoing games.
  private final BlockingQueue<Game> queuedGames = new ArrayBlockingQueue<>(10);
  private final Supplier<Game> gameSupplier;
  private Game runningGame = null;

  @Autowired
  public GameStore(Supplier<Game> supplier) {
    gameSupplier = supplier;
  }

  /**
   * Get a history or running game with id when we want to print the details.
   * @param id gameid
   * @return the game.
   */
  public Optional<Game> getGame(String id) {
    return Optional.ofNullable(playedGames.get(id));
  }

  public Optional<Game> getRunningGame() {
    return Optional.ofNullable(runningGame);
  }


  /**
   * Start the game, create one if the next game isn't prepared.
   * @throws GameRunningException when there is already a game running.
   */
  public synchronized Game startAGame() throws GameRunningException, InterruptedException {
    if (runningGame == null) {
      prepareNextGame();
    }
    if (runningGame.isGameRunning()) {
      throw new GameRunningException(runningGame.getId());
    }
    runningGame.startGame(true);
    playedGames.put(runningGame.getId(), runningGame);
    return runningGame;
  }

  public synchronized Game prepareNextGame() throws GameRunningException, InterruptedException {
    if(runningGame != null && runningGame.isGameRunning()) {
      throw new GameRunningException(runningGame.getId());
    }
    //todo move it to configuration
    runningGame = queuedGames.poll(10, TimeUnit.SECONDS);
    runningGame = gameSupplier.get();
    return runningGame;
  }


  /**
   * Start a game as follower
   * @throws GameRunningException when there is already a game running.
   */
  public synchronized Game startGameAsFollower(Game game) throws GameRunningException {
    if(runningGame != null && runningGame.isGameRunning()) {
      throw new GameRunningException(runningGame.getId());
    }
    runningGame = game;
    runningGame.startGame(false);
    playedGames.put(runningGame.getId(), runningGame);
    return runningGame;
  }

  /**
   * Putting a game into the queue, used for manually created game.
   * @param game the game to put
   * @throws TooManyWaitingGamesException throws exception when there are too many game waiting in the queue.
   */
  public void putGameInQueue(Game game) throws TooManyWaitingGamesException {
    try {
      queuedGames.add(game);
    } catch (IllegalStateException e) {
      throw new TooManyWaitingGamesException("Too many games waiting to be played.");
    }
  }

  public boolean hasGameInQueue() {
    return !queuedGames.isEmpty();
  }

}
