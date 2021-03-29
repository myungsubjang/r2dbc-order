package com.example.r2dbcorder.config.persistence;

import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import com.example.r2dbcorder.util.TestOrderUtil;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public class OmFvrDtlCallback implements BeforeConvertCallback<OmOdFvrDtl> {
    @Override
    public Publisher<OmOdFvrDtl> onBeforeConvert(OmOdFvrDtl favorDetail, SqlIdentifier table) {
        return isNewFavorDetail(favorDetail) ? Mono.just(setFavorNoAndGetFavorDetail(favorDetail)) : Mono.just(favorDetail);
    }

    private boolean isNewFavorDetail(OmOdFvrDtl favorDetail) {
        return !StringUtils.hasText(favorDetail.getOdFvrNo());
    }

    private OmOdFvrDtl setFavorNoAndGetFavorDetail(OmOdFvrDtl favorDetail) {
        return favorDetail.withOdFvrNo(TestOrderUtil.generateOrderFavorNumber());
    }
}
