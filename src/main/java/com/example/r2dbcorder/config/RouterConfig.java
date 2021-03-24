package com.example.r2dbcorder.config;

import com.example.r2dbcorder.handler.OrderHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    @Bean
    RouterFunction<ServerResponse> routerFunction(OrderHandler orderHandler) {
        return RouterFunctions.route()
                .GET("/find-by-odno/{odNo}", RequestPredicates.all(), orderHandler::findByOdNo)
                .build();
    }
}
