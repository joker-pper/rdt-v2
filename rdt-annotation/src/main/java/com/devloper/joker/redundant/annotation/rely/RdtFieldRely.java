package com.devloper.joker.redundant.annotation.rely;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
@Target({ElementType.FIELD})
public @interface RdtFieldRely {
    String property();  //依赖当前类的哪个字段别名,这个字段有相应值所对应的target类
    String[] targetPropertys() default {};  //如果只有一个则说明所有类使用的为同一字段,反之依次按照target class的顺序为所对应字段
    String nullTypeProperty() default "";  //null值所对应类使用的字段
    String unknowTypeProperty() default "";  //未找到对应时的类所使用的字段
    int group() default 0;//依赖property的第几组
    int index() default 0;

    /**
     * 设置具体的class限定唯一,不再根据默认的@KeyTarget中存在的class动态指定多个
     * @return
     */
    Class target() default Void.class;
}
