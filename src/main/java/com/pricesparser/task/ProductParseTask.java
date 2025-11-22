package com.pricesparser.task;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pricesparser.model.Product;
import com.pricesparser.repository.ProductRepository;

public class ProductParseTask implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductParseTask.class);
    
    private final String url;
    private final ProductRepository productRepository;
    
    public ProductParseTask(String url, ProductRepository productRepository) {
        this.url = url;
        this.productRepository = productRepository;
    }
    
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        logger.info("Thread [{}] начал парсинг URL: {}", threadName, url);
        
        try {
            Thread.sleep(1000);
            
            Product product = new Product(
                url,
                "Товар с " + url,
                new BigDecimal("999.99"),
                "Описание товара, полученное из " + url
            );
            
            productRepository.save(product);
            
            logger.info("Thread [{}] успешно сохранил товар: {}", threadName, product.getTitle());
            
        } catch (InterruptedException e) {
            logger.error("Thread [{}] был прерван", threadName);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Thread [{}] ошибка при парсинге URL {}: {}", threadName, url, e.getMessage());
        }
    }
}