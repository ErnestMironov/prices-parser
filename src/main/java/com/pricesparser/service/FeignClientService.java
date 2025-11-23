package com.pricesparser.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pricesparser.client.ExternalApiClient;

@Service
public class FeignClientService {

  private static final Logger logger = LoggerFactory.getLogger(FeignClientService.class);

  private final ExternalApiClient externalApiClient;

  public FeignClientService(ExternalApiClient externalApiClient) {
    this.externalApiClient = externalApiClient;
  }

  public String fetchHtml(String url) {
    logger.debug("Загрузка HTML через FeignClient с URL: {}", url);

    try {
      if (url.contains("example.com")) {
        String html = externalApiClient.getHtml();
        logger.debug("Успешно загружен HTML через FeignClient с URL: {} (размер: {} байт)", url,
            html.length());
        return html;
      } else {
        throw new UnsupportedOperationException(
            "FeignClient настроен только для example.com. Для других URL используйте RestTemplate или WebClient");
      }
    } catch (Exception e) {
      logger.error("Ошибка при загрузке HTML через FeignClient с URL {}: {}", url, e.getMessage());
      throw new RuntimeException("Ошибка при загрузке через FeignClient: " + e.getMessage(), e);
    }
  }
}
