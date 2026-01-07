package com.pricesparser.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pricesparser.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByUrl(String url);

    boolean existsByUrl(String url);

    @Query("SELECT p FROM Product p WHERE " + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
            + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
            + "(:titleFilter IS NULL OR :titleFilter = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :titleFilter, '%')))")
    Page<Product> findFilteredProducts(@Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice, @Param("titleFilter") String titleFilter,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price > :threshold ORDER BY p.price DESC")
    List<Product> findExpensiveProducts(@Param("threshold") BigDecimal threshold);
}
