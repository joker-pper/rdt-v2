package com.devloper.joker.rdt_jpa_test.service;

import com.devloper.joker.rdt_jpa_test.base.BaseService;
import com.devloper.joker.rdt_jpa_test.domain.Goods;
import com.devloper.joker.rdt_jpa_test.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderService extends BaseService<Order, String> implements IOrderService {

}
