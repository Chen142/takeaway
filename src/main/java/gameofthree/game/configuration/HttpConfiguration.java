package gameofthree.game.configuration;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfiguration {

  @Bean
  public RestTemplate restTemplate(
      @Value("${game.http.connect.timeoutMs : 30}") int connectTimeout,
      @Value("${game.http.read.timeoutMs : 100}") int readTimeout
  ) {
    return new RestTemplateBuilder()
        .setConnectTimeout(Duration.ofMillis(connectTimeout))
        .setReadTimeout(Duration.ofMillis(readTimeout))
        .build();
  }

}
