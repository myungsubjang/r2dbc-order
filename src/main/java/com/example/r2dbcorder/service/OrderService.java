package com.example.r2dbcorder.service;

import com.example.r2dbcorder.dto.IOdDtlDto;
import com.example.r2dbcorder.dto.OdDtlDto;
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
        }
        return Mono.just(copyList)
                .flatMap(orderDetailDao::save)
                .zipWith(Mono.just(order), (omOdDtls, omOd) -> omOd.withOmOdDtlList(omOdDtls));
    }

    private Mono<OmOd> saveOrderFavorDetailFromOrder(OmOd order) {
        List<OmOdFvrDtl> copyList = new ArrayList<>(order.getOmOdFvrDtlList());
        String orderNo = order.getOdNo();
        for (OmOdFvrDtl favorDetail : copyList) {
            favorDetail.setOdNo(orderNo);
        }
        return Mono.just(copyList)
                .flatMap(orderFavorDetailDao::save)
                .zipWith(Mono.just(order), (omOdFvrDtls, omOd) -> omOd.withOmOdFvrDtlList(omOdFvrDtls));
    }

    public Mono<OmOd> findOrderByOdNo(String odNo) {
//        return Mono.just(odNo)
//                .flatMap(orderDao::findByOdNo)
//                .flatMap(this::findAllDetailFromOd)
//                .flatMap(this::findAllFvrDetailFromOd)
//                .onErrorReturn(OrderNotFoundException.class, nullOmOd(odNo));

        return findOrderWithStrategy(
                findJustOrderByOdNo(odNo),
                order -> findAllDetailFromOd(order),
                order -> findAllFvrDetailFromOd(order)
        );
    }

    public Mono<OmOd> findOrderOverPrice(String odNo, int price) {
        return findOrderWithStrategy(
                findJustOrderByOdNo(odNo),
                omOd -> overPriceDetail(omOd, price),
                omOd -> findAllFvrDetailFromOd(omOd)
        );
    }

    private Mono<OmOd> findOrderWithStrategy(Mono<OmOd> order, Function<OmOd, Mono<OmOd>> detailStrategy, Function<OmOd, Mono<OmOd>> favorStrategy) {
        return order.flatMap(detailStrategy)
                .flatMap(favorStrategy);
    }

    private Mono<OmOd> findJustOrderByOdNo(String odNo) {
        return Mono.just(odNo)
                .flatMap(orderDao::findByOdNo)
                .onErrorReturn(OrderNotFoundException.class, nullOmOd(odNo));
    }

    private Mono<OmOd> overPriceDetail(OmOd order, int price) {
        return Mono.just(order)
                .zipWith(orderDetailDao.findDtlOverPrice(order.getOdNo(), price), OmOd::withOmOdDtlList);
    }

    private OmOd nullOmOd(String odNo) {
        OmOd nullOrder = new OmOd();
        nullOrder.setOdNo(odNo + " dosen't exists");
        return nullOrder;
    }

    private Mono<OmOd> findAllDetailFromOd(OmOd order) {
        return Mono.just(order)
                .zipWith(orderDetailDao.findAllDetailByOdNo(order.getOdNo()), OmOd::withOmOdDtlList);
    }

    private Mono<OmOd> findAllFvrDetailFromOd(OmOd order) {
        return Mono.just(order)
                .zipWith(orderFavorDetailDao.findAllByOdNo(order.getOdNo()), OmOd::withOmOdFvrDtlList);
    }

    public Mono<List<OmOd>> findAllOrder() {
        return orderDao.findAllOrder()
                .flatMap(order -> findOrderByOdNo(order.getOdNo()))
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
        }
        return orderDetailDao.findTypeByOdNo(odNo, IOdDtlDto.class);
    }
}
