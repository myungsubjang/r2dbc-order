package com.example.r2dbcorder.repository.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Table("OM_OD_DTL")
public class OmOdDtl implements Serializable, Persistable<String> {
    private static final long serialVersionUID = -5200824753588401614L;

    @Id
    private String odNo;
    private int odSeq;
    private int procSeq;
    private String clmNo;
    private String odTypCd;
    private String odPrgsStepCd;
    private String mbNo;
    private int odQty;
    private int cnclQty;
    private int rtngQty;
    private int xchgQty;
    private int slPrc;
    private int dcAmt;
    private String pdNo;
    private String pdNm;
    private String prNo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime odCmptDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime purCfrmDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @CreatedDate
    private LocalDateTime regDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @LastModifiedDate
    private LocalDateTime modDttm;

    @Override
    public String getId() {
        return odNo;
    }

    @Override
    public boolean isNew() {
        return regDttm == null;
    }
}

