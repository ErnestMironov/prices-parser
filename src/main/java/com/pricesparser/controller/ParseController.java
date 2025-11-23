package com.pricesparser.controller;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pricesparser.dto.ParseRequest;
import com.pricesparser.dto.ProductResponse;
import com.pricesparser.service.ProductParseService;
import com.pricesparser.service.ProductService;

@RestController
@RequestMapping("/api")
public class ParseController {

  private static final Logger logger = LoggerFactory.getLogger(ParseController.class);

  private final ProductParseService productParseService;
  private final ProductService productService;

  public ParseController(ProductParseService productParseService, ProductService productService) {
    this.productParseService = productParseService;
    this.productService = productService;
  }

  @PostMapping("/parse")
  public ResponseEntity<String> parseProducts(@RequestBody ParseRequest request) {
    logger.info("Получен запрос на парсинг {} URL", request.getUrls().size());

    try {
      CompletableFuture.runAsync(() -> {
        logger.info("Запуск асинхронного парсинга {} URL", request.getUrls().size());
        productParseService.parseProducts(request.getUrls());
        logger.info("Парсинг завершён для {} URL", request.getUrls().size());
      });

      return ResponseEntity.accepted()
          .body("Парсинг запущен для " + request.getUrls().size() + " URL");
    } catch (Exception e) {
      logger.error("Ошибка при запуске парсинга: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Ошибка при запуске парсинга: " + e.getMessage());
    }
  }

  @GetMapping("/results")
  public ResponseEntity<Page<ProductResponse>> getResults(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir) {

    logger.info("Получен запрос на результаты парсинга: page={}, size={}, sortBy={}, sortDir={}",
        page, size, sortBy, sortDir);

    Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<ProductResponse> results = productService.getProductsFiltered(pageable, null, null, null);

    return ResponseEntity.ok(results);
  }
}
