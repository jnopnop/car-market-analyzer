package org.nop.carmarketanalyzer.operations;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.nop.carmarketanalyzer.persistence.entity.CarEntity;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
public class CarParser {
    private static final String PRICE_REPLACEMENT_REGEX = "[$,]";
    private static final String MILEAGE_REPLACEMENT_REGEX = ",";
    private static final String WHITESPACE = " ";
    private static final String URI_SEPARATOR = "/";
    private static final String SELECTOR_PRICE = "#price-amount-value";
    private static final String SELECTOR_YEAR = "span";
    private static final String SELECTOR_MILEAGE = "div.kms";
    private static final String SELECTOR_URI = "a.result-title";

    public Optional<CarEntity> parse(String carHtml) {
        Document carDocument = Jsoup.parse(carHtml);
        Elements uriElement = carDocument.select(SELECTOR_URI);
        if (uriElement.isEmpty()) {
            return Optional.empty();
        }

        try {
            String uri = extractUri(uriElement);
            String[] uriParts = URLDecoder.decode(uri, StandardCharsets.UTF_8).split(URI_SEPARATOR);
            CarEntity parsedCar = CarEntity.builder()
                    .id("https://autotrader.ca" + uri)
                    .make(uriParts[2])
                    .model(uriParts[3])
                    .mileage(extractMileage(carDocument))
                    .year(extractYear(uriElement))
                    .price(extractPrice(carDocument))
                    .build();

            return Optional.of(parsedCar);
        } catch (Exception e) {
            log.warn("Failed to parse {}", carHtml, e);
            return Optional.empty();
        }
    }

    private static String extractUri(Elements uriElement) {
        return uriElement.attr("href").trim().split("\\?")[0];
    }

    private static BigDecimal extractPrice(Document carDocument) {
        return Optional.of(carDocument.select(SELECTOR_PRICE).text().trim())
                .map(priceString -> priceString.replaceAll(PRICE_REPLACEMENT_REGEX, ""))
                .map(priceString -> {
                    try {
                        return new BigDecimal(priceString);
                    } catch (Exception e) {
                        log.warn("Failed to parse price:{}", priceString);
                        return null;
                    }
                }).orElse(null);
    }

    private static Integer extractYear(Elements uriElement) {
        return Optional.of(uriElement.select(SELECTOR_YEAR).text().trim())
                .map(headerString -> headerString.split(WHITESPACE))
                .filter(headerParts -> headerParts.length > 0)
                .map(headerParts -> headerParts[0])
                .map(yearString -> {
                    try {
                        return Integer.parseInt(yearString);
                    } catch (Exception e) {
                        log.warn("Failed to parse year:{}", yearString);
                        return null;
                    }
                }).orElse(null);
    }

    private static Long extractMileage(Document carDocument) {
        return Optional.of(carDocument.select(SELECTOR_MILEAGE).text().trim().split(WHITESPACE))
                .filter(parts -> parts.length > 1)
                .map(parts -> parts[1].replaceAll(MILEAGE_REPLACEMENT_REGEX, ""))
                .map(mileageString -> {
                    try {
                        return Long.parseLong(mileageString);
                    } catch (Exception e) {
                        log.warn("Failed to parse mileage:{}", mileageString);
                        return null;
                    }
                })
                .orElse(0L);
    }
}
