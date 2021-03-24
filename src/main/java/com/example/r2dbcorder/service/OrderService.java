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
        return Mono.just(odNo)
                .flatMap(orderDao::findByOdNo)
                .flatMap(this::findOrderDetailFromOd)
                .flatMap(this::findOrderFvrDetailFromOd)
                .onErrorReturn(OrderNotFoundException.class, nullOmOd(odNo)); // exception발생하면 바로 리턴하네
    }

    private OmOd nullOmOd(String odNo) {
        OmOd nullOrder = new OmOd();
        nullOrder.setOdNo(odNo + " dosen't exists");
        return nullOrder;
    }

    private Mono<OmOd> findOrderDetailFromOd(OmOd order) {
        return Mono.just(order)
                .zipWith(orderDetailDao.findOrderDetailByOdNo(order.getOdNo()), OmOd::withOmOdDtlList);
    }

    private Mono<OmOd> findOrderFvrDetailFromOd(OmOd order) {
        return Mono.just(order)
                .zipWith(orderFavorDetailDao.findByOdNo(order.getOdNo()), OmOd::withOmOdFvrDtlList);
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

    //projection을 다이나믹하게 구성해보기
//    public Mono<OmOd> findOrderByOdNoAndType(String odNo, String type) {
//         // 프로젝션에 대한 함수를 다이나믹하게 결정.
//
//    }

}
