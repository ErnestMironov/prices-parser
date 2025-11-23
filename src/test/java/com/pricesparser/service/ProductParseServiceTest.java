package com.pricesparser.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

@DisplayName("ProductParseService Tests")
class ProductParseServiceTest {

  private ExecutorService executorService;
  private WebClientService webClientService;
  private UniversalProductParser parser;
  private ProductRepository productRepository;
  private ProductParseService productParseService;

  @BeforeEach
  void setUp() {
    executorService = Executors.newFixedThreadPool(3);
    webClientService = mock(WebClientService.class);
    parser = mock(UniversalProductParser.class);
    productRepository = mock(ProductRepository.class);

    productParseService =
        new ProductParseService(executorService, webClientService, parser, productRepository);
  }

  @AfterEach
  void tearDown() {
    executorService.shutdown();
  }

  @Test
  @DisplayName("Должен распарсить товар асинхронно")
  void shouldParseProductAsync() throws Exception {
    String url = "https://example.com/product";
    String html =
        "<html><head><title>Test Product</title></head><body><h1>Test Product</h1></body></html>";
    Product expectedProduct =
        new Product(url, "Test Product", new BigDecimal("99.99"), "Description");

    when(webClientService.fetchHtmlBlocking(url)).thenReturn(html);
    when(parser.parseFromHtml(url, html)).thenReturn(expectedProduct);
    when(productRepository.existsByUrl(url)).thenReturn(false);
    when(productRepository.save(any(Product.class))).thenReturn(expectedProduct);

    Future<Product> future = productParseService.parseProductAsync(url);
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

    String html = "<html><head><title>Product</title></head></html>";

    for (String url : urls) {
      Product product = new Product(url, "Product", new BigDecimal("99.99"), "Description");
      when(webClientService.fetchHtmlBlocking(url)).thenReturn(html);
      when(parser.parseFromHtml(url, html)).thenReturn(product);
      when(productRepository.existsByUrl(url)).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(product);
    }

    List<Future<Product>> futures = productParseService.parseProductsAsync(urls);

    assertThat(futures).hasSize(3);

    for (Future<Product> future : futures) {
      Product product = future.get();
      assertThat(product).isNotNull();
      assertThat(product.getTitle()).isEqualTo("Product");
    }
  }
}
