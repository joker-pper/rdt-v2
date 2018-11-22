# rdt-v2

> rdt-v2是基于注解维护实体对象之间的冗余字段关系,然后可以进行批量更新或自动填充自动数据,避免重复编写逻辑代码及减少复杂关系维护的操作框架.

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
    
    //持久层操作对象,默认提供mongodb与jpa的一些支持,但可能仍需要情况来提供
    //对应的方法实现,也可以覆盖已存在的实现方法。
    //未存在的可继承相关Operation父类,实现具体的查询/更新方法即可。
    public MongoRdtOperation mongoRdtOperation() {
        MongoRdtOperation operation = new MongoRdtOperation(rdtSupport()) {
        };
        operation.setMongoTemplate(mongoTemplate);
        return operation;
    }
    
    
> example  (jpa-test示例)

```

/***
* 现有商品和订单两个实体
* 订单中持久化商品id,商品购买状态,未持久化商品名称(只是为了展示填充字段)
* 当商品的价格改变时会更新订单中未付款的数据
*
**/

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goods {

    @Id
    private String id;
    private String name;
    private Integer price;
}



@Entity
@Table(name = "t_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    @RdtFieldConditionRely(property = "type", targetPropertys = "id")
    @RdtFieldCondition(target = Goods.class, property = "id")
    private String goodsId;

    @Transient
    @RdtField(target = Goods.class, property = "name")
    private String goodsName;

    /**
     * 当订单类型为2时,当goods的金额值更改后进行更新
     */
    @RdtFieldRely(property = "type")
    private Integer price;

    /**
     * type: 1 已完成 2: 未付款
     */
    @RdtRely(@KeyTarget(target = Goods.class, value = "2"))
    private Integer type;


}

    @Resource
    private IGoodsService goodsService;

    @Resource
    private IOrderService orderService;

    /**
     * 初始化数据
     */
    @Test
    public void initData() {
        goodsService.deleteAll();
        orderService.deleteAll();
        Goods goods = new Goods("1", "商品1", 2333);
        goodsService.save(goods);

        List<Order> orderList = new ArrayList<>();

        for (int i = 0; i < 6; i ++) {
            Order order = new Order();
            order.setId(i + 1 + "");
            order.setGoodsId("1");
            //设置商品名称,由于设置为@Transient不会被保存
            order.setGoodsName(goods.getName());
            order.setPrice(2333);
            order.setType(new Random().nextInt(2) + 1);
            orderList.add(order);
        }
        orderService.saveAll(orderList);
    }

    /**
     * 更新goods同时更新状态为未付款的订单金额
     */
    @Test
    @Transactional
    @Rollback(false)
    public void updateGoods() {
        Goods goods = goodsService.getOne("1");
        Goods before = JSON.parseObject(JsonUtils.toJson(goods), Goods.class);
        goods.setName("新商品1");
        goods.setPrice(666666);
        goodsService.save(goods);
        //更新相关数据,将会只更新order表中price相关的数据
        rdtOperation.updateMulti(goods, before);
    }



    /**
     * 显示所有订单信息
     */
    @Test
    public void findAllOrder() {
        logger.info("result: {}", JsonUtils.toJson(orderService.findAll()));
    }


    @Test
    public void findAllOrderWithFill() {
        List<Order> orderList = orderService.findAll();
        //默认只会填充列为transient的字段值(即更新+填充的方式可以同时使用,且不会填充持久化的数据)
        rdtOperation.fillForShow(orderList);
        logger.info("result: {}", JsonUtils.toJson(orderList));
        logger.info("----------------------------------------------------");
        //会填充所有字段
        rdtOperation.fillForShow(orderList, false, true);
        logger.info("result: {}", JsonUtils.toJson(orderList));
    }

```



> api使用

```
//更新方法

//更新当前对象的所有相关冗余字段数据
updateMulti(Object current);

//根据当前对象与之前对象数据对比后,更新被引用字段值所发生改变后的相关冗余字段数据
updateMulti(Object current, Object before);


  /**
     * fill(collection, allowedNullValue, checkValue, clear, false);
     */
    void fill(Collection<?> collection, boolean allowedNullValue, boolean checkValue, boolean clear);

    /**
     * 填充数据列表的核心方法,根据当前集合数据、参数以及关系填充所引用target持久化类的字段值
     * @param collection 当前需要进行填充的数据列表(支持不同类型的数据)
     * @param allowedNullValue 是否允许条件列值为null,为false时存在null值会抛出 FillNotAllowedValueException 异常
     * @param checkValue 为true时对应条件值的个数必须等于所匹配的结果个数,反之抛出 FillNotAllowedDataException 异常
     * @param clear 为true时会清除未匹配到数据的字段值
     * @param onlyTransient 为true时只填充为transient的column
     *
     * 异常类:
     * @see     com.devloper.joker.redundant.fill.FillNotAllowedValueException
     * @see     com.devloper.joker.redundant.fill.FillNotAllowedDataException
     */
    void fill(Collection<?> collection, boolean allowedNullValue, boolean checkValue, boolean clear, boolean onlyTransient);


    /**
     * fillForShow(collection, false),默认只填充transient的列
     * @param collection
     */
    void fillForShow(Collection<?> collection);


    /**
     * fillForShow(collection, true, false)
     */
    void fillForShow(Collection<?> collection, boolean clear);


```


示例项目:

 rdt-spring-mongodb-test (包含更新示例)
 
 rdt-jpa-test (包含填充及更新示例)
