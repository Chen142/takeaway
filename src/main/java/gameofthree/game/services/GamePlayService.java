package gameofthree.game.services;

import gameofthree.game.Game;
import gameofthree.game.GameManager;
import gameofthree.game.clients.GamePlayClient;
import gameofthree.game.exceptions.InvalidStepException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Play against the other player.
 */
@Service
@Slf4j
public class GamePlayService {

  private final GamePlayClient gamePlayClient;
  private final GameManager gameManager;

  @Autowired
  public GamePlayService(
      GamePlayClient gamePlayClient,
      GameManager gameManager
  ) {
    this.gamePlayClient = gamePlayClient;
    this.gameManager = gameManager;
  }

  public void playNumber(Game game, Integer number) {
    log.info("For game {}: {} played", game.getId(), number);
    gamePlayClient.playNumber(game.getId(), number);
  }

  public void recieveNumber(String gameId, Integer number) throws InvalidStepException {
    if (gameManager.getRunningGame().map(g -> g.getId().equals(gameId)).isPresent()) {
      gameManager.getRunningGame().get().pushStep(number);
    }
  }
}
