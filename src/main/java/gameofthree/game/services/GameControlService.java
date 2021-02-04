package gameofthree.game.services;

import gameofthree.game.Game;
import gameofthree.game.GameManager;
import gameofthree.game.exceptions.GameRunningException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
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
  private static final String GAME_ZERO_ID = "hello world";

  private GameNegotiationService gameNegotiationService;
  private GamePlayService gamePlayService;
  private GameManager gameManager;

  //no need volatile, we have barrier in event wait.
  private String theGameFinished;

  public GameControlService(
      GameNegotiationService gameNegotiationService,
      GamePlayService gamePlayService,
      GameManager gameManager
  ) {
    this.gameNegotiationService = gameNegotiationService;
    this.gameManager = gameManager;
    this.gamePlayService = gamePlayService;
    initGameEventsHandlers();
  }

  private void initGameEventsHandlers() {
    gameManager.attachGameEndsListener(this::onGameEnds);
    gameManager.attachGamePlayListener(this::onGamePlay);
  }

  private void onGamePlay(Game game, Integer integer) {
    gamePlayService.playNumber(game, integer);
  }

  private synchronized void onGameEnds(Game game) {
    theGameFinished = game.getId();
    // trigger the next game. - do it in another thread, don't block it.
    this.notifyAll();
  }

  /**
   * Start a game when application is ready.
   * A piece of ugly logic..
   * @param args
   * @throws Exception Unexpected exceptions
   */
  @Override
  public synchronized void run(String... args) {
    String lastGame = GAME_ZERO_ID;
    while (!Thread.currentThread().isInterrupted()) {
      try {
        if (!lastGame.equals(theGameFinished)) {
          startANewGame(GAME_ZERO_ID);
          // it is null for the 1st round without endgame event triggering the logic.
          if (theGameFinished != null) {
            lastGame = theGameFinished;
          }
        }
        this.wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (ExecutionException e) {
        log.error("Expected exception.", e);
      } catch (GameRunningException e) {
        log.info("Expected game lifecycle, it should not happen.", e);
      }
    }
  }


  private void startANewGame(String lastGameId)
      throws InterruptedException, ExecutionException, GameRunningException {
    gameNegotiationService.createNextGameContext(lastGameId);
    if (Boolean.TRUE.equals(gameNegotiationService.shouldStartNextGame(GAME_ZERO_ID, false).get())) {
      //start the game
      gameManager.startAGameAsStarter();
    }
  }
}
