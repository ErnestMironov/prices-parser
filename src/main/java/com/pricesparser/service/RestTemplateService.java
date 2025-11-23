package com.pricesparser.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestTemplateService {

  private static final Logger logger = LoggerFactory.getLogger(RestTemplateService.class);

  private final RestTemplate restTemplate;

  public RestTemplateService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String fetchHtml(String url) {
    logger.debug("Загрузка HTML через RestTemplate с URL: {}", url);

    try {
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        logger.debug("Успешно загружен HTML через RestTemplate с URL: {} (размер: {} байт)", url,
            response.getBody().length());
        return response.getBody();
      } else {
        throw new RuntimeException(
            "Не удалось загрузить страницу: статус " + response.getStatusCode());
      }

    } catch (HttpClientErrorException e) {
      logger.error("HTTP клиентская ошибка при загрузке URL {}: статус {}, сообщение: {}", url,
          e.getStatusCode(), e.getMessage());
      throw new RuntimeException("HTTP ошибка при загрузке страницы: " + e.getMessage(), e);
    } catch (HttpServerErrorException e) {
      logger.error("HTTP серверная ошибка при загрузке URL {}: статус {}, сообщение: {}", url,
          e.getStatusCode(), e.getMessage());
      throw new RuntimeException("HTTP ошибка при загрузке страницы: " + e.getMessage(), e);
    } catch (ResourceAccessException e) {
      logger.error("Ошибка доступа к ресурсу при загрузке URL {}: {}", url, e.getMessage());
      throw new RuntimeException("Ошибка подключения: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Неожиданная ошибка при загрузке URL {}: {}", url, e.getMessage());
      throw new RuntimeException("Ошибка при загрузке страницы: " + e.getMessage(), e);
    }
  }

  public <T> T getForObject(String url, Class<T> responseType) {
    logger.debug("GET запрос через RestTemplate: {}", url);
    try {
      return restTemplate.getForObject(url, responseType);
    } catch (Exception e) {
      logger.error("Ошибка при GET запросе к {}: {}", url, e.getMessage());
      throw new RuntimeException("Ошибка при GET запросе: " + e.getMessage(), e);
    }
  }

  public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType) {
    logger.debug("GET запрос с ResponseEntity через RestTemplate: {}", url);
    try {
      return restTemplate.getForEntity(url, responseType);
    } catch (Exception e) {
      logger.error("Ошибка при GET запросе к {}: {}", url, e.getMessage());
      throw new RuntimeException("Ошибка при GET запросе: " + e.getMessage(), e);
    }
  }
}
