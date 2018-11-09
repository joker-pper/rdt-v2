# rdt-v2

基于class中的注解构建完成后的关系更新/填充实体字段的框架


注:

	配置的关于字段的实体,仅支持在field上读取
	rdt-core只是对数据关系进行了维护,数据层操作需要根据具体情况实现,可参考默认提供的实现类
	基于方法可覆盖的理念,大多数方法均可被覆盖重写,以便提供更高性能的支持
    基于条件注解和依赖字段注解,通过条件标识对应持久化类中唯一数据
    若当前持久化类/非持久化类中引用的其他持久类的字段(冗余)无需持久化,需要指定为transient(不用更新,框架基于是否被持久化的字段而
     比较更新,若未明确指定,则会进行逻辑层的处理),在RdtResolver中设置对应的注解即可

使用配置:
	
	
	//属性配置类
	public RdtProperties rdtProperties() {
        RdtProperties properties = new RdtProperties();
        //读取class所在的包; 支持,分割
        properties.setBasePackage(basePackage);
        //是否通过saveAll保存
        properties.setComplexBySaveAll(false);
        return properties;
    }
	//框架依赖于该对象的功能解析,可覆盖相应实现方法
	public RdtResolver rdtResolver() {
        return new RdtResolver() {
            
            //base class注解类,用于解析为存在的持久化实体类,默认包含提供的@RdtBaseEntity
            @Override
            protected Class<?>[] customBaseEntityAnnotations() {
                return new Class[] {Document.class};
            }

            @Override
            protected boolean isBaseClassByAnalysis(Class entityClass) {
                return false;
            }

            @Override
            protected String getColumnNameByAnalysis(Class<?> entityClass, Field field) {
                return null;
            }

            @Override
            protected String getEntityNameByAnalysis(Class<?> entityClass) {
                return null;
            }

            //class类的id,默认提供@RdtId(仅作为读取当前类中的id属性字段)
            @Override
            protected Class<?>[] primaryIdAnnotations() {
                return new Class[]{Id.class};
            }

            @Override
            protected String getPrimaryIdByAnalysis(Class aClass, Field field) {
                return null;
            }
            
            //transient注解,用于设置对应字段列属性
            @Override
            protected Class<?>[] columnTransientAnnotations() {
                return new Class[] {Transient.class};
            }
            
            //log中输出json文本的依赖方法
            @Override
            public String toJson(Object o) {
                //return SerializationUtils.serializeToJsonSafely(o);
                return JSON.toJSONString(o);
            }
        };
    }

    //作为读取配置的对象
    public RdtSupport rdtSupport() {
        return rdtProperties().builder(rdtResolver());
    }
    
    //持久层操作对象,默认提供mongodb与jpa的支持,但需要情况提供对应的方法实现,
    //可以覆盖已存在的实现方法。未存在的可继承Operation父类,实现具体的查询/更新方法即可。
    public MongoRdtOperation mongoRdtOperation() {
        MongoRdtOperation operation = new MongoRdtOperation(rdtSupport()) {
        };
        operation.setMongoTemplate(mongoTemplate);
        return operation;
    }
operation 方法:

    update* 根据当前持久化数据中改变的字段值去更新存在于其他持久化类中的冗余字段值
	
    fill    根据当前集合数据根据关系填充所引用target持久化类的字段值,支持属性字段中为对象/List/数组(仅支持一维)
    
    
注解:
   
    
    @RdtId ---- 标识为id字段
    
	@RdtBaseEntity ---- 标识为持久化类
    
	@RdtBaseField
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

    @RdtFieldConditions @RdtFields 支持多个
    
    @RdtRely ---- 用于根据字段类型值指定所对应的持久化类
    属性:
       
       value(KeyTarget[]);  必须指定,其他注解依赖于class顺序作为相对应持久化类的字段值 
                            e.g {@KeyTarget(value = {"用户"}, target = User.class), 
                                @KeyTarget(value = "文章", target = Article.class)}
       nullType(class) 为空值时所对应的class
       unknowType(class) 未找到对应值时使用的class
       group(int) 默认值为0,该属性的第几组依赖配置描述,该属性可能被不同的字段所依赖,其值可能代表不同的含义,即可能存在多组
       valType(class) //描述key的值类型,默认为当前字段的类型,指定时会进行解析,如果字段是枚举类,未填写时会根据数字解析为对
       应的ordinal,反之根据name解析
    
    @KeyTarget
    属性:
         target(class) 对应的持久化类
         value(string[]) 处于该持久化类的类型值
         
    @RdtFieldRely  ---- （依赖于属性的类型值）当前类属性字段对应持久化类的属性字段注解(用于匹配对应值后的更新/填充)
    属性:
        property(string) //依赖当前类的哪个字段别名,这个字段有相应值所对应的target类
        targetPropertys(string[]) //如果只有一个则说明所有类使用的为同一字段,反之依次按照target class的顺序为所对应字段
        nullTypeProperty(string) //null值所对应类使用的字段
        unknowTypeProperty(string) //未找到对应时的类所使用的字段
        group(int) //依赖property的第几组
        index(int)  //作为该组的索引
    
    @RdtFieldConditionRely   --- 作为条件的注解,依赖于属性的类型值
    属性: 同上
    
    @RdtRelys 用于指定多个
    
    @RdtMany  --- 关系注解(填充/文档型子数据更新时) 
    
    @RdtOne   --- 关系注解(填充/文档型子数据更新时) 

示例项目:

 rdt-spring-mongodb-test 
 
 rdt-jpa-test
