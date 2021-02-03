package gameofthree.game.interfaces;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A simple structure to decide who will be the next game starter.
 * lastGameId - make sure we are negotiating for the same game. - "hallo world" is used for the 1st game.
 * roll - uuid, the player rolled larger value (string compare) will becomes the next game starter.
 * Using UUID to make it simpler as no need to worry about same value.
 * demand - true if a game is scheduled manually, always be the starter when the other side is false.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameNegotiationDTO {
  private String lastGameId;
  private String roll;
  private boolean demand;
}
