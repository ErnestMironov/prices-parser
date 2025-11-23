package com.pricesparser.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.pricesparser.model.Product;

@Component
public class ExecutorServiceDemoService implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceDemoService.class);

  private final ProductParseService productParseService;
  private final ExecutorService executorService;

  public ExecutorServiceDemoService(ProductParseService productParseService,
      ExecutorService productParseExecutor) {
    this.productParseService = productParseService;
    this.executorService = productParseExecutor;
  }

  @Override
  public void run(String... args) throws Exception {
    logger.info("=== Этап 7: ExecutorService и многопоточный парсинг ===");

    List<String> urls =
        List.of("https://example.com", "https://example.org", "https://example.net");

    logger.info("Запуск парсинга {} URL через ExecutorService", urls.size());
    long startTime = System.currentTimeMillis();

    CountDownLatch latch = new CountDownLatch(urls.size());
    List<CompletableFuture<Product>> futures =
        urls.stream().map(url -> CompletableFuture.supplyAsync(() -> {
          try {
            return productParseService.parseProduct(url);
          } finally {
            latch.countDown();
          }
        }, executorService)).toList();

    logger.info("Ожидание завершения всех задач парсинга...");
    boolean allCompleted = latch.await(30, TimeUnit.SECONDS);

    if (allCompleted) {
      long duration = System.currentTimeMillis() - startTime;
      logger.info("✅ Все задачи завершены за {} мс", duration);

      int successCount = 0;
      for (CompletableFuture<Product> future : futures) {
        try {
          Product product = future.get();
          if (product != null) {
            successCount++;
            logger.info("✅ Распарсен: {} (цена: {})", product.getTitle(), product.getPrice());
          }
        } catch (Exception e) {
          logger.warn("⚠️ Ошибка при получении результата: {}", e.getMessage());
        }
      }

      logger.info("Успешно распарсено: {} из {}", successCount, urls.size());
    } else {
      logger.warn("⚠️ Таймаут ожидания задач. Осталось задач: {}", latch.getCount());
    }

    logger.info("=== Конец демонстрации ExecutorService ===\n");
  }
}
