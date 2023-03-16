package org.nop.carmarketanalyzer.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nop.carmarketanalyzer.persistence.entity.CarEntity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class CarPersistence {
    private final CarRepository repository;

    @Retryable(
            maxAttempts = 10,
            backoff = @Backoff(delay = 100, maxDelay = 1000),
            retryFor = TransientDataAccessException.class)
    public void saveAll(Collection<CarEntity> cars) {
        for (CarEntity car : cars) {
            if (repository.existsById(car.getId())) {
                continue;
            }

            try {
                repository.save(car);
            } catch (DuplicateKeyException e) {
                log.error("Duplicate key for car:{}", car);
            }
        }
    }
}
