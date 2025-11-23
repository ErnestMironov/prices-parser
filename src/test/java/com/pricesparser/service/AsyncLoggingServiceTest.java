package com.pricesparser.service;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AsyncLoggingService Tests")
class AsyncLoggingServiceTest {

  private AsyncLoggingService asyncLoggingService;

  @BeforeEach
  void setUp() {
    asyncLoggingService = new AsyncLoggingService();
    asyncLoggingService.start();
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    asyncLoggingService.stop();
    Thread.sleep(1000);
  }

  @Test
  @DisplayName("Должен логировать асинхронно")
  void shouldLogAsync() throws InterruptedException {
    asyncLoggingService.logAsync("Test message");

    Thread.sleep(500);

    assertThat(asyncLoggingService.getQueueSize()).isLessThanOrEqualTo(0);
  }

  @Test
  @DisplayName("Должен логировать информацию о товаре")
  void shouldLogProductAsync() throws InterruptedException {
    asyncLoggingService.logProductAsync("https://example.com/product", "Test Product",
        new BigDecimal("99.99"));

    Thread.sleep(500);

    assertThat(asyncLoggingService.getQueueSize()).isLessThanOrEqualTo(0);
  }

  @Test
  @DisplayName("Должен обрабатывать очередь сообщений")
  void shouldProcessQueue() throws InterruptedException {
    for (int i = 0; i < 5; i++) {
      asyncLoggingService.logAsync("Message " + i);
    }

    assertThat(asyncLoggingService.getQueueSize()).isGreaterThanOrEqualTo(0).isLessThanOrEqualTo(5);

    Thread.sleep(2000);

    assertThat(asyncLoggingService.getQueueSize()).isEqualTo(0);
  }
}
