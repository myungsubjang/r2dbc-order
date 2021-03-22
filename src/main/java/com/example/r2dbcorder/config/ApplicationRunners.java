package com.example.r2dbcorder.config;

import com.example.r2dbcorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ApplicationRunners {

    private final OrderService orderService;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
//            OrderCreateRequest order = OrderRequests.create();
//            orderService.createOrder(order);
        };
    }
}
