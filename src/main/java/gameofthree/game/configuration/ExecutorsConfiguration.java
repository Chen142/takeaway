package gameofthree.game.configuration;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorsConfiguration {

  @Bean
  public Executor taskExecutor(
      @Value("${game.thread.core:5}") int coreSize,
      @Value("${game.thread.max:20}") int maxSize,
      @Value("${game.thread.buffer:20}") int bufferSize
      ) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(coreSize);
    executor.setMaxPoolSize(maxSize);
    executor.setQueueCapacity(bufferSize);
    executor.initialize();
    return executor;
  }

}
