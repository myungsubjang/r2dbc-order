package com.example.r2dbcorder.config;

import com.example.r2dbcorder.service.OrderService;
import com.example.r2dbcorder.util.TestOrders;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
@RequiredArgsConstructor
public class ApplicationRunners {

    private final OrderService orderService;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            Flux.range(1, 10)
                    .map(integer -> Integer.toString(integer))
                    .map(memberNumber -> TestOrders.createTestOrder(memberNumber, "name" + memberNumber))
                    .flatMap(orderService::saveOrder)
                    .subscribe();
        };
    }
}
