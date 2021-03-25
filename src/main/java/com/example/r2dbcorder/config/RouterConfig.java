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
                .GET("/find-all", RequestPredicates.all(), orderHandler::findAllOrder)
                .GET("/save-test-order/{memberNo}/{name}", RequestPredicates.all(), orderHandler::saveTestOrder)
                .GET("/find-dto/{odNo}", RequestPredicates.all(), orderHandler::findDtoByOdNo)
                .GET("/find-interface-dto/{odNo}", RequestPredicates.all(), orderHandler::findIDtoByOdNo)
                .GET("/find-type/{odNo}/{type}", RequestPredicates.all(), orderHandler::findTypeByOdNo)
                .GET("/find-over-price/{odNo}/{price}", RequestPredicates.all(), orderHandler::findOrderByDetailOverPrice)
                .GET("/find-over-price/{price}", RequestPredicates.all(), orderHandler::findOrdersOverPrice)
                .GET("/find-orderlist-contain-cancel", RequestPredicates.all(), orderHandler::findOrdersContainCancelDtl)
                .GET("/find-orderlist-contain-cancel/all", RequestPredicates.all(), orderHandler::findOrdersContainCancelAllDtl)
                .build();
    }
}
