package org.nop.carmarketanalyzer.persistence;

import org.nop.carmarketanalyzer.persistence.entity.CarEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends CrudRepository<CarEntity, String> {
}
