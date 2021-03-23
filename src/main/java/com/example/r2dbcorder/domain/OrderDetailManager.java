package com.example.r2dbcorder.domain;

import com.example.r2dbcorder.repository.OrderDetailRepository;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderDetailManager {

    private final OrderDetailRepository orderDetailRepository;

    public Mono<List<OmOdDtl>> save(List<OmOdDtl> orderDetails) {
        return Flux.fromIterable(orderDetails)
                .flatMap(orderDetailRepository::save)
                .collectList();
    }

}
