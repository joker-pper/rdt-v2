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

    String connector() default ",";
}
