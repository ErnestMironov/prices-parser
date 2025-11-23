package com.pricesparser.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

  @Value("${parser.thread-pool.core-size:5}")
  private int corePoolSize;

  @Value("${parser.thread-pool.max-size:10}")
  private int maxPoolSize;

  @Value("${parser.thread-pool.queue-capacity:100}")
  private int queueCapacity;

  @Value("${parser.thread-pool.keep-alive-seconds:60}")
  private long keepAliveSeconds;

  @Bean(name = "productParseExecutor")
  public ExecutorService productParseExecutor() {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
        keepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity), r -> {
          Thread thread = new Thread(r, "ProductParse-" + System.currentTimeMillis());
          thread.setDaemon(false);
          return thread;
        }, new ThreadPoolExecutor.CallerRunsPolicy());

    return executor;
  }
}
