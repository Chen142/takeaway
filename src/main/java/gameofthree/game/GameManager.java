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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Manage the game status, store history games and games to play
 * Collecting history games is not implemented therefore it can be OOM if too many games get played.
 * (normally we connect to a db for it, ignore that part for this small DEMO).
 */
@Component
@Slf4j
public class GameManager {

  private final GamePlayService gamePlayService;
  private final int restBetweenGamesSec;

  private final ConcurrentMap<String, Game> playedGames = new ConcurrentHashMap<>();// stores history games and ongoing games.
  private final BlockingQueue<Game> queuedGames = new ArrayBlockingQueue<>(10);
  private final Supplier<Game> gameSupplier;
  private Game runningGame = null;


  @Autowired
  public GameManager(
      Supplier<Game> gameCreator,
      GamePlayService gamePlayService,
      @Value("${game.rest.sec:10}") int restBetweenGamesSec) {
    gameSupplier = gameCreator;
    this.gamePlayService = gamePlayService;
    this.restBetweenGamesSec = restBetweenGamesSec;
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
   * Start the game as game starter, create one if the next game isn't prepared.
   * @throws GameRunningException when there is already a game running.
   */
  public synchronized Game startAGame() throws GameRunningException, InterruptedException {
    if (runningGame == null) {
      prepareNextGame();
    }
    if (runningGame.isGameRunning()) {
      throw new GameRunningException(runningGame.getId());
    }
    log.info("Game {} started, play as starter.", runningGame.getId());
    connectGame();
    runningGame.startGame(true);
    playedGames.put(runningGame.getId(), runningGame);
    return runningGame;
  }

  public synchronized Game prepareNextGame() throws GameRunningException, InterruptedException {
    if(runningGame != null && runningGame.isGameRunning()) {
      throw new GameRunningException(runningGame.getId());
    }
    runningGame = queuedGames.poll(restBetweenGamesSec, TimeUnit.SECONDS);
    runningGame = gameSupplier.get();
    return runningGame;
  }


  /**
   * Start a game as follower
   * @throws GameRunningException when there is already a game running.
   */
  public synchronized void startGameAsFollower(Game game) throws GameRunningException {
    if(runningGame != null && runningGame.isGameRunning()) {
      throw new GameRunningException(runningGame.getId());
    }
    runningGame = game;
    connectGame();
    runningGame.startGame(false);
    playedGames.put(runningGame.getId(), runningGame);
    log.info("Game {} started, play as follower.", game.getId());
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

  public void connectGame() {
    gamePlayService.connectGame(runningGame);
  }
}
