package com.example.r2dbcorder.domain;

import com.example.r2dbcorder.repository.OrderFavorDetailRepository;
import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFavorDetailManager {

    private final OrderFavorDetailRepository orderFavorDetailRepository;

    public Mono<List<OmOdFvrDtl>> save(List<OmOdFvrDtl> favorDetails) {
        //validation
        return Flux.fromIterable(favorDetails)
                .flatMap(orderFavorDetailRepository::save)
                .collectList();
    }

    public Mono<List<OmOdFvrDtl>> findByOdNo(String odNo) {
        return orderFavorDetailRepository.findByOdNo(odNo)
                .collectList();
    }
}
