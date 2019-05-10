package com.joker17.redundant.annotation.rely;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RdtFieldConditionRelys {
   RdtFieldConditionRely[] value();
}
