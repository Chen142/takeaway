package gameofthree.game.services;

import gameofthree.game.Game;
import gameofthree.game.GameManager;
import gameofthree.game.clients.GameNegotiationClient;
import gameofthree.game.exceptions.GameRunningException;
import gameofthree.game.interfaces.GameInfoDTO;
import gameofthree.game.interfaces.GameNegotiationDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Negotiate with the other player for who will be the next game starter.
 */
@Service
@Slf4j
public class GameNegotiationService {

  private String lastGameId;
  private String nextRoll;

  private final GameNegotiationClient gameNegotiationClient;
  private final GameManager gameManager;


  public GameNegotiationService(
      GameNegotiationClient gameNegotiationClient,
      GameManager gameManager
      ) {
    this.gameNegotiationClient = gameNegotiationClient;
    this.gameManager = gameManager;
  }


  /**
   * Negotiate if the play should start the next game
   * @param lastGameId last game id
   * @param demand if we have a manual game in the queue, we will be in higher priority to be the next game starter
   * @return TRUE if the player is supposed to start the next game, otherwise false
   * @throws GameRunningException Thrown when there is already a game running which have not finished.
   */
  @Async
  public Future<Boolean> shouldStartNextGame(String lastGameId, boolean demand)
      throws GameRunningException {
    GameNegotiationDTO negotiationDTO = new GameNegotiationDTO(lastGameId, nextRoll, demand);
    log.info("Negotiating next game, lastgame : {}, roll: {}, demand: {}", lastGameId, nextRoll, demand);

    try {
      List<GameNegotiationDTO> negotiations = callNegotiate(negotiationDTO);
      for (var roll : negotiations) {
        if (negotiationDTO.compareTo(roll) > 0) {
          log.info("Negotiation result: Next game starter.");
          // we are the winner
          Game nextGame = gameManager.prepareNextGame();
          callConfirm(new GameInfoDTO(nextGame));
          return new AsyncResult<>(Boolean.TRUE);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Exceptional interruption.", e);
    }
    log.info("Negotiation result: Next game follower.");
    return new AsyncResult<>(Boolean.FALSE);
  }

  /**
   * Reply a negotiation request.
   * @param gameNegotiationDTO negotiation request
   * @param demand if we have a manual game in the queue
   * @return negotiations.
   */
  public List<GameNegotiationDTO> replyNegotiation(GameNegotiationDTO gameNegotiationDTO, boolean demand) {
    //if the last game id doesn't match, it is possible that we or the other side hasn't finish the ongoing game,
    //return empty to ask the other side to retry after some time.
    // we don't want to use a signal to WAIT the game to finish here because it can cause http timeout.
    if (!gameNegotiationDTO.getLastGameId().equals(lastGameId)) {
      return Collections.emptyList();
    }
    final GameNegotiationDTO myNegotiation = new GameNegotiationDTO(lastGameId, nextRoll, demand);
    final var result = new ArrayList<GameNegotiationDTO>();
    result.add(gameNegotiationDTO);
    result.add(myNegotiation);
    return result;
  }

  private void callConfirm(GameInfoDTO gameInfoDTO) throws InterruptedException {
    log.info("Confirming next game {} as starter..", gameInfoDTO.getGameId());
    while (!gameNegotiationClient.confirmStarter(gameInfoDTO)) {
      Thread.sleep(3000);
    }
    log.info("Next game confirmed.");
  }

  private List<GameNegotiationDTO> callNegotiate(GameNegotiationDTO negotiationDTO)
      throws InterruptedException {
    boolean retry = false;
    List<GameNegotiationDTO> negotiations = null;
    while (CollectionUtils.isEmpty(negotiations) || negotiations.size() != 2) {
      if (retry) {
        Thread.sleep(3000);
      }
      negotiations = gameNegotiationClient.rollStarter(negotiationDTO);
      retry = true;
    }
    return negotiations;
  }

  /**
   * Should be called when a game ends, or gameofthree.game.application starts.
   * @param lastGameId the id of last game.
   */
  public synchronized void createNextGameContext(String lastGameId) {
    //can be replaced by atomic reference, but it doesn't matter.
    if (this.lastGameId == null || !this.lastGameId.equals(lastGameId)) {
      this.lastGameId = lastGameId;
      nextRoll = UUID.randomUUID().toString();
    }
  }


}
