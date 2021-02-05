package gameofthree.game.rest;

import gameofthree.game.Game;
import gameofthree.game.GameManager;
import gameofthree.game.exceptions.GamePlayException;
import gameofthree.game.exceptions.TooManyWaitingGamesException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used for manually creating a game, observing game play
 * more features can be included if necessary.
 */
@RestController
@RequestMapping("/admin")
public class GameManagementController {

  private final GameManager gameManager;

  @Autowired
  public GameManagementController(GameManager gameManager) {
    this.gameManager = gameManager;
  }

  /**
   * Using http post as RESTful protocal.
   * @param startNumber the number to start a game with.
   * @return gameid the game id.
   */

  @PostMapping(value = "/games/create", consumes = "application/json", produces = "application/json")
  public ResponseEntity<String> createGame(@RequestBody int startNumber) {
    try {
      final String newId = UUID.randomUUID().toString();
      final Game game = new Game(newId, startNumber);
      gameManager.putGameInQueue(game);
      return ResponseEntity.of(Optional.of(newId));
    } catch (TooManyWaitingGamesException e) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    } catch (GamePlayException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid start number.");
    }
  }

  /**
   * return the game play log.
   * @param gameId the game id
   * @return game log
   */
  @GetMapping("/games/{gameId}/log")
  public String viewGameLog(@PathVariable("gameId") String gameId) {
    final var game = gameManager.getGame(gameId);
    final StringBuilder resultBuilder = new StringBuilder();
    game.ifPresent(g -> {
      g.getGameSteps().forEach(gameStep ->
            resultBuilder.append("[")
                .append(gameStep.getOperation())
                .append(" - ")
                .append(gameStep.getNumber())
                .append("]"));
      resultBuilder.append(g.getResult());
    });
    return resultBuilder.toString();
  }

  @GetMapping("/games/list")
  public String listPlayedGames() {
    final var games = gameManager.listGames();
    //format
    final StringBuilder sb = new StringBuilder("Played Games List: ");
    games.forEach(g -> sb.append("[ ").append(g).append(" ]"));
    return sb.toString();
  }

}
