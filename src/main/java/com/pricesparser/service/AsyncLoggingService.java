package com.pricesparser.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class AsyncLoggingService {

  private static final Logger logger = LoggerFactory.getLogger(AsyncLoggingService.class);

  private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
  private Thread daemonThread;
  private volatile boolean running = true;
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @PostConstruct
  public void start() {
    daemonThread = new Thread(() -> {
      logger.info("Демон-поток логирования запущен");

      while (running || !logQueue.isEmpty()) {
        try {
          String logMessage = logQueue.poll(1, TimeUnit.SECONDS);
          if (logMessage != null) {
            processLog(logMessage);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          logger.warn("Демон-поток логирования был прерван");
          break;
        }
      }

      logger.info("Демон-поток логирования завершён");
    }, "AsyncLoggingDaemon");

    daemonThread.setDaemon(true);
    daemonThread.start();
    logger.info("Асинхронный сервис логирования инициализирован (демон-поток)");
  }

  @PreDestroy
  public void stop() {
    running = false;
    if (daemonThread != null && daemonThread.isAlive()) {
      daemonThread.interrupt();
      try {
        daemonThread.join(5000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    logger.info("Асинхронный сервис логирования остановлен");
  }

  public void logAsync(String message) {
    String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
    String logEntry = String.format("[%s] %s", timestamp, message);

    if (!logQueue.offer(logEntry)) {
      logger.warn("Очередь логирования переполнена, сообщение пропущено: {}", message);
    }
  }

  public void logProductAsync(String url, String title, java.math.BigDecimal price) {
    String message =
        String.format("Товар распарсен - URL: %s, Title: %s, Price: %s", url, title, price);
    logAsync(message);
  }

  public void logErrorAsync(String url, String error) {
    String message = String.format("Ошибка при парсинге - URL: %s, Error: %s", url, error);
    logAsync(message);
  }

  public void logStatisticsAsync(int total, int success, int failed) {
    String message = String.format("Статистика парсинга - Всего: %d, Успешно: %d, Ошибок: %d",
        total, success, failed);
    logAsync(message);
  }

  private void processLog(String logMessage) {
    logger.info("[ASYNC] {}", logMessage);
  }

  public int getQueueSize() {
    return logQueue.size();
  }
}
