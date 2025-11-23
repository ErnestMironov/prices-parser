package com.pricesparser.service;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.pricesparser.dto.ProductResponse;
import com.pricesparser.model.Product;
import com.pricesparser.repository.ProductRepository;

@SpringBootTest
@ActiveProfiles("test")
public class ProductServiceTest {

  @Autowired
  private ProductService productService;

  @Autowired
  private ProductRepository productRepository;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();

    productRepository.save(new Product("https://example.com/product1", "Товар 1",
        new BigDecimal("100.00"), "Описание 1"));
    productRepository.save(new Product("https://example.com/product2", "Товар 2",
        new BigDecimal("200.00"), "Описание 2"));
    productRepository.save(new Product("https://example.com/product3", "Товар 3",
        new BigDecimal("50.00"), "Описание 3"));
    productRepository.save(new Product("https://example.com/product4", "Дорогой товар",
        new BigDecimal("500.00"), "Описание 4"));
  }

  @Test
  @DisplayName("Фильтрация по минимальной цене через parallelStream")
  void testFilterByMinPrice() {
    Pageable pageable = PageRequest.of(0, 10);
    var result = productService.getProductsFiltered(pageable, new BigDecimal("150"), null, null);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).extracting(ProductResponse::getPrice)
        .containsExactlyInAnyOrder(new BigDecimal("200.00"), new BigDecimal("500.00"));
  }

  @Test
  @DisplayName("Фильтрация по максимальной цене через parallelStream")
  void testFilterByMaxPrice() {
    Pageable pageable = PageRequest.of(0, 10);
    var result = productService.getProductsFiltered(pageable, null, new BigDecimal("150"), null);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).extracting(ProductResponse::getPrice)
        .containsExactlyInAnyOrder(new BigDecimal("100.00"), new BigDecimal("50.00"));
  }

  @Test
  @DisplayName("Фильтрация по названию через parallelStream")
  void testFilterByTitle() {
    Pageable pageable = PageRequest.of(0, 10);
    var result = productService.getProductsFiltered(pageable, null, null, "Дорогой");

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).contains("Дорогой");
  }

  @Test
  @DisplayName("Сортировка по цене через parallelStream")
  void testSortByPriceParallel() {
    List<ProductResponse> result = productService.getProductsSortedByPriceParallel();

    assertThat(result).hasSize(4);
    assertThat(result).extracting(ProductResponse::getPrice).containsExactly(
        new BigDecimal("50.00"), new BigDecimal("100.00"), new BigDecimal("200.00"),
        new BigDecimal("500.00"));
  }

  @Test
  @DisplayName("Получение дорогих товаров через parallelStream")
  void testGetExpensiveProductsParallel() {
    List<ProductResponse> result =
        productService.getExpensiveProductsParallel(new BigDecimal("150"));

    assertThat(result).hasSize(2);
    assertThat(result).extracting(ProductResponse::getPrice)
        .containsExactly(new BigDecimal("500.00"), new BigDecimal("200.00"));
  }

  @Test
  @DisplayName("Пагинация с фильтрацией")
  void testPaginationWithFiltering() {
    Pageable pageable = PageRequest.of(0, 2, Sort.by("price").ascending());
    var result = productService.getProductsFiltered(pageable, null, null, null);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(4);
    assertThat(result.getTotalPages()).isEqualTo(2);
  }
}
