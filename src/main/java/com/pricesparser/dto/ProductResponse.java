package com.pricesparser.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

  private Long id;
  private String url;
  private String title;
  private BigDecimal price;
  private String description;
  private LocalDateTime parsedAt;
  private LocalDateTime createdAt;

  public ProductResponse() {}

  public ProductResponse(Long id, String url, String title, BigDecimal price, String description,
      LocalDateTime parsedAt, LocalDateTime createdAt) {
    this.id = id;
    this.url = url;
    this.title = title;
    this.price = price;
    this.description = description;
    this.parsedAt = parsedAt;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getParsedAt() {
    return parsedAt;
  }

  public void setParsedAt(LocalDateTime parsedAt) {
    this.parsedAt = parsedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
