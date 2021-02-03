package gameofthree.game.interfaces;

import gameofthree.game.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameInfoDTO {
  private String gameId;
  private int firstNumber;

  public GameInfoDTO(Game game) {
    this.gameId = game.getId();
    this.firstNumber = game.getFirstNumber();
  }

  public Game toGame() {
    return new Game(gameId, firstNumber);
  }

}
