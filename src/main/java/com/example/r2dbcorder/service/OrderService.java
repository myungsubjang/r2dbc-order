package com.example.r2dbcorder.service;

import com.example.r2dbcorder.domain.OrderDetailManager;
import com.example.r2dbcorder.domain.OrderFavorDetailManager;
import com.example.r2dbcorder.domain.OrderManager;
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

    private final OrderManager orderManager;
    private final OrderDetailManager orderDetailManager;
    private final OrderFavorDetailManager orderFavorDetailManager;

    @Transactional
    public Mono<OmOd> saveOrder(OmOd order) {
        return Mono.just(order)
                .flatMap(orderManager::saveOrder)
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
                .flatMap(orderDetailManager::save)
                .zipWith(Mono.just(order), (omOdDtls, omOd) -> omOd.withOmOdDtlList(omOdDtls));
    }

    private Mono<OmOd> saveOrderFavorDetailFromOrder(OmOd order) {
        List<OmOdFvrDtl> copyList = new ArrayList<>(order.getOmOdFvrDtlList());
        String orderNo = order.getOdNo();
        for (OmOdFvrDtl favorDetail : copyList) {
            favorDetail.setOdNo(orderNo);
        }
        return Mono.just(copyList)
                .flatMap(orderFavorDetailManager::save)
                .zipWith(Mono.just(order), (omOdFvrDtls, omOd) -> omOd.withOmOdFvrDtlList(omOdFvrDtls));
    }

    public Mono<OmOd> findOrderByOdNo(String odNo) {
        return Mono.just(odNo)
                .flatMap(orderManager::findByOdNo)
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
                .zipWith(orderDetailManager.findOrderDetailByOdNo(order.getOdNo()), OmOd::withOmOdDtlList);
    }

    private Mono<OmOd> findOrderFvrDetailFromOd(OmOd order) {
        return Mono.just(order)
                .zipWith(orderFavorDetailManager.findByOdNo(order.getOdNo()), OmOd::withOmOdFvrDtlList);
    }

}
