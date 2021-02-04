package gameofthree.game.services;

import gameofthree.game.Game;
import gameofthree.game.GameManager;
import gameofthree.game.exceptions.GameRunningException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * Overall game controlling, starts a new game when existing one is done.
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

  private void onGameEnds(Game game) {
    try {
      startANewGame(game.getId());
    } catch (Exception e) {
      log.error("Fatal error..", e);
    }
  }

  /**
   * Start a game when application is ready.
   * @param args
   * @throws Exception Unexpected exceptions
   */
  @Override
  public void run(String... args) throws Exception {
    startANewGame(GAME_ZERO_ID);
  }

  private void startANewGame(String lastGameId)
      throws InterruptedException, java.util.concurrent.ExecutionException, GameRunningException {
    gameNegotiationService.createNextGameContext(lastGameId);
    if (Boolean.TRUE.equals(gameNegotiationService.shouldStartNextGame(GAME_ZERO_ID, false).get())) {
      //start the game
      gameManager.startAGame();
    }
  }
}
