package com.pricesparser.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pricesparser.dto.ProductResponse;
import com.pricesparser.model.Product;
import com.pricesparser.repository.ProductRepository;
import com.pricesparser.service.ProductService;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProductRepository productRepository;

  @MockBean
  private ProductService productService;

  @Test
  @DisplayName("Должен вернуть список товаров с пагинацией")
  void shouldReturnProductsWithPagination() throws Exception {
    Product product = new Product("https://example.com/product", "Test Product",
        new BigDecimal("99.99"), "Description");
    product.setId(1L);
    product.setCreatedAt(LocalDateTime.now());

    ProductResponse response =
        new ProductResponse(1L, "https://example.com/product", "Test Product",
            new BigDecimal("99.99"), "Description", LocalDateTime.now(), LocalDateTime.now());

    Page<ProductResponse> productPage = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

    when(productService.getProductsFiltered(any(Pageable.class), isNull(), isNull(), isNull()))
        .thenReturn(productPage);

    mockMvc.perform(get("/api/products").param("page", "0").param("size", "10"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Test Product"))
        .andExpect(jsonPath("$.content[0].price").value(99.99));
  }

  @Test
  @DisplayName("Должен вернуть количество товаров")
  void shouldReturnProductCount() throws Exception {
    when(productRepository.count()).thenReturn(5L);

    mockMvc.perform(get("/api/products/count")).andExpect(status().isOk())
        .andExpect(jsonPath("$").value(5));
  }
}
