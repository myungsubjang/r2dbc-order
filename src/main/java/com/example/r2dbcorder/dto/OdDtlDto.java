package com.example.r2dbcorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class OdDtlDto {
    private String odNo;
    private int odSeq;
    private int procSeq;
    private String pdNm;
}
