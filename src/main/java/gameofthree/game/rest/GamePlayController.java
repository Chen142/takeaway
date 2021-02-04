package gameofthree.game.rest;

import gameofthree.game.exceptions.InvalidStepException;
import gameofthree.game.interfaces.GamePlayDTO;
import gameofthree.game.services.GamePlayService;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used to play the game
 */
@Slf4j
@RestController
@RequestMapping("/game")
public class GamePlayController {

  private GamePlayService gamePlayService;

  @Autowired
  public GamePlayController(GamePlayService gamePlayService) {
    this.gamePlayService = gamePlayService;
  }

  @GetMapping("/test")
  public String test() {
    return "hello world";
  }

  /**
   * Play a number.
   * To make it simpler and the general design consistent, it is not really a RESTful endpoint.
   * Use a dto is post body instead of putting /game/{id}/play/{number}
   * @param gamePlayDTO
   */
  @PostMapping(value = "/play", consumes = "application/json", produces = "application/json")
  public void play(@RequestBody GamePlayDTO gamePlayDTO, HttpServletResponse response) {
    try {
      this.gamePlayService.recieveNumber(gamePlayDTO.getGameId(), gamePlayDTO.getNumber());
    } catch (InvalidStepException e) {
      log.error("Invalid stap played.", e);
      response.setStatus(HttpStatus.BAD_REQUEST.value());
    }
  }

}
