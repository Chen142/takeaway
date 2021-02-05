package gameofthree.game.clients;

import gameofthree.game.exceptions.GamePlayException;
import gameofthree.game.interfaces.GamePlayDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GamePlayClient {

  private final RestTemplate restTemplate;
  private final String oppositeUrl;
  private static final String PLAY_ENDPOINT = "/game/play";

  @Autowired
  public GamePlayClient(
      RestTemplate restTemplate,
      @Value("${game.opposite.url}") String oppositeUrl
  ) {
    this.restTemplate = restTemplate;
    this.oppositeUrl = oppositeUrl;
  }

  public void playNumber(String id, Integer number) {
    try {
      final var response = restTemplate.postForEntity(oppositeUrl + PLAY_ENDPOINT,
          new GamePlayDTO(id, number), Void.class);
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new GamePlayException("Unsuccess.. " + response.getStatusCode());
      }
    } catch (Exception e) {
      throw new GamePlayException(e);
    }
  }
}
