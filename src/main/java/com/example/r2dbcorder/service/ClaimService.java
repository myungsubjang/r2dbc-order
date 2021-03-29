package com.example.r2dbcorder.service;

import com.example.r2dbcorder.dto.ClaimRequest;
import com.example.r2dbcorder.repository.dao.OrderDao;
import com.example.r2dbcorder.repository.dao.OrderDetailDao;
import com.example.r2dbcorder.repository.dao.OrderFavorDetailDao;
import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final OrderService orderService;
    private final OrderDao orderDao;
    private final OrderDetailDao orderDetailDao;
    private final OrderFavorDetailDao orderFavorDetailDao;

    //주문번호 주문순번 처리순번 파라미터
    //대상 주문 탐색
    //validation
    //대상 주문을 가지고 작업
    //취소주문상세 생성
    //취소주문혜택 생성
    //주문기본 수정날짜 업데이트
    //주문상세, 주문혜택 취소수량 업데이트
    //리턴 -> 업데이트된 주문 (완전한 주문)
    @Transactional
    public Mono<OmOd> cancelOrder(ClaimRequest claimReq) {
        return orderService.findFullOrderByOdNo(claimReq.getOdNo())
                //validation skip
                .flatMap(order -> cancelOrderInternal(order, claimReq));
    }

    private Mono<OmOd> cancelOrderInternal(OmOd beforeOrder, ClaimRequest claimTarget) {
        Mono<OmOd> savedOrderMono = orderDao.saveOrder(beforeOrder);

        List<Mono<OmOdDtl>> cancelActions = new ArrayList<>();
        for (ClaimRequest.Seq targetSeq : claimTarget.getSeqList()) {
            OmOdDtl targetDtl = beforeOrder.getOmOdDtlList()
                    .stream()
                    .filter(dtl -> dtl.getOdSeq() == targetSeq.getOdSeq() && dtl.getProcSeq() == targetSeq.getProcSeq())
                    .collect(Collectors.toList())
                    .get(0);
            // validation
            //취소수량 업데이트
            OmOdDtl updateDtl = targetDtl.withCnclQty(targetDtl.getOdQty());
            Mono<OmOdDtl> updateCancelDtlMono = Mono.just(updateDtl)
                    .flatMap(orderDetailDao::update);
            //새로운 디테일 생성
            OmOdDtl cancelDtl = targetDtl.withOdTypCd("20")
                    .withOrglProcSeq(targetDtl.getProcSeq())
                    .withProcSeq(targetDtl.getProcSeq() + 1);
            cancelDtl.setRegDttm(null);
            cancelDtl.setModDttm(null);
            Mono<OmOdDtl> createCancelDtlMono = Mono.just(cancelDtl)
                    .zipWith(Mono.just("claimseq"), OmOdDtl::withClmNo)
                    .flatMap(orderDetailDao::save);
            cancelActions.add(updateCancelDtlMono);
            cancelActions.add(createCancelDtlMono);
        }
        // reduce? defer? using?
        return Flux.concat(cancelActions)
                .log()
                .then(savedOrderMono)
                .flatMap(order -> orderService.findFullOrderByOdNo(order.getOdNo()));
    }
}
