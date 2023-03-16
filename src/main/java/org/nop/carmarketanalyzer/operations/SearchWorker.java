package org.nop.carmarketanalyzer.operations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nop.carmarketanalyzer.persistence.entity.CarEntity;
import org.nop.carmarketanalyzer.configuration.properties.CrawlerConfigurationProperties;
import org.nop.carmarketanalyzer.domain.SearchPayload;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SearchWorker {
    private static final int SKIP_INITIAL_ENTRIES = 1;
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final WebClient webClient;
    private final CrawlerConfigurationProperties properties;
    private final CarParser carParser;

    @Retryable(
            maxAttempts = 5,
            recover = "searchFallback",
            backoff = @Backoff(random = true, delay = 500, multiplier = 1.5, maxDelay = 10_000))
    public Collection<CarEntity> search(SearchPayload searchPayload) {
        log.info("Page:{}. Starting extraction...", searchPayload.page());

        Map<String, Object> rawSearchResults = getRawSearchResults(searchPayload);
        List<CarEntity> foundCars = Optional.ofNullable(rawSearchResults.get(properties.searchResultsResponseField()))
                .map(String.class::cast)
                .map(resultsString -> resultsString.split(properties.searchResultsSeparatorRegex()))
                .stream()
                .flatMap(Arrays::stream)
                .skip(SKIP_INITIAL_ENTRIES)
                .map(carParser::parse)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        log.info("Page:{}. Extracted:{} cars", searchPayload.page(), foundCars.size());
        return foundCars;
    }

    @Recover
    public Collection<CarEntity> searchFallback(Throwable e, SearchPayload searchPayload) {
        log.error("Page:{}. Failed, skipping", searchPayload.page(), e);
        return List.of();
    }

    private Map<String, Object> getRawSearchResults(SearchPayload searchPayload) {
        return webClient
                .post()
                .uri(properties.domain() + properties.searchUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ORIGIN, properties.domain())
                .header(HttpHeaders.REFERER, properties.domain())
                .header(HttpHeaders.USER_AGENT, properties.userAgent())
                .bodyValue(searchPayload)
                .retrieve()
                .bodyToMono(RESPONSE_TYPE_REF)
                .block();
    }
}
