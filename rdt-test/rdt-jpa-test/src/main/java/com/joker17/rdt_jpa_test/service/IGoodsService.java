package com.joker17.rdt_jpa_test.service;


import com.joker17.rdt_jpa_test.base.IBaseService;
import com.joker17.rdt_jpa_test.domain.Goods;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IGoodsService extends IBaseService<Goods, String> {
}
