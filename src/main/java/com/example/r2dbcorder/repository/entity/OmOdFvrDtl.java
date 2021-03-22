package com.example.r2dbcorder.repository.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("OmOdFvrDtl")
public class OmOdFvrDtl implements Serializable {
    private static final long serialVersionUID = 2028689674664550029L;

    @Id
    private String odFvrNo;
    private String odNo;
    private Integer odSeq;
    private Integer procSeq;
    private String clmNo;
    private String orglOdFvrNo;
    private String odFvrDvsCd;
    private String dcTnnoCd;
    private Integer aplyQty;
    private Integer cnclQty;
    private Integer fvrAmt;
    private String prNo;
    private String prNm;
    private String cpnNo;
    private String cpnNm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime regDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime modDttm;
}