package com.example.r2dbcorder.handler;

import com.example.r2dbcorder.service.OrderService;
import com.example.r2dbcorder.util.TestOrderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderHandler {

    private final OrderService orderService;

    public Mono<ServerResponse> saveTestOrder(ServerRequest request) {
        String memberNo = request.pathVariable("memberNo");
        String name = request.pathVariable("name");
        return orderService.saveOrder(TestOrderUtil.createTestOrder(memberNo, name))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> findByOdNo(ServerRequest request) {
        return orderService.findOrderByOdNo(request.pathVariable("odNo"))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> findDtoByOdNo(ServerRequest request) {
        String odNo = request.pathVariable("odNo");
        return orderService.findDtoByOdNo(odNo)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> findIDtoByOdNo(ServerRequest request) {
        String odNo = request.pathVariable("odNo");
        return orderService.findIDtoByOdNo(odNo)
                .flatMap(ServerResponse.ok()::bodyValue);
    }
}
