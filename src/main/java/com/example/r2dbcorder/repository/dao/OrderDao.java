package com.example.r2dbcorder.repository.dao;

import com.example.r2dbcorder.exceptions.OrderNotFoundException;
import com.example.r2dbcorder.repository.OrderRepository;
import com.example.r2dbcorder.repository.entity.OmOd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrderDao {

    private final OrderRepository orderRepository;

    public Mono<OmOd> saveOrder(OmOd order) {
        // validation
        if (!StringUtils.hasLength(order.getOdNo())) {
            order.setOdCmptDttm(LocalDateTime.now());
        }
        return orderRepository.save(order);
    }

    public Mono<OmOd> findByOdNo(String odNo) {
        //조회 결과가 없으면 null을 리턴하지 않고 empty를 리턴한다. Reactor에는 null을 리턴하게되면 자동 익셉션 발생한다.
        return orderRepository.findById(odNo)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(String.format("Order not found. %s", odNo))));
    }

    public Flux<OmOd> findAllOrder() {
        return orderRepository.findAll();
    }
}
