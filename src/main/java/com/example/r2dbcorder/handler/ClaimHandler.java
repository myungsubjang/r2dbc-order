package com.example.r2dbcorder.handler;

import com.example.r2dbcorder.dto.ClaimRequest;
import com.example.r2dbcorder.service.ClaimService;
import com.example.r2dbcorder.service.ClaimService2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class ClaimHandler {

    private final ClaimService claimService;
    private final ClaimService2 claimService2;

    public Mono<ServerResponse> cancelOrder(ServerRequest request) {
        return request.bodyToMono(ClaimRequest.class)
                .flatMap(claimService::cancelOrder)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> cancelOrder2(ServerRequest request) {
        return request.bodyToMono(ClaimRequest.class)
                .flatMap(claimService2::cancelOrder)
                .flatMap(ServerResponse.ok()::bodyValue);
    }
}
