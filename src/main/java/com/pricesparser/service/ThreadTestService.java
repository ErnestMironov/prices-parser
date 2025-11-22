package com.pricesparser.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.pricesparser.repository.ProductRepository;
import com.pricesparser.task.ProductParseTask;

@Component
public class ThreadTestService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadTestService.class);
    
    private final ProductRepository productRepository;
    
    public ThreadTestService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Начало тестирования потоков ===");
        
        List<String> urls = List.of(
            "https://example.com/product4",
            "https://example.com/product5",
            "https://example.com/product6"
        );
        
        List<Thread> threads = new ArrayList<>();
        
        for (String url : urls) {
            ProductParseTask task = new ProductParseTask(url, productRepository);
            Thread thread = new Thread(task, "ParseThread-" + url.substring(url.lastIndexOf('/') + 1));
            threads.add(thread);
        }
        
        logger.info("Запуск {} потоков...", threads.size());
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        logger.info("Ожидание завершения всех потоков...");
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        logger.info("Все потоки завершены. Всего товаров в БД: {}", productRepository.count());
        logger.info("=== Конец тестирования потоков ===");
    }
}