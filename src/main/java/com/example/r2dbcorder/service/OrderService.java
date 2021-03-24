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
        return findDtlAndFvrWithStrategy(
                findJustOrderByOdNo(odNo),
                order -> allDetail(order),
                order -> allFavor(order)
        );
    }

    public Mono<OmOd> findOrderOnlyDetailPriceOver(String odNo, int price) {
        return findDtlAndFvrWithStrategy(
                findJustOrderByOdNo(odNo),
                omOd -> overPriceDetail(omOd, price),
                omOd -> allFavor(omOd)
        );
    }

    private Mono<OmOd> findJustOrderByOdNo(String odNo) {
        return Mono.just(odNo)
                .flatMap(orderDao::findByOdNo)
                .onErrorReturn(OrderNotFoundException.class, nullOmOd(odNo));
    }

    private OmOd nullOmOd(String odNo) {
        OmOd nullOrder = new OmOd();
        nullOrder.setOdNo(odNo + " dosen't exists");
        return nullOrder;
    }

    private Mono<OmOd> findDtlAndFvrWithStrategy(Mono<OmOd> order, Function<OmOd, Mono<OmOd>> detailStrategy, Function<OmOd, Mono<OmOd>> favorStrategy) {
        return order.flatMap(detailStrategy)
                .flatMap(favorStrategy);
    }

    private Mono<OmOd> overPriceDetail(OmOd order, int price) {
        return Mono.just(order)
                .zipWith(orderDetailDao.findDtlOverPrice(order.getOdNo(), price), OmOd::withOmOdDtlList);
    }

    private Mono<OmOd> allDetail(OmOd order) {
        return Mono.just(order)
                .zipWith(orderDetailDao.findAllDetailByOdNo(order.getOdNo()), OmOd::withOmOdDtlList);
    }

    private Mono<OmOd> allFavor(OmOd order) {
        return Mono.just(order)
                .zipWith(orderFavorDetailDao.findAllByOdNo(order.getOdNo()), OmOd::withOmOdFvrDtlList);
    }

    public Mono<List<OmOd>> findAllOrder() {
        return orderDao.findAllOrder()
                .flatMap(order -> findOrderByOdNo(order.getOdNo()))
                .collectList();
    }
    //TODO 살아있는 주문만 가져오기
    //TODO 정책을 엮기

    public Mono<List<OmOd>> findOrdersOverPrice(int price) {
        return orderDao.findAllOrder()
                .flatMap(order -> findOrderByOdNo(order.getOdNo()))
                .filter(order -> isOrderOverPrice(order, price))
                .collectList();
    }

    private boolean isOrderOverPrice(OmOd omOd, int price) {
        List<OmOdDtl> details = omOd.getOmOdDtlList();
        int priceSum = details.stream()
                .mapToInt(detail -> detail.getSlPrc() - detail.getDcAmt())
                .sum();
        return priceSum > price;
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

//    public Mono<OmOd> findOrderByOdNo(String odNo) {
//        //motive
//        return Mono.just(odNo)
//                .flatMap(orderDao::findByOdNo)
//                .flatMap(this::findAllDetailFromOd)
//                .flatMap(this::findAllFvrDetailFromOd)
//                .onErrorReturn(OrderNotFoundException.class, nullOmOd(odNo));
//    }
}
