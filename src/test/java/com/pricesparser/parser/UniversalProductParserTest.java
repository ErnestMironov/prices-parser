package com.pricesparser.parser;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pricesparser.model.Product;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

@DisplayName("UniversalProductParser Tests")
class UniversalProductParserTest {
    
    private UniversalProductParser parser;
    
    @BeforeEach
    void setUp() {
        OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
        Tracer tracer = mock(Tracer.class);
        when(openTelemetry.getTracer(any(String.class), any(String.class))).thenReturn(tracer);
        parser = new UniversalProductParser(openTelemetry);
    }
    
    @Test
    @DisplayName("Должен извлечь title из og:title")
    void shouldExtractTitleFromOgTitle() {
        String html = """
            <html>
                <head>
                    <meta property="og:title" content="iPhone 15 Pro" />
                </head>
                <body></body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/product1", html);
        
        assertThat(product.getTitle()).isEqualTo("iPhone 15 Pro");
    }
    
    @Test
    @DisplayName("Должен извлечь title из h1")
    void shouldExtractTitleFromH1() {
        String html = """
            <html>
                <head>
                    <title>Example Site</title>
                </head>
                <body>
                    <h1>Samsung Galaxy S24</h1>
                </body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/product2", html);
        
        assertThat(product.getTitle()).isEqualTo("Samsung Galaxy S24");
    }
    
    @Test
    @DisplayName("Должен извлечь price из data-price")
    void shouldExtractPriceFromDataPrice() {
        String html = """
            <html>
                <body>
                    <div data-price="99999.99">Цена</div>
                </body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/product3", html);
        
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("99999.99"));
    }
    
    @Test
    @DisplayName("Должен извлечь price из класса price")
    void shouldExtractPriceFromPriceClass() {
        String html = """
            <html>
                <body>
                    <span class="price">89 999 руб</span>
                </body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/product4", html);
        
        assertThat(product.getPrice()).isGreaterThan(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Должен извлечь description из meta description")
    void shouldExtractDescriptionFromMetaDescription() {
        String html = """
            <html>
                <head>
                    <meta name="description" content="Флагманский смартфон от Samsung" />
                </head>
                <body></body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/product5", html);
        
        assertThat(product.getDescription()).isEqualTo("Флагманский смартфон от Samsung");
    }
    
    @Test
    @DisplayName("Должен извлечь description из og:description")
    void shouldExtractDescriptionFromOgDescription() {
        String html = """
            <html>
                <head>
                    <meta property="og:description" content="Новейший смартфон Apple" />
                </head>
                <body></body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/product6", html);
        
        assertThat(product.getDescription()).isEqualTo("Новейший смартфон Apple");
    }
    
    @Test
    @DisplayName("Должен корректно обрабатывать полный HTML")
    void shouldParseFullHtml() {
        String html = """
            <html>
                <head>
                    <meta property="og:title" content="MacBook Pro 16" />
                    <meta property="og:description" content="Профессиональный ноутбук" />
                    <meta property="product:price:amount" content="249999.50" />
                </head>
                <body>
                    <h1>MacBook Pro 16</h1>
                </body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/macbook", html);
        
        assertThat(product.getTitle()).isEqualTo("MacBook Pro 16");
        assertThat(product.getDescription()).isEqualTo("Профессиональный ноутбук");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("249999.50"));
        assertThat(product.getUrl()).isEqualTo("https://example.com/macbook");
    }
    
    @Test
    @DisplayName("Должен обрабатывать отсутствие цены")
    void shouldHandleMissingPrice() {
        String html = """
            <html>
                <body>
                    <h1>Товар без цены</h1>
                </body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/no-price", html);
        
        assertThat(product.getTitle()).isEqualTo("Товар без цены");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Должен обрабатывать отсутствие описания")
    void shouldHandleMissingDescription() {
        String html = """
            <html>
                <body>
                    <h1>Товар без описания</h1>
                </body>
            </html>
            """;
        
        Product product = parser.parseFromHtml("https://example.com/no-desc", html);
        
        assertThat(product.getTitle()).isEqualTo("Товар без описания");
        assertThat(product.getDescription()).isEmpty();
    }
}