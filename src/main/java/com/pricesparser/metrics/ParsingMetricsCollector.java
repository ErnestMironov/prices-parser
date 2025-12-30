package com.pricesparser.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Компонент для сбора метрик производительности парсинга.
 * Отслеживает время выполнения, количество успешных и ошибочных парсингов,
 * а также количество записей в БД.
 */
@Component
public class ParsingMetricsCollector {

  private static final Logger logger = LoggerFactory.getLogger(ParsingMetricsCollector.class);

  private final Counter successfulParsingCounter;
  private final Counter failedParsingCounter;
  private final Counter databaseRecordsCounter;
  private final Timer parsingDurationTimer;
  private final Timer databaseInsertTimer;
  private final MeterRegistry meterRegistry;

  public ParsingMetricsCollector(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    // Счётчик успешных парсингов
    this.successfulParsingCounter = Counter.builder("parsing.successful.total")
        .description("Total number of successful parsing operations")
        .tag("type", "parsing")
        .register(meterRegistry);

    // Счётчик ошибочных парсингов
    this.failedParsingCounter = Counter.builder("parsing.failed.total")
        .description("Total number of failed parsing operations")
        .tag("type", "parsing")
        .register(meterRegistry);

    // Счётчик записей в БД
    this.databaseRecordsCounter = Counter.builder("database.records.inserted.total")
        .description("Total number of records inserted into database")
        .tag("type", "database")
        .register(meterRegistry);

    // Таймер для времени выполнения парсинга
    this.parsingDurationTimer = Timer.builder("parsing.duration")
        .description("Time taken to parse a product")
        .tag("type", "parsing")
        .register(meterRegistry);

    // Таймер для времени вставки в БД
    this.databaseInsertTimer = Timer.builder("database.insert.duration")
        .description("Time taken to insert a record into database")
        .tag("type", "database")
        .register(meterRegistry);

    logger.info("ParsingMetricsCollector инициализирован");
  }

  /**
   * Регистрирует успешный парсинг с измерением времени выполнения.
   *
   * @param durationMillis время выполнения в миллисекундах
   */
  public void recordSuccessfulParsing(long durationMillis) {
    successfulParsingCounter.increment();
    parsingDurationTimer.record(java.time.Duration.ofMillis(durationMillis));
    logger.debug("Зарегистрирован успешный парсинг: {} мс", durationMillis);
  }

  /**
   * Регистрирует ошибку при парсинге.
   *
   * @param url URL который не удалось спарсить
   * @param exception исключение которое произошло
   */
  public void recordFailedParsing(String url, Exception exception) {
    failedParsingCounter.increment();
    logger.error("Зарегистрирована ошибка парсинга для URL: {} - {}", url, exception.getMessage());
  }

  /**
   * Регистрирует вставку записи в БД.
   *
   * @param durationMillis время выполнения вставки в миллисекундах
   */
  public void recordDatabaseInsert(long durationMillis) {
    databaseRecordsCounter.increment();
    databaseInsertTimer.record(java.time.Duration.ofMillis(durationMillis));
    logger.debug("Зарегистрирована вставка в БД: {} мс", durationMillis);
  }

  /**
   * Регистрирует множественные вставки в БД.
   *
   * @param count количество записей
   * @param durationMillis время выполнения в миллисекундах
   */
  public void recordBatchDatabaseInsert(int count, long durationMillis) {
    databaseRecordsCounter.increment(count);
    databaseInsertTimer.record(java.time.Duration.ofMillis(durationMillis));
    logger.debug("Зарегистрирована батч-вставка {} записей: {} мс", count, durationMillis);
  }

  /**
   * Возвращает текущее количество успешных парсингов.
   */
  public double getSuccessfulParsingCount() {
    return successfulParsingCounter.count();
  }

  /**
   * Возвращает текущее количество ошибочных парсингов.
   */
  public double getFailedParsingCount() {
    return failedParsingCounter.count();
  }

  /**
   * Возвращает текущее количество записей в БД.
   */
  public double getDatabaseRecordsCount() {
    return databaseRecordsCounter.count();
  }

  /**
   * Возвращает среднее время парсинга в миллисекундах.
   */
  public double getAverageParsingDuration() {
    return parsingDurationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
  }

  /**
   * Возвращает среднее время вставки в БД в миллисекундах.
   */
  public double getAverageDatabaseInsertDuration() {
    return databaseInsertTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
  }

  /**
   * Выводит отчёт по метрикам.
   */
  public void printMetricsReport() {
    logger.info("===== ОТЧЕТ ПО МЕТРИКАМ =====");
    logger.info("Успешных парсингов: {}", getSuccessfulParsingCount());
    logger.info("Ошибочных парсингов: {}", getFailedParsingCount());
    logger.info("Записей в БД: {}", getDatabaseRecordsCount());
    logger.info("Среднее время парсинга: {:.2f} мс", getAverageParsingDuration());
    logger.info("Среднее время вставки в БД: {:.2f} мс", getAverageDatabaseInsertDuration());
    logger.info("============================");
  }
}
