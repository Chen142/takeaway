package gameofthree.game;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Automatically generating a game.
 * Replace it with something waiting for manual creation if we don't want the automation.
 */
@Component
public class AutoGameGenerator implements Supplier<Game> {

  private final Random random = new Random();

  @Override
  public Game get() {
    return new Game(UUID.randomUUID().toString(), random.nextInt(1000 + 1));
  }
}
