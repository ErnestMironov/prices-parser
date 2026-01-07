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
import org.springframework.stereotype.Component;

import com.pricesparser.model.Product;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Component
public class UniversalProductParser implements ProductParser {

  private static final Logger logger = LoggerFactory.getLogger(UniversalProductParser.class);

  private static final Pattern PRICE_PATTERN =
      Pattern.compile("([0-9\\s]+[\\.,]?[0-9]*)\\s*([рруб₽])?", Pattern.CASE_INSENSITIVE);

  private static final Pattern PRICE_IN_TEXT_PATTERN = Pattern.compile(
      "([0-9]{1,3}(?:\\s[0-9]{3})*(?:[\\.,][0-9]{2})?)\\s*[рруб₽]", Pattern.CASE_INSENSITIVE);

  private final Tracer tracer;

  public UniversalProductParser(OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer("com.pricesparser.parser", "1.0.0");
  }

  @Override
  public Product parse(String url) {
    Span span = tracer.spanBuilder("fetchHtml")
        .setAttribute("url", url)
        .startSpan();

    try (Scope scope = span.makeCurrent()) {
      logger.info("Парсинг URL: {}", url);

      Document doc = Jsoup.connect(url).userAgent(
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
          .header("Accept",
              "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
          .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
          .header("Connection", "keep-alive").header("Upgrade-Insecure-Requests", "1")
          .header("Referer", "https://pitergsm.ru/").header("Sec-Fetch-Dest", "document")
          .header("Sec-Fetch-Mode", "navigate").header("Sec-Fetch-Site", "same-origin")
          .header("Cache-Control", "max-age=0").timeout(15000).followRedirects(true).get();

      String html = doc.html();
      logger.info("HTML загружен, размер: {} байт", html.length());
      logger.info("Title страницы: {}", doc.title());

      span.setAttribute("html.size", html.length());
      span.setAttribute("page.title", doc.title());

      logger.info("Найдено h1 элементов: {}", doc.select("h1").size());
      logger.info("Найдено .product__price элементов: {}", doc.select(".product__price").size());
      logger.info("Найдено div.product__descr элементов: {}",
          doc.select("div.product__descr").size());

      if (doc.select("h1").isEmpty()) {
        logger.warn("h1 не найден! Первые 500 символов HTML:\n{}",
            html.substring(0, Math.min(500, html.length())));
      }

      return parseFromHtml(url, html);

    } catch (Exception e) {
      logger.error("Ошибка при парсинге URL {}: {}", url, e.getMessage(), e);
      span.recordException(e);
      span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
      throw new RuntimeException("Не удалось распарсить товар: " + e.getMessage(), e);
    } finally {
      span.end();
    }
  }

  @Override
  public Product parseFromHtml(String url, String html) {
    Span span = tracer.spanBuilder("parseFromHtml")
        .setAttribute("url", url)
        .setAttribute("html.size", html.length())
        .startSpan();

    try (Scope scope = span.makeCurrent()) {
      Document doc = Jsoup.parse(html);

      Span titleSpan = tracer.spanBuilder("extractTitle").startSpan();
      String title = null;
      try (Scope titleScope = titleSpan.makeCurrent()) {
        title = extractTitle(doc);
      } finally {
        titleSpan.setAttribute("title", title != null ? title : "");
        titleSpan.end();
      }

      Span priceSpan = tracer.spanBuilder("extractPrice")
          .setAttribute("url", url)
          .startSpan();
      BigDecimal price = null;
      try (Scope priceScope = priceSpan.makeCurrent()) {
        price = extractPrice(doc, url);
      } finally {
        priceSpan.setAttribute("price", price != null ? price.toString() : "0");
        priceSpan.end();
      }

      Span descSpan = tracer.spanBuilder("extractDescription").startSpan();
      String description = null;
      try (Scope descScope = descSpan.makeCurrent()) {
        description = extractDescription(doc);
      } finally {
        descSpan.setAttribute("description.length", description != null ? description.length() : 0);
        descSpan.end();
      }

      Product product = new Product(url, title, price, description);
      product.setParsedAt(LocalDateTime.now());

      logger.debug("Извлечено - Title: {}, Price: {}, Description: {}", title, price, description);

      span.setAttribute("product.title", title != null ? title : "");
      span.setAttribute("product.price", price != null ? price.toString() : "0");
      span.setAttribute("product.description.length", description != null ? description.length() : 0);

      return product;

    } catch (Exception e) {
      logger.error("Ошибка при парсинге HTML для URL {}: {}", url, e.getMessage());
      span.recordException(e);
      span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
      throw new RuntimeException("Не удалось распарсить HTML: " + e.getMessage(), e);
    } finally {
      span.end();
    }
  }

  private String extractTitle(Document doc) {
    String title = null;

    Element h1Name = doc.selectFirst("h1[itemprop=name]");
    if (h1Name != null) {
      title = h1Name.text().trim();
      if (!title.isEmpty()) {
        logger.debug("Title найден в h1[itemprop=name]: {}", title);
        return title;
      }
    }

    Element h1Section = doc.selectFirst("h1.section__title");
    if (h1Section != null) {
      title = h1Section.text().trim();
      if (!title.isEmpty()) {
        logger.debug("Title найден в h1.section__title: {}", title);
        return title;
      }
    }

    title = doc.select("meta[property=og:title]").attr("content");
    if (title != null && !title.isEmpty()) {
      return title.trim();
    }

    title = doc.select("meta[name=title]").attr("content");
    if (title != null && !title.isEmpty()) {
      return title.trim();
    }

    Element h1 = doc.selectFirst("h1");
    if (h1 != null) {
      String h1Text = h1.text().trim();
      if (!h1Text.isEmpty()) {
        return h1Text;
      }
    }

    title = doc.title();
    if (title != null && !title.isEmpty()) {
      return title.trim();
    }

    return "Без названия";
  }

  private BigDecimal extractPrice(Document doc, String url) {
    String priceText = null;

    var priceContainers = doc.select(".product__price[itemprop=offers], [itemprop=offers]");
    for (Element priceContainer : priceContainers) {
      Element priceSpan = priceContainer.selectFirst("span[itemprop=price]");
      if (priceSpan != null) {
        priceText = priceSpan.text().trim();
        if (priceText != null && !priceText.isEmpty()) {
          BigDecimal price = parsePrice(priceText);
          if (price.compareTo(BigDecimal.ZERO) > 0) {
            logger.debug("Цена найдена в span[itemprop=price]: {}", priceText);
            return price;
          }
        }
      }

      priceText = priceContainer.select("[itemprop=price]").attr("content");
      if (priceText != null && !priceText.isEmpty()) {
        BigDecimal price = parsePrice(priceText);
        if (price.compareTo(BigDecimal.ZERO) > 0) {
          logger.debug("Цена найдена в [itemprop=price] content: {}", priceText);
          return price;
        }
      }

      priceText = priceContainer.select("[itemprop=price]").text();
      if (priceText != null && !priceText.isEmpty()) {
        BigDecimal price = parsePrice(priceText);
        if (price.compareTo(BigDecimal.ZERO) > 0) {
          logger.debug("Цена найдена в [itemprop=price] text: {}", priceText);
          return price;
        }
      }

      priceText = priceContainer.ownText().trim();
      if (priceText == null || priceText.isEmpty()) {
        priceText = priceContainer.text().trim();
      }
      if (priceText != null && !priceText.isEmpty()) {
        BigDecimal price = parsePrice(priceText);
        if (price.compareTo(BigDecimal.ZERO) > 0) {
          logger.debug("Цена найдена в .product__price тексте: {}", priceText);
          return price;
        }
      }
    }

    priceText = doc.select("meta[property=product:price:amount]").attr("content");
    if (priceText != null && !priceText.isEmpty()) {
      logger.debug("Цена найдена в meta[property=product:price:amount]: {}", priceText);
      return parsePrice(priceText);
    }

    priceText = doc.select("[itemprop=price]").attr("content");
    if (priceText != null && !priceText.isEmpty()) {
      logger.debug("Цена найдена в [itemprop=price]: {}", priceText);
      return parsePrice(priceText);
    }

    Element priceSpan = doc.selectFirst("span[itemprop=price]");
    if (priceSpan != null) {
      priceText = priceSpan.text().trim();
      if (priceText != null && !priceText.isEmpty()) {
        BigDecimal price = parsePrice(priceText);
        if (price.compareTo(BigDecimal.ZERO) > 0) {
          logger.debug("Цена найдена в span[itemprop=price] напрямую: {}", priceText);
          return price;
        }
      }
    }

    priceText = doc.select("[data-price]").attr("data-price");
    if (priceText != null && !priceText.isEmpty()) {
      logger.debug("Цена найдена в [data-price]: {}", priceText);
      return parsePrice(priceText);
    }

    Element priceElement = doc.selectFirst(".price, .product-price, [class*=price]");
    if (priceElement != null) {
      priceText = priceElement.attr("data-price");
      if (priceText == null || priceText.isEmpty()) {
        priceText = priceElement.text();
      }
      if (priceText != null && !priceText.isEmpty()) {
        logger.debug("Цена найдена в элементе с классом price: {}", priceText);
        return parsePrice(priceText);
      }
    }

    String bodyText = doc.body() != null ? doc.body().text() : "";
    Matcher matcher = PRICE_IN_TEXT_PATTERN.matcher(bodyText);
    if (matcher.find()) {
      String foundPrice = matcher.group(1);
      logger.debug("Цена найдена в тексте страницы: {}", foundPrice);
      return parsePrice(foundPrice);
    }

    logger.warn("Цена не найдена для URL: {}", url);
    return BigDecimal.ZERO;
  }

  private BigDecimal parsePrice(String priceText) {
    if (priceText == null || priceText.isEmpty()) {
      return BigDecimal.ZERO;
    }

    try {
      String cleanPrice = priceText.replaceAll("[^0-9.,]", "").replace(",", ".");

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

    Element descElement = doc.selectFirst("div.product__descr[itemprop=description]");
    if (descElement != null) {
      description = descElement.text().trim();
      if (description != null && !description.isEmpty()) {
        logger.debug("Description найден в div.product__descr[itemprop=description]");
        return description;
      }
    }

    description = doc.select("meta[property=og:description]").attr("content");
    if (description != null && !description.isEmpty()) {
      return description.trim();
    }

    description = doc.select("meta[name=description]").attr("content");
    if (description != null && !description.isEmpty()) {
      return description.trim();
    }

    descElement = doc.selectFirst(
        ".description, .product-description, [class*=description], [itemprop=description], .product-info, .product__description");
    if (descElement != null) {
      return descElement.text().trim();
    }

    return "";
  }
}
