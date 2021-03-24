package com.example.r2dbcorder.repository;

import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderFavorDetailRepository extends ReactiveCrudRepository<OmOdFvrDtl, String> {

    Flux<OmOdFvrDtl> findByOdNo(String odNo);
}
