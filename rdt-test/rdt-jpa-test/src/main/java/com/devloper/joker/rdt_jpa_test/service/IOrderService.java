package com.devloper.joker.rdt_jpa_test.service;


import com.devloper.joker.rdt_jpa_test.base.IBaseService;
import com.devloper.joker.rdt_jpa_test.domain.Order;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IOrderService extends IBaseService<Order, String> {
}
