package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.fill.FillOneKeyModel;
import com.devloper.joker.redundant.fill.FillRSModel;
import com.devloper.joker.redundant.fill.RdtFillBuilder;
import com.devloper.joker.redundant.model.*;
import com.devloper.joker.redundant.resolver.RdtResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

public abstract class AbstractOperation {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RdtProperties properties;

    protected RdtResolver rdtResolver;

    protected RdtSupport rdtSupport;

    protected String symbol = "->";

    protected Boolean logDetail = true;

    protected RdtFillBuilder fillBuilder;


    public AbstractOperation(RdtSupport rdtSupport) {
        this.rdtSupport = rdtSupport;
        this.rdtResolver = rdtSupport.getRdtResolver();
        this.properties = rdtSupport.getProperties();
        this.fillBuilder = RdtFillBuilder.of(rdtSupport);
    }

    public RdtResolver getRdtResolver() {
        return rdtResolver;
    }

    public abstract <T> T findById(Class<T> entityClass, Object id);

    protected abstract <T> List<T> findByIdIn(Class<T> entityClass, String idKey, Collection<Object> ids);

    public <T> List<T> findByIdIn(Class<T> entityClass, Collection<Object> ids) {
        if (ids == null || ids.size() == 0) {
            throw new IllegalArgumentException("ids must be not empty.");
        }
        if (ids.size() == 1) {
            T result = findById(entityClass, ids.iterator().next());
            List<T> dataList = new ArrayList<T>();
            if (result != null) {
                dataList.add(result);
            }
            return dataList;
        }
        return findByIdIn(entityClass, getPrimaryId(entityClass), ids);
    }

    protected <T> T save(T entity) {
        if (entity == null) {
            throw new NullPointerException("entity must be not null.");
        }
        return save(entity, (Class<T>)entity.getClass());
    }

    protected abstract <T> T save(T entity, Class<T> entityClass);

    protected  <T> Collection<T> saveAll(Collection<T> data) {
        if (data == null || data.size() == 0) {
            throw new IllegalArgumentException("data must be not empty.");
        }
        return saveAll(data, (Class<T>) data.iterator().next().getClass());
    }

    protected abstract <T> Collection<T> saveAll(Collection<T> data, Class<T> entityClass);

    /**
     * 获取以key值为key的map数据
     * @param data
     * @param key
     * @param <T>
     * @return
     */
    protected <T> Map<Object, T> getKeyMap(Collection<T> data, String key) {
        return rdtSupport.getKeyMap(data, key);
    }


    public ClassModel getClassModel(Class entityClass) {
        return rdtSupport.getClassModel(entityClass);
    }

    public String getPrimaryId(Class entityClass) {
        return rdtSupport.getPrimaryId(entityClass);
    }

    public Map<Object, Object> getBeforeData(Object entity) {
        return getBeforeData(entity, true);
    }


