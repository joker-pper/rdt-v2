package com.joker17.mp.test.rdt;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.joker17.mp.test.support.JackSonUtils;
import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.core.RdtConfigurationBuilder;
import com.joker17.redundant.core.RdtProperties;
import com.joker17.redundant.core.RdtResolver;
import com.joker17.redundant.model.ClassModel;
import com.joker17.redundant.spring.RdtSpringResolver;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

@Configuration
public class RdtConfig {

    @Bean
    public RdtResolver rdtResolver() {
        return new RdtSpringResolver() {
            @Override
            protected Class<?>[] customBaseEntityAnnotations() {
                return new Class[] {TableName.class};
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
                return new Class[] {TableId.class};
            }

            @Override
            protected String getPrimaryIdByAnalysis(Class entityClass, Field field) {
                return null;
            }

            @Override
            protected Class<?>[] columnTransientAnnotations() {
                return new Class[0];
            }

            @Override
            public boolean isColumnTransient(ClassModel classModel, Field field) {
                boolean result = super.isColumnTransient(classModel, field);
                if (!result) {
                    TableField tableField = getAnnotation(field, TableField.class);
                    if (tableField != null && !tableField.exist()) {
                        result = true;
                    }
                }
                return result;
            }

            @Override
            public String toJson(Object o) {
                return JackSonUtils.toJson(o);
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
