package com.example.r2dbcorder.config;

import com.example.r2dbcorder.domain.OrderDetailManager;
import com.example.r2dbcorder.domain.OrderManager;
import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import com.example.r2dbcorder.service.OrderService;
import com.example.r2dbcorder.util.TestOrders;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.DispatcherHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ApplicationRunners {

    private final OrderService orderService;
    private final OrderManager orderManager;
    private final OrderDetailManager orderDetailManager;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
//            Flux<OmOd> savedOrderFlux = Flux.range(1, 10)
            Flux.range(1, 10)
                    .map(integer -> Integer.toString(integer))
                    .map(memberNumber -> TestOrders.createTestOrder(memberNumber, "name" + memberNumber))
                    .flatMap(orderManager::save)
                    .map(order -> order.withOmOdDtlList(orderDetailManager.setOrderNoToOrderDetails(order.getOdNo(), order.getOmOdDtlList())))
//                    .map(orderManager::setOrderNoToOrderDetail)
                    .map(Mono::just)
                    .flatMap(orderMono -> orderMono.zipWhen(order -> orderDetailManager.save(order.getOmOdDtlList())))
                    .map(tuple -> tuple.getT1().withOmOdDtlList(tuple.getT2()))

                    .subscribe();

//            DispatcherHandler
//                    .cache();
//            Flux<List<OmOdDtl>> savedOrderDetailsFlux = savedOrderFlux.flatMap(order -> orderDetailManager.save(order.getOmOdDtlList()));
//            savedOrderFlux
        };
    }
}
