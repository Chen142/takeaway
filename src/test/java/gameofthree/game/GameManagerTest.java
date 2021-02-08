package gameofthree.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import gameofthree.game.exceptions.GameRunningException;
import gameofthree.game.exceptions.InvalidStepException;
import gameofthree.game.exceptions.TooManyWaitingGamesException;
import gameofthree.testutil.TestUtil;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class GameManagerTest {

  @Test
  void testStartAGameAsStarter()
      throws GameRunningException, InterruptedException, TooManyWaitingGamesException {
    GameManager gameManager = new GameManager(() -> new Game("test", 3),
        1, 1);
    AtomicReference<Integer> numberPlayed = new AtomicReference<>(0);
    gameManager.attachGamePlayListener((g, i) -> numberPlayed.set(i));
    gameManager.startAGameAsStarter();
    TestUtil.check(() ->
      gameManager.getRunningGame().get().getId().equals("test") &&
      gameManager.getRunningGame().get().isGameRunning()
    , 5000);
    assertThat(numberPlayed.get()).isEqualTo(3);

    GameManager gameManager2 = new GameManager(() -> new Game("test", 3),
        1, 1);
    gameManager2.putGameInQueue(new Game("test1", 5));
    gameManager2.startAGameAsStarter();
    TestUtil.check(() ->
            gameManager2.getRunningGame().get().getId().equals("test1")
        , 3000);
  }

  @Test
  void testStartGameAsFollower() throws GameRunningException {
    GameManager gameManager = new GameManager(() -> new Game("test0", 3),
        1, 1);
    gameManager.startGameAsFollower(new Game("test", 100));
    assertThat(gameManager.getRunningGame().get().getId()).isEqualTo("test");
    assertThat(gameManager.getRunningGame().get().isGameRunning()).isEqualTo(true);

    // should cancel the ongoing game if the game starter is not doing anything.
    TestUtil.check(() -> !gameManager.getRunningGame().get().isGameRunning(), 3000);
  }

  @Test
  void testGameLifecycle() throws GameRunningException, InvalidStepException {
    GameManager gameManager = new GameManager(() -> new Game("test0", 3),
        1, 1
    );
    var game = new Game("test", 3);
    gameManager.startGameAsFollower(game);
    game.pushStep(3);// finish the game as winner
    assertThat(gameManager.getRunningGame().get().isGameRunning()).isFalse();
    gameManager.startGameAsFollower(new Game("test2", 2));
    assertThat(gameManager.getGame("test")).isNotEmpty();
    assertThat(gameManager.getGame("test2")).isNotEmpty();
  }

  @Test
  void testException() throws GameRunningException {
    GameManager gameManager = new GameManager(() -> new Game("test0", 3),
        1, 1
    );
    gameManager.startGameAsFollower(new Game("test", 100));
    assertThatThrownBy(() -> gameManager.startGameAsFollower(new Game("test", 100)))
        .hasMessage("An existing game test is running.");
  }

}