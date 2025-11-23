package com.pricesparser.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ThreadPoolConfig Tests")
public class ThreadPoolConfigTest {

  @Autowired
  @Qualifier("productParseExecutor")
  private ExecutorService executorService;

  @Test
  @DisplayName("Должен создать ExecutorService")
  void shouldCreateExecutorService() {
    assertThat(executorService).isNotNull();
    assertThat(executorService).isInstanceOf(ThreadPoolExecutor.class);
  }

  @Test
  @DisplayName("ExecutorService должен быть настроен правильно")
  void shouldHaveCorrectConfiguration() {
    if (executorService instanceof ThreadPoolExecutor threadPool) {
      assertThat(threadPool.getCorePoolSize()).isGreaterThan(0);
      assertThat(threadPool.getMaximumPoolSize())
          .isGreaterThanOrEqualTo(threadPool.getCorePoolSize());
    }
  }
}