    /**
     * 获取base class之前的数据
     * @param entity
     * @param check 如果为true时仅在use property时查询
     * @return key: id, val: domain
     */
    public Map<Object, Object> getBeforeData(Object entity, boolean check) {
        Map<Object, Object> result = new HashMap<Object, Object>(16);
        if (entity != null) {
            entity = parseEntityData(entity);
            Class entityClass = null;
            boolean flag = false;
            String idKey = null;
            ClassModel classModel = null;

            for (Object current : (Collection) entity) {
                if (current == null) {
                    continue;
                }
                if (entityClass == null) {
                    entityClass = current.getClass();
                } else {
                    if (!entityClass.equals(current.getClass())) {
                        throw new IllegalArgumentException("the domain object args must be the same class type");
                    }
                }
                //判断class是否为base class
                classModel = classModel == null ? getClassModel(entityClass) : classModel;
                flag = classModel != null ? classModel.getBaseClass() : flag;
                //不是base class时停止
                if (!flag) {
                    break;
                }
                //获取idKey
                idKey = idKey == null ? classModel.getPrimaryId() : idKey;
                Object idKeyVal = rdtResolver.getPropertyValue(current, idKey);
                result.put(idKeyVal, null);
            }

            if (flag) {
                if (check && classModel.getUsedPropertySet().isEmpty()) {
                    logger.debug("{} has no used property, and use check so not to load before data", classModel.getClassName());
                } else {
                    //查找之前的数据
                    Collection<Object> dataList = null;
                    Set<Object> resultKeys = result.keySet();
                    if (!resultKeys.isEmpty()) {
                        if (resultKeys.size() > 1) {
                            dataList = findByIdIn(entityClass, idKey, result.keySet());
                        } else {
                            dataList = new ArrayList<Object>();
                            Object modelData = findById(entityClass, resultKeys.iterator().next());
                            if (modelData != null) {
                                dataList.add(modelData);
                            }
                        }
                    }
                    if (dataList != null) {
                        for (Object data : dataList) {
                            Object idKeyVal = rdtResolver.getPropertyValue(data, idKey);
                            result.put(idKeyVal, data);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 用于转换传入的实体数据参数为集合数据
     *
     * @param entity data | collection | array[data...]  array[collection]
     * @return
     */
    protected Collection<Object> parseEntityData(Object entity) {
        if (entity != null) {
            if (entity instanceof Collection) {
                Iterator<Object> iterator = ((Collection) entity).iterator();
                while (iterator.hasNext()) {
                    if (iterator.next() == null) iterator.remove();
                }
            } else {
                if (entity.getClass().isArray()) {
                    int size = Array.getLength(entity);
                    List<Object> resultTemp = new ArrayList(size);

                    for (int i = 0; i < size; ++i) {
                        Object data = Array.get(entity, i);
                        if (data != null) {
                            if (size == 1) {
                                if (data instanceof Collection) resultTemp.addAll((Collection) data);
                            }

                            if (!(data instanceof Collection)) {
                                resultTemp.add(data);
                            }
                        }
                    }

                    entity = resultTemp;
                } else {
                    List<Object> dataList = new ArrayList<Object>();
                    dataList.add(entity);
                    entity = dataList;
                }
            }
        } else entity = new ArrayList<Object>();
        return (Collection<Object>) entity;
    }

    /**
     * 更新当前数据所有相关冗余字段数据
     * @param entity
     */
    public void updateRelevant(Object entity) throws Exception {
        Collection<Object> dataList = parseEntityData(entity);
        for (Object data : dataList) {
            updateMulti(data);
        }
    }

    /**
     * 通过更新数据前的数据以及当前数据去更新所有相关冗余字段数据
     * @param entity
     * @param beforeKeyDataMap
     */
    public void updateRelevant(Object entity, Map<Object, Object> beforeKeyDataMap) {
        Collection<Object> dataList = parseEntityData(entity);
        String idKey = null;

        boolean isLoad = beforeKeyDataMap != null && !beforeKeyDataMap.isEmpty();
        for (Object data : dataList) {
            if (data == null) {
                continue;
            }
            Object before = null;
            if (isLoad) {
                if (idKey == null) {
                    Class dataClass = data.getClass();
                    idKey = getPrimaryId(dataClass);
                }
                Object idVal = rdtResolver.getPropertyValue(data, idKey);
                before = beforeKeyDataMap.get(idVal);
            }
            updateMulti(data, before);
        }
    }


    /**
     * 更新当前对象的所有相关冗余字段数据
     * @param current
     */
    public void updateMulti(Object current) {
        Object before = null;
        try {
            if (current != null) before = current.getClass().newInstance();
        } catch (Exception e) {
            logger.warn("unable to create instance about {}, will continue update", current.getClass().getName());
        }
        updateMulti(current, before, true);
    }


    /**
     * 根据当前对象与之前对象数据对比进行更新相关字段
     * @param current
     * @param before
     */
    public void updateMulti(Object current, Object before) {
        updateMulti(current, before, false);
    }

    private void updateMulti(Object current, Object before, boolean allUsedPropertysChange) {
        if (current == null) return;
        //获取当前entity的class
        Class entityClass = current.getClass();
        ClassModel classModel = getClassModel(entityClass);
        if (classModel == null) logger.warn("rdt not contains class {} will continue", entityClass.getName());
        else {
            String entityClassName = classModel.getClassName();
            if (!classModel.getBaseClass()) {
                logger.warn("{} is not base class, can't to modify", entityClassName);
            } else {
                String idKey = classModel.getPrimaryId();
                Object idKeyVal = rdtResolver.getPropertyValue(current, idKey);
                Set<String> usedPropertys = classModel.getUsedPropertySet();
                if (usedPropertys.isEmpty()) {
                    logger.debug("{} 【{}】has no used property, continue modify", entityClassName, idKeyVal);
                } else {
                    if (before == null) {
                        logger.debug("{} 【{}】not exist before data, continue modify", entityClassName, idKeyVal);
                    } else {
                        if (!before.getClass().equals(entityClass)) {
                            logger.warn("{} 【{}】 before data type is {}, continue modify", entityClassName, idKeyVal, before.getClass().getName());
                        } else {

                            //获取当前实体所使用的字段中发生改变的数据
                            ChangedVo changedVo = new ChangedVo();
                            changedVo.setBefore(before);
                            changedVo.setCurrent(current);
                            changedVo.setPrimaryId(idKey);
                            changedVo.setPrimaryIdVal(idKeyVal);
                            Map<String, Object> changedPropertyValMap = new HashMap<String, Object>(16);
                            for (String property : usedPropertys) {
                                Object currentVal = rdtResolver.getPropertyValue(current, property);
                                Object beforeVal = rdtResolver.getPropertyValue(before, property);
                                boolean changed = false;
                                if (!allUsedPropertysChange) {
                                    if (currentVal != null) {
                                        changed = !currentVal.equals(beforeVal);
                                    } else {
                                        if (beforeVal != null) {
                                            changed = !beforeVal.equals(currentVal);
                                        }
                                    }
                                }
                                changed = !changed ? allUsedPropertysChange : changed;

                                if (changed) {
                                    //添加该字段值为changed property用于更新相关值
                                    changedVo.addChangedProperty(property);
                                    changedPropertyValMap.put(property, currentVal);
                                }
                                changedVo.setVal(property, currentVal, beforeVal);
                            }

                            List<String> changedPropertys = changedVo.getChangedPropertys();
                            if (!changedPropertys.isEmpty()) {
                                List<String> propertys = new ArrayList<String>(classModel.getPropertyColumnMap().keySet());
                                propertys.removeAll(changedPropertys);
                                for (String property : propertys) {
                                    Object currentVal = rdtResolver.getPropertyValue(current, property);
                                    Object beforeVal = rdtResolver.getPropertyValue(before, property);
                                    changedVo.setVal(property, currentVal, beforeVal);
                                }
                                logger.info("{} 【{}={}】changed propertys {} will to modify", entityClassName, idKey, idKeyVal, changedPropertyValMap);
                                updateMultiCore(classModel, changedVo);
                            } else {
                                logger.debug("{} 【{}={}】has no changed propertys, continue modify", entityClassName, idKey, idKeyVal);
                            }
                        }
                    }
                }

            }
        }

    }

    protected void updateMultiCore(ClassModel classModel, ChangedVo changedVo) {
        updateModifyDescribeSimple(classModel, changedVo);
        updateModifyRelyDescribeSimple(classModel, changedVo);
    }

    protected void handlerThrowException(Exception e) {
        if (Boolean.TRUE.equals(properties.getThrowException())) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new IllegalStateException(e);
            }
        }
    }

    protected void updateModifyDescribeSimple(final ClassModel classModel, final ChangedVo vo) {
        Set<Class> changedRelaxedClassSet = classModel.getChangedRelaxedClassSet();
        final List<String> changedPropertys = vo.getChangedPropertys();
        for (Class relaxedClass : changedRelaxedClassSet) {
            ClassModel currentClassModel = properties.getClassModel(relaxedClass); //要修改的classModel
            rdtSupport.doModifyDescribeHandle(classModel, currentClassModel, new RdtSupport.ModifyDescribeCallBack() {
                @Override
                public void execute(ClassModel classModel, ClassModel currentClassModel, ModifyDescribe describe) {
                    ModifyDescribe currentDescribe = rdtSupport.getModifyDescribe(describe, changedPropertys); //获取当前的修改条件
                    if (currentDescribe != null) {
                        updateModifyDescribeSimple(classModel, currentClassModel, currentDescribe, vo);
                    }
                }
            });
        }
    }


    /**
     * 处理当前保存实体值变化时所要修改相关实体类的字段数据的业务逻辑
     *
     * @param classModel       触发更新的实体
     * @param modifyClassModel 当前所要更新的实体
     * @param describe         对应的修改信息
     * @param vo
     */
    protected void updateModifyDescribeSimple(final ClassModel classModel, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo) {
        final Map<String, Object> conditionMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

        final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

        rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, String targetProperty, Object targetPropertyVal) {
                String property = modifyCondition.getColumn().getProperty();
                conditionDataMap.put(property, targetPropertyVal);
                if (logDetail) {
                    conditionMap.put(property + symbol + targetProperty, targetPropertyVal);
                } else {
                    conditionMap.put(property, targetPropertyVal);
                }
            }
        });


        rdtSupport.doModifyColumnHandle(vo, describe, new RdtSupport.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, String targetProperty, Object targetPropertyVal) {
                String property = modifyColumn.getColumn().getProperty();
                updateDataMap.put(property, targetPropertyVal);
                if (logDetail) {
                    updateLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                } else {
                    updateLogMap.put(property, targetPropertyVal);
                }
            }
        });


