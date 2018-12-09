package com.joker17.redundant.annotation.rely;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
public @interface KeyTarget {
    /**
     * 处于该持久化类的类型值列表(值为字符串null时默认解析为null值,需要注意String时不要使用null字符串作为状态值)
     */
    String[] value() default {};
    Class target();
}
