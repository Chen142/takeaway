package gameofthree.game.clients;

import gameofthree.game.interfaces.GameInfoDTO;
import gameofthree.game.interfaces.GameNegotiationDTO;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class GameNegotiationClient {

  private final RestTemplate restTemplate;
  private final String oppositeUrl;

  private static final String ROLL_STARTER_ENDPOINT = "/negotiate/roll";
  private static final String CONFIRM_STARTER_ENDPOINT = "/negotiate/confirm";

  @Autowired
  public GameNegotiationClient(
      RestTemplate restTemplate,
      @Value("${game.opposite.url}") String oppositeUrl
  ) {
    this.restTemplate = restTemplate;
    this.oppositeUrl = oppositeUrl;
  }

  public List<GameNegotiationDTO> rollStarter(GameNegotiationDTO gameNegotiationDTO) {
    try {
      final var entity = new HttpEntity<>(gameNegotiationDTO);
      return restTemplate.exchange(
          oppositeUrl + ROLL_STARTER_ENDPOINT,
          HttpMethod.POST,
          entity,
          new ParameterizedTypeReference<List<GameNegotiationDTO>>() {}).getBody();
    } catch (Exception e) {
      log.error("Error communicating with opposite side.");
      return Collections.emptyList();
    }
  }

  public boolean confirmStarter(GameInfoDTO gameInfoDTO) {
    try {
      final var entity = new HttpEntity<>(gameInfoDTO);
      return Boolean.TRUE.equals(restTemplate.exchange(
          oppositeUrl + CONFIRM_STARTER_ENDPOINT,
          HttpMethod.POST,
          entity,
          Boolean.class).getBody());
    } catch (Exception e) {
      log.error("Error communicating with opposite side.");
      return false;
    }
  }



}
