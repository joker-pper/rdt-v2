package com.joker17.rdt_jpa_test.rdt;

import com.joker17.rdt_jpa_test.core.RepositoryUtils;
import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.operation.AbstractJpaOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Component
@ConditionalOnProperty(name = "config.hibernate-or-jpa", havingValue = "jpa")
public class JpaOperation extends AbstractJpaOperation {

    public JpaOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    //@Resource
    @PersistenceContext
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    protected <T> T save(T entity, Class<T> entityClass) {
        return (T)getCrudRepository(entityClass).save(entity);
    }

    @Override
    protected <T> Collection<T> saveAll(Collection<T> data, Class<T> entityClass) {
        return (Collection<T>)getCrudRepository(entityClass).saveAll(data);
    }

    @Override
    protected <T> List<T> findByIdInExecute(Class<T> entityClass, String idKey, Collection<Object> ids) {
        return new ArrayList<T>((Collection) getCrudRepository(entityClass).findAllById(ids));
    }

    @Override
    public <T> T findByIdExecute(Class<T> entityClass, Object id) {
        Optional optional = getCrudRepository(entityClass).findById(id);
        if (optional.isPresent()) {
            return (T) optional.get();
        }
        return null;
    }

    public CrudRepository getCrudRepository(Class entityClass) {
        return RepositoryUtils.getCrudRepository(entityClass);
    }

    public CrudRepository getActualCrudRepository(Class entityClass) {
        return RepositoryUtils.getActualCrudRepository(entityClass);
    }

}
