package com.example.r2dbcorder.repository;

import com.example.r2dbcorder.repository.entity.OmOdDtl;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderDetailRepository extends ReactiveCrudRepository<OmOdDtl, String> {

    Flux<OmOdDtl> findByOdNo(String odNo);
}
