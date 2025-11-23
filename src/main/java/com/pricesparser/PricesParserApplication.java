package com.pricesparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PricesParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricesParserApplication.class, args);
    }
}
