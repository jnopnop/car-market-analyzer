package org.nop.carmarketanalyzer.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "search.settings")
public record CrawlerConfigurationProperties(
        int parallelism,
        String domain,
        String searchUri,
        String userAgent,
        String searchResultsResponseField,
        String searchResultsSeparatorRegex
) {
}
