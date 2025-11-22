package com.pricesparser.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.pricesparser.model.Product;

@DataJpaTest
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product(
            "https://example.com/product1",
            "Тестовый товар",
            new BigDecimal("999.99"),
            "Описание тестового товара"
        );
    }

    @Test
    @DisplayName("Должен сохранить товар и присвоить ID")
    void shouldSaveProductAndAssignId() {
        Product saved = productRepository.save(testProduct);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo("https://example.com/product1");
        assertThat(saved.getTitle()).isEqualTo("Тестовый товар");
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(saved.getDescription()).isEqualTo("Описание тестового товара");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getParsedAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен найти товар по ID")
    void shouldFindProductById() {
        Product saved = productRepository.save(testProduct);
        Long id = saved.getId();
        
        Optional<Product> found = productRepository.findById(id);
        
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getTitle()).isEqualTo("Тестовый товар");
    }

    @Test
    @DisplayName("Должен найти товар по URL")
    void shouldFindProductByUrl() {
        productRepository.save(testProduct);
        
        Optional<Product> found = productRepository.findByUrl("https://example.com/product1");
        
        assertThat(found).isPresent();
        assertThat(found.get().getUrl()).isEqualTo("https://example.com/product1");
        assertThat(found.get().getTitle()).isEqualTo("Тестовый товар");
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional если товар не найден по URL")
    void shouldReturnEmptyWhenProductNotFoundByUrl() {
        Optional<Product> found = productRepository.findByUrl("https://nonexistent.com/product");
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Должен проверить существование товара по URL")
    void shouldCheckIfProductExistsByUrl() {
        productRepository.save(testProduct);
        
        boolean exists = productRepository.existsByUrl("https://example.com/product1");
        boolean notExists = productRepository.existsByUrl("https://nonexistent.com/product");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Должен найти все товары")
    void shouldFindAllProducts() {
        Product product1 = new Product("https://example.com/product1", "Товар 1", new BigDecimal("100.00"), "Описание 1");
        Product product2 = new Product("https://example.com/product2", "Товар 2", new BigDecimal("200.00"), "Описание 2");
        
        productRepository.save(product1);
        productRepository.save(product2);
        
        List<Product> all = productRepository.findAll();
        
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Product::getTitle).containsExactlyInAnyOrder("Товар 1", "Товар 2");
    }

    @Test
    @DisplayName("Должен удалить товар")
    void shouldDeleteProduct() {
        Product saved = productRepository.save(testProduct);
        Long id = saved.getId();
        
        productRepository.deleteById(id);
        
        Optional<Product> found = productRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Должен обновить товар")
    void shouldUpdateProduct() {
        Product saved = productRepository.save(testProduct);
        Long id = saved.getId();
        
        saved.setTitle("Обновлённый товар");
        saved.setPrice(new BigDecimal("1499.99"));
        productRepository.save(saved);
        
        Optional<Product> updated = productRepository.findById(id);
        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("Обновлённый товар");
        assertThat(updated.get().getPrice()).isEqualByComparingTo(new BigDecimal("1499.99"));
    }

    @Test
    @DisplayName("Должен установить createdAt при создании")
    void shouldSetCreatedAtOnCreation() {
        Product saved = productRepository.save(testProduct);
        
        assertThat(saved.getCreatedAt()).isNotNull();
        
        Product fromDb = productRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getCreatedAt()).isNotNull();
        assertThat(fromDb.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Должен установить parsedAt при создании")
    void shouldSetParsedAtOnCreation() {
        Product saved = productRepository.save(testProduct);
        
        assertThat(saved.getParsedAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен подсчитать количество товаров")
    void shouldCountProducts() {
        assertThat(productRepository.count()).isEqualTo(0);
        
        productRepository.save(testProduct);
        productRepository.save(new Product("https://example.com/product2", "Товар 2", new BigDecimal("200.00"), null));
        
        assertThat(productRepository.count()).isEqualTo(2);
    }
}