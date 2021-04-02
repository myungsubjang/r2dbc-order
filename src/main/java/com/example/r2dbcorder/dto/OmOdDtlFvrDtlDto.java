package com.example.r2dbcorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OmOdDtlFvrDtlDto {
    private String odNo;
    private String mbNo;
    private Integer odSeq;
    private Integer procSeq;
    private Integer odQty;
    private Integer cnclQty;
    private Integer slPrc;
    private Integer dcAmt;
    private Integer fvrAmt;
    private String prNo;
    private String prNm;
    private String cpnNo;
    private String cpnNm;
}
