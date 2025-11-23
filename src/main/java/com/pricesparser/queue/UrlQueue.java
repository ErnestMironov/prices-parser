package com.pricesparser.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlQueue {

  private static final Logger logger = LoggerFactory.getLogger(UrlQueue.class);

  private final BlockingQueue<String> queue;
  private final int capacity;

  public UrlQueue(int capacity) {
    this.capacity = capacity;
    this.queue = new LinkedBlockingQueue<>(capacity);
    logger.info("Создана UrlQueue с ёмкостью: {}", capacity);
  }

  public UrlQueue() {
    this.capacity = Integer.MAX_VALUE;
    this.queue = new LinkedBlockingQueue<>();
    logger.info("Создана UrlQueue без ограничения ёмкости");
  }

  public void put(String url) throws InterruptedException {
    queue.put(url);
    logger.debug("URL добавлен в очередь: {} (размер очереди: {})", url, queue.size());
  }

  public boolean offer(String url) {
    boolean result = queue.offer(url);
    if (result) {
      logger.debug("URL добавлен в очередь: {} (размер очереди: {})", url, queue.size());
    } else {
      logger.warn("Не удалось добавить URL в очередь (очередь заполнена): {}", url);
    }
    return result;
  }

  public boolean offer(String url, long timeout, TimeUnit unit) throws InterruptedException {
    boolean result = queue.offer(url, timeout, unit);
    if (result) {
      logger.debug("URL добавлен в очередь: {} (размер очереди: {})", url, queue.size());
    } else {
      logger.warn("Таймаут при добавлении URL в очередь: {}", url);
    }
    return result;
  }

  public String take() throws InterruptedException {
    String url = queue.take();
    logger.debug("URL извлечён из очереди: {} (размер очереди: {})", url, queue.size());
    return url;
  }

  public String poll(long timeout, TimeUnit unit) throws InterruptedException {
    String url = queue.poll(timeout, unit);
    if (url != null) {
      logger.debug("URL извлечён из очереди: {} (размер очереди: {})", url, queue.size());
    } else {
      logger.debug("Таймаут при извлечении URL из очереди");
    }
    return url;
  }

  public int size() {
    return queue.size();
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  public int getCapacity() {
    return capacity;
  }
}
