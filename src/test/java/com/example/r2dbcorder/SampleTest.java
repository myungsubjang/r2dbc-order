package com.example.r2dbcorder;

import com.example.r2dbcorder.dto.OmOdDtlFvrDtlDto;
import com.example.r2dbcorder.repository.entity.OmOd;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.http.converter.json.GsonFactoryBean;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

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

    @Test
    void contextualTest() {
        String key = "message";
        Mono.just("hello")
                .flatMap(s -> Mono.deferContextual(ctx -> Mono.just(s + " " + ctx.get(key))))
                .contextWrite(ctx -> ctx.put(key, "World"))
                .doOnNext(System.out::println)
                .subscribe();
    }

    @Test
    void mapperTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
//        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
//        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String, Object> rowMap = new HashMap<>();
        rowMap.put("OD_NO", "202104020001");
        rowMap.put("OD_SEQ", "1");
        rowMap.put("ORGL_PROC_SEQ", "0");
        System.out.println(mapper.convertValue(rowMap, OmOdDtlFvrDtlDto.class));
    }

}
