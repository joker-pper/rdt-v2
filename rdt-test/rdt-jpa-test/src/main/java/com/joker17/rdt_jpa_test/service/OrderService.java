package com.joker17.rdt_jpa_test.service;

import com.joker17.rdt_jpa_test.base.BaseService;
import com.joker17.rdt_jpa_test.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderService extends BaseService<Order, String> implements IOrderService {

}
