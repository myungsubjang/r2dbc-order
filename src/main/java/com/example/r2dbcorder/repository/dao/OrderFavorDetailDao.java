package com.example.r2dbcorder.repository.dao;

import com.example.r2dbcorder.repository.OrderFavorDetailRepository;
import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFavorDetailDao {

    private final OrderFavorDetailRepository orderFavorDetailRepository;

    public Mono<List<OmOdFvrDtl>> saveList(List<OmOdFvrDtl> favorDetails) {
        //validation
        return Flux.fromIterable(favorDetails)
                .flatMap(orderFavorDetailRepository::save)
                .collectList();
    }

    public Mono<List<OmOdFvrDtl>> findAllByOdNo(String odNo) {
        //validation
        return orderFavorDetailRepository.findByOdNo(odNo)
                .collectList();
    }

    public Flux<OmOdFvrDtl> findFvrByOdNoOdSeqProcSeq(String odNo, int odSeq, int procSeq) {
        //validation
        return orderFavorDetailRepository.findByOdNoAndOdSeqAndProcSeq(odNo, odSeq, procSeq);
//                .collectList();
    }

}
