package com.pricesparser.benchmark;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.pricesparser.model.Product;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class ParsingBenchmark {

    private List<Product> products;

    @org.openjdk.jmh.annotations.Setup
    public void setup() {
        products = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Product product = new Product(
                "https://example.com/product" + i,
                "Product " + i,
                new BigDecimal(100 + i * 10),
                "Description " + i
            );
            products.add(product);
        }
    }

    @Benchmark
    public List<BigDecimal> benchmarkForLoop() {
        List<BigDecimal> prices = new ArrayList<>();
        for (Product product : products) {
            if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.valueOf(500)) > 0) {
                prices.add(product.getPrice());
            }
        }
        return prices;
    }

    @Benchmark
    public List<BigDecimal> benchmarkStream() {
        return products.stream()
            .filter(product -> product.getPrice() != null && product.getPrice().compareTo(BigDecimal.valueOf(500)) > 0)
            .map(Product::getPrice)
            .collect(Collectors.toList());
    }

    @Benchmark
    public List<BigDecimal> benchmarkParallelStream() {
        return products.parallelStream()
            .filter(product -> product.getPrice() != null && product.getPrice().compareTo(BigDecimal.valueOf(500)) > 0)
            .map(Product::getPrice)
            .collect(Collectors.toList());
    }

    @Benchmark
    public BigDecimal benchmarkForLoopSum() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Product product : products) {
            if (product.getPrice() != null) {
                sum = sum.add(product.getPrice());
            }
        }
        return sum;
    }

    @Benchmark
    public BigDecimal benchmarkStreamSum() {
        return products.stream()
            .filter(product -> product.getPrice() != null)
            .map(Product::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Benchmark
    public BigDecimal benchmarkParallelStreamSum() {
        return products.parallelStream()
            .filter(product -> product.getPrice() != null)
            .map(Product::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
