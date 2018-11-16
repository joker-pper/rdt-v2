package com.devloper.joker.rdt_jpa_test.rdt;

import com.devloper.joker.rdt_jpa_test.core.RepositoryUtils;
import com.devloper.joker.redundant.model.*;
import com.devloper.joker.redundant.operation.AbstractJpaOperation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Component
public class JpaOperation extends AbstractJpaOperation {

    public JpaOperation(RdtSupport rdtSupport) {
        super(rdtSupport);
    }

    //@Resource
    @PersistenceContext
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    public <T> T findById(Class<T> entityClass, Object id) {
        Optional optional = getCrudRepository(entityClass).findById(id);
        if (optional.isPresent()) {
            return (T) optional.get();
        }
        return null;
    }

    @Override
    public CrudRepository getCrudRepository(Class entityClass) {
        return RepositoryUtils.getCrudRepository(entityClass);
    }

    @Override
    public CrudRepository getActualCrudRepository(Class entityClass) {
        return RepositoryUtils.getActualCrudRepository(entityClass);
    }

    @Override
    public <T> T save(T entity) {
        return super.save(entity);
    }

}
