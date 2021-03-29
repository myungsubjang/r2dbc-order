package com.example.r2dbcorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class ClaimRequest {

    private String odNo;
    private List<Seq> seqList;

    @Data
    public static class Seq {
        private int odSeq;
        private int procSeq;
    }
}
