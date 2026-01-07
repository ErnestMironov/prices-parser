package com.pricesparser.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.pricesparser.dto.ProductResponse;
import com.pricesparser.model.Product;
import com.pricesparser.repository.ProductRepository;

@Service
public class ProductService {

  private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public Page<ProductResponse> getProductsFiltered(Pageable pageable, BigDecimal minPrice,
      BigDecimal maxPrice, String titleFilter) {
    logger.info("Получение товаров с фильтрацией: minPrice={}, maxPrice={}, titleFilter={}",
        minPrice, maxPrice, titleFilter);

    Page<Product> filteredPage =
        productRepository.findFilteredProducts(minPrice, maxPrice, titleFilter, pageable);

    List<ProductResponse> responses =
        filteredPage.getContent().stream().map(this::toResponse).collect(Collectors.toList());

    logger.debug("Найдено {} товаров из {} (страница {})", filteredPage.getNumberOfElements(),
        filteredPage.getTotalElements(), filteredPage.getNumber());

    return new PageImpl<>(responses, pageable, filteredPage.getTotalElements());
  }

  public List<ProductResponse> getProductsSortedByPriceParallel() {
    logger.info("Получение всех товаров, отсортированных по цене");

    List<Product> products =
        productRepository.findAll(Sort.by(Sort.Order.asc("price").nullsLast()));

    return products.stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<ProductResponse> getExpensiveProductsParallel(BigDecimal threshold) {
    logger.info("Получение дорогих товаров (цена > {})", threshold);

    List<Product> products = productRepository.findExpensiveProducts(threshold);

    return products.stream().map(this::toResponse).collect(Collectors.toList());
  }

  private ProductResponse toResponse(Product product) {
    return new ProductResponse(product.getId(), product.getUrl(), product.getTitle(),
        product.getPrice(), product.getDescription(), product.getParsedAt(),
        product.getCreatedAt());
  }
}
