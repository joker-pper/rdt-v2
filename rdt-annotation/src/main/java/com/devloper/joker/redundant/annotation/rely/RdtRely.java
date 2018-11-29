package com.devloper.joker.redundant.annotation.rely;


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
   KeyTarget[] value();//必须指定,其他注解依赖于class顺序作为相对应持久化类的字段值 e.g {@KeyTarget(value = {"用户"}, target = User.class), @KeyTarget(value = "文章", target = Article.class)}
   Class nullType() default Void.class;//为空时使用的class
   Class unknowType() default Void.class;//未找到对应值时使用的class
   int group() default 0;//该属性的第几组依赖配置描述,该属性可能被不同的字段所依赖,其值可能代表不同的含义,即可能存在多组

   /**
    * 描述key的值类型,默认为当前字段的类型,指定时会进行解析
    * 如果字段是枚举类,未填写时会根据数字解析为对应的ordinal,反之根据name解析
    * @return
    */
   Class valType() default Void.class;

   /**
    * val值是否进行唯一限定,默认为值仅对应一个target class
    */
   boolean unique() default true;

}
