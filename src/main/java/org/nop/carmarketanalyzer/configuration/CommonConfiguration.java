package org.nop.carmarketanalyzer.configuration;

import org.nop.carmarketanalyzer.operations.CarParser;
import org.nop.carmarketanalyzer.persistence.CarRepository;
import org.nop.carmarketanalyzer.configuration.properties.CrawlerConfigurationProperties;
import org.nop.carmarketanalyzer.configuration.properties.SearchConfigurationProperties;
import org.nop.carmarketanalyzer.operations.CarSearchCrawler;
import org.nop.carmarketanalyzer.operations.SearchWorker;
import org.nop.carmarketanalyzer.persistence.CarPersistence;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Configuration
@EnableRetry
@EnableConfigurationProperties({
        CrawlerConfigurationProperties.class,
        SearchConfigurationProperties.class
})
public class CommonConfiguration {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs
                                .defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean
    public CarPersistence carPersistence(CarRepository carRepository) {
        return new CarPersistence(carRepository);
    }

    @Bean
    public CarParser carParser() {
        return new CarParser();
    }

    @Bean
    public SearchWorker searchWorker(CrawlerConfigurationProperties crawlerConfigurationProperties) {
        return new SearchWorker(webClient(), crawlerConfigurationProperties, carParser());
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService crawlersThreadPool(CrawlerConfigurationProperties crawlerConfigurationProperties) {
        return Executors.newFixedThreadPool(crawlerConfigurationProperties.parallelism());
    }

    @Bean
    public CarSearchCrawler carSearchCrawler(
            SearchWorker searchWorker,
            CarPersistence carPersistence,
            ExecutorService crawlersThreadPool,
            SearchConfigurationProperties searchConfigurationProperties) {
        return new CarSearchCrawler(searchConfigurationProperties, searchWorker, carPersistence, crawlersThreadPool);
    }
}
