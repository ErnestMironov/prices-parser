package com.pricesparser.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricesparser.dto.ParseRequest;
import com.pricesparser.dto.ProductResponse;
import com.pricesparser.service.ProductParseService;
import com.pricesparser.service.ProductService;

@WebMvcTest(ParseController.class)
@DisplayName("ParseController Tests")
class ParseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ProductParseService productParseService;

  @MockBean
  private ProductService productService;

  @Test
  @DisplayName("Должен принять запрос на парсинг")
  void shouldAcceptParseRequest() throws Exception {
    ParseRequest request = new ParseRequest(List.of("https://example.com/product1"));

    mockMvc
        .perform(post("/api/parse").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isAccepted())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Парсинг запущен")));
  }

  @Test
  @DisplayName("Должен вернуть результаты парсинга с пагинацией")
  void shouldReturnResultsWithPagination() throws Exception {
    ProductResponse response = new ProductResponse(1L, "https://example.com/product",
        "Test Product", java.math.BigDecimal.valueOf(99.99), "Description", null, null);

    Page<ProductResponse> productPage = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

    when(productService.getProductsFiltered(any(), any(), any(), any())).thenReturn(productPage);

    mockMvc.perform(get("/api/results").param("page", "0").param("size", "10"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Test Product"))
        .andExpect(jsonPath("$.content[0].price").value(99.99))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @DisplayName("Должен вернуть результаты с сортировкой")
  void shouldReturnResultsWithSorting() throws Exception {
    ProductResponse response = new ProductResponse(1L, "https://example.com/product",
        "Test Product", java.math.BigDecimal.valueOf(99.99), "Description", null, null);

    Page<ProductResponse> productPage = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

    when(productService.getProductsFiltered(any(), any(), any(), any())).thenReturn(productPage);

    mockMvc
        .perform(get("/api/results").param("page", "0").param("size", "10").param("sortBy", "price")
            .param("sortDir", "desc"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].title").value("Test Product"));
  }
}
