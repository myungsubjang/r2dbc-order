package com.example.r2dbcorder.domain;

import com.example.r2dbcorder.repository.OrderRepository;
import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderManager {

    private final OrderRepository orderRepository;

    public Mono<OmOd> save(OmOd order) {
        // validation
        return orderRepository.save(order);
    }

    public OmOd setOrderNoToOrderDetail(OmOd order) {
        List<OmOdDtl> orderDetails = new ArrayList<>(order.getOmOdDtlList());
        orderDetails.forEach(detail -> detail.setOdNo(order.getOdNo()));
        return order.withOmOdDtlList(orderDetails);
    }

}
