package com.example.r2dbcorder.service;

import com.example.r2dbcorder.dto.ClaimRequest;
import com.example.r2dbcorder.repository.dao.OrderDao;
import com.example.r2dbcorder.repository.dao.OrderDetailDao;
import com.example.r2dbcorder.repository.dao.OrderFavorDetailDao;
import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final OrderService orderService;
    private final OrderDao orderDao;
    private final OrderDetailDao orderDetailDao;
    private final OrderFavorDetailDao orderFavorDetailDao;

    @Transactional
    public Mono<OmOd> cancelOrder(ClaimRequest claimReq) {
        return orderService.findFullOrderByOdNo(claimReq.getOdNo())
                //validation
                .flatMap(order -> cancelOrderInternal2(order, claimReq));
    }

    private Mono<OmOd> cancelOrderInternal(OmOd beforeOrder, ClaimRequest claimTarget) {

        Mono<String> claimNo = Mono.just("claimseq");
        Flux<List<OmOdFvrDtl>> cancelFvrProcess = Flux.fromIterable(claimTarget.getSeqList())
                .flatMap(targetSeq -> {
                    return Flux.fromIterable(beforeOrder.getOmOdFvrDtlList())
                            .filter(fvr -> fvr.getOdSeq() == targetSeq.getOdSeq() && fvr.getProcSeq() == targetSeq.getProcSeq())
                            .collectList()
                            .switchIfEmpty(Mono.error(new RuntimeException("There are no order detail")));
                }).concatMap(fvrDtl -> {
                    if (fvrDtl == null) return Flux.error(new IllegalArgumentException("not null"));
                    return Mono.just(fvrDtl);
                })
                .flatMap(orderFvrList -> {
                    return Flux.concat(updateCancelFvrList(orderFvrList), createCancelFvrList(orderFvrList, claimNo));
                });

        Flux<OmOdDtl> cancelDtlProcess = Flux.fromIterable(claimTarget.getSeqList())
                .flatMap(targetSeq -> {
                    return Flux.fromIterable(beforeOrder.getOmOdDtlList())
                            .filter(dtl -> dtl.getOdSeq() == targetSeq.getOdSeq() && dtl.getProcSeq() == targetSeq.getProcSeq())
                            .single() //단 하나임을 보장한다.
                            .switchIfEmpty(Mono.error(new RuntimeException("There are no order detail")));
                }).flatMap(dtl -> Flux.concat(updateCancelDtl(dtl), createCancelDtl(dtl, claimNo)));

        return Flux.concat(cancelDtlProcess, cancelFvrProcess)
                .then(orderDao.saveOrder(beforeOrder))
                .flatMap(order -> orderService.findFullOrderByOdNo(order.getOdNo()));
    }

    private Mono<OmOd> cancelOrderInternal3(OmOd beforeOrder, ClaimRequest claimRequest) {
        Mono<String> claimNo = Mono.just("claimNo");
        Flux<OmOdDtl> cancelDtlProcess = Flux.fromIterable(claimRequest.getSeqList())
                .flatMap(seq -> findDtlMatchingSeq(beforeOrder.getOmOdDtlList(), seq))
                .flatMap(dtl -> validateDtlForCancel(Flux.just(dtl)))
                .flatMap(dtl -> Flux.concat(updateCancelDtl(dtl), createCancelDtl(dtl, claimNo)));

        Flux<List<OmOdFvrDtl>> cancelFvrListProcess = Flux.fromIterable(claimRequest.getSeqList())
                .flatMap(seq -> findFvrListMatchingSeq(beforeOrder.getOmOdFvrDtlList(), seq))
                .flatMap(fvrList -> validateFvrListForCancel(Flux.just(fvrList)))
                .flatMap(fvrList -> Flux.concat(updateCancelFvrList(fvrList), createCancelFvrList(fvrList, claimNo)));

        return Flux.concat(cancelDtlProcess, cancelFvrListProcess)
                .then(orderDao.saveOrder(beforeOrder))
                .flatMap(order -> orderService.findFullOrderByOdNo(order.getOdNo()));
    }

    private Mono<OmOd> cancelOrderInternal2(OmOd beforeOrder, ClaimRequest claimRequest) {
        return Mono.just(beforeOrder)
                .flatMapMany(order -> validateOrderForCancel(order, claimRequest))
                .then(Mono.just("claimNo"))
                .flatMapMany(claimNo -> processCancel(beforeOrder, claimRequest, claimNo))
                .then(orderDao.saveOrder(beforeOrder))
                .flatMap(order -> orderService.findFullOrderByOdNo(order.getOdNo()));
    }

    private Flux<?> processCancel(OmOd beforeOrder, ClaimRequest claimRequest, String claimNo) {
        return Flux.concat(
                processCancelDtl(beforeOrder.getOmOdDtlList(), claimRequest.getSeqList(), claimNo),
                processCancelFvr(beforeOrder.getOmOdFvrDtlList(), claimRequest.getSeqList(), claimNo)
        );
    }

    private Flux<?> processCancel2(OmOd beforeOrder, ClaimRequest claimRequest, String claimNo) {
        Flux<OmOdDtl> targetDtlFlux = Flux.fromIterable(claimRequest.getSeqList())
                .flatMap(targetSeq -> findDtlMatchingSeq(beforeOrder.getOmOdDtlList(), targetSeq));
        Flux<List<OmOdFvrDtl>> updateCancelActions = targetDtlFlux.flatMap(dtl -> updateCancelDtl(dtl))
                .flatMap(dtl -> {
                    ClaimRequest.Seq targetSeq = new ClaimRequest.Seq();
                    targetSeq.setOdSeq(dtl.getOdSeq());
                    targetSeq.setProcSeq(dtl.getProcSeq());
                    return findFvrListMatchingSeq(beforeOrder.getOmOdFvrDtlList(), targetSeq)
                            .flatMap(fvrList -> updateCancelFvrList(fvrList));
                });
        Flux<List<OmOdFvrDtl>> createCancelActions = targetDtlFlux.flatMap(dtl -> {
            ClaimRequest.Seq targetSeq = new ClaimRequest.Seq();
            targetSeq.setOdSeq(dtl.getOdSeq());
            targetSeq.setProcSeq(dtl.getProcSeq());
            return findFvrListMatchingSeq(beforeOrder.getOmOdFvrDtlList(), targetSeq)
                    .zipWith(createCancelDtl(dtl, Mono.just(claimNo)))
                    .flatMap(tuple -> createCancelFvrList2(tuple.getT1(), Mono.just(claimNo), tuple.getT2()));
        });
        return Flux.concat(updateCancelActions, createCancelActions);
    }

    private Flux<?> processCancelDtl(List<OmOdDtl> dtlList, List<ClaimRequest.Seq> seqList, String claimNo) {
        return Flux.fromIterable(seqList)
                .flatMap(targetSeq -> findDtlMatchingSeq(dtlList, targetSeq))
                .flatMap(dtl -> Flux.concat(updateCancelDtl(dtl), createCancelDtl(dtl, Mono.just(claimNo))));
    }

    private Flux<?> processCancelFvr(List<OmOdFvrDtl> fvrList, List<ClaimRequest.Seq> seqList, String claimNo) {
        return Flux.fromIterable(seqList)
                .flatMap(targetSeq -> findFvrListMatchingSeq(fvrList, targetSeq))
                .flatMap(orderFvr -> Flux.concat(updateCancelFvrList(orderFvr), createCancelFvrList(orderFvr, Mono.just(claimNo))));
    }

    private Mono<OmOdDtl> findDtlMatchingSeq(List<OmOdDtl> dtlList, ClaimRequest.Seq seq) {
        return Flux.fromIterable(dtlList)
                .filter(dtl -> dtl.getOdSeq() == seq.getOdSeq() && dtl.getProcSeq() == seq.getProcSeq())
                .single();
    }

    private Mono<List<OmOdFvrDtl>> findFvrListMatchingSeq(List<OmOdFvrDtl> fvrList, ClaimRequest.Seq seq) {
        return Flux.fromIterable(fvrList)
                .filter(fvr -> fvr.getOdSeq() == seq.getOdSeq() && fvr.getProcSeq() == seq.getProcSeq())
                .collectList();
    }

    private Flux<?> validateOrderForCancel(OmOd beforeOrder, ClaimRequest claimRequest) {
        return Flux.concat(
                validateDtlListForCancel(beforeOrder.getOmOdDtlList(), claimRequest.getSeqList()),
                validateFvrListForCancel(beforeOrder.getOmOdFvrDtlList(), claimRequest.getSeqList())
        );
    }

    private Flux<OmOdDtl> validateDtlListForCancel(List<OmOdDtl> dtlList, List<ClaimRequest.Seq> seqList) {
        Flux<OmOdDtl> matchingDtl = Flux.fromIterable(seqList)
                .flatMap(seq -> findDtlMatchingSeq(dtlList, seq))
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

    private Flux<List<OmOdFvrDtl>> validateFvrListForCancel(List<OmOdFvrDtl> fvrList, List<ClaimRequest.Seq> seqList) {
        Flux<List<OmOdFvrDtl>> matchingFvrListFlux = Flux.fromIterable(seqList)
                .flatMap(seq -> findFvrListMatchingSeq(fvrList, seq))
                .switchIfEmpty(Flux.error(new RuntimeException("there are no detail")));
        return validateFvrListForCancel(matchingFvrListFlux);
    }

    private Flux<List<OmOdFvrDtl>> validateFvrListForCancel(Flux<List<OmOdFvrDtl>> fvrDtlFlux) {
        return fvrDtlFlux.handle((fvrList, sink) -> {
                if (!isValidToCancelFvr(fvrList)) {
                    sink.error(new RuntimeException("not valid order detail"));
                }
                sink.next(fvrList);
             });
    }

    private boolean isValidToCancelDtl(OmOdDtl orderDetail) {
        //임시
        return true;
    }

    private boolean isValidToCancelFvr(List<OmOdFvrDtl> fvrList) {
        //임시
        return true;
    }

    private Mono<OmOdDtl> updateCancelDtl(OmOdDtl targetDtl) {
        OmOdDtl updateDtl = targetDtl.withCnclQty(targetDtl.getOdQty());
        return Mono.just(updateDtl)
                .flatMap(orderDetailDao::update);
    }

    private Mono<OmOdDtl> createCancelDtl(OmOdDtl targetDtl, Mono<String> claimNo) {
        OmOdDtl cancelDtl = targetDtl.withOdTypCd("20")
                .withOrglProcSeq(targetDtl.getProcSeq())
                .withRegDttm(null)
                .withModDttm(null);
        return Mono.just(cancelDtl)
                .zipWith(claimNo, OmOdDtl::withClmNo)
                .zipWith(orderDetailDao.findNextProcSeq(cancelDtl.getOdNo(), cancelDtl.getOdSeq()), OmOdDtl::withProcSeq)
                .flatMap(orderDetailDao::save);
    }

    private Mono<List<OmOdFvrDtl>> updateCancelFvrList(List<OmOdFvrDtl> targetFvrList) {
        return Flux.fromIterable(targetFvrList)
                .flatMap(targetFvr -> updateCancelFvr(targetFvr))
                .collectList();
    }

    private Mono<OmOdFvrDtl> updateCancelFvr(OmOdFvrDtl targetFvr) {
        return Mono.just(targetFvr)
                .map(odFvr -> odFvr.withCnclQty(odFvr.getAplyQty()))
                .flatMap(orderFavorDetailDao::save);
    }

    private Mono<List<OmOdFvrDtl>> createCancelFvrList(List<OmOdFvrDtl> targetFvrList, Mono<String> claimNo) {
        return Flux.fromIterable(targetFvrList)
                .flatMap(targetFvr -> createCancelFvr(targetFvr, claimNo))
                .collectList();
    }

    private Mono<OmOdFvrDtl> createCancelFvr(OmOdFvrDtl targetFvr, Mono<String> claimNo) {
        OmOdFvrDtl cancelFvr = targetFvr.withOrglOdFvrNo(targetFvr.getOdFvrNo())
                .withProcSeq(targetFvr.getProcSeq() + 1)
                .withOdFvrDvsCd("CNCL")
                .withOdFvrNo(null);
        return Mono.just(cancelFvr)
                .zipWith(claimNo, OmOdFvrDtl::withClmNo)

                .flatMap(orderFavorDetailDao::save);
    }

    private Mono<List<OmOdFvrDtl>> createCancelFvrList2(List<OmOdFvrDtl> targetFvrList, Mono<String> claimNo, OmOdDtl createdDtl) {
        return Flux.fromIterable(targetFvrList)
                .flatMap(targetFvr -> createCancelFvr2(targetFvr, claimNo, createdDtl.getProcSeq()))
                .collectList();
    }

    private Mono<OmOdFvrDtl> createCancelFvr2(OmOdFvrDtl targetFvr, Mono<String> claimNo, int procSeq) {
        OmOdFvrDtl cancelFvr = targetFvr.withOrglOdFvrNo(targetFvr.getOdFvrNo())
                .withProcSeq(procSeq)
                .withOdFvrDvsCd("CNCL")
                .withOdFvrNo(null);
        return Mono.just(cancelFvr)
                .zipWith(claimNo, OmOdFvrDtl::withClmNo)

                .flatMap(orderFavorDetailDao::save);
    }
}
