package com.pricesparser.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.test.StepVerifier;

@DisplayName("WebClientService Tests")
class WebClientServiceTest {

  private WebClientService webClientService;

  @BeforeEach
  void setUp() {
    webClientService = new WebClientService();
  }

  @Test
  @DisplayName("Должен загрузить HTML с реальной страницы")
  void shouldFetchHtmlFromRealPage() {
    String url = "https://example.com";

    String html = webClientService.fetchHtmlBlocking(url);

    assertThat(html).isNotNull();
    assertThat(html).isNotEmpty();
    assertThat(html).contains("Example Domain");
  }

  @Test
  @DisplayName("Должен обработать ошибку 404")
  void shouldHandle404Error() {
    String url = "https://example.com/nonexistent-page";

    try {
      webClientService.fetchHtmlBlocking(url);
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).contains("Не удалось загрузить страницу");
      assertThat(e.getCause()).isInstanceOf(WebClientResponseException.class);
    }
  }

  @Test
  @DisplayName("Должен загрузить HTML асинхронно")
  void shouldFetchHtmlAsync() {
    String url = "https://example.com";

    StepVerifier.create(webClientService.fetchHtml(url)).expectNextMatches(html -> {
      assertThat(html).isNotNull();
      assertThat(html).isNotEmpty();
      assertThat(html).contains("Example Domain");
      return true;
    }).verifyComplete();
  }

  @Test
  @DisplayName("Должен обработать таймаут")
  void shouldHandleTimeout() {
    String url = "http://httpstat.us/200?sleep=15000";

    try {
      webClientService.fetchHtmlBlocking(url);
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).contains("Не удалось загрузить страницу");
    }
  }
}
