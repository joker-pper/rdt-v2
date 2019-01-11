package com.joker17.rdt_jpa_test.rdt;

import com.joker17.rdt_jpa_test.support.JsonUtils;
import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.core.RdtConfigurationBuilder;
import com.joker17.redundant.core.RdtProperties;
import com.joker17.redundant.core.RdtResolver;
import com.joker17.redundant.spring.RdtSpringResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
        return new RdtSpringResolver() {
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
