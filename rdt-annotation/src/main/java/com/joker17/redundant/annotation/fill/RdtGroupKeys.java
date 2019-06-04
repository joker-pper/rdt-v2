package com.joker17.redundant.annotation.fill;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RdtGroupKeys {

    String property() default "";

    Class target();

    int index() default 0;

    /**
     * 当列为String类型时的连接符号
     * @return
     */
    String connector() default ",";

    String nullTips() default "";
}
