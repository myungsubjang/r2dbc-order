package com.example.r2dbcorder.handler;

import com.example.r2dbcorder.dto.ClaimRequest;
import com.example.r2dbcorder.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class ClaimHandler {

    private final ClaimService claimService;

    public Mono<ServerResponse> cancelOrder(ServerRequest request) {
        return request.bodyToMono(ClaimRequest.class)
                .log()
                .flatMap(ServerResponse.ok()::bodyValue);
    }
}
