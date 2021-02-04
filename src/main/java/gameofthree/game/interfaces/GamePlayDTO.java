package gameofthree.game.interfaces;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used to communicate between players for a number is played.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GamePlayDTO {
  private String gameId;
  private int number;
}
