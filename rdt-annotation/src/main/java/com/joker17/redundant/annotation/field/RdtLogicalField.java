package com.joker17.redundant.annotation.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface RdtLogicalField {

    /**
     * 作用于类时,若禁用将不支持逻辑值状态
     * @return
     */
    boolean disabled() default false;

    /**
     * 默认为指定列,作用于类时生效
     * @return
     */
    String property() default "";

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
