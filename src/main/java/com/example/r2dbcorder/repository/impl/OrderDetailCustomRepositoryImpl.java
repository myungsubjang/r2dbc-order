package com.example.r2dbcorder.repository.impl;

import com.example.r2dbcorder.dto.OmOdDtlFvrDtlDto;
import com.example.r2dbcorder.repository.OrderDetailCustomRepository;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.data.relational.core.query.Query.*;
import static org.springframework.data.relational.core.query.Criteria.*;

public class OrderDetailCustomRepositoryImpl implements OrderDetailCustomRepository {

    private final R2dbcEntityTemplate template;
    private final ObjectMapper columnMapper;

    public OrderDetailCustomRepositoryImpl(R2dbcEntityTemplate template) {
        this.template = template;
        this.columnMapper = createColumnMapper();
    }

    private ObjectMapper createColumnMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return mapper;
    }

    @Override
    public Mono<Integer> update(OmOdDtl orderDetail) {
        return template.update(
                query(getDtlCompositeKeyCriteria(orderDetail)),
                Update.update("od_no", orderDetail.getOdNo())
                    .set("od_seq", orderDetail.getOdSeq())
                    .set("proc_seq", orderDetail.getProcSeq())
                    .set("orgl_proc_seq", orderDetail.getOrglProcSeq())
                    .set("clm_no", orderDetail.getClmNo())
                    .set("od_typ_cd", orderDetail.getOdTypCd())
                    .set("od_prgs_step_cd", orderDetail.getOdPrgsStepCd())
                    .set("mb_no", orderDetail.getMbNo())
                    .set("od_qty", orderDetail.getOdQty())
                    .set("cncl_qty", orderDetail.getCnclQty())
                    .set("rtng_qty", orderDetail.getRtngQty())
                    .set("xchg_qty", orderDetail.getXchgQty())
                    .set("sl_prc", orderDetail.getSlPrc())
                    .set("dc_amt", orderDetail.getDcAmt())
                    .set("pd_no", orderDetail.getPdNo())
                    .set("pd_nm", orderDetail.getPdNm())
                    .set("pr_no", orderDetail.getPrNo())
                    .set("od_cmpt_dttm", orderDetail.getOdCmptDttm())
                    .set("pur_cfrm_dttm", orderDetail.getPurCfrmDttm())
                    .set("reg_dttm", orderDetail.getRegDttm())
                    .set("mod_dttm", LocalDateTime.now()),
                OmOdDtl.class
        );
    }

    private Criteria getDtlCompositeKeyCriteria(OmOdDtl orderDetail) {
        return where("od_no").is(orderDetail.getOdNo())
                .and("od_seq").is(orderDetail.getOdSeq())
                .and("proc_seq").is(orderDetail.getProcSeq());
    }

    public Flux<OmOdDtlFvrDtlDto> joinPractice(String odNo) {
        DatabaseClient dbClient = template.getDatabaseClient();
        return dbClient.sql("SELECt *\n" +
                "FROM OM_OD_DTL a\n" +
                "LEFT OUTER JOIN OM_OD_FVR_DTL b\n" +
                "ON a.OD_NO = b.OD_NO\n" +
                "AND a.OD_SEQ = b.OD_SEQ\n" +
                "AND a.PROC_SEQ = b.PROC_SEQ\n" +
                "WHERE a.OD_NO = :odNo")
                .bind("odNo", odNo)
                .fetch()
                .all()
                .map(rowMap -> convertRowMapToObject(rowMap, OmOdDtlFvrDtlDto.class));

    }

    public <T> T convertRowMapToObject(Map<String, Object> rowMap, Class<T> clazz) {
        return columnMapper.convertValue(rowMap, clazz);
    }
}
