package com.pricesparser.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "external-api", url = "https://example.com")
public interface ExternalApiClient {

  @GetMapping
  String getHtml();

  @GetMapping("/")
  String getRootHtml();
}
