package com.joker17.rdt_jpa_test.service;


import com.joker17.rdt_jpa_test.base.IBaseService;
import com.joker17.rdt_jpa_test.domain.User;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IUserService extends IBaseService<User, Long> {
    void updateUser(Long id, String username);
}
