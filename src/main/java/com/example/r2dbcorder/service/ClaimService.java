package com.example.r2dbcorder.service;

import com.example.r2dbcorder.dto.ClaimRequest;
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

import java.util.List;

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
                }).flatMap(orderDetail -> {
                    return Flux.concat(updateCancelDtl(orderDetail), createCancelDtl(orderDetail, claimNo));
                });

        return Flux.concat(cancelDtlProcess, cancelFvrProcess)
                .then(orderDao.saveOrder(beforeOrder))
                .flatMap(order -> orderService.findFullOrderByOdNo(order.getOdNo()));
    }

    private Mono<OmOd> cancelOrderInternal2(OmOd beforeOrder, ClaimRequest claimRequest) {
        return Mono.just(beforeOrder)
                .flatMapMany(order -> validateForCancel2(order, claimRequest))
                .then(Mono.just("claimNo"))
                .flatMapMany(claimNo -> processCancel(beforeOrder, claimRequest, claimNo))
                .then(orderDao.saveOrder(beforeOrder))
                .flatMap(order -> orderService.findFullOrderByOdNo(order.getOdNo()));

//        return Flux.fromIterable(claimRequest.getSeqList())
//                .flatMap(seq -> validateToCancel(beforeOrder, seq))
//                .then(Mono.just("claimNo"))
//                .flatMapMany(claimNo -> processCancel(beforeOrder, claimRequest, claimNo))
//                .then(orderDao.saveOrder(beforeOrder))
//                .flatMap(order -> orderService.findFullOrderByOdNo(order.getOdNo()));
    }

    private Mono<OmOd> cancelOrderInternal3(OmOd beforeOrder, ClaimRequest claimRequest) {
        Mono<String> claimNo = Mono.just("claimseq");
        Flux.fromIterable(claimRequest.getSeqList())
                .flatMap(seq -> filterDtlWithSeq(beforeOrder.getOmOdDtlList(), seq))
                .flatMap(dtl -> validateDtlForCancel(Flux.just(dtl)))
                .flatMap(dtl -> Flux.concat(updateCancelDtl(dtl), createCancelDtl(dtl, claimNo)));

        Flux.fromIterable(claimRequest.getSeqList())
                .flatMap(seq -> filterFvrWithSeq(beforeOrder.getOmOdFvrDtlList(), seq))
                .flatMap(fvrList -> validateFvrListForCancel(Flux.just(fvrList)))
                .flatMap(fvrList -> Flux.concat(updateCancelFvrList(fvrList), createCancelFvrList(fvrList, claimNo)));
        return null;
    }

    private Flux<?> processCancel(OmOd beforeOrder, ClaimRequest claimRequest, String claimNo) {
        return Flux.concat(
                processCancelDtl(beforeOrder.getOmOdDtlList(), claimRequest.getSeqList(), claimNo),
                processCancelFvr(beforeOrder.getOmOdFvrDtlList(), claimRequest.getSeqList(), claimNo)
        );
    }

    private Flux<?> processCancelDtl(List<OmOdDtl> dtlList, List<ClaimRequest.Seq> seqList, String claimNo) {
        return Flux.fromIterable(seqList)
                .flatMap(targetSeq -> filterDtlWithSeq(dtlList, targetSeq))
                .flatMap(orderDtl -> Flux.concat(updateCancelDtl(orderDtl), createCancelDtl(orderDtl, Mono.just(claimNo)))); // 그냥 타입으로도 바꿀수잇음
    }

    private Flux<?> processCancelFvr(List<OmOdFvrDtl> fvrList, List<ClaimRequest.Seq> seqList, String claimNo) {
        return Flux.fromIterable(seqList)
                .flatMap(targetSeq -> filterFvrWithSeq(fvrList, targetSeq))
                .flatMap(orderFvr -> Flux.concat(updateCancelFvrList(orderFvr), createCancelFvrList(orderFvr, Mono.just(claimNo))));
    }

    private Mono<OmOdDtl> filterDtlWithSeq(List<OmOdDtl> dtlList, ClaimRequest.Seq seq) {
        return Flux.fromIterable(dtlList)
                .filter(dtl -> dtl.getOdSeq() == seq.getOdSeq() && dtl.getProcSeq() == seq.getProcSeq())
                .single();
    }

    private Mono<List<OmOdFvrDtl>> filterFvrWithSeq(List<OmOdFvrDtl> fvrList, ClaimRequest.Seq seq) {
        return Flux.fromIterable(fvrList)
                .filter(fvr -> fvr.getOdSeq() == seq.getOdSeq() && fvr.getProcSeq() == seq.getProcSeq())
                .collectList();
    }

    private Flux<?> validateToCancel(OmOd cancelOrder, ClaimRequest.Seq seq) {
        Mono<OmOdDtl> cancelDtlValidator = Flux.fromIterable(cancelOrder.getOmOdDtlList())
                .filter(dtl -> dtl.getOdSeq() == seq.getOdSeq() && dtl.getProcSeq() == seq.getProcSeq())
                .single()
                .handle((dtl, sink) -> {
                    if (!isValidToCancelDtl(dtl)) {
                        sink.error(new RuntimeException());
                    }
                    sink.next(dtl);
                });

        Mono<List<OmOdFvrDtl>> cancelFvrValidator = Flux.fromIterable(cancelOrder.getOmOdFvrDtlList())
                .filter(fvr -> fvr.getOdSeq() == seq.getOdSeq() && fvr.getProcSeq() == seq.getProcSeq())
                .collectList()
                .switchIfEmpty(Mono.error(new RuntimeException()))
                .handle((fvrList, sink) -> {
                    if (!isValidToCancelFvr(fvrList)) {
                        sink.error(new RuntimeException());
                    }
                    sink.next(fvrList);
                });
        return Flux.concat(cancelDtlValidator, cancelFvrValidator);
    }

    private Flux<?> validateForCancel2(OmOd beforeOrder, ClaimRequest claimRequest) {
        return Flux.concat(
                validateDtlToCancel(beforeOrder.getOmOdDtlList(), claimRequest.getSeqList()),
                validateFvrToCancel(beforeOrder.getOmOdFvrDtlList(), claimRequest.getSeqList())
        );
    }

    private Flux<OmOdDtl> validateDtlToCancel(List<OmOdDtl> dtlList, List<ClaimRequest.Seq> seqList) {
        return Flux.fromIterable(seqList)
                .flatMap(seq -> filterDtlWithSeq(dtlList, seq))
                .switchIfEmpty(Flux.error(new RuntimeException("there are no detail")))
                .handle((dtl, sink) -> {
                    if (!isValidToCancelDtl(dtl)) {
                        sink.error(new RuntimeException("not valid order detail"));
                    }
                    sink.next(dtl);
                });
    }

    private Flux<OmOdDtl> validateDtlForCancel(Flux<OmOdDtl> dtlFlux) {
        return dtlFlux.handle((dtl, sink) -> {
                if (!isValidToCancelDtl(dtl)) {
                    sink.error(new RuntimeException("not valid order detail"));
                }
                sink.next(dtl);
            });
    }

    private Flux<List<OmOdFvrDtl>> validateFvrToCancel(List<OmOdFvrDtl> fvrList, List<ClaimRequest.Seq> seqList) {
        return Flux.fromIterable(seqList)
                .flatMap(seq -> filterFvrWithSeq(fvrList, seq))
                .switchIfEmpty(Flux.error(new RuntimeException("there are no detail")))
                .handle((filteredFvrList, sink) -> {
                    if (!isValidToCancelFvr(filteredFvrList)) {
                        sink.error(new RuntimeException("not valid order detail"));
                    }
                    sink.next(filteredFvrList);
                });
    }

    private Flux<List<OmOdFvrDtl>> validateFvrListForCancel(Flux<List<OmOdFvrDtl>> fvrDtlFlux) {
        return fvrDtlFlux.handle((fvrList, sink) -> {
                if (!isValidToCancelFvr(fvrList)) {
                    sink.error(new RuntimeException("not valid order detail"));
                }
                sink.next(fvrList);
            });
    }

    //TODO handle을 append할 수 있도록?
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
                .withProcSeq(targetDtl.getProcSeq() + 1)
                .withRegDttm(null)
                .withModDttm(null);
        return Mono.just(cancelDtl)
                .zipWith(claimNo, OmOdDtl::withClmNo)
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
}
