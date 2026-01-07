package com.pricesparser.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pricesparser.model.Product;
import com.pricesparser.parser.UniversalProductParser;
import com.pricesparser.repository.ProductRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

@DisplayName("ProductParseService Tests")
class ProductParseServiceTest {

  private ExecutorService executorService;
  private UniversalProductParser parser;
  private ProductRepository productRepository;
  private AsyncLoggingService asyncLoggingService;
  private MeterRegistry meterRegistry;
  private Timer parseDurationTimer;
  private Counter parseSuccessCounter;
  private Counter parseErrorsCounter;
  private Counter productsSavedCounter;
  private OpenTelemetry openTelemetry;
  private ProductParseService productParseService;

  @BeforeEach
  void setUp() {
    executorService = Executors.newFixedThreadPool(3);
    parser = mock(UniversalProductParser.class);
    productRepository = mock(ProductRepository.class);
    asyncLoggingService = mock(AsyncLoggingService.class);

    meterRegistry = new SimpleMeterRegistry();
    parseDurationTimer = Timer.builder("parse_duration_seconds").register(meterRegistry);
    parseSuccessCounter = Counter.builder("parse_success_total").register(meterRegistry);
    parseErrorsCounter = Counter.builder("parse_errors_total").register(meterRegistry);
    productsSavedCounter = Counter.builder("products_saved_total").register(meterRegistry);

    openTelemetry = mock(OpenTelemetry.class);
    Tracer tracer = mock(Tracer.class);
    when(openTelemetry.getTracer(any(String.class), any(String.class))).thenReturn(tracer);

    productParseService = new ProductParseService(executorService, parser, productRepository,
        asyncLoggingService, parseDurationTimer, parseSuccessCounter, parseErrorsCounter,
        productsSavedCounter, openTelemetry);
  }

  @AfterEach
  void tearDown() {
    executorService.shutdown();
  }

  @Test
  @DisplayName("Должен распарсить товар асинхронно")
  void shouldParseProductAsync() throws Exception {
    String url = "https://example.com/product";
    Product expectedProduct =
        new Product(url, "Test Product", new BigDecimal("99.99"), "Description");

    when(parser.parse(url)).thenReturn(expectedProduct);
    when(productRepository.findByUrl(url)).thenReturn(java.util.Optional.empty());
    when(productRepository.save(any(Product.class))).thenReturn(expectedProduct);

    CompletableFuture<Product> future = productParseService.parseProductAsync(url);
    Product result = future.get();

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Test Product");
    assertThat(result.getUrl()).isEqualTo(url);
  }

  @Test
  @DisplayName("Должен распарсить несколько товаров параллельно")
  void shouldParseMultipleProductsInParallel() throws Exception {
    List<String> urls = List.of("https://example.com/product1", "https://example.com/product2",
        "https://example.com/product3");

    for (String url : urls) {
      Product product = new Product(url, "Product", new BigDecimal("99.99"), "Description");
      when(parser.parse(url)).thenReturn(product);
      when(productRepository.findByUrl(url)).thenReturn(java.util.Optional.empty());
      when(productRepository.save(any(Product.class))).thenReturn(product);
    }

    List<CompletableFuture<Product>> futures = productParseService.parseProductsAsync(urls);

    assertThat(futures).hasSize(3);

    for (CompletableFuture<Product> future : futures) {
      Product product = future.get();
      assertThat(product).isNotNull();
      assertThat(product.getTitle()).isEqualTo("Product");
    }
  }
}
