package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.core.RdtConfiguration;
import com.devloper.joker.redundant.fill.FillOneKeyModel;
import com.devloper.joker.redundant.model.*;
import com.devloper.joker.redundant.support.DataSupport;
import com.devloper.joker.redundant.utils.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.util.*;

public abstract class MongoRdtOperation extends AbstractMongoOperation {

    protected MongoTemplate mongoTemplate;

    public MongoRdtOperation(RdtConfiguration configuration) {
        super(configuration);
    }

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected <T> T save(T entity, Class<T> entityClass) {
        mongoTemplate.save(entity);
        return entity;
    }

    @Override
    public <T> List<T> findByIdIn(Class<T> entityClass, String idKey, Collection<Object> ids) {
        Query query = new Query(criteriaIn(Criteria.where(idKey), ids));
        List<T> list = mongoTemplate.find(query, entityClass);
        return list;
    }

    @Override
    public <T> T findById(Class<T> entityClass, Object id) {
        return mongoTemplate.findById(id, entityClass);
    }


    @Override
    protected <T> List<T> findByFillKeyModelExecute(FillOneKeyModel fillKeyVO) {
        Class<T> entityClass = fillKeyVO.getEntityClass();
        Query query = new Query(criteriaIn(Criteria.where(fillKeyVO.getKey()), fillKeyVO.getKeyValues()));
        return mongoTemplate.find(query, entityClass);
    }

