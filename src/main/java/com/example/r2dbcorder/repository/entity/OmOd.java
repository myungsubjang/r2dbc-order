package com.example.r2dbcorder.repository.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@Table("OM_OD")
public class OmOd implements Serializable {

    private static final long serialVersionUID = -5793348114310316331L;

    @Id
    String odNo;
    String mbNo;
    String odrNm;
    String orglOdNo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime odCmptDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @CreatedDate
    LocalDateTime regDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @LastModifiedDate
    LocalDateTime modDttm;

    @Transient
    List<OmOdDtl> omOdDtlList;
    @Transient
    List<OmOdFvrDtl> omOdFvrDtlList;
}
