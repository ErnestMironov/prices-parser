package com.pricesparser.service;

import java.math.BigDecimal;
import java.util.Comparator;
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

    List<Product> allProducts = productRepository.findAll();

    List<Product> filtered = allProducts.parallelStream()
        .filter(product -> minPrice == null || product.getPrice() == null
            || product.getPrice().compareTo(minPrice) >= 0)
        .filter(product -> maxPrice == null || product.getPrice() == null
            || product.getPrice().compareTo(maxPrice) <= 0)
        .filter(product -> titleFilter == null || titleFilter.isEmpty()
            || (product.getTitle() != null
                && product.getTitle().toLowerCase().contains(titleFilter.toLowerCase())))
        .collect(Collectors.toList());

    logger.debug("После фильтрации осталось {} товаров из {}", filtered.size(), allProducts.size());

    Sort sort = pageable.getSort();
    if (sort.isSorted()) {
      Comparator<Product> comparator = buildComparator(sort);
      filtered = filtered.parallelStream().sorted(comparator).collect(Collectors.toList());
    }

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filtered.size());
    List<Product> pagedProducts =
        start < filtered.size() ? filtered.subList(start, end) : List.of();

    List<ProductResponse> responses =
        pagedProducts.stream().map(this::toResponse).collect(Collectors.toList());

    return new PageImpl<>(responses, pageable, filtered.size());
  }

  private Comparator<Product> buildComparator(Sort sort) {
    Comparator<Product> comparator = null;

    for (Sort.Order order : sort) {
      Comparator<Product> orderComparator = switch (order.getProperty().toLowerCase()) {
        case "price" -> Comparator.comparing(Product::getPrice,
            Comparator.nullsLast(Comparator.naturalOrder()));
        case "title" -> Comparator.comparing(Product::getTitle,
            Comparator.nullsLast(Comparator.naturalOrder()));
        case "parsedat" -> Comparator.comparing(Product::getParsedAt,
            Comparator.nullsLast(Comparator.naturalOrder()));
        case "createdat" -> Comparator.comparing(Product::getCreatedAt,
            Comparator.nullsLast(Comparator.naturalOrder()));
        default -> Comparator.comparing(Product::getId);
      };

      if (order.isDescending()) {
        orderComparator = orderComparator.reversed();
      }

      comparator = comparator == null ? orderComparator : comparator.thenComparing(orderComparator);
    }

    return comparator != null ? comparator : Comparator.comparing(Product::getId);
  }

  public List<ProductResponse> getProductsSortedByPriceParallel() {
    logger.info("Получение всех товаров, отсортированных по цене (parallelStream)");

    List<Product> products = productRepository.findAll();

    return products.parallelStream()
        .sorted(Comparator.comparing(Product::getPrice,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .map(this::toResponse).collect(Collectors.toList());
  }

  public List<ProductResponse> getExpensiveProductsParallel(BigDecimal threshold) {
    logger.info("Получение дорогих товаров (цена > {}) через parallelStream", threshold);

    List<Product> products = productRepository.findAll();

    return products.parallelStream()
        .filter(
            product -> product.getPrice() != null && product.getPrice().compareTo(threshold) > 0)
        .sorted(Comparator.comparing(Product::getPrice, Comparator.reverseOrder()))
        .map(this::toResponse).collect(Collectors.toList());
  }

  private ProductResponse toResponse(Product product) {
    return new ProductResponse(product.getId(), product.getUrl(), product.getTitle(),
        product.getPrice(), product.getDescription(), product.getParsedAt(),
        product.getCreatedAt());
  }
}
