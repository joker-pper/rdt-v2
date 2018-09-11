package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.model.*;
import com.devloper.joker.redundant.resolver.RdtOperationResolver;
import com.devloper.joker.redundant.support.DataSupport;
import com.devloper.joker.redundant.support.Prototype;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.lang.reflect.Field;
import java.util.*;

public abstract class MongoRdtOperation extends RdtOperationResolver {

    protected MongoTemplate mongoTemplate;

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Object findById(Class entityClass, Object id) {
        return mongoTemplate.findById(id, entityClass);
    }

    @Override
    protected Log updateModifyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo, Map<String, Object> conditionValMap, Map<String, Object> updateValMap) {
        Criteria criteria = new Criteria();
        Update update = new Update();

        for (String property: conditionValMap.keySet()) {
            criteria.and(property).is(conditionValMap.get(property));
        }
        for (String property: updateValMap.keySet()) {
            update.set(property, updateValMap.get(property));
        }
        Query query = Query.query(criteria);
        mongoTemplate.updateMulti(query, update, modifyClassModel.getCurrentClass());

        return null;
    }


    @Override
    protected Log updateModifyRelyDescribeSimpleImpl(ClassModel classModel, ClassModel modifyClassModel, ChangedVo vo, Map<String, Object> conditionValMap, Map<String, Object> updateValMap, Column relyCoumn, int group, ModifyRelyDescribe describe) {
        Criteria criteria = new Criteria();

        Update update = new Update();
        String relyProperty = relyCoumn.getProperty();

        List<Object> unknowNotExistValList = describe.getUnknowNotExistValList();  //非这些值时为classModel
        List<Object> valList = describe.getValList();

        if (!valList.isEmpty()) {
            if (unknowNotExistValList.isEmpty()) criteria.and(relyProperty).in(valList);
            else { //满足在valList 或 非unknowNotExistValList时
                criteria.orOperator(Criteria.where(relyProperty).in(valList), Criteria.where(relyProperty).nin(unknowNotExistValList));
            }
        } else {
            if (!unknowNotExistValList.isEmpty()) criteria.and(relyProperty).nin(unknowNotExistValList);
        }

        Map typeConditionMap = criteria.getCriteriaObject().toMap(); //获取为当前classModel的条件约束

        for (String property: conditionValMap.keySet()) {
            criteria.and(property).is(conditionValMap.get(property));
        }

        Map<String, Object> logConditionMap = new LinkedHashMap<String, Object>(16);

        //将条件放入logConditionMap中作为log查询参数输出
        if (typeConditionMap != null && !typeConditionMap.isEmpty()) {
            for (Object key : typeConditionMap.keySet()) {
                logConditionMap.put(key.toString(), typeConditionMap.get(key));
            }
        }

        for (String property: updateValMap.keySet()) {
            update.set(property, updateValMap.get(property));
        }
        Query query = Query.query(criteria);
        mongoTemplate.updateMulti(query, update, modifyClassModel.getCurrentClass());

        return new Log(logConditionMap, null);
    }

    @Override
    protected ClassModel getModifyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis) {
        return getClassModel(complexAnalysis.getRootClass());
    }

    @Override
    protected String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition) {
        return complexAnalysis.getPrefix() + "." + modifyCondition.getProperty();
    }

    @Override
    protected String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, Column column) {
        return complexAnalysis.getPrefix() + "." + column.getProperty();
    }

    @Override
    protected Log updateModifyDescribeOneImpl(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo, Map<String, Object> conditionValMap, Map<String, Object> updateValMap) {
        Criteria criteria = new Criteria();
        Update update = new Update();

        for (String property: conditionValMap.keySet()) {
            criteria.and(property).is(conditionValMap.get(property));
        }
        for (String property: updateValMap.keySet()) {
            update.set(property, updateValMap.get(property));
        }
        Query query = Query.query(criteria);
        mongoTemplate.updateMulti(query, update, modifyClassModel.getCurrentClass());
        return null;
    }


    @Override
    protected ClassModel getModifyRelyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis) {
        return getModifyDescribeOneModifyClassModel(complexClassModel, complexAnalysis);
    }

    @Override
    protected String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition) {
        return getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
    }

    @Override
    protected String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, Column column) {
        return getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, column);
    }

    @Override
    protected Log updateModifyRelyDescribeOneImpl(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ClassModel modifyClassModel, ModifyRelyDescribe describe, ChangedVo vo, Map<String, Object> conditionValMap, Map<String, Object> updateValMap, Column relyCoumn, int group) {
        Criteria criteria = new Criteria();

        Update update = new Update();
        String relyProperty = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, relyCoumn);

        List<Object> unknowNotExistValList = describe.getUnknowNotExistValList();
        List<Object> valList = describe.getValList();

        if (!valList.isEmpty()) {
            if (unknowNotExistValList.isEmpty()) criteria.and(relyProperty).in(valList);
            else { //满足在valList 或 非unknowNotExistValList时
                criteria.orOperator(Criteria.where(relyProperty).in(valList), Criteria.where(relyProperty).nin(unknowNotExistValList));
            }
        } else {
            if (!unknowNotExistValList.isEmpty()) criteria.and(relyProperty).nin(unknowNotExistValList);
        }

        Map typeConditionMap = criteria.getCriteriaObject().toMap(); //获取为当前classModel的条件约束

        for (String property: conditionValMap.keySet()) {
            criteria.and(property).is(conditionValMap.get(property));
        }

        Map<String, Object> logConditionMap = new LinkedHashMap<String, Object>(16);
        //将条件放入logConditionMap中作为log查询参数输出
        if (typeConditionMap != null && !typeConditionMap.isEmpty()) {
            for (Object key : typeConditionMap.keySet()) {
                logConditionMap.put(key.toString(), typeConditionMap.get(key));
            }
        }

        for (String property: updateValMap.keySet()) {
            update.set(property, updateValMap.get(property));
        }
        Query query = Query.query(criteria);
        mongoTemplate.updateMulti(query, update, modifyClassModel.getCurrentClass());

        return new Log(logConditionMap, null);
    }


    @Override
    protected ClassModel getModifyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis) {
        return getModifyDescribeOneModifyClassModel(complexClassModel, complexAnalysis);
    }


    @Override
    protected Log updateModifyDescribeManyImpl(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ClassModel modifyClassModel, ModifyDescribe describe, ChangedVo vo) {
        Criteria criteria = new Criteria();

        final Map<String, Object> conditionValMap = new HashMap<String, Object>(16);  //存放比较属性值的map
        final Map<String, Object> updateValMap = new HashMap<String, Object>(16);  //存放要更新属性值的map
        Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16); //查询条件log数据

        //统一处理
        updateManyDataConditionHandle(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, criteria, conditionValMap, conditionLogMap,null);

        final String analysisPrefix = complexAnalysis.getPrefix() + ".";
        final String symbol = super.getSymbol();
        final boolean logDetail = super.getLogDetail();


        final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

        rdtSupport.doModifyColumnHandle(vo, describe, new RdtSupport.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getProperty();
                updateValMap.put(property, targetPropertyVal);
                if (logDetail) {
                    updateLogMap.put(analysisPrefix + property + symbol + targetProperty, targetPropertyVal);
                } else {
                    updateLogMap.put(analysisPrefix + property, targetPropertyVal);
                }

            }
        });

        Class modifyClass = modifyClassModel.getCurrentClass();

        Query query = new Query(criteria);

        long pageSize = properties.getPageSize();
        if (pageSize == -1) { //直接查询全部然后处理
            List<Object> dataList = mongoTemplate.find(query, modifyClass);
            updateManyData(dataList, complexClassModel, modifyClassModel, complexAnalysis, conditionValMap, updateValMap, properties.getComplexBySaveAll(), null, null);
        } else {
            long total = mongoTemplate.count(query, modifyClass);
            if (total > 0) {
                long totalPage = (total % pageSize == 0) ? total / pageSize : total / pageSize + 1;
                for (int i = 0; i < totalPage; i ++) {
                    Pageable pageable = getPageable(i, pageSize);
                    if (pageable != null) query.with(pageable);
                    List<Object> dataList = mongoTemplate.find(query, modifyClass);
                    updateManyData(dataList, complexClassModel, modifyClassModel, complexAnalysis, conditionValMap, updateValMap, properties.getComplexBySaveAll(), null, null);
                    if (pageable == null) {
                        logger.warn("getPageable(long page, long size) return null, so have no by limit page update data");
                        break;
                    }
                }
            }
        }

        return new Log(conditionLogMap, updateLogMap);
    }

    protected abstract Pageable getPageable(long page, long size);

    @Override
    protected ClassModel getModifyRelyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis) {
        return getModifyDescribeManyModifyClassModel(complexClassModel, complexAnalysis);
    }

    @Override
    protected Log updateModifyRelyDescribeManyImpl(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ClassModel modifyClassModel, ModifyRelyDescribe describe, ChangedVo vo, Column relyColumn, int group) {
        Criteria criteria = new Criteria();

        final Map<String, Object> conditionValMap = new HashMap<String, Object>(16);  //存放比较属性值的map
        final Map<String, Object> updateValMap = new HashMap<String, Object>(16);  //存放要更新属性值的map
        final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);
        Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16); //查询条件log数据

        updateManyDataConditionHandle(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, criteria, conditionValMap, conditionLogMap, relyColumn);

        final String analysisPrefix = complexAnalysis.getPrefix() + ".";
        final String symbol = super.getSymbol();
        final boolean logDetail = super.getLogDetail();

        rdtSupport.doModifyColumnHandle(vo, describe, new RdtSupport.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getProperty();
                updateValMap.put(property, targetPropertyVal);
                if (logDetail) {
                    updateLogMap.put(analysisPrefix + property + symbol + targetProperty, targetPropertyVal);
                } else {
                    updateLogMap.put(analysisPrefix + property, targetPropertyVal);
                }
            }
        });

        Class modifyClass = modifyClassModel.getCurrentClass();

        Query query = new Query(criteria);

        long pageSize = properties.getPageSize();
        if (pageSize == -1) { //直接查询全部然后处理
            List<Object> dataList = mongoTemplate.find(query, modifyClass);
            updateManyData(dataList, complexClassModel, modifyClassModel, complexAnalysis, conditionValMap, updateValMap, properties.getComplexBySaveAll(), describe, relyColumn);
        } else {
            long total = mongoTemplate.count(query, modifyClass);
            if (total > 0) {
                long totalPage = (total % pageSize == 0) ? total / pageSize : total / pageSize + 1;
                for (int i = 0; i < totalPage; i ++) {
                    Pageable pageable = getPageable(i, pageSize);
                    if (pageable != null) query.with(pageable);
                    List<Object> dataList = mongoTemplate.find(query, modifyClass);
                    updateManyData(dataList, complexClassModel, modifyClassModel, complexAnalysis, conditionValMap, updateValMap, properties.getComplexBySaveAll(), describe, relyColumn);
                    if (pageable == null) {
                        logger.warn("getPageable(long page, long size) return null, so have no by limit page update data");
                        break;
                    }
                }
            }
        }
        return new Log(conditionLogMap, updateLogMap);
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
    protected void updateManyDataConditionHandle(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ClassModel modifyClassModel, Object describe, final ChangedVo vo, Criteria criteria,  final Map<String, Object> conditionValMap, final Map<String, Object> conditionLogMap, final Column relyColumn) {
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

        final Map<String, String> conditionPropertyMap = new HashMap<String, String>();


        rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition condition, String targetProperty, Object targetPropertyVal) {
                String property = condition.getProperty();
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

        if (relyColumn != null) { //有依赖列时,此时为ModifyRelyDescribe

            List<Object> unknowNotExistValList;
            List<Object> valList;

            if (describe instanceof ModifyRelyDescribe) {
                unknowNotExistValList = ((ModifyRelyDescribe) describe).getUnknowNotExistValList();
                valList = ((ModifyRelyDescribe) describe).getValList();
            } else throw new IllegalArgumentException("has rely column must be ModifyRelyDescribe instance");

            String relyProperty = relyColumn.getProperty();

            if (lastIsOne) relyProperty = lastProperty + "." + relyProperty;

            if (!valList.isEmpty()) {
                if (unknowNotExistValList.isEmpty()) lastCriteria.and(relyProperty).in(valList);
                else { //满足在valList 或 非unknowNotExistValList时
                    lastCriteria.orOperator(Criteria.where(relyProperty).in(valList), Criteria.where(relyProperty).nin(unknowNotExistValList));
                }
            } else {
                if (!unknowNotExistValList.isEmpty()) lastCriteria.and(relyProperty).nin(unknowNotExistValList);
            }
        }

        if (!elemMatchList.isEmpty()) {  //设置criteria关系
            for (ElemMatchWait wait : elemMatchList) {
                Criteria parent = wait.getParent();
                String name = wait.getName();
                Criteria child = wait.getChild();
                parent.and(name).elemMatch(child);  //设置属性名称
            }
        }

        //处理log条件map
        Map criteriaObjectMap = Prototype.of(criteria.getCriteriaObject().toMap()).deepClone().getModel();
        Map<String, Object> currentConditionLogMap = new LinkedHashMap<String, Object>(criteriaObjectMap);
        if (!super.getLogDetail()) {
            conditionLogMap.putAll(currentConditionLogMap);
        } else {
            if (!currentConditionLogMap.isEmpty()) {
                for (String key : currentConditionLogMap.keySet()) {
                    Object val = currentConditionLogMap.get(key);
                    if (val instanceof Map) {
                        handleComplexConditionLogMap((Map) val, key, conditionPropertyMap);
                    }
                    conditionLogMap.put(key, val);
                }
            }
        }
    }


    protected void handleComplexConditionLogMap(Map<String, Object> valMap, String parentKey, Map<String, String> conditionPropertyMap) {
        if (valMap != null) {
            if ("$elemMatch".equals(parentKey)) {
                Iterator<Map.Entry<String, Object>> iterator = valMap.entrySet().iterator();
                Map<String, Object> newMap = new LinkedHashMap<String, Object>();
                while(iterator.hasNext()){
                    Map.Entry<String, Object> currentEntry = iterator.next();
                    String currentKey = currentEntry.getKey();
                    String targetProperty = conditionPropertyMap.get(currentKey);
                    if (targetProperty != null) {
                        newMap.put(currentKey + super.getSymbol() + targetProperty, valMap.get(currentKey));
                        iterator.remove();
                    }
                }
                valMap.putAll(newMap);
            } else {
                for (String key : valMap.keySet()) {
                    Object val = valMap.get(key);
                    if (val instanceof Map) {
                        handleComplexConditionLogMap((Map<String, Object>) val, key, conditionPropertyMap);
                    }
                }
            }
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
                    currentCriteria.and(primaryId).is(rdtResolver.getPropertyValue(data, primaryId)); //id条件
                }

                String accessProperty = complexAnalysis.getPrefix();//访问的属性

                if (!lastIsOne) accessProperty += ".*";  //用于访问many的子对象

                DataSupport.dispose(data, accessProperty, new DataSupport.Callback() {
                    @Override
                    public void execute(String resultProperty, Object result) {
                        if (result != null) {
                            boolean flag = true;  //当前数据是否满足要求

                            if (describe != null) {  //存在依赖时
                                List<Object> unknowNotExistValList = describe.getUnknowNotExistValList();
                                List<Object> valList = describe.getValList();

                                String relyProperty = relyColumn.getProperty();
                                Object relyPropertyVal = rdtResolver.getPropertyValue(result, relyProperty);

                                if (!valList.isEmpty()) {
                                    if (unknowNotExistValList.isEmpty()) {
                                        flag = valList.contains(relyPropertyVal);
                                    } else { //满足在valList 或 非unknowNotExistValList时
                                        flag = valList.contains(relyPropertyVal) || !unknowNotExistValList.contains(relyProperty);
                                    }
                                } else {
                                    if (!unknowNotExistValList.isEmpty()) flag = !unknowNotExistValList.contains(relyProperty);
                                }
                            }


                            if (flag) {
                                for (String currentProperty : conditionValMap.keySet()) {
                                    Object conditionVal = conditionValMap.get(currentProperty);
                                    Object currentVal = rdtResolver.getPropertyValue(result, currentProperty);
                                    if ((currentVal != null && !currentVal.equals(conditionVal)) || (currentVal != null && conditionVal == null)) {
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
                                    //加入标识,避免更新出错
                                    Map<String, Field> propertyFieldMap = complexClassModel.getPropertyFieldMap();
                                    if (propertyFieldMap.containsKey("id")) {
                                        currentCriteria.and(usePropertyPrefix + "id").is(rdtResolver.getPropertyValue(result, "id"));
                                    }
                                }
                            }
                        }
                    }
                });

                if (!saveAll) {
                    Query currentQuery = Query.query(currentCriteria);
                    mongoTemplate.updateMulti(currentQuery, currentUpdate, modifyClass);
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
