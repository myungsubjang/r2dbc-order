package com.example.r2dbcorder.service;

import com.example.r2dbcorder.dto.ClaimRequest;
import com.example.r2dbcorder.exceptions.OrderNotFoundException;
import com.example.r2dbcorder.repository.dao.OrderDao;
import com.example.r2dbcorder.repository.dao.OrderDetailDao;
import com.example.r2dbcorder.repository.dao.OrderFavorDetailDao;
import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ClaimService {

    private final OrderService orderService;
    private final OrderDao orderDao;
    private final OrderDetailDao orderDetailDao;
    private final OrderFavorDetailDao orderFavorDetailDao;

    private static final Scheduler SCHEDULER = Schedulers.parallel();

    @Transactional
    public Mono<OmOd> cancelOrder(ClaimRequest claimReq) {
        return orderService.findFullOrderByOdNo(claimReq.getOdNo())
                .flatMap(order -> cancelOrderInternal(order, claimReq));
    }

    private Mono<OmOd> cancelOrderInternal(OmOd targetOrder, ClaimRequest claimRequest) {
        return validateOrderForCancel(targetOrder, claimRequest)
                .then(getClaimNo())
                .flatMapMany(claimNo -> processCancel(targetOrder, claimRequest, claimNo))
                .then(updateModdtimeAndGet(targetOrder))
                .flatMap(order -> orderService.findFullOrderByOdNo(order.getOdNo()));
    }

    private Flux<?> processCancel(OmOd beforeOrder, ClaimRequest claimRequest, String claimNo) {
        return Flux.fromIterable(claimRequest.getSeqList())
                .flatMap(seq -> {
                    OmOdDtl matchingDtl = findDtlMatchingSeq(beforeOrder.getOmOdDtlList(), seq);
                    List<OmOdFvrDtl> matchingFvrList = findFvrListMatchingSeq(beforeOrder.getOmOdFvrDtlList(), seq);
                    return Flux.merge(
                            updateCancelDtl(matchingDtl),
                            updateCancelFvr(matchingFvrList),
                            createCancelDtl(matchingDtl, claimNo).flatMap(dtl -> createCancelFvr(matchingFvrList, claimNo, dtl.getProcSeq()))
                    ).subscribeOn(SCHEDULER);
                });
    }

    private OmOdDtl findDtlMatchingSeq(List<OmOdDtl> dtlList, ClaimRequest.Seq seq) {
        Optional<OmOdDtl> optionalDtl = dtlList.stream()
                .filter(dtl -> dtl.getOdSeq() == seq.getOdSeq() && dtl.getProcSeq() == seq.getProcSeq())
                .findAny();
        if (optionalDtl.isPresent()) {
            return optionalDtl.get();
        }
        throw new OrderNotFoundException("order detail not found exception.");
    }

    private List<OmOdFvrDtl> findFvrListMatchingSeq(List<OmOdFvrDtl> fvrList, ClaimRequest.Seq seq) {
        return fvrList.stream()
                .filter(fvr -> fvr.getOdSeq() == seq.getOdSeq() && fvr.getProcSeq() == seq.getProcSeq())
                .collect(Collectors.toList());
    }

    private Flux<?> validateOrderForCancel(OmOd beforeOrder, ClaimRequest claimRequest) {
        return Flux.concat(
                validateDtlListForCancel(beforeOrder.getOmOdDtlList(), claimRequest.getSeqList()),
                validateFvrListForCancel(beforeOrder.getOmOdFvrDtlList(), claimRequest.getSeqList())
        );
    }

    private Flux<OmOdDtl> validateDtlListForCancel(List<OmOdDtl> dtlList, List<ClaimRequest.Seq> seqList) {
        Flux<OmOdDtl> matchingDtl = Flux.fromIterable(seqList)
                .map(seq -> findDtlMatchingSeq(dtlList, seq))
                .switchIfEmpty(Flux.error(new RuntimeException("there are no detail")));
        return validateDtlForCancel(matchingDtl);
    }

    private Flux<OmOdDtl> validateDtlForCancel(Flux<OmOdDtl> dtlFlux) {
        return dtlFlux.handle((dtl, sink) -> {
            if (!isValidToCancelDtl(dtl)) {
                sink.error(new RuntimeException("not valid order detail"));
            }
            sink.next(dtl);
        });
    }

    private boolean isValidToCancelDtl(OmOdDtl orderDetail) {
        //임시
        return true;
    }

    private Flux<List<OmOdFvrDtl>> validateFvrListForCancel(List<OmOdFvrDtl> fvrList, List<ClaimRequest.Seq> seqList) {
        Flux<List<OmOdFvrDtl>> matchingFvrListFlux = Flux.fromIterable(seqList)
                .map(seq -> findFvrListMatchingSeq(fvrList, seq));
        return validateFvrListForCancel(matchingFvrListFlux);
    }

    private Flux<List<OmOdFvrDtl>> validateFvrListForCancel(Flux<List<OmOdFvrDtl>> fvrDtlFlux) {
        return fvrDtlFlux.handle((fvrList, sink) -> {
            if (!isValidToCancelFvr(fvrList)) {
                sink.error(new RuntimeException("not valid order favor"));
            }
            sink.next(fvrList);
        });
    }

    private boolean isValidToCancelFvr(List<OmOdFvrDtl> fvrList) {
        //임시
        return true;
    }

    private Mono<String> getClaimNo() {
        return Mono.just("claimNo"); //임시
    }

    private Mono<OmOdDtl> updateCancelDtl(OmOdDtl targetDtl) {
        OmOdDtl updateDtl = targetDtl.withCnclQty(targetDtl.getOdQty());
        return Mono.just(updateDtl)
                .flatMap(orderDetailDao::update);
    }

    private Mono<OmOdDtl> createCancelDtl(OmOdDtl targetDtl, String claimNo) {
        OmOdDtl cancelDtl = targetDtl.withOdTypCd("20")
                .withOrglProcSeq(targetDtl.getProcSeq())
                .withClmNo(claimNo)
                .withRegDttm(null)
                .withModDttm(null);
        return Mono.just(cancelDtl)
                .zipWith(orderDetailDao.findNextProcSeq(cancelDtl.getOdNo(), cancelDtl.getOdSeq()), OmOdDtl::withProcSeq)
                .flatMap(orderDetailDao::save);
    }

    private Mono<List<OmOdFvrDtl>> updateCancelFvr(List<OmOdFvrDtl> targetFvrList) {
        return Flux.fromIterable(targetFvrList)
                .flatMap(targetFvr -> updateCancelFvr(targetFvr))
                .collectList();
    }

    private Mono<OmOdFvrDtl> updateCancelFvr(OmOdFvrDtl targetFvr) {
        return Mono.just(targetFvr)
                .map(odFvr -> odFvr.withCnclQty(odFvr.getAplyQty()))
                .flatMap(orderFavorDetailDao::save);
    }

    private Mono<List<OmOdFvrDtl>> createCancelFvr(List<OmOdFvrDtl> targetFvrList, String claimNo, int createdProcSeq) {
        return Flux.fromIterable(targetFvrList)
                .flatMap(targetFvr -> createCancelFvr(targetFvr, claimNo, createdProcSeq))
                .collectList();
    }

    private Mono<OmOdFvrDtl> createCancelFvr(OmOdFvrDtl targetFvr, String claimNo, int createdProcSeq) {
        OmOdFvrDtl cancelFvr = targetFvr.withOrglOdFvrNo(targetFvr.getOdFvrNo())
                .withProcSeq(createdProcSeq)
                .withOdFvrDvsCd("CNCL")
                .withClmNo(claimNo)
                .withOdFvrNo(null);
        return orderFavorDetailDao.save(cancelFvr);
    }

    private Mono<OmOd> updateModdtimeAndGet(OmOd targetOrder) {
        return orderDao.saveOrder(targetOrder);
    }
}
