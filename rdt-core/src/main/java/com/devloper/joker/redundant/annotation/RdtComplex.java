package com.devloper.joker.redundant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于描述依赖某字段时的对象注解
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RdtComplex {
    String rely();//所依赖字段,用于表明不同类型值下所对应的class type
    int group() default 0;//依赖字段的第几组配置
    boolean[] single() default true;//默认为单文档进行处理
}
