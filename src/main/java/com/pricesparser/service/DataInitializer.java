package com.pricesparser.service;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.pricesparser.model.Product;
import com.pricesparser.repository.ProductRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            Product product1 = new Product(
                "https://example.com/product1",
                "iPhone 17 Pro",
                new BigDecimal("99999.99"),
                "Новейший смартфон Apple с аллюминиевым корпусом"
            );
            
            Product product2 = new Product(
                "https://example.com/product2",
                "Samsung Galaxy S26",
                new BigDecimal("89999.00"),
                "Флагманский смартфон от Samsung"
            );
            
            Product product3 = new Product(
                "https://example.com/product3",
                "MacBook Pro 16",
                new BigDecimal("249999.50"),
                "Профессиональный ноутбук с чипом M3"
            );
            
            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
            
            System.out.println("✅ Создано 3 тестовых товара");
        }
    }
}