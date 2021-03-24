package com.example.r2dbcorder.handler;

import com.example.r2dbcorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderHandler {

    private final OrderService orderService;

    public Mono<ServerResponse> findByOdNo(ServerRequest request) {
        return orderService.findOrderByOdNo(request.pathVariable("odNo"))
                .flatMap(ServerResponse.ok()::bodyValue);
    }
}
