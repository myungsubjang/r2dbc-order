package com.example.r2dbcorder.config;

import com.example.r2dbcorder.repository.OrderRepository;
import com.example.r2dbcorder.service.OrderService;
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
//            OrderCreateRequest order = OrderRequests.create();
//            orderService.createOrder(order); // 함수형이니까
            Flux.range(1, 10)
                .map(i -> OrderManager.createOrder(i, "name")) // 멤버 정보를 받는 곳, 사용자번호, 이름
                .map(OrderManager::save); // 서비스는 파이프라인을 만드는 곳, Manager는 특정 도메인의 함수를 제공해주는 곳. 직접 Repository에 접근할 이유가 없도록.

        };
    }
}
