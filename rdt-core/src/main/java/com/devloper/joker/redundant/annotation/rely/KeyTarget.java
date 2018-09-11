package com.devloper.joker.redundant.annotation.rely;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
public @interface KeyTarget {
    String[] value() default {};
    Class target();
}
