package gameofthree.game;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import gameofthree.game.Game.GameResult;
import gameofthree.game.exceptions.InvalidStepException;
import org.junit.jupiter.api.Test;

class GameTest {

  @Test
  void testPushStep() throws InvalidStepException {
    Game game = new Game("test", 50);
    game.startGame(false);
    game.setOnNumberPlayed(n -> assertThat(n).isEqualTo(17));
    game.pushStep(50);
    game.setOnNumberPlayed(n -> assertThat(n).isEqualTo(2));
    game.pushStep(6);
    game.setOnNumberPlayed(n -> fail("should not be called"));
    game.pushStep(1);
    assertThat(game.getResult()).isEqualTo(GameResult.LOSS);
    assertThat(game.isGameRunning()).isFalse();

    game = new Game("test", 50);
    game.setOnNumberPlayed(n -> assertThat(n).isEqualTo(50));
    game.startGame(true);
    game.setOnNumberPlayed(n -> assertThat(n).isEqualTo(6));
    game.pushStep(17);
    game.setOnNumberPlayed(n -> assertThat(n).isEqualTo(1));
    game.pushStep(2);
    assertThat(game.getResult()).isEqualTo(GameResult.WIN);
    assertThat(game.isGameRunning()).isFalse();
  }

  @Test
  void testPushStepValidate() {
    //50 -> 17 -> 6
    Game game = new Game("test", 50);
    game.setOnNumberPlayed(n -> fail("Validate should not pass."));
    assertThatThrownBy(() -> game.pushStep(5)).hasMessage("Game hasn't started.");
    game.startGame(false);
    assertThatThrownBy(() -> game.pushStep(-1)).hasMessage("Negative number not allowed.");
    assertThatThrownBy(() -> game.pushStep(15)).hasMessage("The opposite side is trying to cheat.");
  }

}