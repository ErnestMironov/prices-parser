package com.pricesparser.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskService {

  private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

  private final ProductParseService productParseService;

  private static final List<String> SCHEDULED_URLS = List.of("https://example.net");

  public ScheduledTaskService(ProductParseService productParseService) {
    this.productParseService = productParseService;
  }

  @Scheduled(fixedRate = 60000, initialDelay = 10000)
  public void scheduledParseTask() {
    logger.info("=== Запуск периодического парсинга (@Scheduled) ===");
    try {
      productParseService.parseProducts(SCHEDULED_URLS);
      logger.info("✅ Периодический парсинг (@Scheduled) завершён");
    } catch (Exception e) {
      logger.error("❌ Ошибка при периодическом парсинге (@Scheduled): {}", e.getMessage());
    }
  }
}
