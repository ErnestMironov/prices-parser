package com.pricesparser.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pricesparser.dto.ProductResponse;
import com.pricesparser.repository.ProductRepository;
import com.pricesparser.service.ProductService;

@RestController
@RequestMapping("/api")
public class ProductController {

  private final ProductRepository productRepository;
  private final ProductService productService;

  public ProductController(ProductRepository productRepository, ProductService productService) {
    this.productRepository = productRepository;
    this.productService = productService;
  }

  @GetMapping("/products")
  public ResponseEntity<Page<ProductResponse>> getProducts(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String titleFilter) {

    Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<ProductResponse> result =
        productService.getProductsFiltered(pageable, minPrice, maxPrice, titleFilter);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/products/count")
  public ResponseEntity<Long> getProductCount() {
    return ResponseEntity.ok(productRepository.count());
  }

  @GetMapping("/products/expensive")
  public ResponseEntity<List<ProductResponse>> getExpensiveProducts(
      @RequestParam(defaultValue = "100") BigDecimal threshold) {
    return ResponseEntity.ok(productService.getExpensiveProductsParallel(threshold));
  }

  @GetMapping("/products/sorted-by-price")
  public ResponseEntity<List<ProductResponse>> getProductsSortedByPrice() {
    return ResponseEntity.ok(productService.getProductsSortedByPriceParallel());
  }
}
