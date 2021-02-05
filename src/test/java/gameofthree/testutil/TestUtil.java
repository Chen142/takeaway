package gameofthree.testutil;


import static org.assertj.core.api.Assertions.fail;

import java.util.function.Supplier;

public interface TestUtil {

  static void check(Supplier<Boolean> condition, long timeout) {
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < timeout) {
      if (condition.get()) {
        return;
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    fail("Check timeout.");
  }

}
