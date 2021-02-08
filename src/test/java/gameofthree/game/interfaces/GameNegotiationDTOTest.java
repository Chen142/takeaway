package gameofthree.game.interfaces;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GameNegotiationDTOTest {

  @Test
  void testCompare() {
    var roll1 = new GameNegotiationDTO("17fe6646-8f4d-47ba-8f22-d8a7addd9dad",
        "c7aed167-bdb3-4413-8f92-9fcc6b31e81d", false);
    var roll2 = new GameNegotiationDTO("17fe6646-8f4d-47ba-8f22-d8a7addd9dad",
        "ec835b4a-718b-4899-9d3f-e1f5f6bb6b2f", false);
    assertThat(roll1.compareTo(roll2)).isLessThan(0);
    assertThat(roll2.compareTo(roll1)).isGreaterThan(0);

  }
}