package org.nop.carmarketanalyzer;

import lombok.extern.slf4j.Slf4j;
import org.nop.carmarketanalyzer.operations.CarSearchCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;

@Slf4j
@SpringBootApplication
public class CarMarketAnalyzerApplication implements CommandLineRunner {

    @Autowired
    CarSearchCrawler crawler;
    @Autowired
    ExecutorService crawlersThreadPool;

    public static void main(String[] args) {
        SpringApplication.run(CarMarketAnalyzerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        crawler.crawl();
        crawlersThreadPool.shutdown();
    }

}
