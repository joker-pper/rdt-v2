package com.joker17.redundant.annotation.rely;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该字段的值决定依赖于其字段对应的target class
 * 依赖字段是为了找到相对应的target class
 */
@Retention(RetentionPolicy.RUNTIME)   //在运行时可以获取
@Target({ElementType.FIELD})
public @interface RdtRely {
   /**
    * 用于指定所有的target class
    * 其他注解依赖于class顺序作为相对应持久化类的字段值
    * e.g {@KeyTarget(value = {"用户"}, target = User.class), @KeyTarget(value = "文章", target = Article.class)}
    * @return
    */
   KeyTarget[] value();


   /**
    * 未找到对应值时使用的target class
    * @return
    */
   Class unknownType() default Void.class;

   /**
    * 该属性的第几组依赖配置描述,该属性可能被不同的字段所依赖,其值可能代表不同的含义,即可能存在多组
    * @return
    */
   int group() default 0;

   /**
    * 描述key的值类型,默认为当前字段的类型,指定时会进行解析
    * 如果字段是枚举类,未填写时会根据数字解析为对应的ordinal,反之根据name解析
    * @return
    */
   Class valType() default Void.class;

   /**
    * val值是否进行唯一限定,默认为每个类型值仅对应一个target class
    */
   boolean unique() default true;

    /**
     * 配置除value配置外允许的值列表,仅用于通过填充时的验证(既不影响fill,又不影响update,等同于略过状态值)
     * @return
     */
   String[] allowValues() default {};


   /**
    * fill时未找到状态值抛出异常时的提示信息(全局)
    * @return
    */
   String typeTips() default "";

}
