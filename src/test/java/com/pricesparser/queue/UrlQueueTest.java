package com.pricesparser.queue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UrlQueue Tests")
class UrlQueueTest {

  private UrlQueue urlQueue;

  @BeforeEach
  void setUp() {
    urlQueue = new UrlQueue(5);
  }

  @Test
  @DisplayName("Должен добавить и извлечь URL из очереди")
  void shouldAddAndTakeUrl() throws InterruptedException {
    String url = "https://example.com/test";

    urlQueue.put(url);
    String result = urlQueue.take();

    assertThat(result).isEqualTo(url);
    assertThat(urlQueue.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Должен блокировать поток при извлечении из пустой очереди")
  void shouldBlockWhenQueueIsEmpty() throws InterruptedException {
    CountDownLatch startedLatch = new CountDownLatch(1);
    CountDownLatch blockedLatch = new CountDownLatch(1);

    Thread thread = new Thread(() -> {
      startedLatch.countDown();
      try {
        urlQueue.take();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      blockedLatch.countDown();
    });

    thread.start();
    startedLatch.await(1, TimeUnit.SECONDS);

    Thread.sleep(100);

    assertThat(blockedLatch.getCount()).isEqualTo(1);

    urlQueue.offer("https://example.com/test");

    boolean completed = blockedLatch.await(2, TimeUnit.SECONDS);
    assertThat(completed).isTrue();

    thread.interrupt();
  }

  @Test
  @DisplayName("Должен работать с несколькими URL")
  void shouldWorkWithMultipleUrls() throws InterruptedException {
    String[] urls =
        {"https://example.com/test1", "https://example.com/test2", "https://example.com/test3"};

    for (String url : urls) {
      urlQueue.offer(url);
    }

    assertThat(urlQueue.size()).isEqualTo(3);

    for (String url : urls) {
      String result = urlQueue.take();
      assertThat(result).isIn(urls);
    }

    assertThat(urlQueue.isEmpty()).isTrue();
  }
}
