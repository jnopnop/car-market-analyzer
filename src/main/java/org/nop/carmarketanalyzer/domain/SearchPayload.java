package org.nop.carmarketanalyzer.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.beans.Transient;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public record SearchPayload(
        @JsonProperty("Address") String address,
        @JsonProperty("Proximity") long proximity,
        @JsonProperty("Skip") long skip,
        @JsonProperty("SortBy") String sortBy,
        @JsonProperty("Top") long pageSize,
        @JsonProperty("WithPhotos") boolean withPhotos,
        @JsonProperty("WithPrice") boolean withPrice,
        @JsonProperty("IsNew") boolean isNew,
        @JsonProperty("IsUsed") boolean isUsed,
        @JsonProperty("IsDamaged") boolean isDamaged,
        @JsonProperty("IsCpo") boolean isCpo,
        @JsonProperty("IsDealer") boolean isDealer,
        @JsonProperty("IsPrivate") boolean isPrivate
) {

    public SearchPayload forPage(long pageNumber) {
        return this.toBuilder()
                .skip(pageNumber * pageSize)
                .build();
    }

    @Transient
    public long page() {
        return skip / pageSize;
    }
}
