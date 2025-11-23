package com.pricesparser.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(12)
public class InterServiceDemoService implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(InterServiceDemoService.class);

  private final RestTemplateService restTemplateService;
  private final WebClientService webClientService;
  private final FeignClientService feignClientService;

  public InterServiceDemoService(RestTemplateService restTemplateService,
      WebClientService webClientService, FeignClientService feignClientService) {
    this.restTemplateService = restTemplateService;
    this.webClientService = webClientService;
    this.feignClientService = feignClientService;
  }

  @Override
  public void run(String... args) throws Exception {
    logger.info("=== Этап 12: Межсервисное взаимодействие ===");

    List<String> testUrls = List.of("https://example.com", "https://example.org");

    logger.info("--- Демонстрация RestTemplate (синхронный, блокирующий) ---");
    for (String url : testUrls) {
      try {
        long startTime = System.currentTimeMillis();
        String html = restTemplateService.fetchHtml(url);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("✅ RestTemplate: загружено {} байт за {} мс", html.length(), duration);
      } catch (Exception e) {
        logger.error("❌ RestTemplate: ошибка - {}", e.getMessage());
      }
    }

    logger.info("--- Демонстрация WebClient (асинхронный, неблокирующий) ---");
    for (String url : testUrls) {
      try {
        long startTime = System.currentTimeMillis();
        String html = webClientService.fetchHtmlBlocking(url);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("✅ WebClient: загружено {} байт за {} мс", html.length(), duration);
      } catch (Exception e) {
        logger.error("❌ WebClient: ошибка - {}", e.getMessage());
      }
    }

    logger.info("--- Демонстрация FeignClient (декларативный, простой) ---");
    try {
      long startTime = System.currentTimeMillis();
      String html = feignClientService.fetchHtml("https://example.com");
      long duration = System.currentTimeMillis() - startTime;
      logger.info("✅ FeignClient: загружено {} байт за {} мс", html.length(), duration);
    } catch (Exception e) {
      logger.error("❌ FeignClient: ошибка - {}", e.getMessage());
    }

    logger.info("--- Сравнение подходов ---");
    logger.info("RestTemplate: синхронный, блокирующий, простой в использовании");
    logger.info("WebClient: асинхронный, неблокирующий, реактивный, лучше для высокой нагрузки");
    logger.info("FeignClient: декларативный, простой интерфейс, автоматическая генерация клиента");
    logger.info("=== Конец демонстрации межсервисного взаимодействия ===\n");
  }
}
