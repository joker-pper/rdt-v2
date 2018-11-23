package com.devloper.joker.rdt_sbm.rdt;

import com.alibaba.fastjson.JSON;
import com.devloper.joker.redundant.core.RdtConfiguration;
import com.devloper.joker.redundant.core.RdtConfigurationBuilder;
import com.devloper.joker.redundant.core.RdtProperties;
import com.devloper.joker.redundant.operation.MongoRdtOperation;
import com.devloper.joker.redundant.core.RdtResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Resource;
import java.lang.reflect.Field;

@Configuration
public class RdtConfig {
    @Resource
    private MongoTemplate mongoTemplate;

    @Value("${rdt.domain.basePackage}")
    private String basePackage;

    @Bean
    public RdtResolver rdtResolver() {
        return new RdtResolver() {

            @Override
            protected Class<?>[] customBaseEntityAnnotations() {
                return new Class[] {Document.class};
            }

            @Override
            protected boolean isBaseClassByAnalysis(Class entityClass) {
                return false;
            }

            @Override
            protected String getColumnNameByAnalysis(Class<?> entityClass, Field field) {
                return null;
            }

            @Override
            protected String getEntityNameByAnalysis(Class<?> entityClass) {
                return null;
            }

            @Override
            protected Class<?>[] primaryIdAnnotations() {
                return new Class[]{Id.class};
            }

            @Override
            protected String getPrimaryIdByAnalysis(Class aClass, Field field) {
                return null;
            }

            @Override
            protected Class<?>[] columnTransientAnnotations() {
                return new Class[] {Transient.class};
            }

            @Override
            public String toJson(Object o) {
                //return SerializationUtils.serializeToJsonSafely(o);
                return JSON.toJSONString(o);
            }
        };
    }

    @Bean
    public RdtProperties rdtProperties() {
        RdtProperties properties = new RdtProperties();
        properties.setBasePackage(basePackage);
        //是否通过saveAll保存
        properties.setComplexBySaveAll(false);
        properties.setDeepCloneChangedModify(Boolean.TRUE);
        return properties;
    }

    @Bean
    public RdtConfiguration rdtConfiguration() {
        return RdtConfigurationBuilder.build(rdtProperties(), rdtResolver());
    }

    @Bean
    public MongoRdtOperation mongoRdtOperation() {
        MongoRdtOperation operation = new MongoRdtOperation(rdtConfiguration()) {
        };
        operation.setMongoTemplate(mongoTemplate);
        return operation;
    }
}
