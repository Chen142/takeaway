package gameofthree.game;

import gameofthree.game.exceptions.InvalidateStepException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GamePlayService {

  private Game runningGame;

  public void sendNumberToOpposite(int number, String gameId) {
    log.info("For game {}: {} played", gameId, number);
    //todo

  }

  public void recieveNumber(int number, String gameId) throws InvalidateStepException {
    if (runningGame.getId().equals(gameId)) {
      runningGame.pushStep(number);
    } else {
      throw new InvalidateStepException("Incorrect GameId.");
    }
  }

  public void connectGame(Game runningGame) {
    this.runningGame = runningGame;
    runningGame.setPlayNumber(n -> sendNumberToOpposite(n, runningGame.getId()));
  }
}
