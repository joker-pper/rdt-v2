package com.joker17.redundant.annotation.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于更新列值的条件注解
 */
@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
@Target({ElementType.FIELD})  //作用到类/接口上
public @interface RdtFieldCondition {

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

    /**
     * fill时不允许条件为空抛出异常时的提示信息
     * @return
     */
    String nullTips() default "";

}
