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
        return orderService.findFullOrderByOdNo(request.pathVariable("odNo"))
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

    public Mono<ServerResponse> findTypeByOdNo(ServerRequest request) {
        String odNo = request.pathVariable("odNo");
        String type = request.pathVariable("type");
        return orderService.findTypeByOdNo(odNo, type)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> findOrderByDetailOverPrice(ServerRequest request) {
        String odNo = request.pathVariable("odNo");
        String priceStr = request.pathVariable("price");
        int price = Integer.parseInt(priceStr);
        return orderService.findOrderOnlyDetailPriceOver(odNo, price)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> findAllOrder(ServerRequest request) {
        return orderService.findAllOrder()
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> findOrdersOverPrice(ServerRequest request) {
        String priceStr = request.pathVariable("price");
        int price = Integer.parseInt(priceStr);
        return orderService.findOrdersOverPrice(price)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> findOrdersContainCancelDtl(ServerRequest request) {
        return orderService.findOrderListContainCancelDtl()
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> findOrdersContainCancelAllDtl(ServerRequest request) {
        return orderService.findOrderListContainCancelFullDtl()
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> joinPractice(ServerRequest request) {
        return orderService.joinPractice(request.pathVariable("odNo"))
                .collectList()
                .flatMap(ServerResponse.ok()::bodyValue);
    }
}
