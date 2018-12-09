package com.joker17.redundant.annotation.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
@Target({ElementType.FIELD})
public @interface RdtBaseField {
    String alias() default "";  //别名,如果没有则默认为属性名称
    String columnName() default ""; //列名

}
