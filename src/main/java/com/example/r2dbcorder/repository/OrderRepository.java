package com.example.r2dbcorder.repository;

import com.example.r2dbcorder.repository.entity.OmOd;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderRepository extends ReactiveCrudRepository<OmOd, String> {
}
