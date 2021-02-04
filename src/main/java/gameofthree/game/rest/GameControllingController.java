package gameofthree.game.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used for manually creating a game, observing game play
 * more features can be included if necessary.
 */
@RestController
public class GameControllingController {

  /**
   * Using http post as RESTful protocal.
   * @param startNumber the number to start a game with.
   * @return gameid the game id.
   */
  @PostMapping
  public String createGame(int startNumber) {
    //todo
    return null;

  }

  /**
   * return the game play log.
   * @param gameId
   * @return
   */
  @GetMapping
  public String viewGameLog(String gameId) {
    //todo
    return null;
  }

}
