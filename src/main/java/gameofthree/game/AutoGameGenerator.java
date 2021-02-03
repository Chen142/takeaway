package gameofthree.game;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class AutoGameGenerator implements Supplier<Game> {

  @Override
  public Game get() {
    return null;
  }
}
