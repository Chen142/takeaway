package gameofthree.rest;

import gameofthree.game.GameManager;
import gameofthree.game.exceptions.GameRunningException;
import gameofthree.game.interfaces.GameInfoDTO;
import gameofthree.game.interfaces.GameNegotiationDTO;
import gameofthree.game.negotiation.GameNegotiationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A very simple negotiation Used to decide which game to play next.
 * Exception & Cheating | brain split not considered. (not the major part of the challenge)
 * When a game comes to ends, both players will roll a number (uuid used instead to avoid draw) and call rollStarter() to each other for the next game,
 * when gets called, each player returns the number from self together with the number from the other side as a list containing 2 numbers
 * therefore both side will get the same result so that they both know who will be the next starter.
 * Then the starter will send the next game info to follower and TRUE should be returned when the follower confirms.
 */
@RestController
@RequestMapping("/negotiate")
public class GameNegotiationController {

  private final GameNegotiationService gameNegotiationService;
  private final GameManager gameManager;

  @Autowired
  public GameNegotiationController(GameNegotiationService gameNegotiationService, GameManager gameManager) {
    this.gameNegotiationService = gameNegotiationService;
    this.gameManager = gameManager;
  }

  /**
   * Used to roll next starter
   * @param negotiation used to negotiate.
   * @return a list containing 2 negotiations from each side, so that both players know who will be the next starter.
   */
  @PostMapping(value = "/roll", consumes = "application/json", produces = "application/json")
  public List<GameNegotiationDTO> rollStarter(@RequestBody GameNegotiationDTO negotiation) {
    return gameNegotiationService.replyNegotiation(negotiation, gameManager.hasGameInQueue());
  }

  /**
   * The next game starter calls confirmStarter and sync the next game information.
   * @param gameInfoDTO the next game info
   * @return Always TRUE
   * @throws GameRunningException Thrown when the follower hasn't finish it's ongoing game, the starter will retry after some time.
   */
  @PostMapping(value = "/confirm", consumes = "application/json", produces = "application/json")
  public Boolean confirmStarter(@RequestBody GameInfoDTO gameInfoDTO) throws GameRunningException {
    gameManager.startGameAsFollower(gameInfoDTO.toGame());
    return Boolean.TRUE;
  }


}
