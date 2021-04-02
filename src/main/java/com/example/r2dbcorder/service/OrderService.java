package com.example.r2dbcorder.service;

import com.example.r2dbcorder.dto.DtoSample;
import com.example.r2dbcorder.dto.IOdDtlDto;
import com.example.r2dbcorder.dto.OdDtlDto;
import com.example.r2dbcorder.dto.OmOdDtlFvrDtlDto;
import com.example.r2dbcorder.repository.dao.OrderDetailDao;
import com.example.r2dbcorder.repository.dao.OrderFavorDetailDao;
import com.example.r2dbcorder.repository.dao.OrderDao;
import com.example.r2dbcorder.exceptions.OrderNotFoundException;
import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderDao orderDao;
    private final OrderDetailDao orderDetailDao;
    private final OrderFavorDetailDao orderFavorDetailDao;

    @Transactional
    public Mono<OmOd> saveOrder(OmOd order) {
        return Mono.just(order)
                .flatMap(orderDao::saveOrder)
                .flatMap(this::saveOrderDetailFromOrder)
                .flatMap(this::saveOrderFavorDetailFromOrder);
    }

    private Mono<OmOd> saveOrderDetailFromOrder(OmOd order) {
        List<OmOdDtl> copyList = new ArrayList<>(order.getOmOdDtlList());
        String orderNo = order.getOdNo();
        for (OmOdDtl orderDetail : copyList) {
            orderDetail.setOdNo(orderNo);
            orderDetail.setOdCmptDttm(order.getOdCmptDttm());
        }
        return Mono.just(copyList)
                .flatMap(orderDetailDao::saveList)
                .zipWith(Mono.just(order), (omOdDtls, omOd) -> omOd.withOmOdDtlList(omOdDtls));
    }

    private Mono<OmOd> saveOrderFavorDetailFromOrder(OmOd order) {
        List<OmOdFvrDtl> copyList = new ArrayList<>(order.getOmOdFvrDtlList());
        String orderNo = order.getOdNo();
        for (OmOdFvrDtl favorDetail : copyList) {
            favorDetail.setOdNo(orderNo);
        }
        return Mono.just(copyList)
                .flatMap(orderFavorDetailDao::saveList)
                .zipWith(Mono.just(order), (omOdFvrDtls, omOd) -> omOd.withOmOdFvrDtlList(omOdFvrDtls));
    }

