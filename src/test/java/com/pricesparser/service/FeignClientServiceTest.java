package com.pricesparser.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FeignClientService Tests")
public class FeignClientServiceTest {

  @Autowired
  private FeignClientService feignClientService;

  @Test
  @DisplayName("Должен загрузить HTML через FeignClient")
  void shouldFetchHtml() {
    String html = feignClientService.fetchHtml("https://example.com");

    assertThat(html).isNotNull();
    assertThat(html).isNotEmpty();
    assertThat(html).contains("Example Domain");
  }
}
