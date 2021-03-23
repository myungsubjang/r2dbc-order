package com.example.r2dbcorder.domain;

import com.example.r2dbcorder.repository.OrderDetailRepository;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderDetailManager {

    private final OrderDetailRepository orderDetailRepository;

    public Mono<List<OmOdDtl>> save(List<OmOdDtl> orderDetails) {
        return Flux.fromIterable(setInitialValues(orderDetails))
                .flatMap(orderDetailRepository::save)
                .collectList();
    }

    private List<OmOdDtl> setInitialValues(List<OmOdDtl> orderDetails) {
        List<OmOdDtl> copyList = new ArrayList<>(orderDetails);
        int sequence = 0;
        for (OmOdDtl orderDetail : copyList) {
            orderDetail.setOdSeq(++sequence);
            orderDetail.setProcSeq(1);
            orderDetail.setOdTypCd("10");
            orderDetail.setOdPrgsStepCd("01");
        }
        return copyList;
    }

    public List<OmOdDtl> setOrderNoToOrderDetails(String orderNo, List<OmOdDtl> orderDetails) {
        List<OmOdDtl> copyList = new ArrayList<>(orderDetails);
        for (OmOdDtl orderDetail : copyList) {
            orderDetail.setOdNo(orderNo);
        }
        return copyList;
    }

}
