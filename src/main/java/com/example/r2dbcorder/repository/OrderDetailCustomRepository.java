package com.example.r2dbcorder.repository;

import com.example.r2dbcorder.dto.OmOdDtlFvrDtlDto;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderDetailCustomRepository {

    Mono<Integer> update(OmOdDtl orderDetail);

    Flux<OmOdDtlFvrDtlDto> joinPractice(String odNo);
}
