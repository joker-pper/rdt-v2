package com.devloper.joker.redundant.annotation.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于更新列值的注解,当满足对应组的更新条件后会更新为对应的值
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RdtField {

    /**
     * 对应修改类的属性别名(如果与属性别名一致可不填写)
     * @return
     */
    String property() default "";

    /**
     * 基于哪个修改类
     * @return
     */
    Class target();

    /**
     * 所属target的第几组
     * @return
     */
    int index() default 0;
}
