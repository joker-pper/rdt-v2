package com.joker17.redundant.annotation.fill;

import com.joker17.redundant.annotation.RdtFillType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RdtGroupConcatField {

    String property() default "";

    Class target();

    int index() default 0;

    /**
     * save时: (填充模式为持久化)
     * 默认只填充持久化字段
     * 启用时跟随填充
     * 禁用时会被忽略
     */
    RdtFillType fillSave() default RdtFillType.DEFAULT;

    /**
     * show时: (填充模式为非持久化)
     *  默认只填充非持久化字段
     *  启用时跟随填充
     *  禁用时会被忽略
     */
    RdtFillType fillShow() default RdtFillType.DEFAULT;

    String connector() default ",";
}
