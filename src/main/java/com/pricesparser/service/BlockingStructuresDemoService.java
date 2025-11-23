package com.pricesparser.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.pricesparser.queue.UrlQueue;
import com.pricesparser.repository.ProductRepository;
import com.pricesparser.task.ProductParseTask;
import com.pricesparser.util.ThreadSafeCounter;

@Component
public class BlockingStructuresDemoService implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(BlockingStructuresDemoService.class);

  private final ProductRepository productRepository;

  public BlockingStructuresDemoService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    logger.info("=== Этап 6: Блокирующие структуры и синхронизация ===");

    logger.info("\n--- 1. BlockingQueue для очереди URL ---");
    UrlQueue urlQueue = new UrlQueue(10);

    List<String> urls = List.of("https://example.com/blocking1", "https://example.com/blocking2",
        "https://example.com/blocking3");

    for (String url : urls) {
      urlQueue.offer(url);
    }
    logger.info("Добавлено {} URL в очередь. Текущий размер очереди: {}", urls.size(),
        urlQueue.size());

    logger.info("\n--- 2. ReentrantLock для синхронизации ---");
    ThreadSafeCounter counter = new ThreadSafeCounter();
    counter.reset();
    logger.info("Создан потокобезопасный счётчик (начальное значение: {})", counter.get());

    logger.info("\n--- 3. CountDownLatch для координации потоков ---");
    int numberOfThreads = urls.size();
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    logger.info("Создан CountDownLatch на {} потоков", numberOfThreads);

    logger.info("\n--- 4. Запуск потоков с использованием всех инструментов ---");
    List<Thread> threads = new ArrayList<>();

    for (int i = 0; i < numberOfThreads; i++) {
      final int threadIndex = i;
      Thread thread = new Thread(() -> {
        String threadName = Thread.currentThread().getName();
        try {
          counter.increment();
          logger.info("[{}] Счётчик увеличен до {}", threadName, counter.get());

          String url = urlQueue.take();
          logger.info("[{}] Извлечён URL из очереди: {}", threadName, url);

          ProductParseTask task = new ProductParseTask(url, productRepository);
          task.run();

          counter.decrement();
          logger.info("[{}] Счётчик уменьшен до {}", threadName, counter.get());

        } catch (InterruptedException e) {
          logger.error("[{}] Поток был прерван", threadName);
          Thread.currentThread().interrupt();
        } catch (Exception e) {
          logger.error("[{}] Ошибка при выполнении задачи: {}", threadName, e.getMessage());
        } finally {
          latch.countDown();
          logger.info("[{}] Завершил работу. Осталось потоков: {}", threadName, latch.getCount());
        }
      }, "BlockingThread-" + (threadIndex + 1));

      threads.add(thread);
    }

    logger.info("Запуск {} потоков...", threads.size());
    for (Thread thread : threads) {
      thread.start();
    }

    logger.info("Ожидание завершения всех потоков (CountDownLatch)...");
    boolean allCompleted = latch.await(30, TimeUnit.SECONDS);

    if (allCompleted) {
      logger.info("✅ Все потоки завершены! Финальное значение счётчика: {}", counter.get());
    } else {
      logger.warn("⚠️ Таймаут ожидания потоков. Осталось потоков: {}", latch.getCount());
    }

    logger.info("Итоговый размер очереди: {} (ожидается 0)", urlQueue.size());
    logger.info("Всего товаров в БД: {}", productRepository.count());
    logger.info("=== Конец демонстрации блокирующих структур ===\n");
  }
}
