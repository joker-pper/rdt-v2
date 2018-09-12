package com.devloper.joker.rdt_sbm.rdt;

import com.alibaba.fastjson.JSONObject;
import com.devloper.joker.redundant.model.RdtProperties;
import com.devloper.joker.redundant.model.RdtSupport;
import com.devloper.joker.redundant.operation.MongoRdtOperation;
import com.devloper.joker.redundant.resolver.RdtResolver;
import com.mongodb.util.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.SerializationUtils;

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
                return SerializationUtils.serializeToJsonSafely(o);
            }
        };
    }

    @Bean
    public RdtProperties rdtProperties() {
        RdtProperties properties = new RdtProperties();
        properties.setBasePackage(basePackage);
        properties.setEnableEntityName(true);
        properties.setEnableColumnName(true);
        //是否通过saveAll保存
        properties.setComplexBySaveAll(false);
        properties.setDeepCloneChangedModify(false);
        return properties;
    }

    @Bean
    public RdtSupport rdtSupport() {
        return rdtProperties().builder(rdtResolver());
    }

    @Bean
    public MongoRdtOperation mongoRdtOperation() {
        MongoRdtOperationImpl operation = new MongoRdtOperationImpl();
        operation.setRdtSupport(rdtSupport());
        operation.setMongoTemplate(mongoTemplate);
        return operation;
    }
}
