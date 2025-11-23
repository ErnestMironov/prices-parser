package com.pricesparser.util;

import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadSafeCounter {

  private static final Logger logger = LoggerFactory.getLogger(ThreadSafeCounter.class);

  private int count = 0;
  private final ReentrantLock lock = new ReentrantLock(true);

  public void increment() {
    lock.lock();
    try {
      int oldValue = count;
      count++;
      logger.debug("Счётчик увеличен: {} -> {} (поток: {})", oldValue, count,
          Thread.currentThread().getName());
    } finally {
      lock.unlock();
    }
  }

  public void decrement() {
    lock.lock();
    try {
      int oldValue = count;
      count--;
      logger.debug("Счётчик уменьшен: {} -> {} (поток: {})", oldValue, count,
          Thread.currentThread().getName());
    } finally {
      lock.unlock();
    }
  }

  public int get() {
    lock.lock();
    try {
      return count;
    } finally {
      lock.unlock();
    }
  }

  public void reset() {
    lock.lock();
    try {
      logger.info("Счётчик сброшен: {} -> 0 (поток: {})", count, Thread.currentThread().getName());
      count = 0;
    } finally {
      lock.unlock();
    }
  }

  public boolean tryIncrement() {
    if (lock.tryLock()) {
      try {
        count++;
        logger.debug("Счётчик увеличен (tryLock): {} (поток: {})", count,
            Thread.currentThread().getName());
        return true;
      } finally {
        lock.unlock();
      }
    } else {
      logger.debug("Не удалось заблокировать счётчик (занят другим потоком)");
      return false;
    }
  }

  public boolean isHeldByCurrentThread() {
    return lock.isHeldByCurrentThread();
  }
}
