package com.example.r2dbcorder.config.persistence;

import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.util.TestOrderUtil;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public class OmOdCallback implements BeforeConvertCallback<OmOd> {

    @Override
    public Publisher<OmOd> onBeforeConvert(OmOd order, SqlIdentifier table) {
        return isNewOrder(order) ? Mono.just(setOrderNoAndGet(order)) : Mono.just(order);
    }

    private boolean isNewOrder(OmOd order) {
        return !StringUtils.hasText(order.getOdNo());
    }

    private OmOd setOrderNoAndGet(OmOd order) {
        return order.withOdNo(TestOrderUtil.generateOrderNumber());
    }
}
