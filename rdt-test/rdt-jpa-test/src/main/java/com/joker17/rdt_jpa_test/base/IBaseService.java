package com.joker17.rdt_jpa_test.base;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IBaseService<T, ID> extends JpaRepository<T, ID> {
    Class<T> getEntityClass();
}
