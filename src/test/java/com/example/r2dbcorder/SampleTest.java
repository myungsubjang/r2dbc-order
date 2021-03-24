package com.example.r2dbcorder;

import com.example.r2dbcorder.repository.entity.OmOd;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SampleTest {

    @Test
    void immutableJacksonTest() throws JsonProcessingException {
        OmOd order = new ObjectMapper().readValue("{\"odNo\": \"123\"}", OmOd.class);
        Assertions.assertThat(order.getOdNo())
                .isEqualTo("123");
    }

    @Test
    void leadingZeroTest() {
        int number = 1;
        String numberStr = String.format("%05d", number);
        System.out.println(numberStr);
    }

}
