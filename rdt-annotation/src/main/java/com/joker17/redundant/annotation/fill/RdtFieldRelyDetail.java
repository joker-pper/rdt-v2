package com.joker17.redundant.annotation.fill;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface RdtFieldRelyDetail {

    Class target();

    /**
     * fill show 时所忽略的类型值列表 (使用自身值)
     * @return
     */
    String[] fillShowIgnoresType() default {};

    /**
     * fill save 时所忽略的类型值列表 (使用自身值)
     * @return
     */
    String[] fillSaveIgnoresType() default {};


}
