package gameofthree.game.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GamePlayClient {

  private final RestTemplate restTemplate;
  private final String oppsiteUrl;
  private static final

  @Autowired
  public GamePlayClient(
      RestTemplate restTemplate,
      @Value("${game.opposite.url}") String oppositeUrl
  ) {
    this.restTemplate = restTemplate;
    this.oppsiteUrl = oppositeUrl;
  }

  public void playNumber(String id, Integer number) {

  }
}
