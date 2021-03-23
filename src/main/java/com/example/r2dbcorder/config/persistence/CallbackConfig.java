package com.example.r2dbcorder.config.persistence;

import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import com.example.r2dbcorder.util.TestOrders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Configuration
public class CallbackConfig {

    @Bean
    public BeforeConvertCallback<OmOd> omOdIdSetCallback() {
        return (order, table) -> isNewOrder(order) ? Mono.just(setOrderNoAndGet(order)) : Mono.just(order);
    }

    private boolean isNewOrder(OmOd order) {
        return !StringUtils.hasText(order.getOdNo());
    }

    private OmOd setOrderNoAndGet(OmOd order) {
        return order.withOdNo(TestOrders.generateOrderNumber());
    }

    @Bean
    public BeforeConvertCallback<OmOdFvrDtl> omOdFvrDtlIdSetCallback() {
        return (favorDetail, table) -> isNewFavorDetail(favorDetail) ?
                Mono.just(setFavorNoAndGetFavorDetail(favorDetail)) : Mono.just(favorDetail);
    }

    private boolean isNewFavorDetail(OmOdFvrDtl favorDetail) {
        return !StringUtils.hasText(favorDetail.getOdFvrNo());
    }

    private OmOdFvrDtl setFavorNoAndGetFavorDetail(OmOdFvrDtl favorDetail) {
        return favorDetail.withOdFvrNo(TestOrders.generateOrderFavorNumber());
    }

}
