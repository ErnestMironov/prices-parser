package com.pricesparser.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class WebClientService {

  private static final Logger logger = LoggerFactory.getLogger(WebClientService.class);

  private final WebClient webClient;

  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
  private static final Duration TIMEOUT = Duration.ofSeconds(10);

  public WebClientService() {
    this.webClient = WebClient.builder().defaultHeader("User-Agent", USER_AGENT)
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build();
  }

  public Mono<String> fetchHtml(String url) {
    logger.debug("Загрузка HTML с URL: {}", url);

    return webClient.get().uri(url).retrieve().bodyToMono(String.class).timeout(TIMEOUT)
        .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1)).filter(throwable -> {
          if (throwable instanceof WebClientResponseException e) {
            return e.getStatusCode().is5xxServerError();
          }
          return throwable instanceof java.util.concurrent.TimeoutException;
        }).doBeforeRetry(
            retrySignal -> logger.warn("Повторная попытка загрузки URL: {} (попытка {})", url,
                retrySignal.totalRetries() + 1)))
        .doOnSuccess(html -> logger.debug("Успешно загружен HTML с URL: {} (размер: {} байт)", url,
            html.length()))
        .doOnError(error -> {
          if (error instanceof WebClientResponseException e) {
            logger.error("Ошибка HTTP при загрузке URL {}: статус {}, сообщение: {}", url,
                e.getStatusCode(), e.getMessage());
          } else {
            logger.error("Ошибка при загрузке URL {}: {}", url, error.getMessage());
          }
        });
  }

  public String fetchHtmlBlocking(String url) {
    try {
      return fetchHtml(url).block(Duration.ofSeconds(15));
    } catch (Exception e) {
      logger.error("Ошибка при блокирующей загрузке URL {}: {}", url, e.getMessage());
      throw new RuntimeException("Не удалось загрузить страницу: " + e.getMessage(), e);
    }
  }
}
