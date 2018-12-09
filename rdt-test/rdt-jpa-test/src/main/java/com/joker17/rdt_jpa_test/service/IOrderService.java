package com.joker17.rdt_jpa_test.service;


import com.joker17.rdt_jpa_test.base.IBaseService;
import com.joker17.rdt_jpa_test.domain.Order;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IOrderService extends IBaseService<Order, String> {
}
