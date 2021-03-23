package com.example.r2dbcorder.repository.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("OM_OD_FVR_DTL")
public class OmOdFvrDtl implements Serializable {
    private static final long serialVersionUID = 2028689674664550029L;

    @Id
    private String odFvrNo;
    private String odNo;
    private int odSeq;
    private int procSeq;
    private String clmNo;
    private String orglOdFvrNo;
    private String odFvrDvsCd;
    private String dcTnnoCd;
    private int aplyQty;
    private int cnclQty;
    private int fvrAmt;
    private String prNo;
    private String prNm;
    private String cpnNo;
    private String cpnNm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @CreatedDate
    private LocalDateTime regDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @LastModifiedDate
    private LocalDateTime modDttm;
}