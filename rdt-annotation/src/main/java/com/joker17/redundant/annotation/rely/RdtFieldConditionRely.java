package com.joker17.redundant.annotation.rely;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
@Target({ElementType.FIELD})  //作用到类/接口上
public @interface RdtFieldConditionRely {
    String property();  //依赖当前类的哪个字段别名,这个字段有相应值所对应的target类
    String[] targetPropertys() default {};  //如果只有一个则说明所有类使用的为同一字段,反之依次按照target class的顺序为所对应字段
    int group() default 0;
    int index() default 0;//所属组配置的第几个

    /**
     * 设置具体的class限定唯一,不再根据默认的@KeyTarget中存在的class动态指定多个
     * @return
     */
    Class target() default Void.class;

    /**
     * fill时不允许条件为空抛出异常时的提示信息
     * @return
     */
    String[] nullTips() default {};
}
