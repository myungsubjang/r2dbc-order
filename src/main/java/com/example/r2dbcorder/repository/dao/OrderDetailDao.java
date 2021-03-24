package com.example.r2dbcorder.repository.dao;

import com.example.r2dbcorder.dto.IOdDtlDto;
import com.example.r2dbcorder.dto.OdDtlDto;
import com.example.r2dbcorder.repository.OrderDetailRepository;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@RequiredArgsConstructor
public class OrderDetailDao {

    private final OrderDetailRepository orderDetailRepository;

    public Mono<List<OmOdDtl>> save(List<OmOdDtl> orderDetails) {
        //validation
        return Flux.fromIterable(orderDetails)
                .flatMap(orderDetailRepository::save)
                .collectList();
    }

    public Mono<List<OmOdDtl>> findAllDetailByOdNo(String odNo) {
        return orderDetailRepository.findByOdNo(odNo)
                .collectList();
    }

    public Mono<List<OdDtlDto>> findDtoByOdNo(String odNo) {
        return orderDetailRepository.findDtoByOdNo(odNo)
                .collectList();
    }

    public Mono<List<IOdDtlDto>> findIDtoByOdNo(String odNo) {
        return orderDetailRepository.findIDtoByOdNo(odNo)
                .collectList();
    }

    public <T> Mono<? extends List<? extends T>> findTypeByOdNo(String odNo, Class<T> type) {
        // enummap으로 제공하는게 나을듯?
        return orderDetailRepository.findTypeByOdNo(odNo, type)
                .collectList();
    }

    public Mono<List<OmOdDtl>> findDtlOverPrice(String odNo, int price) {
        return orderDetailRepository.findByOdNoAndSlPrcGreaterThan(odNo, price)
                .collectList();
    }


}
