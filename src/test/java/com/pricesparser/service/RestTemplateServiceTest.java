package com.pricesparser.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RestTemplateService Tests")
public class RestTemplateServiceTest {

  @Autowired
  private RestTemplateService restTemplateService;

  @Test
  @DisplayName("Должен загрузить HTML через RestTemplate")
  void shouldFetchHtml() {
    String html = restTemplateService.fetchHtml("https://example.com");

    assertThat(html).isNotNull();
    assertThat(html).isNotEmpty();
    assertThat(html).contains("Example Domain");
  }

  @Test
  @DisplayName("Должен обработать ошибку 404")
  void shouldHandle404Error() {
    assertThatThrownBy(() -> restTemplateService.fetchHtml("https://example.com/nonexistent"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("Должен выполнить GET запрос")
  void shouldPerformGetRequest() {
    String html = restTemplateService.getForObject("https://example.com", String.class);

    assertThat(html).isNotNull();
    assertThat(html).isNotEmpty();
  }
}
