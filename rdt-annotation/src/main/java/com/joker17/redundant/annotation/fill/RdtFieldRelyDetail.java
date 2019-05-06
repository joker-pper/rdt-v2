package com.joker17.redundant.annotation.fill;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface RdtFieldRelyDetail {
    Class target();

    /**
     * fill show 时所忽略的类型值列表
     * @return
     */
    String[] fillShowIgnoresType() default {};

    /**
     * fill save 时所忽略的类型值列表
     * @return
     */
    String[] fillSaveIgnoresType() default {};

    /**
     * 是否禁用update(将会覆盖掉全局配置)
     */
    boolean disableUpdate() default false;
}
