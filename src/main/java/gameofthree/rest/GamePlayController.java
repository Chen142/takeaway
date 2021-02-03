package gameofthree.rest;


import gameofthree.game.GameManager;
import gameofthree.game.interfaces.GamePlayDTO;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used to play the game
 */
@RestController
public class GamePlayController {

  private GameManager gameManager;

  @Autowired
  public GamePlayController(GameManager gameManager) {
    this.gameManager = gameManager;
  }

  @GetMapping("/test")
  public String test() {
    return "hello world";
  }

  @PostMapping("/putgame")
  public String queueGame() {
    String gameId = UUID.randomUUID().toString();

    return "random";

  }

  @PostMapping("")
  public void play(GamePlayDTO gamePlayDTO) {

  }

}
