package com.pricesparser.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pricesparser.model.Product;
import com.pricesparser.parser.UniversalProductParser;
import com.pricesparser.repository.ProductRepository;

@Service
public class ProductParseService {

  private static final Logger logger = LoggerFactory.getLogger(ProductParseService.class);

  private final ExecutorService executorService;
  private final UniversalProductParser parser;
  private final ProductRepository productRepository;
  private final AsyncLoggingService asyncLoggingService;

  public ProductParseService(ExecutorService productParseExecutor, UniversalProductParser parser,
      ProductRepository productRepository, AsyncLoggingService asyncLoggingService) {
    this.executorService = productParseExecutor;
    this.parser = parser;
    this.productRepository = productRepository;
    this.asyncLoggingService = asyncLoggingService;
  }

  public Future<Product> parseProductAsync(String url) {
    return CompletableFuture.supplyAsync(() -> parseProduct(url), executorService);
  }

  public Product parseProduct(String url) {
    String threadName = Thread.currentThread().getName();
    logger.info("[{}] Начало парсинга URL: {}", threadName, url);

    try {
      Product product = parser.parse(url);
      logger.debug("[{}] Товар распарсен: title={}, price={}", threadName, product.getTitle(),
          product.getPrice());

      Product existing = productRepository.findByUrl(url).orElse(null);
      if (existing != null) {
        product.setId(existing.getId());
        product.setCreatedAt(existing.getCreatedAt());
        product = productRepository.save(product);
        logger.info("[{}] Товар обновлён: {}", threadName, product.getTitle());
        asyncLoggingService.logProductAsync(url, product.getTitle(), product.getPrice());
      } else {
        product = productRepository.save(product);
        logger.info("[{}] Товар сохранён: {}", threadName, product.getTitle());
        asyncLoggingService.logProductAsync(url, product.getTitle(), product.getPrice());
      }

      return product;

    } catch (Exception e) {
      logger.error("[{}] Ошибка при парсинге URL {}: {}", threadName, url, e.getMessage(), e);
      asyncLoggingService.logErrorAsync(url, e.getMessage());
      throw new RuntimeException("Не удалось распарсить товар по URL: " + url, e);
    }
  }

  public List<Future<Product>> parseProductsAsync(List<String> urls) {
    logger.info("Запуск параллельного парсинга {} URL", urls.size());
    return urls.stream().map(this::parseProductAsync).toList();
  }

  public List<Product> parseProducts(List<String> urls) {
    logger.info("Запуск параллельного парсинга {} URL", urls.size());
    List<Future<Product>> futures = parseProductsAsync(urls);

    return futures.stream().map(future -> {
      try {
        return future.get();
      } catch (Exception e) {
        logger.error("Ошибка при получении результата парсинга: {}", e.getMessage());
        return null;
      }
    }).filter(product -> product != null).toList();
  }

  public int parseProductsBatch(List<String> urls) {
    logger.info("Обработка батча из {} URL", urls.size());
    List<Future<Product>> futures = parseProductsAsync(urls);

    int successCount = 0;
    for (Future<Product> future : futures) {
      try {
        Product product = future.get();
        if (product != null) {
          successCount++;
        }
      } catch (Exception e) {
        logger.error("Ошибка при получении результата парсинга: {}", e.getMessage());
      }
    }

    logger.info("Батч обработан: успешно {}/{}", successCount, urls.size());
    return successCount;
  }
}