        try {
            updateModifyDescribeSimpleImpl(classModel, modifyClassModel, describe, vo, conditionDataMap, updateDataMap);
            logger.debug("{} modify about {}【{}={}】data, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    describe.getIndex(), rdtResolver.toJson(conditionMap), rdtResolver.toJson(updateLogMap));
        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    describe.getIndex(), rdtResolver.toJson(conditionMap), rdtResolver.toJson(updateLogMap));
            logger.warn("rdt update field has error", e);
            handlerThrowException(e);
        }
    }

    /**
     * 处理当前保存实体值变化时所要修改相关实体类的字段数据的业务逻辑的实现
     * @param classModel 触发更新的实体对象信息
     * @param modifyClassModel 要修改的实体对象信息
     * @param describe
     * @param vo
     * @param conditionValMap 条件约束数据
     * @param updateValMap    更新字段数据
     */
    protected abstract void updateModifyDescribeSimpleImpl(final ClassModel classModel, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo, final Map<String, Object> conditionValMap, final Map<String, Object> updateValMap);


    protected void updateModifyRelyDescribeSimple(final ClassModel classModel, final ChangedVo vo) {
        Set<Class> changedRelaxedClassSet = classModel.getChangedRelaxedClassSet();

        final List<String> changedPropertys = vo.getChangedPropertys();
        for (Class relaxedClass : changedRelaxedClassSet) {
            ClassModel modifyClassModel = properties.getClassModel(relaxedClass); //要修改的classModel

            rdtSupport.doModifyRelyDescribeHandle(classModel, modifyClassModel, new RdtSupport.ModifyRelyDescribeCallBack() {
                @Override
                public void execute(ClassModel classModel, ClassModel currentClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {
                    ModifyRelyDescribe currentDescribe = rdtSupport.getModifyRelyDescribe(describe, changedPropertys);
                    if (currentDescribe != null) {
                        updateModifyRelyDescribeSimple(classModel, currentClassModel, vo, relyColumn, group, currentDescribe);
                    }
                }
            });

        }
    }

    /**
     * 处理当前保存实体值变化时所要修改相关实体类的字段数据的业务逻辑(存在依赖时)
     * @param classModel
     * @param modifyClassModel
     * @param vo
     * @param relyColumn        依赖列,modifyClassModel中的column
     * @param group            所处group
     * @param describe
     */
    protected void updateModifyRelyDescribeSimple(final ClassModel classModel, final ClassModel modifyClassModel, final ChangedVo vo, final Column relyColumn, final int group, final ModifyRelyDescribe describe) {

        final Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

        final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
        final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

        rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
            @Override
            public void execute(ModifyCondition modifyCondition, String targetProperty, Object targetPropertyVal) {
                String property = modifyCondition.getColumn().getProperty();
                conditionDataMap.put(property, targetPropertyVal);
                if (logDetail) {
                    conditionLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                } else {
                    conditionLogMap.put(property, targetPropertyVal);
                }
            }
        });

        rdtSupport.doModifyColumnHandle(vo, describe, new RdtSupport.ModifyColumnCallBack() {
            @Override
            public void execute(ModifyColumn modifyColumn, String targetProperty, Object targetPropertyVal) {
                updateDataMap.put(modifyColumn.getColumn().getProperty(), targetPropertyVal);
                if (logDetail) {
                    updateLogMap.put(modifyColumn.getColumn().getProperty() + symbol + targetProperty, targetPropertyVal);
                } else {
                    updateLogMap.put(modifyColumn.getColumn().getProperty(), targetPropertyVal);
                }
            }
        });

        RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

        try {
            updateModifyRelyDescribeSimpleImpl(classModel, modifyClassModel, vo, conditionDataMap, updateDataMap, relyColumn, group, describe, rdtLog);

            logger.debug("{} modify about {}【{}={}】data with rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data with rely column error - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));
            logger.warn("rdt update field with rely has error", e);
            handlerThrowException(e);
        }

    }


    /**
     * 处理当前保存实体值变化时所要修改相关实体类的字段数据的业务逻辑的实现(存在依赖时)
     * @param classModel
     * @param modifyClassModel
     * @param vo
     * @param conditionValMap
     * @param updateValMap
     * @param relyColumn 依赖列,modifyClassModel中的column
     * @param group
     * @param describe
     * @param rdtLog
     * @return
     */
    protected abstract void updateModifyRelyDescribeSimpleImpl(final ClassModel classModel, final ClassModel modifyClassModel, final ChangedVo vo, final Map<String, Object> conditionValMap, final Map<String, Object> updateValMap, final Column relyColumn, final int group, final ModifyRelyDescribe describe, RdtLog rdtLog);



    protected <T> List<T> findByFillKeyModel(FillOneKeyModel fillOneKeyModel) {
        List<T> result = null;
        /*if (fillOneKeyModel.getIsPrimaryKey()) {
            //均为id key时
            result = findByIdIn(fillOneKeyModel.getEntityClass(), fillOneKeyModel.getKey(), fillOneKeyModel.getKeyValues());
        }*/

        int keyValuesSize = fillOneKeyModel.getKeyValues().size();
        if (keyValuesSize > 0) {
            result = findByFillKeyModelExecute(fillOneKeyModel);
        }

        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * 提供基于fillKeyModel查询相关数据的方法
     * @param fillOneKeyModel
     * @param <T>
     * @return
     */
    protected abstract <T> List<T> findByFillKeyModelExecute(FillOneKeyModel fillOneKeyModel);


    public void fill(Collection<?> collection) {
        fill(collection, true, false, false);
    }

    /**
     * 根据关系填充对应字段列
     *
     * @param collection
     * @param allowedNullValue 是否允许条件列的值为空
     * @param checkValue       为true时对应条件值的个数必须等于所匹配的结果个数,反之抛出异常
     * @param clear            为true时会清除未匹配到数据的字段值
     */
    public void fill(Collection<?> collection, boolean allowedNullValue, boolean checkValue, boolean clear) {
        FillRSModel fillRSModel = new FillRSModel();
        //处理数据关系
        fillRelationshipHandle(fillRSModel, collection, allowedNullValue, checkValue, clear);
        Map<Class, List<FillOneKeyModel>> fillKeyModelListMap = fillRSModel.getFillKeyModelListMap();
        if (!fillKeyModelListMap.isEmpty()) {
            for (Class entityClass : fillKeyModelListMap.keySet()) {
                List<FillOneKeyModel> fillOneKeyModelList = fillKeyModelListMap.get(entityClass);
                for (FillOneKeyModel fillOneKeyModel : fillOneKeyModelList) {
                    List<Object> entityList = findByFillKeyModel(fillOneKeyModel);
                    fillBuilder.setFillKeyData(fillOneKeyModel, entityClass, entityList, checkValue, clear);
                }
            }
        }
    }

    /**
     * 处理关系
     * @param fillRSModel
     * @param collection
     * @param withNullKeyValue
     * @param checkValue
     * @param clear
     */
    protected void fillRelationshipHandle(FillRSModel fillRSModel, Collection<?> collection, final boolean withNullKeyValue, final boolean checkValue, final boolean clear) {
        fillBuilder.fillRelationshipHandle(fillRSModel, collection, withNullKeyValue, checkValue, clear);
    }



    public static class RdtLog {
        private Map<String, Object> condition;
        private Map<String, Object> update;

        public RdtLog() {
            this(new LinkedHashMap<String, Object>(16), new LinkedHashMap<String, Object>(16));
        }

        public RdtLog(Map<String, Object> condition, Map<String, Object> update) {
            setCondition(condition);
            setUpdate(update);
        }

        public Map<String, Object> getCondition() {
            return condition;
        }

        public void setCondition(Map<String, Object> condition) {
            if (condition == null) throw new IllegalArgumentException("condition must be not null");
            this.condition = condition;
        }

        public Map<String, Object> getUpdate() {
            return update;
        }

        public void setUpdate(Map<String, Object> update) {
            if (update == null) throw new IllegalArgumentException("update must be not null");
            this.update = update;
        }

        public void putCondition(String key, Object value) {
            condition.put(key, value);
        }

        public Object getCondition(String key) {
            return condition.get(key);
        }

        public void putUpdate(String key, Object value) {
            update.put(key, value);
        }

        public Object getUpdate(String key) {
            return update.get(key);
        }

        public void putConditionTop(Map allMap) {
            if (allMap != null && allMap.size() != 0) {
                Map<String, Object> temp = new LinkedHashMap<String, Object>(16);
                for (Object key : allMap.keySet()) {
                    temp.put(key.toString(), allMap.get(key));
                }
                temp.putAll(condition);
                this.condition = temp;
            }
        }

        public void putUpdateTop(Map allMap) {
            if (allMap != null && allMap.size() != 0) {
                Map<String, Object> temp = new LinkedHashMap<String, Object>(16);
                for (Object key : allMap.keySet()) {
                    temp.put(key.toString(), allMap.get(key));
                }
                temp.putAll(update);
                this.update = temp;
            }
        }


        public void clear() {
            condition.clear();
            update.clear();
        }

    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Boolean getLogDetail() {
        return logDetail;
    }

    public void setLogDetail(Boolean logDetail) {
        this.logDetail = logDetail;
    }
}