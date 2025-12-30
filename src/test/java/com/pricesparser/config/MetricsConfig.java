package com.pricesparser.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Timer parseDurationTimer(MeterRegistry registry) {
        return Timer.builder("parse_duration_seconds")
                .description("Time taken to parse a product")
                .register(registry);
    }

    @Bean
    public Counter parseSuccessCounter(MeterRegistry registry) {
        return Counter.builder("parse_success_total")
                .description("Total number of successful parses")
                .register(registry);
    }

    @Bean
    public Counter parseErrorsCounter(MeterRegistry registry) {
        return Counter.builder("parse_errors_total")
                .description("Total number of parse errors")
                .tag("error_type", "parse_error")
                .register(registry);
    }

    @Bean
    public Counter productsSavedCounter(MeterRegistry registry) {
        return Counter.builder("products_saved_total")
                .description("Total number of products saved to database")
                .register(registry);
    }
}