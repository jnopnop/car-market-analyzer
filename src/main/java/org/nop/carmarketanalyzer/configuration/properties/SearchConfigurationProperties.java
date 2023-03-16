package org.nop.carmarketanalyzer.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "search.payload")
public record SearchConfigurationProperties(
        String address,
        long proximity,
        long pageSize,
        String sortBy,
        boolean withPhotos,
        boolean withPrice,
        boolean isNew,
        boolean isUsed,
        boolean isDamaged,
        boolean isCpo,
        boolean isDealer,
        boolean isPrivate
) {
}
