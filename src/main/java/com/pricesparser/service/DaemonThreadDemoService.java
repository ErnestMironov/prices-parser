package com.pricesparser.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DaemonThreadDemoService implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(DaemonThreadDemoService.class);

  private final AsyncLoggingService asyncLoggingService;

  public DaemonThreadDemoService(AsyncLoggingService asyncLoggingService) {
    this.asyncLoggingService = asyncLoggingService;
  }

  @Override
  public void run(String... args) throws Exception {
    logger.info("=== Этап 8: Демон-потоки для логирования ===");

    logger.info("Демон-поток - это фоновый поток, который:");
    logger.info("1. Работает в фоновом режиме");
    logger.info("2. Не препятствует завершению приложения");
    logger.info("3. Автоматически завершается, когда все обычные потоки завершены");

    logger.info("\n--- Тестирование асинхронного логирования ---");

    List<String> testUrls = List.of("https://example.com/product1", "https://example.com/product2",
        "https://example.com/product3");

    for (int i = 0; i < testUrls.size(); i++) {
      String url = testUrls.get(i);
      asyncLoggingService.logProductAsync(url, "Test Product " + (i + 1),
          new BigDecimal("99" + i + ".99"));

      logger.info("Отправлено сообщение в очередь логирования (размер очереди: {})",
          asyncLoggingService.getQueueSize());

      Thread.sleep(100);
    }

    logger.info("Ожидание обработки всех сообщений демон-потоком...");
    Thread.sleep(2000);

    asyncLoggingService.logStatisticsAsync(10, 8, 2);
    Thread.sleep(500);

    logger.info("Размер очереди логирования: {}", asyncLoggingService.getQueueSize());
    logger.info("=== Конец демонстрации демон-потоков ===\n");
  }
}
