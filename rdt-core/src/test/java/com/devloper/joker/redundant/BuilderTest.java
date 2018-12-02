package com.devloper.joker.redundant;

import com.devloper.joker.redundant.core.RdtConfiguration;
import com.devloper.joker.redundant.core.RdtConfigurationBuilder;
import com.devloper.joker.redundant.core.RdtProperties;
import com.devloper.joker.redundant.core.RdtResolver;

import java.lang.reflect.Field;

public class BuilderTest {


    public static void main(String[] args) {

        RdtProperties properties = new RdtProperties();
        properties.setBasePackage("com.devloper.joker.redundant.domain");

        RdtResolver rdtResolver = new RdtResolver() {
            @Override
            protected Class<?>[] customBaseEntityAnnotations() {
                return new Class[0];
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
                return new Class[0];
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
            public String toJson(Object o) {
                return null;
            }
        };

        RdtConfiguration rdtConfiguration = RdtConfigurationBuilder.build(properties, rdtResolver);

        System.out.println(rdtConfiguration);

    }
}
