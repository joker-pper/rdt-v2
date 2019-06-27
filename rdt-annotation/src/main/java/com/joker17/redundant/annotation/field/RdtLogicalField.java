package com.joker17.redundant.annotation.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RdtLogicalField {

    /**
     * 值类型
     * @return
     */
    Class valType() default Void.class;

    /**
     * 正常状态的逻辑值(为空时将使用全局值)
     * @return
     */
    String[] value() default {};

}
