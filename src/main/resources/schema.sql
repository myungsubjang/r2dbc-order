create table om_od
(
    od_no        VARCHAR(16) not null PRIMARY KEY, --주문번호
    mb_no        VARCHAR(16),                      -- 회원번호
    odr_nm       VARCHAR(16),                      -- 주문자명
    orgl_od_no   VARCHAR(16),                      -- 원주문번호
    od_cmpt_dttm TIMESTAMP,                        --주문완료일시
    reg_dttm     TIMESTAMP,
    mod_dttm     TIMESTAMP
);

create table om_od_dtl
(
    od_no           VARCHAR(16) not null, -- 주문번호
    od_seq          integer     not null, --주문순번
    proc_seq        integer     not null, -- 처리순번
    clm_no          VARCHAR(16),          -- 클레임번호
    od_typ_cd       VARCHAR(16),          -- 주문유형코드 10 주문 20 취소 30 교환 40 반품
    od_prgs_step_cd VARCHAR(16),          -- 주문진행상태코드 01 주문완료 02 출고지시 03 상품준비 04 발송완료 04 배송완료 20 취소완료
    mb_no           VARCHAR(16),          -- 회원번호
    od_qty          integer,              --주문수량
    cncl_qty        integer,              --취소수량
    rtng_qty        integer,              -- 반품수량
    xchg_qty        integer,              -- 교환수량
    sl_prc          integer,              -- 판매가
    dc_amt          integer,              -- 할인금액
    pd_no           VARCHAR(16),          -- 상품번호
    pd_nm           VARCHAR(16),          -- 상품명
    pr_no           VARCHAR(16),          -- 프로모션번
    od_cmpt_dttm    TIMESTAMP,            -- 주문완료일시
    pur_cfrm_dttm   TIMESTAMP,            -- 구매확정일시
    reg_dttm        TIMESTAMP,
    mod_dttm        TIMESTAMP,
    primary key (od_no, od_seq, proc_seq)
);

create table om_od_fvr_dtl
(
    od_fvr_no      VARCHAR(16) not null PRIMARY KEY, -- 주문혜택번호
    od_no          VARCHAR(16) not null,             -- 주문번호
    od_seq         integer     not null,             --주문순번
    proc_seq       integer     not null,             -- 처리순번
    clm_no         VARCHAR(16),                      -- 클레임번호
    orgl_od_fvr_no VARCHAR(16),                      -- 원주문혜택번호
    od_fvr_dvs_cd  VARCHAR(16),                      -- 주문혜택구분코드 발생 HAPN 취소 CNCL
    dc_tnno_cd     VARCHAR(16),                      -- 할인차수코드 1차 1st 2차 2nd 3차 3rd 4차 4th 5차 5th
    aply_qty       integer,                          --적용수량
    cncl_qty       integer,                          --취소수량
    fvr_amt        integer,                          -- 혜택금액
    pr_no          VARCHAR(16),                      -- 프로모션번호
    pr_nm          VARCHAR(16),                      -- 프로모션명
    cpn_no         VARCHAR(16),                      --쿠폰번호
    cpn_nm         VARCHAR(16),                      -- 쿠폰명
    reg_dttm       TIMESTAMP,
    mod_dttm       TIMESTAMP
);