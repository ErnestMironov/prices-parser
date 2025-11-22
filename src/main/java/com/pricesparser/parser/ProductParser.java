package com.pricesparser.parser;

import com.pricesparser.model.Product;

public interface ProductParser {
    
    Product parse(String url);
    
    Product parseFromHtml(String url, String html);
}