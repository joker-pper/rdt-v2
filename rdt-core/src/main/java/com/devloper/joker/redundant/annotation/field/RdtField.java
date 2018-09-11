package com.devloper.joker.redundant.annotation.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 修改冗余字段的注解
 */
@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
@Target({ElementType.FIELD})
public @interface RdtField {
    String property() default "";  //通过修改类对应的属性(别名)(不为空时通过该属性值查找对应的类属性,为空时则默认使用当前属性名)的值作为更新的值
    Class target();  //基于哪个修改的类
    String columnName() default ""; //列名
    int index() default 0;  //第几个
}
