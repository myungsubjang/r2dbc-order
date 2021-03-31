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

    public Mono<List<OmOdDtl>> saveList(List<OmOdDtl> orderDetails) {
        //validation
        return Flux.fromIterable(orderDetails)
                .flatMap(orderDetailRepository::save)
                .collectList();
    }

    public Mono<OmOdDtl> save(OmOdDtl orderDetail) {
        return orderDetailRepository.save(orderDetail);
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
        return orderDetailRepository.findTypeByOdNo(odNo, type)
                .collectList();
    }

    public Mono<List<OmOdDtl>> findDtlOverPrice(String odNo, int price) {
        return orderDetailRepository.findByOdNoAndSlPrcGreaterThan(odNo, price)
                .collectList();
    }

    public Flux<OmOdDtl> findAllOdTypDtl(String odTypCd) {
        return orderDetailRepository.findByOdTypCd(odTypCd);
    }

    public Mono<OmOdDtl> update(OmOdDtl orderDetail) {
        return orderDetailRepository.update(orderDetail)
                .log()
                .flatMap(i -> orderDetailRepository.findByOdNoAndOdSeqAndProcSeq(orderDetail.getOdNo(), orderDetail.getOdSeq(), orderDetail.getProcSeq()));
    }

    public Mono<Integer> findNextProcSeq(String odNo, int odSeq) {
        return orderDetailRepository.findNextProcSeq(odNo, odSeq);
    }

}
