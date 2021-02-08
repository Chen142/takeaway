package gameofthree.game.services;

import gameofthree.game.Game;
import gameofthree.game.Game.GameResult;
import gameofthree.game.GameManager;
import gameofthree.game.exceptions.GameRunningException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * Overall game controlling, starts a new game when application is ready, or an existing game is done.
 * Connecting Games to GamePlayService so that they can communicate with the other player.
 */
@Service
@Slf4j
public class GameControlService implements CommandLineRunner {

  //lastGameId of the 1st game.
  public static final String GAME_ZERO_ID = "hello world";

  private final int gameKOTimeout;

  private GameNegotiationService gameNegotiationService;
  private GamePlayService gamePlayService;
  private GameManager gameManager;

  //no need volatile, we have barrier in event wait.
  private String theGameFinished = GAME_ZERO_ID;

  public GameControlService(
      GameNegotiationService gameNegotiationService,
      GamePlayService gamePlayService,
      GameManager gameManager,
      @Value("${game.ko.sec:30}") int gameKOTimeout
  ) {
    this.gameNegotiationService = gameNegotiationService;
    this.gameManager = gameManager;
    this.gamePlayService = gamePlayService;
    this.gameKOTimeout = gameKOTimeout;
    initGameEventsHandlers();
  }

  private void initGameEventsHandlers() {
    gameManager.attachGameEndsListener(this::onGameEnds);
    gameManager.attachGamePlayListener(this::onGamePlay);
  }

  private void onGamePlay(Game game, Integer integer) {
    gamePlayService.playNumber(game, integer);
  }

  synchronized void onGameEnds(Game game) {
    log.info("Game {} ends with result: {}", game.getId(),
        game.getResult() == GameResult.EXCEPTION ? game.getGameException() : game.getResult());
    if (game.getResult() == GameResult.EXCEPTION) {
      // the other player is gone, start from the beginning..
      reset();
    } else {
      theGameFinished = game.getId();
      this.notifyAll();
    }
  }

  private synchronized void reset() {
    theGameFinished = GAME_ZERO_ID;
    this.notifyAll();
  }

  /**
   * Start a game when application is ready.
   * @param args
   * @throws Exception Unexpected exceptions
   */
  @Override
  public synchronized void run(String... args) {
    String lastGame = "";
    while (!Thread.currentThread().isInterrupted()) {
      try {
        if (!lastGame.equals(theGameFinished)) {
          // it is null for the 1st round without endgame event triggering the logic.
          lastGame = theGameFinished;
          startANewGame(lastGame);
        }
        this.wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (ExecutionException e) {
        //looks like the other player is gone during the negotiation..
        log.error("Players out of sync, resetting global state..", e);
        theGameFinished = GAME_ZERO_ID;
        lastGame = "";
      } catch (GameRunningException e) {
        log.info("Expected game lifecycle, it should not happen.", e);
        lastGame = "";
      }
    }
  }

  private void startANewGame(String lastGameId)
      throws InterruptedException, ExecutionException, GameRunningException {
    gameNegotiationService.createNextGameContext(lastGameId);
    if (Boolean.TRUE.equals(gameNegotiationService.shouldStartNextGame(lastGameId, gameManager.hasDemandGame()).get())) {
      //start the game
      gameManager.startAGameAsStarter();
    } else {
      //if the kick starter dead right before confirming the next game, we don't want to wait for him forever.
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          if (gameManager.getRunningGame()
              .map(Game::getId)
              .map(id -> id.equals(lastGameId))
              .orElse(false)) {
            log.error("Game starter not confirming a new game.. resetting negotiation...");
            reset();
          }
        }
      }, gameKOTimeout * 1000L);
    }
  }
}
