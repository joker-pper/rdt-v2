package com.joker17.redundant.annotation.rely;

import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.annotation.fill.RdtFieldRelyDetail;

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
    /**
     * 依赖当前类的哪个字段别名,通过该字段值匹配对应的target类
     * @return
     */
    String property();

    /**
     * 只配置一个时为全局所对应的字段,若存在不同则需要按照target class的顺序依次配置
     * @return
     */
    String[] targetPropertys() default {};
    int group() default 0;//依赖property的第几组
    int index() default 0;

    /**
     * 设置具体的class限定唯一,不再根据默认的@KeyTarget中存在的class动态指定多个
     * @return
     */
    Class target() default Void.class;


    RdtFillType[] fillSave() default RdtFillType.DEFAULT;


    /**
     * show时: (填充模式为非持久化)
     *  默认只填充非持久化字段
     *  启用时跟随填充 e.g:填充已持久化的订单金额在未支付状态下的值
     *  禁用时会被忽略
     */
    RdtFillType[] fillShow() default RdtFillType.DEFAULT;

    /**
     * 配置特定target class的某些属性
     * @return
     */
    RdtFieldRelyDetail[] details() default {};


    /**
     * 是否禁用更新(全局)
     * @return
     */
    boolean disableUpdate() default false;
}
