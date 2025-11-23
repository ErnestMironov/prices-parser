package com.pricesparser.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

  @Bean(name = "scheduledExecutorService")
  public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newScheduledThreadPool(2, r -> {
      Thread thread = new Thread(r, "ScheduledTask-" + System.currentTimeMillis());
      thread.setDaemon(false);
      return thread;
    });
  }
}
