package com.pricesparser.service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("ScheduledParseService Tests")
class ScheduledParseServiceTest {

  private ScheduledExecutorService scheduledExecutorService;
  private ProductParseService productParseService;
  private ScheduledParseService scheduledParseService;

  @BeforeEach
  void setUp() {
    scheduledExecutorService = java.util.concurrent.Executors.newScheduledThreadPool(1);
    productParseService = mock(ProductParseService.class);

    when(productParseService.parseProducts(anyList())).thenReturn(List.of());

    scheduledParseService =
        new ScheduledParseService(scheduledExecutorService, productParseService);
  }

  @AfterEach
  void tearDown() {
    scheduledParseService.stopScheduledTasks();
  }

  @Test
  @DisplayName("Должен запустить периодическую задачу")
  void shouldStartScheduledTask() throws InterruptedException {
    scheduledParseService.startScheduledTasks();

    Thread.sleep(6000);

    verify(productParseService, times(1)).parseProducts(anyList());
  }

  @Test
  @DisplayName("Должен остановить периодическую задачу")
  void shouldStopScheduledTask() {
    scheduledParseService.startScheduledTasks();
    scheduledParseService.stopScheduledTasks();

    assertThat(scheduledExecutorService.isShutdown()).isTrue();
  }
}
