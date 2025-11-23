package com.pricesparser.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-html-client", url = "https://example.com")
public interface ProductHtmlClient {

  @GetMapping
  String getHtml();

  @GetMapping("/{path}")
  String getHtmlByPath(@PathVariable String path);
}
