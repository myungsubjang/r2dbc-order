package com.example.r2dbcorder.repository;

import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderFavorDetailRepository extends ReactiveCrudRepository<OmOdFvrDtl, String> {
}
