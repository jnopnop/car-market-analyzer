package org.nop.carmarketanalyzer.operations;

import org.nop.carmarketanalyzer.configuration.properties.SearchConfigurationProperties;
import org.nop.carmarketanalyzer.domain.SearchPayload;
import org.nop.carmarketanalyzer.persistence.CarPersistence;
import org.nop.carmarketanalyzer.persistence.entity.CarEntity;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.stream.LongStream;

public class CarSearchCrawler {
    private static final int TOTAL_PAGES = 267;
    private final SearchPayload templateSearchPayload;
    private final SearchWorker searchWorker;
    private final CarPersistence persistence;
    private final ExecutorService threadPool;

    public CarSearchCrawler(SearchConfigurationProperties properties,
                            SearchWorker searchWorker,
                            CarPersistence carPersistence,
                            ExecutorService threadPool) {
        this.templateSearchPayload = toSearchPayload(properties).forPage(0);
        this.searchWorker = searchWorker;
        this.persistence = carPersistence;
        this.threadPool = threadPool;
    }

    public void crawl() {
        LongStream.range(0, TOTAL_PAGES)
                .mapToObj(CrawlerWorker::new)
                .forEach(threadPool::submit);
    }

    private static SearchPayload toSearchPayload(SearchConfigurationProperties properties) {
        return SearchPayload.builder()
                .address(properties.address())
                .pageSize(properties.pageSize())
                .proximity(properties.proximity())
                .sortBy(properties.sortBy())
                .withPhotos(properties.withPhotos())
                .withPrice(properties.withPrice())
                .isNew(properties.isNew())
                .isCpo(properties.isCpo())
                .isPrivate(properties.isPrivate())
                .isUsed(properties.isUsed())
                .isDamaged(properties.isDamaged())
                .isDealer(properties.isDealer())
                .build();
    }

    private class CrawlerWorker implements Runnable {
        private final SearchPayload searchPayload;

        private CrawlerWorker(long page) {
            this.searchPayload = templateSearchPayload.forPage(page);
        }

        @Override
        public void run() {
            Collection<CarEntity> foundCars = searchWorker.search(searchPayload);
            persistence.saveAll(foundCars);
        }
    }
}
