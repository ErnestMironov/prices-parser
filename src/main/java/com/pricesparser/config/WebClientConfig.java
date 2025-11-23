package com.pricesparser.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient webClient() {
    HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(10))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build();
  }
}
