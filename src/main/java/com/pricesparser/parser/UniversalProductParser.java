package com.pricesparser.parser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pricesparser.model.Product;

public class UniversalProductParser implements ProductParser {
    
    private static final Logger logger = LoggerFactory.getLogger(UniversalProductParser.class);
    
    private static final Pattern PRICE_PATTERN = Pattern.compile("([0-9]+[\\.,]?[0-9]*)\\s*([рруб])?", Pattern.CASE_INSENSITIVE);
    
    @Override
    public Product parse(String url) {
        try {
            logger.debug("Парсинг URL: {}", url);
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
            
            return parseFromHtml(url, doc.html());
            
        } catch (Exception e) {
            logger.error("Ошибка при парсинге URL {}: {}", url, e.getMessage());
            throw new RuntimeException("Не удалось распарсить товар: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Product parseFromHtml(String url, String html) {
        try {
            Document doc = Jsoup.parse(html);
            
            String title = extractTitle(doc);
            BigDecimal price = extractPrice(doc);
            String description = extractDescription(doc);
            
            Product product = new Product(url, title, price, description);
            product.setParsedAt(LocalDateTime.now());
            
            logger.debug("Извлечено - Title: {}, Price: {}, Description: {}", title, price, description);
            
            return product;
            
        } catch (Exception e) {
            logger.error("Ошибка при парсинге HTML для URL {}: {}", url, e.getMessage());
            throw new RuntimeException("Не удалось распарсить HTML: " + e.getMessage(), e);
        }
    }
    
    private String extractTitle(Document doc) {
        String title = null;
        
        title = doc.select("meta[property=og:title]").attr("content");
        if (!title.isEmpty()) {
            return title;
        }
        
        title = doc.select("meta[name=title]").attr("content");
        if (!title.isEmpty()) {
            return title;
        }
        
        Element h1 = doc.selectFirst("h1");
        if (h1 != null) {
            return h1.text();
        }
        
        title = doc.title();
        if (!title.isEmpty()) {
            return title;
        }
        
        return "Без названия";
    }
    
    private BigDecimal extractPrice(Document doc) {
        String priceText = null;
        
        priceText = doc.select("meta[property=product:price:amount]").attr("content");
        if (!priceText.isEmpty()) {
            return parsePrice(priceText);
        }
        
        priceText = doc.select("[data-price]").attr("data-price");
        if (!priceText.isEmpty()) {
            return parsePrice(priceText);
        }
        
        priceText = doc.select(".price, .product-price, [class*=price]").first() != null 
            ? doc.select(".price, .product-price, [class*=price]").first().text()
            : null;
        
        if (priceText != null && !priceText.isEmpty()) {
            return parsePrice(priceText);
        }
        
        String bodyText = doc.body().text();
        Matcher matcher = PRICE_PATTERN.matcher(bodyText);
        if (matcher.find()) {
            return parsePrice(matcher.group(1));
        }
        
        return BigDecimal.ZERO;
    }
    
    private BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            String cleanPrice = priceText.replaceAll("[^0-9.,]", "")
                .replace(",", ".");
            
            if (cleanPrice.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            return new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            logger.warn("Не удалось распарсить цену: {}", priceText);
            return BigDecimal.ZERO;
        }
    }
    
    private String extractDescription(Document doc) {
        String description = null;
        
        description = doc.select("meta[property=og:description]").attr("content");
        if (!description.isEmpty()) {
            return description;
        }
        
        description = doc.select("meta[name=description]").attr("content");
        if (!description.isEmpty()) {
            return description;
        }
        
        Element descElement = doc.select(".description, .product-description, [class*=description]").first();
        if (descElement != null) {
            return descElement.text();
        }
        
        return "";
    }
}