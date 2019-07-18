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

    /**
     * 通过动态获取的实体类
     * @return
     */
    Class gain() default Void.class;

    /**
     * 获取数据的来源列
     * @return
     */
    String gainProperty() default "";

    /**
     * 获取数据的条件列
     * @return
     */
    String[] gainConditionPropertys() default {};

    /**
     * 获取数据的条件列(依赖于当前类的列值)
     * @return
     */
    String[] gainConditionValueRelyPropertys() default {"id"};
}
