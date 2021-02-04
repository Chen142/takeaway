package gameofthree.game;

import gameofthree.game.exceptions.GameRunningException;
import gameofthree.game.exceptions.TooManyWaitingGamesException;
import gameofthree.game.services.GamePlayService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Manage the game lifecycle, store history games and the games to play
 * Collecting history games is not implemented therefore it can be OOM if too many games get played.
 * (normally we connect to a db for it, not super important for this small project :P ).
 */
@Component
@Slf4j
public class GameManager {

  private final int restBetweenGamesSec;

  private final ConcurrentMap<String, Game> playedGames = new ConcurrentHashMap<>();// stores history games and ongoing games.
  private final BlockingQueue<Game> queuedGames = new ArrayBlockingQueue<>(10);
  private final Supplier<Game> gameSupplier;

  private Game runningGame = null;

  //game events
  private final List<Consumer<Game>> onGameEnds = new ArrayList<>();
  private final List<Consumer<Game>> onGameStarts = new ArrayList<>();
  private final List<BiConsumer<Game, Integer>> onNumberPlayed = new ArrayList<>();

  @Autowired
  public GameManager(
      Supplier<Game> gameCreator,
      @Value("${game.rest.sec:10}") int restBetweenGamesSec) {
    gameSupplier = gameCreator;
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
    playedGames.put(runningGame.getId(), runningGame);
    attachGameEventsHandlers();
    runningGame.startGame(true);
    return runningGame;
  }

  /**
   * Get the next game, create one if there is no next game.
   * @return
   * @throws GameRunningException
   * @throws InterruptedException
   */
  public synchronized Game prepareNextGame() throws GameRunningException, InterruptedException {
    if (runningGame != null && runningGame.isGameRunning()) {
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

  // game events handler:
  private void attachGameEventsHandlers() {
    runningGame.setOnGameStarts(this::callOnGameStarts);
    runningGame.setOnGameEnds(game -> {
      callOnGameEnds(game);
      detachGameEventsHandlers();
    });
    runningGame.setPlayNumber(this::callOnNumberPlayed);
  }

  private void detachGameEventsHandlers() {
    runningGame.setOnGameStarts(null);
    runningGame.setOnGameEnds(null);
    runningGame.setPlayNumber(null);
  }

  private void callOnGameStarts(Game runningGame) {
    this.onGameStarts.forEach(c -> c.accept(runningGame));
  }

  private void callOnGameEnds(Game game) {
    this.onGameEnds.forEach(c -> c.accept(game));
  }

  private void callOnNumberPlayed(Integer number) {
    this.onNumberPlayed.forEach(c -> c.accept(runningGame, number));
  }

  public void attachGameStartsListener(Consumer<Game> listener) {
    this.onGameStarts.add(listener);

  }
  public void attachGameEndsListener(Consumer<Game> listener) {
    this.onGameEnds.add(listener);

  }
  public void attachGamePlayListener(BiConsumer<Game, Integer> listener) {
    this.onNumberPlayed.add(listener);
  }

}
