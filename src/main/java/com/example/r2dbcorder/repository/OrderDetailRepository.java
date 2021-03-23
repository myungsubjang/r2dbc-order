package com.example.r2dbcorder.repository;

import com.example.r2dbcorder.repository.entity.OmOdDtl;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderDetailRepository extends ReactiveCrudRepository<OmOdDtl, String> {
}
