package com.joker17.rdt_jpa_test.rdt;

import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.operation.AbstractHibernateOperation;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Method;
import java.util.List;


@Component
@ConditionalOnProperty(name = "config.hibernate-or-jpa", havingValue = "hibernate")
public class HibernateOperation extends AbstractHibernateOperation {

    @PersistenceContext
    private EntityManager entityManager;

    private SessionFactory sessionFactory;

    public HibernateOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Session getSession() {
        try {
            return this.entityManager.unwrap(Session.class);
        } catch (Exception e) {
            logger.info("通过entityManager获取Session失败,通过SessionFactory进行获取..");
            SessionFactory sessionFactory = getSessionFactory();
            Session session = null;
            try {
                session = sessionFactory.getCurrentSession();
            } catch (HibernateException ex) {
            }
            return session == null ? sessionFactory.openSession() : session;
        }
    }

    /**
     * 获取SessionFactory
     * @return
     */
    protected SessionFactory getSessionFactory() {
        if (this.sessionFactory == null) {
            EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
            try {
                Method getSessionFactory = entityManagerFactory.getClass().getMethod("getSessionFactory");
                this.sessionFactory = (SessionFactory) ReflectionUtils.invokeMethod(getSessionFactory, entityManagerFactory);
            } catch (NoSuchMethodException var3) {
                throw new IllegalStateException("No compatible Hibernate EntityManagerFactory found: " + var3);
            }
        }
        return sessionFactory;
    }

    @Override
    protected <T> List<T> findByConditionsExecute(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys) {
        return super.findByConditionsExecute(entityClass, conditionPropertys, conditionValues, null);
    }
}
