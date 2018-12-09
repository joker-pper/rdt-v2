package com.joker17.rdt_jpa_test.repository;

import com.joker17.rdt_jpa_test.domain.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends BaseRepository<Order, String>{
}
