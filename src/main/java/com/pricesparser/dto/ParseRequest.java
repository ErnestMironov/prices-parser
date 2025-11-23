package com.pricesparser.dto;

import java.util.List;

public class ParseRequest {

  private List<String> urls;

  public ParseRequest() {}

  public ParseRequest(List<String> urls) {
    this.urls = urls;
  }

  public List<String> getUrls() {
    return urls;
  }

  public void setUrls(List<String> urls) {
    this.urls = urls;
  }
}
