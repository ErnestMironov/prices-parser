package com.pricesparser.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ScheduledTaskService implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

  private final ProductParseService productParseService;
  private final ProductLinksLoaderService linksLoaderService;

  private List<String> allUrls = new ArrayList<>();
  private final AtomicInteger currentIndex = new AtomicInteger(0);
  private static final int BATCH_SIZE = 5;
  private static final long DELAY_BETWEEN_BATCHES_MS = 3000;

  public ScheduledTaskService(ProductParseService productParseService,
      ProductLinksLoaderService linksLoaderService) {
    this.productParseService = productParseService;
    this.linksLoaderService = linksLoaderService;
  }

  @PostConstruct
  public void init() {
    allUrls = linksLoaderService.loadProductLinks();
    logger.info("Инициализировано {} URL для обработки", allUrls.size());
  }

  @Override
  public void run(String... args) throws Exception {
    logger.info("=== Автоматический парсинг товаров запущен ===");
    logger.info("Всего URL для обработки: {}", allUrls.size());
    logger.info("Размер батча: {}, задержка между батчами: {} мс", BATCH_SIZE,
        DELAY_BETWEEN_BATCHES_MS);
  }

  @Scheduled(fixedDelay = DELAY_BETWEEN_BATCHES_MS, initialDelay = 5000)
  public void scheduledParseTask() {
    int startIndex = currentIndex.get();
    if (startIndex >= allUrls.size()) {
      logger.info("Все URL обработаны. Обработано: {}/{}", allUrls.size(), allUrls.size());
      return;
    }

    int endIndex = Math.min(startIndex + BATCH_SIZE, allUrls.size());
    List<String> batch = allUrls.subList(startIndex, endIndex);

    logger.info("Обработка батча {}-{} из {}", startIndex + 1, endIndex, allUrls.size());

    try {
      int successCount = productParseService.parseProductsBatch(batch);
      currentIndex.set(endIndex);
      logger.info("Батч завершён. Успешно: {}/{}. Прогресс: {}/{}", successCount, batch.size(),
          endIndex, allUrls.size());
    } catch (Exception e) {
      logger.error("Ошибка при обработке батча: {}", e.getMessage());
      currentIndex.set(endIndex);
    }
  }
}
