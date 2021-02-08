package gameofthree.game.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gameofthree.game.Game;
import gameofthree.game.GameManager;
import gameofthree.game.exceptions.GameRunningException;
import gameofthree.testutil.TestUtil;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.AsyncResult;

@ExtendWith(MockitoExtension.class)
class GameControlServiceTest {

  @Mock
  private GameNegotiationService gameNegotiationService;
  @Mock
  private GameManager gameManager;
  @Mock
  private GamePlayService gamePlayService;

  @Test
  public void testCreateNewGameWhenGameEnds() throws GameRunningException, InterruptedException {
    GameControlService gameControlService =
        new GameControlService(gameNegotiationService, gamePlayService, gameManager, 1000);
    AtomicInteger gameStarted = new AtomicInteger(0);
    when(gameNegotiationService.shouldStartNextGame(any(), anyBoolean()))
        .thenReturn(new AsyncResult<>(true));
    when(gameManager.startAGameAsStarter()).thenAnswer((p) -> {
      gameStarted.incrementAndGet();
      return null;
    });
    Future<?> future = initThread(gameControlService);
    try {
      TestUtil.check(() -> gameStarted.get() == 1, 1000);
      gameControlService.onGameEnds(new Game("test", 10));
      TestUtil.check(() -> gameStarted.get() == 2, 1000);
      verify(gameNegotiationService).shouldStartNextGame(eq("test"), eq(false));
    } finally {
      future.cancel(true);
    }
  }

  private Future<?> initThread(GameControlService gameControlService) {
    return Executors.newSingleThreadExecutor().submit((Runnable) gameControlService::run);
  }

}