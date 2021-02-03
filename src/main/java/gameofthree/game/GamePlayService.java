package gameofthree.game;

import gameofthree.game.negotiation.GameNegotiationService;
import org.springframework.stereotype.Service;

@Service
public class GamePlayService {

  private GameNegotiationService gameNegotiationService;
  private GameStore gameStore;

  public GamePlayService(
      GameNegotiationService gameNegotiationService,
      GameStore gameStore) {
    this.gameNegotiationService = gameNegotiationService;
    this.gameStore = gameStore;
  }

  public void playNextGame() {

  }





}
