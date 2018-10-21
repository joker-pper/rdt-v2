package com.devloper.joker.rdt_jpa_test.core;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class RepositoryUtils implements CommandLineRunner {

    private static Map<Class, CrudRepository> domainRepositoryMap = new HashMap<>(); //定义的实体repository实例
    private static Map<Class, CrudRepository> domainActualRepositoryMap = new HashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public void run(String... strings) throws Exception {
        Map<String, CrudRepository> repositoryMap = applicationContext.getBeansOfType(CrudRepository.class);
        for (String beanName : repositoryMap.keySet()) {
            CrudRepository bean = repositoryMap.get(beanName);
            Object actualRepository = AopDataUtils.getTargetInstance(bean);
            EntityInformation entityInformation = (EntityInformation) AopDataUtils.getFieldValue(actualRepository, "entityInformation");
            Class javaType = entityInformation.getJavaType();
            domainRepositoryMap.put(javaType, bean);
            domainActualRepositoryMap.put(javaType, (CrudRepository) actualRepository);
        }

    }

    public static CrudRepository getCrudRepository(Class entityClass) {
        return domainRepositoryMap.get(entityClass);
    }

    /**
     * 通过此实例不会触发自定义repository所在包的拦截方法
     * @param entityClass
     * @return
     */
    public static CrudRepository getActualCrudRepository(Class entityClass) {
        return domainActualRepositoryMap.get(entityClass);
    }


}