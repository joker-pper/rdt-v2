package com.devloper.joker.redundant.annotation.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 修改冗余字段的条件
 */
@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
@Target({ElementType.FIELD})  //作用到类/接口上
public @interface RdtFieldCondition {
    String property() default "";  //比较的属性,别名,相等时可修改
    Class target();  //基于哪个修改的类
    int index() default 0;  //第几个
}
