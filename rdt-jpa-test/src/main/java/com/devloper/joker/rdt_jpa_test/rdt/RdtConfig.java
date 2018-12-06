package com.devloper.joker.rdt_jpa_test.rdt;

import com.devloper.joker.rdt_jpa_test.support.JsonUtils;
import com.devloper.joker.redundant.core.RdtConfiguration;
import com.devloper.joker.redundant.core.RdtConfigurationBuilder;
import com.devloper.joker.redundant.core.RdtProperties;
import com.devloper.joker.redundant.core.RdtResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Transient;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.reflect.Field;

@Configuration
public class RdtConfig {

    @Bean
    public RdtResolver rdtResolver() {
        return new RdtResolver() {

            @Override
            protected Class<?>[] customBaseEntityAnnotations() {
                return new Class[] {Entity.class};
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
                return new Class[] {Transient.class, javax.persistence.Transient.class};
            }

            @Override
            public String toJson(Object o) {
                return JsonUtils.toJson(o);
            }
        };
    }

    @Bean
    @ConfigurationProperties(prefix = "rdt-config")
    public RdtProperties rdtProperties() {
        RdtProperties properties = new RdtProperties();
        return properties;
    }

    @Bean
    public RdtConfiguration rdtConfiguration() {
        return RdtConfigurationBuilder.build(rdtProperties(), rdtResolver());
    }
}
