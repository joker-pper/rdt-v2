package com.joker17.redundant.annotation.rely;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
public @interface KeyTarget {
    /**
     * 属于该持久化类型时的状态值列表,默认进行填充及更新(值为字符串null时默认解析为null值,需要注意
     *      String时不要使用null字符串作为状态值)
     */
    String[] value() default {};

    Class target();


    /**
     * 更新时所忽略的值,配置后更新时将会移除在当前中的状态值后再匹配(不影响填充)
     * 单项时支持 $value(即使用value的值)
     */
    String[] ignoreUpdateValue() default {};

    /**
     * fill时未找到状态值抛出异常时的提示信息
     * @return
     */
    String typeNotAllowedTips() default "";

}
