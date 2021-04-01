package com.example.r2dbcorder.repository;

import com.example.r2dbcorder.dto.IOdDtlDto;
import com.example.r2dbcorder.dto.OdDtlDto;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderDetailRepository extends ReactiveCrudRepository<OmOdDtl, String>, OrderDetailCustomRepository {

    Flux<OmOdDtl> findByOdNo(String odNo);

    Flux<OdDtlDto> findDtoByOdNo(String odNo);

    Flux<IOdDtlDto> findIDtoByOdNo(String odNo);

    <T> Flux<T> findTypeByOdNo(String odNo, Class<T> type);

    Flux<OmOdDtl> findByOdNoAndSlPrcGreaterThan(String odNo, int price);

    Flux<OmOdDtl> findByOdTypCd(String odTypCd);

    Mono<OmOdDtl> findByOdNoAndOdSeqAndProcSeq(String odNo, int odSeq, int procSeq);

    @Query("SELECT MAX(PROC_SEQ) + 1 FROM OM_OD_DTL WHERE OD_NO = :odNo AND OD_SEQ = :odSeq")
    Mono<Integer> findNextProcSeq(String odNo, int odSeq);
}
