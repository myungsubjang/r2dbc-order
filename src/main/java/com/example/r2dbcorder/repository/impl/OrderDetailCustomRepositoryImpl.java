package com.example.r2dbcorder.repository.impl;

import com.example.r2dbcorder.repository.OrderDetailCustomRepository;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class OrderDetailCustomRepositoryImpl implements OrderDetailCustomRepository {

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<Integer> update(OmOdDtl orderDetail) {
        return template.update(
                Query.query(getDtlCompositeKeyCriteria(orderDetail)),
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
        return Criteria.where("od_no").is(orderDetail.getOdNo())
                .and("od_seq").is(orderDetail.getOdSeq())
                .and("proc_seq").is(orderDetail.getProcSeq());
    }
}
