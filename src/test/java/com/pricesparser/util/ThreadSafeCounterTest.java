package com.pricesparser.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ThreadSafeCounter Tests")
class ThreadSafeCounterTest {

  private ThreadSafeCounter counter;

  @BeforeEach
  void setUp() {
    counter = new ThreadSafeCounter();
  }

  @Test
  @DisplayName("Должен увеличивать и уменьшать счётчик")
  void shouldIncrementAndDecrement() {
    assertThat(counter.get()).isEqualTo(0);

    counter.increment();
    assertThat(counter.get()).isEqualTo(1);

    counter.increment();
    assertThat(counter.get()).isEqualTo(2);

    counter.decrement();
    assertThat(counter.get()).isEqualTo(1);
  }

  @Test
  @DisplayName("Должен быть потокобезопасным при многопоточном доступе")
  void shouldBeThreadSafe() throws InterruptedException {
    int numberOfThreads = 10;
    int incrementsPerThread = 100;
    CountDownLatch latch = new CountDownLatch(numberOfThreads);

    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

    for (int i = 0; i < numberOfThreads; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < incrementsPerThread; j++) {
            counter.increment();
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await(5, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(counter.get()).isEqualTo(numberOfThreads * incrementsPerThread);
  }

  @Test
  @DisplayName("Должен сбрасывать счётчик")
  void shouldReset() {
    counter.increment();
    counter.increment();
    assertThat(counter.get()).isEqualTo(2);

    counter.reset();
    assertThat(counter.get()).isEqualTo(0);
  }
}
