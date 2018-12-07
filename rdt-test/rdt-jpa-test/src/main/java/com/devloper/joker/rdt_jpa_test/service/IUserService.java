package com.devloper.joker.rdt_jpa_test.service;


import com.devloper.joker.rdt_jpa_test.base.IBaseService;
import com.devloper.joker.rdt_jpa_test.domain.User;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IUserService extends IBaseService<User, Long> {
    void updateUser(Long id, String username);
}
