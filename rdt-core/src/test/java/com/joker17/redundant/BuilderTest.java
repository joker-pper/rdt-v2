package com.joker17.redundant;

import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.core.RdtConfigurationBuilder;
import com.joker17.redundant.core.RdtProperties;
import com.joker17.redundant.core.RdtResolver;
import com.joker17.redundant.domain.Article;
import com.joker17.redundant.domain.User;
import com.joker17.redundant.fill.FillType;
import com.joker17.redundant.model.ClassModel;
import com.joker17.redundant.model.Column;
import com.joker17.redundant.model.ModifyRelyDescribe;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

public class BuilderTest {

    private RdtProperties properties = null;

    private RdtResolver rdtResolver = null;

    private RdtConfiguration rdtConfiguration = null;

    @Before
    public void before() {
        properties = new RdtProperties();

        properties.setBasePackage("com.joker17.redundant.domain");
        properties.setShowDescribe(true);
        rdtResolver = new RdtResolver() {
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

        rdtConfiguration = RdtConfigurationBuilder.build(properties, rdtResolver);
    }

    @Test
    public void test() {
        System.out.println(rdtConfiguration);
    }


    @Test
    public void test2() {
        final int size = 500;
        final CountDownLatch doneSignal = new CountDownLatch(size);

        for (int i = 0; i < size; i ++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    rdtConfiguration.doModifyRelyDescribeHandle(rdtConfiguration.getClassModel(User.class), rdtConfiguration.getClassModel(Article.class), new RdtConfiguration.ModifyRelyDescribeCallBack() {
                        @Override
                        public void execute(ClassModel classModel, ClassModel modifyClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {
                            rdtConfiguration.getModifyRelyDescribeForFill(describe, FillType.PERSISTENT);
                        }
                    });
                    doneSignal.countDown();
                }
            }).start();
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
