#注解使用介绍

```
@RdtId ---- 标识为id字段

@RdtBaseEntity ---- 标识为持久化类

@RdtBaseField ---- 标识持久化列的别名和列名
属性:
    property(string) 属性字段别名
    columnName(string) 列名
    
@RdtField  ---- 当前类属性字段对应持久化类的属性字段注解(用于匹配对应值后的更新/填充)
属性:
    property(string) 对应持久化类的属性字段 (若属性字段配置别名,则需使用别名值)
    target(class) 对应持久化类
    index(int) 索引,默认值为0,当前类中可存在基于target class的多组情形
    
@RdtFieldCondition ---- 当前类属性字段对应持久化类的属性条件字段注解(用于匹配对应值)
属性:
    property(string) 对应持久化类的属性字段 (若属性字段配置别名,则需使用别名值)
    target(class) 对应持久化类
    index(int) 索引,默认值为0,当前类中可存在基于target class的多组情形

@RdtFieldConditions(value(RdtFieldCondition[])) ---- 用于指定多个@RdtFieldCondition

@RdtFields(value(RdtField[])) ---- 用于指定多个@RdtField

@RdtRely ---- 依赖字段配置描述,用于根据字段类型值指定所对应的持久化类
属性:
   value(KeyTarget[]);  必须指定,其他注解依赖于class顺序作为相对应持久化类的字段值 
                        e.g {@KeyTarget(value = {"用户"}, target = User.class), 
                            @KeyTarget(value = "文章", target = Article.class)}
   nullType(class) 为空值时所对应的持久化类
   unknowType(class) 非已指定类型值时对应的持久化类(**不太建议使用**)
   group(int) 该属性(依赖字段)的第几组依赖配置描述,默认值为0(即默认该依赖字段可能存在
   多组配置)
   valType(class) 描述当前@RdtRely字段值类型,如果未指定则默认为当前属性字段的类型,最
   后进行类型转换解析为对应的值；其中如果字段值类型是枚举类,数字值时会根据对应的ordinal
   解析,反之根据对应的name解析

@KeyTarget  ---- 用于配置处于该持久化类时的类型值
属性:
     target(class) 对应的持久化类
     value(string[]) 处于该持久化类的类型值列表
     
@RdtRelys(value(RdtRely[])) ---- 用于指定多个@RdtRely,用于配置当前依赖字段的多组配置

@RdtFieldRely  ---- （依赖于依赖字段的类型值）当前类属性字段对应持久化类的属性字段注解
(用于匹配对应值后的更新/填充)
属性:
    property(string) 依赖当前类的依赖字段名称
	group(int) 依赖字段的第几组配置的索引值,默认值为0
	index(int) 作为确定当前依赖配置组后(property+group作为标识)的第几组列索引,默认值为0

    targetPropertys(string[]) 如果只有一个值则说明对应的所有持久类所使用的为同一字段,反之
	依次按照依赖字段配置的value中的持久类的顺序为所对应的字段名称
    nullTypeProperty(string) 为空值时所对应的持久化类使用的字段
    unknowTypeProperty(string) 非已指定类型值时对应的持久化类使用的字段
    

@RdtFieldConditionRely   --- （依赖于依赖字段的类型值）当前类属性字段对应持久化类的属性条
件字段注解(用于匹配对应值)
属性: 同上

@RdtMany  --- 关系注解,多(集合、数组)对象(填充/文档型子数据更新时) 

@RdtOne   --- 关系注解，单对象(填充/文档型子数据更新时) 

```