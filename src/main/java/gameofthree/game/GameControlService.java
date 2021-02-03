package gameofthree.game;

import gameofthree.game.negotiation.GameNegotiationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class GameControlService implements CommandLineRunner {

  //lastGameId of the 1st game.
  private static final String GAME_ZERO_ID = "hello world";

  private GameNegotiationService gameNegotiationService;
  private GameManager gameManager;

  public GameControlService(
      GameNegotiationService gameNegotiationService,
      GameManager gameManager
  ) {
    this.gameNegotiationService = gameNegotiationService;
    this.gameManager = gameManager;
  }

  public void playNextGame() {

  }


  @Override
  public void run(String... args) throws Exception {
    gameNegotiationService.createNextGameContext(GAME_ZERO_ID);
    if (Boolean.TRUE.equals(gameNegotiationService.shouldStartNextGame(GAME_ZERO_ID, false).get())) {
      //start the game
      gameManager.startAGame();
    }
  }
}
