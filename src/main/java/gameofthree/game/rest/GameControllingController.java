package gameofthree.game.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used for manually creating a game, observing game play
 * more features can be included if necessary.
 */
@RestController
@RequestMapping("/admin")
public class GameControllingController {

  /**
   * Using http post as RESTful protocal.
   * @param startNumber the number to start a game with.
   * @return gameid the game id.
   */

  @PostMapping("/games/create")
  public String createGame(int startNumber) {
    //todo
    return null;
  }

  /**
   * return the game play log.
   * @param gameId
   * @return
   */
  @GetMapping("/games/{id}/log")
  public String viewGameLog(String gameId) {
    //todo
    return null;
  }

  @GetMapping("/games/list")
  public String listPlayedGames(String gameId) {
    //todo
    return null;
  }

}
