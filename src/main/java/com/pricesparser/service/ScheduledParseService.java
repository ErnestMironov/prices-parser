package com.pricesparser.service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class ScheduledParseService implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(ScheduledParseService.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final ProductParseService productParseService;

  private static final List<String> SCHEDULED_URLS =
      List.of("https://example.com", "https://example.org");

  public ScheduledParseService(ScheduledExecutorService scheduledExecutorService,
      ProductParseService productParseService) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.productParseService = productParseService;
  }

  // @PostConstruct
  public void startScheduledTasks() {
    logger.info("Инициализация периодических задач парсинга");

    scheduledExecutorService.scheduleAtFixedRate(() -> {
      logger.info("=== Запуск периодического парсинга (ScheduledExecutorService) ===");
      try {
        productParseService.parseProducts(SCHEDULED_URLS);
        logger.info("✅ Периодический парсинг завершён");
      } catch (Exception e) {
        logger.error("❌ Ошибка при периодическом парсинге: {}", e.getMessage());
      }
    }, 5, 30, TimeUnit.SECONDS);

    logger.info(
        "Периодическая задача парсинга запланирована: каждые 30 секунд, первая через 5 секунд");
  }

  @PreDestroy
  public void stopScheduledTasks() {
    logger.info("Остановка периодических задач парсинга");
    scheduledExecutorService.shutdown();
    try {
      if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduledExecutorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduledExecutorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void run(String... args) throws Exception {
    logger.info("=== Этап 9: ScheduledExecutorService ===");
    logger
        .info("ScheduledExecutorService - это ExecutorService для выполнения задач по расписанию:");
    logger.info("1. schedule() - выполнить задачу один раз через указанное время");
    logger
        .info("2. scheduleAtFixedRate() - выполнять задачу периодически с фиксированной частотой");
    logger.info(
        "3. scheduleWithFixedDelay() - выполнять задачу периодически с фиксированной задержкой");
    logger.info("Периодический парсинг будет запускаться каждые 30 секунд");
    logger.info("=== Конец демонстрации ScheduledExecutorService ===\n");
  }
}
