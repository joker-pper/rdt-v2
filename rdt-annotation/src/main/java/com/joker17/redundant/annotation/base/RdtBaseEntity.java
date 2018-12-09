package com.joker17.redundant.annotation.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


//说明此类作为要修改冗余字段的基类
@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
@Target({ElementType.TYPE})  //作用到类/接口上
public @interface RdtBaseEntity {
    String name() default "";
}
