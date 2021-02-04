package gameofthree.game.services;

import gameofthree.game.Game;
import gameofthree.game.GameManager;
import gameofthree.game.clients.GamePlayClient;
import gameofthree.game.exceptions.GamePlayException;
import gameofthree.game.exceptions.InvalidStepException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Logic for playing against the other player.
 * Very light for now.
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

  /**
   * play number to the other player.
   * IMPORTANT! - Use a thread other than the http IO thread the gameplay could be running on!
   * @param game the game.
   * @param number the number played.
   */
  @Async
  public void playNumber(Game game, Integer number) {
    try {
      log.info("For game {}: {} played", game.getId(), number);
      gamePlayClient.playNumber(game.getId(), number);
    } catch (GamePlayException e) {
      //the other player must be lost, end the game now...
      //there are some problems here if one player find the other disconnected and starts a new game, the other can
      //still in the old game - the next game negotiation will fail. I guess it is beyond of the project scope. :P
      log.error("Game play error: Game {}, Msg {}.", game.getId(), e.getMessage(), e);
      gameManager.getRunningGame().ifPresent(g -> g.endGameExceptionally(e.getMessage()));
    }
  }

  /**
   * recieve a number from the other player.
   * ! It is ok to do it sync. we have async for playNumber.
   * @param gameId the gameid
   * @param number the number
   * @throws InvalidStepException the number is invalid.
   */
  public void recieveNumber(String gameId, Integer number) throws InvalidStepException {
    log.info("For game {}: {} received.", gameId, number);
    if (gameManager.getRunningGame().map(g -> g.getId().equals(gameId)).isPresent()) {
      gameManager.getRunningGame().get().pushStep(number);
    }
  }
}
