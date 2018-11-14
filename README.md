# rdt-v2

基于注解构建完成后的关系更新/填充实体字段的框架

特性：

````
使用简单,依赖性低
完成java bean类数据关系的维护
基于条件注解和依赖字段注解,通过条件标识对应持久化类中唯一数据
对数据操作层进行抽象,可根据具体场景自行实现/覆盖数据层数据的操作方法
基于方法可定制化,均可覆盖大部分所提供解析的方法
支持持久化数据相关的冗余数据更新
支持vo,dto,pojo等的数据列表填充(提供多组参数值配置,适用性高,并对传入的数据进行条件分类,减少数据层的操作次数)
````

   
> 注意事项

````
关于字段的注解仅支持在字段上配置
合理利用字段transient特性,以提升性能(可避免框架中不必要的逻辑处理)
RdtResolver可配置提供外的注解,包含全局持久化类注解,id注解,transient注解,建议提供toJson方法的支持
建议唯一性数据标识不存在重复
````

> 如何引用

````
请自行根据需要打包所依赖jar,rdt-core及rdt-annotation作为核心jar
rdt-jpa及rdt-spring-mongodb为已提供的数据层操作实现,可作为具体使用框架实现的参考
````

> 注解介绍: [详情](https://github.com/joker-pper/rdt-v2/wiki/Rdt%E6%B3%A8%E8%A7%A3%E4%BB%8B%E7%BB%8D)

> api介绍: [详情](https://github.com/joker-pper/rdt-v2/blob/master/rdt-core/src/main/java/com/devloper/joker/redundant/operation/RdtOperation.java)


>使用配置:
	
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


示例项目:

 rdt-spring-mongodb-test 
 
 rdt-jpa-test (包含填充及更新示例)
