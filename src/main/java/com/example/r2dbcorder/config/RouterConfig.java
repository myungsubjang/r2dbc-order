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
                .GET("/find/{odNo}", RequestPredicates.all(), orderHandler::findByOdNo)
                .GET("/save-test-order/{memberNo}/{name}", RequestPredicates.all(), orderHandler::saveTestOrder)
                .GET("/find-dto/{odNo}", RequestPredicates.all(), orderHandler::findDtoByOdNo)
                .GET("/find-interface-dto/{odNo}", RequestPredicates.all(), orderHandler::findIDtoByOdNo)
                .GET("/find-type/{odNo}/{type}", RequestPredicates.all(), orderHandler::findTypeByOdNo)
                .build();
    }
}
