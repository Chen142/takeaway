package gameofthree.game;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class AutoGameGenerator implements Supplier<Game> {

  private final Random random = new Random();

  @Override
  public Game get() {
    return new Game(UUID.randomUUID().toString(), random.nextInt(1000 + 1));
  }
}
