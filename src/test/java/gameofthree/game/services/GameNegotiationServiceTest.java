package gameofthree.game.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import gameofthree.game.Game;
import gameofthree.game.GameManager;
import gameofthree.game.clients.GameNegotiationClient;
import gameofthree.game.exceptions.GameRunningException;
import gameofthree.game.interfaces.GameNegotiationDTO;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameNegotiationServiceTest {

  private final static String MAX_ROLL = "zzzzz";
  private final static String MIN_ROLL = "00000";
  private final static String LAST_GAME = "test";

  @Mock
  private GameNegotiationClient client;
  @Mock
  private GameManager gameManager;


  @Test
  void shouldStartNextGame() throws GameRunningException, ExecutionException, InterruptedException {
    var gameNegotiationService = new GameNegotiationService(client, gameManager);
    gameNegotiationService.createNextGameContext(LAST_GAME);
    when(gameManager.prepareNextGame()).thenReturn(new Game("next", 100));
    when(client.confirmStarter(any())).thenReturn(true);
    // Roll as game follower
    when(client.rollStarter(any())).thenAnswer(p -> {
      GameNegotiationDTO roll = p.getArgument(0);
      return Arrays.asList(roll, new GameNegotiationDTO(LAST_GAME, MAX_ROLL, false));
    });
    assertThat(gameNegotiationService.shouldStartNextGame(LAST_GAME, false).get())
        .isFalse();

    // Roll as game starter
    when(client.rollStarter(any())).thenAnswer(p -> {
      GameNegotiationDTO roll = p.getArgument(0);
      return Arrays.asList(roll, new GameNegotiationDTO(LAST_GAME, MIN_ROLL, false));
    });
    assertThat(gameNegotiationService.shouldStartNextGame(LAST_GAME, false).get())
        .isTrue();

    // Roll as game demanded starter
    when(client.rollStarter(any())).thenAnswer(p -> {
      GameNegotiationDTO roll = p.getArgument(0);
      return Arrays.asList(roll, new GameNegotiationDTO(LAST_GAME, MAX_ROLL, false));
    });
    assertThat(gameNegotiationService.shouldStartNextGame(LAST_GAME, true).get())
        .isTrue();

    // Roll as game demanded follower
    when(client.rollStarter(any())).thenAnswer(p -> {
      GameNegotiationDTO roll = p.getArgument(0);
      return Arrays.asList(roll, new GameNegotiationDTO(LAST_GAME, MAX_ROLL, true));
    });
    assertThat(gameNegotiationService.shouldStartNextGame(LAST_GAME, true).get())
        .isFalse();

  }

  @Test
  void replyNegotiation() {
    var gameNegotiationService = new GameNegotiationService(client, gameManager);
    gameNegotiationService.createNextGameContext(LAST_GAME);
    var roll = new GameNegotiationDTO(LAST_GAME, MAX_ROLL, false);
    var reply = gameNegotiationService.replyNegotiation(roll, true);
    assertThat(reply).hasSize(2);
    assertThat(reply).contains(roll);
    assertThat(reply).anyMatch(GameNegotiationDTO::isDemand);
    assertThat(reply).allMatch(r -> r.getLastGameId().equals(LAST_GAME));



  }

}