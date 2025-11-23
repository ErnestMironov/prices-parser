package com.pricesparser.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductLinksLoaderService {

  private static final Logger logger = LoggerFactory.getLogger(ProductLinksLoaderService.class);
  private static final String BASE_URL = "https://pitergsm.ru";
  private static final String LINKS_FILE = "product_links.json";

  private final ObjectMapper objectMapper;

  public ProductLinksLoaderService() {
    this.objectMapper = new ObjectMapper();
  }

  public List<String> loadProductLinks() {
    try {
      ClassPathResource resource = new ClassPathResource(LINKS_FILE);
      if (!resource.exists()) {
        logger.warn("Файл {} не найден в classpath, пробуем загрузить из корня проекта",
            LINKS_FILE);
        try {
          List<String> relativePaths = objectMapper.readValue(
              Files.readAllBytes(Paths.get(LINKS_FILE)), new TypeReference<List<String>>() {});
          return convertToFullUrls(relativePaths);
        } catch (IOException e) {
          logger.error("Не удалось загрузить файл {}: {}", LINKS_FILE, e.getMessage());
          return List.of();
        }
      }

      List<String> relativePaths =
          objectMapper.readValue(resource.getInputStream(), new TypeReference<List<String>>() {});

      List<String> fullUrls = convertToFullUrls(relativePaths);
      logger.info("Загружено {} ссылок из файла {}", fullUrls.size(), LINKS_FILE);
      return fullUrls;

    } catch (Exception e) {
      logger.error("Ошибка при загрузке ссылок из файла {}: {}", LINKS_FILE, e.getMessage());
      return List.of();
    }
  }

  private List<String> convertToFullUrls(List<String> relativePaths) {
    return relativePaths.stream().map(path -> BASE_URL + path).collect(Collectors.toList());
  }
}