    @Override
    protected <T> List<T> findByFillManyKeyExecute(Class<T> entityClass, List<Column> conditionColumnValues, Set<Column> columnValues, List<Object> conditionGroupValue) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        int index = 0;
        for (Column column : conditionColumnValues) {
            Object value = conditionGroupValue.get(index ++);
            criteria.and(column.getProperty()).is(value);
        }
        return mongoTemplate.find(query, entityClass);
    }

    /**
     * 用于批量保存子文档数据
     * @param data
     * @param entityClass
     * @param <T>
     * @return
     */
    @Override
    protected <T> Collection<T> saveAll(Collection<T> data, Class<T> entityClass) {
        for (T entity : data) {
            mongoTemplate.save(entity);
        }
        return data;
    }

    public Map getCriteriaToMap(Criteria criteria) {
        return criteria.getCriteriaObject();
    }

    protected Pageable getPageable(long page, long size) {
        return PageRequest.of((int) page, (int)size);
    }

    protected void updateMulti(Criteria criteria, Update update, Class entityClass) {
        Query query = Query.query(criteria);
        mongoTemplate.updateMulti(query, update, entityClass);
    }

    protected Criteria criteriaIn(Criteria criteria, Collection<?> vals) {
        if (vals != null && vals.size() == 1) {
            return criteria.is(vals.iterator().next());
        }
        return criteria.in(vals);
    }

    protected Criteria criteriaNotIn(Criteria criteria, Collection<?> vals) {
        if (vals != null && vals.size() == 1) {
            return criteria.ne(vals.iterator().next());
        }
        return criteria.nin(vals);
    }

    /**
     * 添加为对应类的条件(即对应的属性字段为哪些符合的条件)
     * @param describe
     * @param criteria
     * @param relyProperty
     */
    protected void modelTypeCriteriaProcessing(ModifyRelyDescribe describe, Criteria criteria, String relyProperty) {
        List<Object> unknowNotExistValList = describe.getUnknowNotExistValList();
        List<Object> valList = describe.getValList();

        if (!valList.isEmpty()) {
            if (unknowNotExistValList.isEmpty()) {
                criteriaIn(criteria.and(relyProperty), valList);
            } else { //满足在valList 或 非unknowNotExistValList时
                criteria.orOperator(criteriaIn(Criteria.where(relyProperty), valList), criteriaNotIn(Criteria.where(relyProperty), unknowNotExistValList));
            }
        } else {
            if (!unknowNotExistValList.isEmpty()) {
                criteriaNotIn(criteria.and(relyProperty), unknowNotExistValList);
            }
        }
    }

    @Override
    protected void updateModifyDescribeSimpleImpl(ClassModel classModel, final ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo) {
        final Criteria criteria = new Criteria();
        final Update update = new Update();

        //设置查询条件
        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyCondition.getColumn().getProperty();
                criteria.and(property).is(targetPropertyVal);
            }
        });

        //设置更新值
        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getColumn().getProperty();
                update.set(property, targetPropertyVal);
            }
        });

        updateMulti(criteria, update, modifyClassModel.getCurrentClass());
    }

    @Override
    protected void updateModifyRelyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ChangedVo vo, Column relyColumn, int group, ModifyRelyDescribe describe) {
        final Criteria criteria = new Criteria();
        final Update update = new Update();
        String relyProperty = relyColumn.getProperty();

        modelTypeCriteriaProcessing(describe, criteria, relyProperty);

        //设置查询条件
        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyCondition.getColumn().getProperty();
                criteria.and(property).is(targetPropertyVal);
            }
        });

        //设置更新值
        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getColumn().getProperty();
                update.set(property, targetPropertyVal);
            }
        });

        updateMulti(criteria, update, modifyClassModel.getCurrentClass());
    }

    @Override
    protected void updateModifyDescribeOneImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo) {
        final Criteria criteria = new Criteria();
        final Update update = new Update();

        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                String property = getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
                criteria.and(property).is(targetPropertyVal); //用作查询条件
            }
        });

        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyColumn);
                update.set(property, targetPropertyVal); //用作更新值
            }
        });

        updateMulti(criteria, update, modifyClassModel.getCurrentClass());
    }

    @Override
    protected void updateModifyRelyDescribeOneImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, ClassModel modifyClassModel, ModifyRelyDescribe describe, ChangedVo vo, Column relyColumn, int group) {

        final Criteria criteria = new Criteria();
        final Update update = new Update();

        String relyProperty = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, relyColumn);

        modelTypeCriteriaProcessing(describe, criteria, relyProperty);

        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                String property = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
                criteria.and(property).is(targetPropertyVal); //用作查询条件
            }
        });

        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyColumn);
                update.set(property, targetPropertyVal); //用作更新值
            }
        });

        updateMulti(criteria, update, modifyClassModel.getCurrentClass());
    }


    @Override
    protected void updateModifyDescribeManyImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo, RdtLog rdtLog) {
        Criteria criteria = new Criteria();

        final Map<String, Object> conditionValMap = new HashMap<String, Object>(16);  //存放比较属性值的map
        final Map<String, Object> updateValMap = new HashMap<String, Object>(16);  //存放要更新属性值的map
        Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16); //查询条件log数据

        final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

        //统一处理
        updateManyDataConditionRelationshipHandle(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, criteria, conditionValMap, conditionLogMap,null);

        final String analysisPrefix = complexAnalysis.getPrefix() + ".";

        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getColumn().getProperty();
                updateValMap.put(property, targetPropertyVal);
                if (isLoggerSupport()) {
                    updateLogMap.put(getPropertyMark(analysisPrefix + property, targetProperty), targetPropertyVal);
                }
            }
        });

        Class modifyClass = modifyClassModel.getCurrentClass();
        Query query = new Query(criteria);
        doWithPageableCallBack(query, modifyClass, new PageableCallBack() {
            @Override
            void run(List data) {
                updateManyData(data, complexClassModel, modifyClassModel, complexAnalysis, conditionValMap, updateValMap, properties.getComplexBySaveAll(), null, null);
            }
        });


        //设置log
        rdtLog.setCondition(conditionLogMap);
        rdtLog.setUpdate(updateLogMap);
    }

    @Override
    protected void updateModifyRelyDescribeManyImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, int group, RdtLog rdtLog) {
        Criteria criteria = new Criteria();

        final Map<String, Object> conditionValMap = new HashMap<String, Object>(16);  //存放比较属性值的map
        final Map<String, Object> updateValMap = new HashMap<String, Object>(16);  //存放要更新属性值的map
        final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);
        Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16); //查询条件log数据

        updateManyDataConditionRelationshipHandle(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, criteria, conditionValMap, conditionLogMap, relyColumn);

        final String analysisPrefix = complexAnalysis.getPrefix() + ".";

        configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getColumn().getProperty();
                updateValMap.put(property, targetPropertyVal);

                if (isLoggerSupport()) {
                    updateLogMap.put(getPropertyMark(analysisPrefix + property, targetProperty), targetPropertyVal);
                }
            }
        });

        Class modifyClass = modifyClassModel.getCurrentClass();

        Query query = new Query(criteria);
        doWithPageableCallBack(query, modifyClass, new PageableCallBack() {
            @Override
            void run(List data) {
                updateManyData(data, complexClassModel, modifyClassModel, complexAnalysis, conditionValMap, updateValMap, properties.getComplexBySaveAll(), describe, relyColumn);
            }
        });

        rdtLog.setCondition(conditionLogMap);
        rdtLog.setUpdate(updateLogMap);
    }

    protected abstract class PageableCallBack<T> {
       abstract void run(List<T> data);
    }

    protected void doWithPageableCallBack(Query query, Class entityClass, PageableCallBack callBack) {
        if (callBack == null) {
            return;
        }
        long pageSize = properties.getPageSize();
        if (pageSize == -1) {
            //直接查询全部然后处理
            callBack.run(mongoTemplate.find(query, entityClass));
        } else {
            long total = mongoTemplate.count(query, entityClass);
            if (total > 0) {
                long totalPage = (total % pageSize == 0) ? total / pageSize : total / pageSize + 1;
                for (int i = 0; i < totalPage; i ++) {
                    Pageable pageable = getPageable(i, pageSize);
                    boolean hasPageable = pageable != null;
                    if (hasPageable) {
                        query.with(pageable);
                    }
                    callBack.run(mongoTemplate.find(query, entityClass));
                    //没有pageable说明并未分页
                    if (!hasPageable) {
                        logger.warn("getPageable(long page, long size) return null, so have no by limit page update data");
                        break;
                    }
                }
            }
        }
    }

    /**
     * 统一处理为many时的查询条件,及条件属性map赋值
     * @param classModel
     * @param complexClassModel
     * @param complexAnalysis
     * @param modifyClassModel
     * @param describe
     * @param vo
     * @param criteria
     * @param conditionValMap 存放complexClassModel的对应的属性的值的条件map
     * @param conditionLogMap 条件log对象
     * @param relyColumn
     */
    protected void updateManyDataConditionRelationshipHandle(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ClassModel modifyClassModel, ModifyDescribe describe, final ChangedVo vo, Criteria criteria, final Map<String, Object> conditionValMap, final Map<String, Object> conditionLogMap, final Column relyColumn) {
        if (criteria == null) throw new IllegalArgumentException("criteria must not null");
        if (describe == null) throw new IllegalArgumentException("describe must not null");
        List<Boolean> oneList = complexAnalysis.getOneList();
        List<String> propertyList = complexAnalysis.getPropertyList();

        //动态封装条件
        Stack<Criteria> stack = new Stack<Criteria>();
        stack.push(criteria);

        List<ElemMatchWait> elemMatchList = new ArrayList<ElemMatchWait>(); //存放需要设置关系的criteria数据
        StringBuilder propertyBuilder = new StringBuilder();  //最终是列表时则长度为0

        for (int i = 0; i < propertyList.size(); i ++) {
            String property = propertyList.get(i);
            Boolean one = oneList.get(i);
            propertyBuilder.append(property);
            if (!one) {
                //当为列表时取当前最新的criteria
                Criteria parent = stack.pop();

                Criteria child = new Criteria();
                stack.push(child);

                //等到处理完毕后再进行设置关系,将使用elemMatch
                ElemMatchWait matchWait = new ElemMatchWait();
                matchWait.setParent(parent);
                matchWait.setName(propertyBuilder.toString()); //此时属性名称
                matchWait.setChild(child);
                elemMatchList.add(matchWait);

                propertyBuilder.setLength(0);
            } else {
                propertyBuilder.append(".");
            }
        }
        final boolean lastIsOne = oneList.get(oneList.size() - 1);  //最后的属性值是否为one
        final String lastProperty = propertyList.get(propertyList.size() - 1);

        final Criteria lastCriteria = stack.pop();

        final Map<String, String> conditionPropertyMap = new HashMap<String, String>(16);

        configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition condition, int position, String targetProperty, Object targetPropertyVal) {
                String property = condition.getColumn().getProperty();
                conditionValMap.put(property, targetPropertyVal);//存放此对象条件属性值的信息

                String currentProperty;
                if (lastIsOne) { //说明是以one结尾的对象
                    currentProperty = lastProperty + "." + property;
                } else { //以many结尾的对象
                    currentProperty = property;
                }
                lastCriteria.and(currentProperty).is(targetPropertyVal);

                conditionPropertyMap.put(currentProperty, targetProperty);

            }
        });

        if (relyColumn != null) {
            //有依赖列时,此时为ModifyRelyDescribe
            if (!(describe instanceof ModifyRelyDescribe)) {
                throw new IllegalArgumentException("has rely column must be ModifyRelyDescribe instance");
            }

            String relyProperty = relyColumn.getProperty();
            if (lastIsOne) relyProperty = lastProperty + "." + relyProperty;

            modelTypeCriteriaProcessing((ModifyRelyDescribe)describe, lastCriteria, relyProperty);
        }

        if (!elemMatchList.isEmpty()) {  //设置criteria关系
            for (ElemMatchWait wait : elemMatchList) {
                Criteria parent = wait.getParent();
                String name = wait.getName();
                Criteria child = wait.getChild();
                parent.and(name).elemMatch(child);  //设置属性名称
            }
        }

        updateManyDataConditionRelationshipLogHandle(criteria, conditionPropertyMap, conditionLogMap);
    }

    protected void updateManyDataConditionRelationshipLogHandle(Criteria criteria, Map<String, String> conditionPropertyMap, Map<String, Object> conditionLogMap) {
        if (isLoggerSupport()) {
            //处理log条件map
            conditionLogMap.putAll(getParsedCriteriaMark(criteria, conditionPropertyMap));
        }
    }


    protected Map<String, Object> getParsedCriteriaMark(Criteria criteria, Map<String, String> conditionPropertyMap) {
        Map criteriaObjectMap = rdtResolver.deepClone(getCriteriaToMap(criteria));

        if (criteria.equals(conditionPropertyMap)) {
            logger.warn("rdt parse criteria result has problem, the deep clone object must be not equals.");
            return new HashMap<String, Object>(16);
        }

        Map<String, Object> currentConditionLogMap = new LinkedHashMap<String, Object>(criteriaObjectMap);

        if (getIsLogDetail()) {
            parseCriteriaMark(currentConditionLogMap, false, conditionPropertyMap);
        }

        return currentConditionLogMap;
    }



    protected void parseCriteriaMark(Map<String, Object> valMap, boolean isElemMatch, Map<String, String> conditionPropertyMap) {
        if (valMap != null && !valMap.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iterator = valMap.entrySet().iterator();
            Map<String, Object> newMap = new LinkedHashMap<String, Object>(16);

            while (iterator.hasNext()) {
                Map.Entry<String, Object> currentEntry = iterator.next();
                String currentKey = currentEntry.getKey();
                Object currentValue = currentEntry.getValue();

                if (isElemMatch) {
                    String targetProperty = conditionPropertyMap.get(currentKey);
                    if (targetProperty != null) {
                        newMap.put(getPropertyMark(currentKey, targetProperty), valMap.get(currentKey));
                        iterator.remove();
                    }
                }
                if (currentValue instanceof Map) {
                    parseCriteriaMark((Map<String, Object>)currentValue, "$elemMatch".equals(currentKey), conditionPropertyMap);
                }
            }
            valMap.putAll(newMap);
        }

    }



    protected void updateManyData(List<Object> dataList, final ClassModel complexClassModel, final ClassModel modifyClassModel, final ComplexAnalysis complexAnalysis, final Map<String, Object> conditionValMap, final Map<String, Object> updateValMap, final boolean saveAll, final ModifyRelyDescribe describe, final Column relyColumn) {
        Class modifyClass = modifyClassModel.getCurrentClass();

        if (dataList != null && !dataList.isEmpty()) {
            List<Boolean> oneList = complexAnalysis.getOneList();
            boolean lastIsOne = oneList.get(oneList.size() - 1);

            for (Object data : dataList) {
                final Update currentUpdate = new Update();  //使用update语句更新

                final Criteria currentCriteria = new Criteria();

                if (!saveAll) {
                    String primaryId = modifyClassModel.getPrimaryId();
                    //加入持久化类的id条件
                    currentCriteria.and(primaryId).is(rdtResolver.getPropertyValue(data, primaryId));
                }

                String accessProperty = complexAnalysis.getPrefix();//访问的属性

                if (!lastIsOne) accessProperty += ".*";  //用于访问many的子对象

                DataSupport.dispose(data, accessProperty, new DataSupport.Callback() {
                    @Override
                    public void execute(String resultProperty, Object result) {
                        if (result != null) {
                            boolean flag = true;  //当前数据是否满足要求

                            if (describe != null) {  //存在依赖时
                                String relyProperty = relyColumn.getProperty();
                                Object relyPropertyVal = rdtResolver.getPropertyValue(result, relyProperty);
                                flag = configuration.isMatchedType(describe, relyPropertyVal);
                            }

                            if (flag) {
                                for (String currentProperty : conditionValMap.keySet()) {
                                    Object conditionVal = conditionValMap.get(currentProperty);
                                    Object currentVal = rdtResolver.getPropertyValue(result, currentProperty);

                                    if (!configuration.isMatchedValue(currentVal, conditionVal)) {
                                        flag = false;
                                        break;
                                    }

                                }
                            }

                            if (flag) {
                                String usePropertyPrefix = resultProperty.replace("[", "").replace("]", "") + "."; //去除[]
                                for (String currentProperty : updateValMap.keySet()) {
                                    Object updateVal = updateValMap.get(currentProperty);
                                    //设置对应的属性值
                                    if (saveAll) {
                                        rdtResolver.setPropertyValue(result, currentProperty, updateVal);
                                    } else {
                                        currentUpdate.set(usePropertyPrefix + currentProperty, updateVal);
                                    }
                                }

                                if (!saveAll) {
                                    //加入result类的id标识,避免更新出错
                                    String primaryId = complexClassModel.getPrimaryId();
                                    if (StringUtils.isNotEmpty(primaryId)) {
                                        currentCriteria.and(usePropertyPrefix + primaryId).is(rdtResolver.getPropertyValue(result, primaryId));
                                    }
                                }
                            }
                        }
                    }
                });

                if (!saveAll) {
                    updateMulti(currentCriteria, currentUpdate, modifyClass);
                }
            }

            if (saveAll) this.saveAll(dataList);
        }
    }



    public static class ElemMatchWait {
        private Criteria parent;
        private String name;
        private Criteria child;

        public Criteria getParent() {
            return parent;
        }

        public void setParent(Criteria parent) {
            this.parent = parent;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Criteria getChild() {
            return child;
        }

        public void setChild(Criteria child) {
            this.child = child;
        }
    }

}