//    public Mono<OmOd> findOrderByOdN2o(String odNo) {
//        return Mono.just(odNo)
//                .flatMap(orderDao::findByOdNo)
//                .flatMap(this::allDetailByOdNo)
//                .flatMap(this::allFavorByOdNo)
//                .onErrorReturn(OrderNotFoundException.class, nullOmOd(odNo));
//    }

    private Mono<OmOd> makeOrderWithStrategies(Mono<OmOd> targetOd, Function<OmOd, Mono<OmOd>> detailStrategy, Function<OmOd, Mono<OmOd>> favorStrategy) {
        return targetOd
                .flatMap(detailStrategy)
                .flatMap(favorStrategy);
    }

    public Mono<OmOd> findFullOrderByOdNo(String odNo) {
        return makeOrderWithStrategies(
                findJustOrderByOdNo(odNo),
                order -> allDetailByOdNo(order),
                order -> allFavorByOdNo(order)
        );
    }

    public Mono<OmOd> findOrderOnlyDetailPriceOver(String odNo, int price) {
        return makeOrderWithStrategies(
                findJustOrderByOdNo(odNo),
                omOd -> overPriceDetail(omOd, price),
                omOd -> favorsDependsOnDetails(omOd)
        );
    }

    public Mono<List<OmOd>> findOrdersOverPrice(int price) {
        return orderDao.findAllOrder()
                .flatMap(od -> findFullOrderByOdNo(od.getOdNo()))
                .filter(order -> isOrderOverPrice(order, price))
                .collectList();
    }

    private Mono<OmOd> findJustOrderByOdNo(String odNo) {
        return Mono.just(odNo)
                .flatMap(orderDao::findByOdNo);
//                .onErrorReturn(OrderNotFoundException.class, nullOmOd(odNo));
    }

    private OmOd nullOmOd(String odNo) {
        OmOd nullOrder = new OmOd();
        nullOrder.setOdNo(odNo + " dosen't exists");
        return nullOrder;
    }

    private Mono<OmOd> overPriceDetail(OmOd order, int price) {
        return Mono.just(order)
                .zipWith(orderDetailDao.findDtlOverPrice(order.getOdNo(), price), OmOd::withOmOdDtlList);
    }

    private Mono<OmOd> allDetailByOdNo(OmOd order) {
        return Mono.just(order)
                .zipWith(orderDetailDao.findAllDetailByOdNo(order.getOdNo()), OmOd::withOmOdDtlList);
    }

    private Mono<OmOd> allFavorByOdNo(OmOd order) {
        return Mono.just(order)
                .zipWith(orderFavorDetailDao.findAllByOdNo(order.getOdNo()), OmOd::withOmOdFvrDtlList);
    }

    private Mono<OmOd> favorsDependsOnDetails(OmOd order) {
        return Flux.fromIterable(order.getOmOdDtlList())
                .flatMap(odDtl -> orderFavorDetailDao.findFvrByOdNoOdSeqProcSeq(odDtl.getOdNo(), odDtl.getOdSeq(), odDtl.getProcSeq()))
                .collectList()
                .zipWith(Mono.just(order), (odFvrDtls, omOd) -> omOd.withOmOdFvrDtlList(odFvrDtls));
    }

    public Mono<List<OmOd>> findAllOrder() {
        return orderDao.findAllOrder()
                .flatMap(order -> makeOrderWithStrategies(
                        Mono.just(order),
                        od -> allDetailByOdNo(od),
                        od -> allFavorByOdNo(od)
                ))
                .collectList();
    }

    private boolean isOrderOverPrice(OmOd omOd, int price) {
        List<OmOdDtl> details = omOd.getOmOdDtlList();
        int priceSum = details.stream()
                .mapToInt(detail -> detail.getSlPrc() - detail.getDcAmt())
                .sum();
        return priceSum > price;
    }

    //취소주문상세가 있는 주문들만 조회 (주문 + 취소주문상세 + 취소주문상세혜택)
    public Mono<List<OmOd>> findOrderListContainCancelDtl() {
//        return orderDetailDao.findAllOdTypDtl("20")
//            .groupBy(OmOdDtl::getOdNo)
//            .flatMap(groupedFlux -> findJustOrderByOdNo(groupedFlux.key())
//                    .zipWith(groupedFlux.collectList(), OmOd::withOmOdDtlList)
//            ).flatMap(order -> favorsDependsOnDetails(order))
//            .collectList();
        return orderDetailDao.findAllOdTypDtl("20")
                .groupBy(OmOdDtl::getOdNo)
                .flatMap(groupedFlux -> makeOrderWithStrategies(
                        findJustOrderByOdNo(groupedFlux.key()),
                        order -> Mono.just(order).zipWith(groupedFlux.collectList(), OmOd::withOmOdDtlList),
                        order -> favorsDependsOnDetails(order)
                )).collectList();
    }

    //취소주문상세가 있는 주문들의 전체 상세까지 조회
    public Mono<List<OmOd>> findOrderListContainCancelFullDtl() {
        return orderDetailDao.findAllOdTypDtl("20")
                .groupBy(OmOdDtl::getOdNo)
                .flatMap(groupedFlux -> findFullOrderByOdNo(groupedFlux.key()))
                .collectList();
    }

    public Mono<List<OdDtlDto>> findDtoByOdNo(String odNo) {
        return orderDetailDao.findDtoByOdNo(odNo);
    }

    public Mono<List<IOdDtlDto>> findIDtoByOdNo(String odNo) {
        return orderDetailDao.findIDtoByOdNo(odNo);
    }

    public Mono<? extends List<?>> findTypeByOdNo(String odNo, String type) {
        if (type.equals("dto")) {
            return orderDetailDao.findTypeByOdNo(odNo, OdDtlDto.class);
        } else if (type.equals("sample")) {
            return orderDetailDao.findTypeByOdNo(odNo, DtoSample.class); // not working.
        }
        return orderDetailDao.findTypeByOdNo(odNo, IOdDtlDto.class);
    }

    public Flux<OmOdDtlFvrDtlDto> joinPractice(String odNo) {
        return orderDetailDao.joinPractice(odNo);
    }

}
