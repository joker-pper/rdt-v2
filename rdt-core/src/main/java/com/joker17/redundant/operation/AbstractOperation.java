package com.joker17.redundant.operation;

import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.core.RdtProperties;
import com.joker17.redundant.fill.*;
import com.joker17.redundant.model.*;
import com.joker17.redundant.core.RdtResolver;
import com.joker17.redundant.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

public abstract class AbstractOperation implements RdtOperation, RdtFillThrowExceptionHandler {

    protected final Logger logger = LoggerFactory.getLogger(RdtOperation.class);

    protected RdtProperties properties;

    protected RdtResolver rdtResolver;

    protected RdtConfiguration configuration;

    protected String symbol = "->";

    protected RdtFillBuilder fillBuilder;

    public AbstractOperation(RdtConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("rdt configuration must not be null.");
        }
        this.configuration = configuration;
        this.rdtResolver = configuration.getRdtResolver();
        this.properties = configuration.getProperties();
        this.fillBuilder = RdtFillBuilder.of(configuration, this);
    }


    @Override
    public <T> List<T> findByIdIn(Class<T> entityClass, Collection<Object> ids) {
        return findByIdIn(entityClass, ids, properties.getFindByIdWithLogical());
    }

    @Override
    public <T> List<T> findByIdIn(Class<T> entityClass, Collection<Object> ids, boolean logical) {
        if (ids == null || ids.size() == 0) {
            throw new IllegalArgumentException("ids must be not empty.");
        }
        if (ids.size() == 1) {
            T data = findById(entityClass, ids.iterator().next(), logical);
            List<T> dataList = new ArrayList<T>();
            if (data != null) {
                dataList.add(data);
            }
            return dataList;
        }
        final List<T> dataList = rdtResolver.getNotNullList(findByIdInExecute(entityClass, getPrimaryId(entityClass), ids));
        if (logical && !dataList.isEmpty()) {
            //逻辑值处理
            configuration.doLogicalModelHandle(entityClass, new RdtConfiguration.LogicalModelCallBack() {
                @Override
                public void execute(ClassModel dataModel, LogicalModel logicalModel) {
                    String logicalProperty = logicalModel.getColumn().getProperty();
                    List<Object> logicalValues = logicalModel.getValues();
                    Iterator<T> iterator = dataList.iterator();
                    while (iterator.hasNext()) {
                        T data = iterator.next();
                        if (!logicalValues.contains(rdtResolver.getPropertyValue(data, logicalProperty))) {
                            //当前数据未处于逻辑正常值时移除
                            iterator.remove();
                        }
                    }
                }
            });
        }
        return dataList;
    }

    @Override
    public <T> T findById(Class<T> entityClass, Object id) {
        return findById(entityClass, id, properties.getFindByIdWithLogical());
    }

    @Override
    public <T> T findById(Class<T> entityClass, Object id, boolean logical) {
        final T data = findByIdExecute(entityClass, id);
        //默认为正常数据
        final boolean[] allowed = new boolean[] {true};
        if (logical && data != null) {
            configuration.doLogicalModelHandle(entityClass, new RdtConfiguration.LogicalModelCallBack() {
                @Override
                public void execute(ClassModel dataModel, LogicalModel logicalModel) {
                    String logicalProperty = logicalModel.getColumn().getProperty();
                    List<Object> logicalValues = logicalModel.getValues();
                    allowed[0] = logicalValues.contains(rdtResolver.getPropertyValue(data, logicalProperty));
                }
            });
        }
        return allowed[0] ? data : null;
    }


    @Override
    public <T> T save(T entity) {
        if (entity == null) {
            throw new NullPointerException("entity must be not null.");
        }
        return save(entity, (Class<T>)entity.getClass());
    }


    @Override
    public <T> Collection<T> saveAll(Collection<T> collection) {
        if (collection == null || collection.size() == 0) {
            throw new IllegalArgumentException("data must be not empty.");
        }
        return saveAll(collection, (Class<T>) collection.iterator().next().getClass());
    }

    /**
     * findByIdIn查询具体实现逻辑(默认使用findByConditions实现)
     * @param entityClass
     * @param idKey
     * @param ids
     * @param <T>
     * @return
     */
    protected <T> List<T> findByIdInExecute(Class<T> entityClass, String idKey, Collection<Object> ids) {
        return findByConditions(entityClass, Arrays.asList(idKey), Arrays.asList((Object) ids), false);
    }

    /**
     * findById查询具体实现逻辑(默认使用findByConditions实现)
     * @param entityClass
     * @param id
     * @param <T>
     * @return
     */
    protected <T> T findByIdExecute(Class<T> entityClass, Object id) {
        List<T> dataList = findByConditions(entityClass, Arrays.asList(getPrimaryId(entityClass)), Arrays.asList(id), false);
        return dataList.isEmpty() ? null : dataList.get(0);
    }

    protected abstract <T> T save(T entity, Class<T> entityClass);

    protected abstract <T> Collection<T> saveAll(Collection<T> collection, Class<T> entityClass);


    @Override
    public <R, T> Map<R, T> transferMap(Collection<T> data, String key) {
        return configuration.transferMap(data, key);
    }

    @Override
    public <R, T> Map<R, T> transferMap(Collection<T> data, String key, Map<R, T> sourceMap) {
        return configuration.transferMap(data, key, sourceMap);
    }

    @Override
    public ClassModel getClassModel(Class entityClass) {
        return configuration.getClassModel(entityClass);
    }

    @Override
    public String getPrimaryId(Class entityClass) {
        return configuration.getPrimaryId(entityClass);
    }

    @Override
    public <K extends Serializable, T> Map<K, T> getPrimaryKeyEntityMap(Object data) {
        return getPrimaryKeyEntityMap(data, true);
    }

    @Override
    public <K extends Serializable, T> Map<K, T> getPrimaryKeyEntityMap(Object data, boolean check) {
        Map<Object, Object> result = new HashMap<Object, Object>(16);
        if (data != null) {
            data = parseEntityData(data);
            Class entityClass = null;
            boolean flag = false;
            String idKey = null;
            ClassModel classModel = null;

            for (Object current : (Collection) data) {
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
                if (idKeyVal != null) {
                    result.put(idKeyVal, null);
                }
            }

            if (flag) {
                if (check && classModel.getUsedPropertySet().isEmpty()) {
                    logger.debug("{} has no used property, and use check so not to load before data", classModel.getClassName());
                } else {
                    //查找数据
                    Collection<Object> resultDataList = null;
                    Set<Object> resultKeys = result.keySet();
                    if (!resultKeys.isEmpty()) {
                        if (resultKeys.size() > 1) {
                            resultDataList = findByIdIn(entityClass, result.keySet());
                        } else {
                            resultDataList = new ArrayList<Object>();
                            Object modelData = findById(entityClass, resultKeys.iterator().next());
                            if (modelData != null) {
                                resultDataList.add(modelData);
                            }
                        }
                    }
                    result = transferMap(resultDataList, idKey, result);
                }
            }
        }
        return (Map)result;
    }

    /**
     * 用于转换传入的实体数据参数为集合数据
     *
     * @param entity data | collection | array[data...]  array[collection]
     * @return
     */
    public Collection<Object> parseEntityData(Object entity) {
        return resolveDataToCollection(entity, true);
    }

    /**
     * 转换数据为集合数据
     * @param data
     * @param removeNull 是否移除null
     * @return
     */
    public Collection<Object> resolveDataToCollection(Object data, boolean removeNull) {
        Collection<Object> resultCollection = null;
        if (data != null) {
            if (data instanceof Collection) {
                resultCollection = ((Collection) data);
                Iterator<Object> iterator = resultCollection.iterator();
                if (removeNull) {
                    while (iterator.hasNext()) {
                        if (iterator.next() == null) {
                            iterator.remove();
                        }
                    }
                }
            } else {
                if (data.getClass().isArray()) {
                    int size = Array.getLength(data);
                    List<Object> resultTempList = new ArrayList(size);
                    for (int i = 0; i < size; i++) {
                        Object resultTemp = Array.get(data, i);
                        resultTempList.addAll(resolveDataToCollection(resultTemp, removeNull));
                    }
                    resultCollection = resultTempList;
                } else {
                    resultCollection = new ArrayList<Object>();
                    if (data != null || !removeNull) {
                        resultCollection.add(data);
                    }
                }
            }
        }
        resultCollection = resultCollection == null ? new ArrayList<Object>() : resultCollection;
        return resultCollection;
    }


    public RdtResolver getRdtResolver() {
        return rdtResolver;
    }


    @Override
    public void updateRelevant(Object multiData) {
        Collection<Object> dataList = parseEntityData(multiData);
        for (Object data : dataList) {
            updateMulti(data);
        }
    }

    @Override
    public void updateRelevant(Object multiData, Map<? extends Serializable, ? extends Object> beforePrimaryKeyEntityMap) {
        Collection<Object> dataList = parseEntityData(multiData);
        String idKey = null;

        boolean isLoad = beforePrimaryKeyEntityMap != null && !beforePrimaryKeyEntityMap.isEmpty();
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
                before = beforePrimaryKeyEntityMap.get(idVal);
            }
            updateMulti(data, before);
        }
    }


    @Override
    public void updateMulti(Object current) {
        Object before = null;
        try {
            if (current != null) before = current.getClass().newInstance();
        } catch (Exception e) {
            logger.warn("unable to create instance about {}, will continue update", current.getClass().getName());
        }
        updateMulti(current, before, true);
    }


    @Override
    public void updateMulti(Object current, Object before) {
        updateMulti(current, before, false);
    }

    /**
     * 更新逻辑具体实现
     * @param current
     * @param before
     * @param allUsedPropertysChange
     */
    protected void updateMulti(Object current, Object before, boolean allUsedPropertysChange) {
        if (current == null) return;
        //获取当前entity的class
        Class entityClass = current.getClass();
        ClassModel classModel = getClassModel(entityClass);
        if (classModel == null) {
            logger.debug("rdt not contains class {} will continue", entityClass.getName());
        } else {
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

                                if (!Boolean.TRUE.equals(properties.getCloseUpdateMulti())) {
                                    updateMultiCore(classModel, changedVo);
                                }
                                updateMultiAfter(classModel, changedVo);
                            } else {
                                logger.debug("{} 【{}={}】has no changed propertys {}, continue modify", entityClassName, idKey, idKeyVal, usedPropertys);
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


    /**
     * 可用于当前实体数据变更后的操作
     * @param classModel
     * @param changedVo
     */
    protected void updateMultiAfter(ClassModel classModel, ChangedVo changedVo) {
    }


    protected void handlerUpdateThrowException(Exception e) {
        if (Boolean.TRUE.equals(properties.getIsUpdateThrowException())) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new IllegalStateException(e);
            }
        }
    }

    protected boolean isLoggerSupport() {
        return logger.isDebugEnabled();
    }

    protected void loggerSupport(String s, Object ...values) {
        logger.debug(s, values);
    }

    protected String getPropertyMark(String property, String targetProperty) {
        if (getIsUpdateLogDetail() && targetProperty != null) {
            return property + symbol + targetProperty;
        }
        return property;
    }


    protected void updateModifyDescribeSimple(final ClassModel classModel, final ChangedVo vo) {
        Set<Class> changedRelaxedClassSet = classModel.getChangedRelaxedClassSet();
        final List<String> changedPropertys = vo.getChangedPropertys();
        for (Class relaxedClass : changedRelaxedClassSet) {
            ClassModel currentClassModel = properties.getClassModel(relaxedClass); //要修改的classModel
            configuration.doModifyDescribeHandle(classModel, currentClassModel, new RdtConfiguration.ModifyDescribeCallBack() {
                @Override
                public void execute(ClassModel classModel, ClassModel currentClassModel, ModifyDescribe describe) {
                    ModifyDescribe currentDescribe = configuration.getModifyDescribe(describe, changedPropertys); //获取当前的修改条件
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

        Exception exception = null;
        try {
            updateModifyDescribeSimpleImpl(classModel, modifyClassModel, describe, vo);
        } catch (Exception e) {
            exception = e;
        }
        updateModifyDescribeSimpleLogOutput(classModel, modifyClassModel, describe, vo, exception);
        if (exception != null) {
            handlerUpdateThrowException(exception);
        }

    }




    /**
     * 处理当前保存实体值变化时所要修改相关实体类的字段数据的业务逻辑的实现
     * @param classModel 触发更新的实体对象信息
     * @param modifyClassModel 要修改的实体对象信息
     * @param describe
     * @param vo
     */
    protected abstract void updateModifyDescribeSimpleImpl(final ClassModel classModel, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo);


    protected void updateModifyRelyDescribeSimple(final ClassModel classModel, final ChangedVo vo) {
        Set<Class> changedRelaxedClassSet = classModel.getChangedRelaxedClassSet();

        final List<String> changedPropertys = vo.getChangedPropertys();
        for (Class relaxedClass : changedRelaxedClassSet) {
            ClassModel modifyClassModel = properties.getClassModel(relaxedClass); //要修改的classModel

            configuration.doModifyRelyDescribeHandle(classModel, modifyClassModel, new RdtConfiguration.ModifyRelyDescribeCallBack() {
                @Override
                public void execute(ClassModel classModel, ClassModel modifyClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {

                    ModifyRelyDescribe currentDescribe = configuration.getModifyRelyDescribe(describe, changedPropertys);

                    if (currentDescribe != null) {
                        updateModifyRelyDescribeSimple(classModel, modifyClassModel, vo, relyColumn, group, currentDescribe);
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

        Exception exception = null;
        try {
            updateModifyRelyDescribeSimpleImpl(classModel, modifyClassModel, vo, relyColumn, group, describe);
        } catch (Exception e) {
            exception = e;
        }
        updateModifyRelyDescribeSimpleLogOutput(classModel, modifyClassModel, vo, relyColumn, group, describe, exception);
        if (exception != null) {
            handlerUpdateThrowException(exception);
        }
    }


    /**
     * 处理当前保存实体值变化时所要修改相关实体类的字段数据的业务逻辑的实现(存在依赖时)
     * @param classModel
     * @param modifyClassModel
     * @param vo
     * @param relyColumn 依赖列,modifyClassModel中的column
     * @param group
     * @param describe
     * @return
     */
    protected abstract void updateModifyRelyDescribeSimpleImpl(final ClassModel classModel, final ClassModel modifyClassModel, final ChangedVo vo, final Column relyColumn, final int group, final ModifyRelyDescribe describe);


    /**
     * 返回当前依赖列的条件map
     * @param describe
     * @param relyProperty
     * @return
     */
    protected Map getModelTypeProcessingCriteriaMap(ModifyRelyDescribe describe, final String relyProperty) {
        final Map allMap = new HashMap(16);
        configuration.matchedTypeHandle(describe, new RdtConfiguration.MatchedTypeCallback() {
            @Override
            public void in(List<Object> inValList) {
                allMap.put(relyProperty, inValList);
            }

            @Override
            public void or(List<Object> inValList, List<Object> notInValList) {
                Map notValMap = new HashMap(16);
                notValMap.put(relyProperty, notInValList);

                Map notMap = new HashMap(16);
                notMap.put("not", notValMap);

                Map inMap = new HashMap(16);
                inMap.put(relyProperty, inValList);

                allMap.put("or", Arrays.asList(inMap, notMap));
            }

            @Override
            public void notIn(List<Object> notInValList) {
                Map notMap = new HashMap(16);
                notMap.put(relyProperty, notInValList);
                allMap.put("not", notMap);
            }
        }, true);
        return allMap;
    }

    protected void updateModifyDescribeSimpleLogOutput(final ClassModel classModel, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo, final Exception exception) {
        boolean hasException = exception != null;
        if (isLoggerSupport() || hasException) {
            RdtLog rdtLog = new RdtLog();
            final Map<String, Object> conditionLogMap = rdtLog.getCondition();
            final Map<String, Object> updateLogMap = rdtLog.getUpdate();

            configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
                @Override
                public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                    conditionLogMap.put(getPropertyMark(modifyCondition.getColumn().getProperty(), targetProperty), targetPropertyVal);
                }
            });

            configuration.doLogicalModelHandle(modifyClassModel, properties.getUpdateMultiWithLogical(), new RdtConfiguration.LogicalModelCallBack() {
                @Override
                public void execute(ClassModel dataModel, LogicalModel logicalModel) {
                    conditionLogMap.put(logicalModel.getColumn().getProperty(), logicalModel.getValues());
                }
            });

            configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
                @Override
                public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                    updateLogMap.put(getPropertyMark(modifyColumn.getColumn().getProperty(), targetProperty), targetPropertyVal);
                }
            });

            if (hasException) {
                logger.warn("{} modify about {}【{}={}】data error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()), exception);
            } else {
                loggerSupport("{} modify about {}【{}={}】data, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));
            }

        }
    }

    protected void updateModifyRelyDescribeSimpleLogOutput(final ClassModel classModel, final ClassModel modifyClassModel, final ChangedVo vo, final Column relyColumn, final int group, final ModifyRelyDescribe describe, Exception exception) {
        boolean hasException = exception != null;
        if (isLoggerSupport() || hasException) {
            RdtLog rdtLog = new RdtLog();
            final Map<String, Object> conditionLogMap = rdtLog.getCondition();
            final Map<String, Object> updateLogMap = rdtLog.getUpdate();

            configuration.doModifyConditionHandle(vo, describe, new RdtConfiguration.ModifyConditionCallBack() {
                @Override
                public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                    conditionLogMap.put(getPropertyMark(modifyCondition.getColumn().getProperty(), targetProperty), targetPropertyVal);
                }
            });

            configuration.doLogicalModelHandle(modifyClassModel, properties.getUpdateMultiWithLogical(), new RdtConfiguration.LogicalModelCallBack() {
                @Override
                public void execute(ClassModel dataModel, LogicalModel logicalModel) {
                    conditionLogMap.put(logicalModel.getColumn().getProperty(), logicalModel.getValues());
                }
            });

            configuration.doModifyColumnHandle(vo, describe, new RdtConfiguration.ModifyColumnCallBack() {
                @Override
                public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                    updateLogMap.put(getPropertyMark(modifyColumn.getColumn().getProperty(), targetProperty), targetPropertyVal);
                }
            });

            String relyProperty = relyColumn.getProperty();
            rdtLog.putConditionTop(getModelTypeProcessingCriteriaMap(describe, relyProperty));

            if (hasException) {
                logger.warn("{} modify about {}【{}={}】data with rely column error - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()), exception);
            } else {
                loggerSupport("{} modify about {}【{}={}】data with rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));
            }
        }

    }

    @Override
    public <T> List<T> convertPropertyResults(Class<T> entityClass, List<?> queryResults, List<String> queryPropertys) {
        List<T> results = new ArrayList<T>(16);
        if (queryResults != null && !queryResults.isEmpty()) {
            if (queryPropertys == null || queryPropertys.isEmpty()) {
                //指定查询属性为空时,默认查询结果即为实体对象数据
                results = (List<T>) queryResults;
            } else {
                for (Object queryResult : queryResults) {
                    T data = null;
                    try {
                        data = entityClass.newInstance();
                    } catch (Exception e) {
                    }
                    int i = 0;
                    //将结果数据转换成集合数据,并同时根据查询属性进行转换
                    Collection<Object> queryResultCollection = resolveDataToCollection(queryResult, false);
                    for (Object result : queryResultCollection) {
                        rdtResolver.setPropertyValue(data, queryPropertys.get(i++), result);
                    }
                    results.add(data);
                }
            }
        }
        return results;
    }

    /**
     * 通过column集合获取对应的property列表数据
     * @param columnCollection
     * @return
     */
    protected List<String> getPropertyList(Collection<Column> columnCollection) {
        return rdtResolver.getPropertyList(columnCollection);
    }


    protected <T> List<T> findByFillKeyModel(FillOneKeyModel fillOneKeyModel) {
        List<T> result = null;
        int keyValuesSize = fillOneKeyModel.getKeyValues().size();
        if (keyValuesSize > 0) {
            result = findByFillKeyModelExecute(fillOneKeyModel);
        }
        return result == null ? Collections.<T>emptyList() : result;
    }

    /**
     * 提供基于fillKeyModel查询相关数据的方法  (单条件时的数据)
     * @param fillOneKeyModel
     * @param <T>
     * @return
     */
    protected <T> List<T> findByFillKeyModelExecute(FillOneKeyModel fillOneKeyModel) {
        Class<T> entityClass = fillOneKeyModel.getEntityClass();
        List<String> conditionPropertys = Arrays.asList(fillOneKeyModel.getKey());
        List<Object> conditionValues = Arrays.asList((Object) fillOneKeyModel.getKeyValues());

        Set<Column> columnSet = fillOneKeyModel.getColumnValues();
        List<String> selectPropertys = getPropertyList(columnSet);
        return findByConditions(entityClass, conditionPropertys, conditionValues, selectPropertys.toArray(new String[selectPropertys.size()]));
    }


    protected <T> List<T> findByFillManyKey(Class<T> entityClass, List<Column> conditionColumnValues, Set<Column> columnValues, List<Object> conditionGroupValue) {
        List<T> result = findByFillManyKeyExecute(entityClass, conditionColumnValues, columnValues, conditionGroupValue);
        return result == null ? Collections.<T>emptyList() : result;
    }


    /**
     * 获取entity class当前条件组的数据  (多条件时的数据)
     * @param entityClass
     * @param conditionColumnValues 条件列
     * @param columnValues 需要加载的列字段
     * @param conditionGroupValue 条件列对应的值
     * @param <T>
     * @return
     */
    protected <T> List<T> findByFillManyKeyExecute(Class<T> entityClass, List<Column> conditionColumnValues, Set<Column> columnValues, List<Object> conditionGroupValue) {
        List<String> conditionPropertys = getPropertyList(conditionColumnValues);
        List<String> selectPropertys = getPropertyList(columnValues);
        return findByConditions(entityClass, conditionPropertys, conditionGroupValue, selectPropertys.toArray(new String[selectPropertys.size()]));
    }


    @Override
    public <T> List<T> findByConditions(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, boolean logical, String... selectPropertys) {
        if (logical) {
            //跟随逻辑值时
            ClassModel classModel = getClassModel(entityClass);
            if (classModel != null) {
                LogicalModel logicalMode = classModel.getLogicalModel();
                if (logicalMode != null) {
                    //避免影响到相关变量值
                    conditionPropertys = new ArrayList<String>(conditionPropertys);
                    conditionValues = new ArrayList<Object>(conditionValues);

                    conditionPropertys.add(logicalMode.getColumn().getProperty());
                    conditionValues.add(logicalMode.getValues());
                }
            }
        }
        List<T> result = findByConditionsExecute(entityClass, conditionPropertys, conditionValues, selectPropertys);
        return result == null ? Collections.<T>emptyList() : result;

    }

    @Override
    public <T> List<T> findByConditions(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys) {
        return findByConditions(entityClass, conditionPropertys, conditionValues, properties.getFillWithLogical(), selectPropertys);
    }


    protected abstract <T> List<T> findByConditionsExecute(Class<T> entityClass, List<String> conditionPropertys, List<Object> conditionValues, String... selectPropertys);

    @Override
    public <T> Map<List<Object>, List<Object>> getGroupKeysMap(Class<T> entityClass, List<String> conditionPropertys, List<? extends Object> conditionValues, String selectProperty) {
        return (Map) getGroupKeysMapImpl(entityClass, conditionPropertys, conditionValues, selectProperty, true);
    }


    protected <T> Map<Object, List<Object>> getGroupKeysMapImpl(Class<T> entityClass, List<String> conditionPropertys, List<? extends Object> conditionValues, String selectProperty, boolean isByListKey) {
        Map<Object, List<Object>> resultMap = new HashMap<Object, List<Object>>(16);
        List<T> dataList = findByConditions(entityClass, conditionPropertys, (List<Object>) conditionValues, selectProperty);
        boolean isOneConditionProperty = conditionPropertys != null && conditionPropertys.size() == 1;
        if (!isByListKey && !isOneConditionProperty) {
            throw new IllegalArgumentException("when not by list key, must be only one condition property.");
        }
        String uniqueConditionProperty = isOneConditionProperty ? conditionPropertys.get(0) : null;
        if (dataList != null) {
            for (T data : dataList) {
                //条件列值作为key
                Object key;
                if (!isByListKey && isOneConditionProperty) {
                    key = rdtResolver.getPropertyValue(data, uniqueConditionProperty);
                } else {
                    List<Object> keyList = new ArrayList<Object>(16);
                    for (String conditionProperty : conditionPropertys) {
                        keyList.add(rdtResolver.getPropertyValue(data, conditionProperty));
                    }
                    key = keyList;
                }

                List<Object> valueList = resultMap.get(key);
                if (valueList == null) {
                    valueList = new ArrayList<Object>(16);
                    resultMap.put(key, valueList);
                }

                //未进行处理null值,默认均为合法值
                Object selectPropertyValue = rdtResolver.getPropertyValue(data, selectProperty);
                valueList.add(selectPropertyValue);
            }
        }
        return resultMap;
    }

    @Override
    public <T> Map<Object, List<Object>> getGroupKeysMap(Class<T> entityClass, String conditionProperty, Object conditionValue, String selectProperty) {
        return getGroupKeysMapImpl(entityClass, Arrays.asList(conditionProperty), Arrays.asList(conditionValue), selectProperty, false);
    }

    @Override
    public void fillForShow(Collection<?> collection) {
        fillForShow(collection, false);
    }

    @Override
    public void fillForShow(Collection<?> collection, boolean clear) {
        fillForShow(collection, clear, FillType.TRANSIENT);
    }

    @Override
    public void fillForShow(Collection<?> collection, boolean clear, FillType fillType) {
        fill(collection, true, false, clear, fillType);
    }

    @Override
    public void fillForSave(Collection<?> collection) {
        fillForSave(collection, false);
    }

    @Override
    public void fillForSave(Collection<?> collection, boolean allowedNullValue) {
        fillForSave(collection, allowedNullValue, true);
    }

    @Override
    public void fillForSave(Collection<?> collection, boolean allowedNullValue, boolean checkValue) {
        fillForSave(collection, allowedNullValue, checkValue, FillType.PERSISTENT);
    }


    @Override
    public void fillForSave(Collection<?> collection, boolean allowedNullValue, boolean checkValue, FillType fillType) {
        fill(collection, allowedNullValue, checkValue, true, fillType);
    }

    @Override
    public void fill(Collection<?> collection, boolean allowedNullValue, boolean checkValue, boolean clear) {
        fill(collection, allowedNullValue, checkValue, clear, FillType.ALL);
    }

    @Override
    public void fill(Collection<?> collection, boolean allowedNullValue, boolean checkValue, boolean clear, FillType fillType) {
        FillRSModel fillRSModel = new FillRSModel();
        //处理数据关系
        fillRelationshipHandle(fillRSModel, collection, allowedNullValue, checkValue, clear, fillType);

        fillGroupKeysRelationshipHandle(fillRSModel, collection, allowedNullValue, checkValue, clear, fillType);

        //多条件时每组条件值都要进行查询,优先处理
        Map<Class, FillManyKeyModel> fillManyKeyModelMap = fillRSModel.getFillManyKeyModelMap();
        if (!fillManyKeyModelMap.isEmpty()) {
            for (Class entityClass : fillManyKeyModelMap.keySet()) {
                FillManyKeyModel fillManyKeyModel = fillManyKeyModelMap.get(entityClass);
                for (FillManyKeyDetail detail : fillManyKeyModel.getManyKeyDetails()) {
                    int index = 0;
                    int groupValueNullIndex = detail.getGroupValueNullIndex();
                    for (List<Object> groupValue : detail.getConditionGroupValues()) {
                        boolean isGroupValueNull = groupValueNullIndex == index ++;
                        List<Column> conditionColumnValues = detail.getConditionColumnValues();
                        Object entityData = null;
                        if (!isGroupValueNull) {
                            //获取当前条件组的数据(当前每组条件数据值都会进行一次查询操作)
                            List<Object> dataList = findByFillManyKey(entityClass, conditionColumnValues, detail.getColumnValues(), groupValue);

                            boolean isAllowed = false;
                            if (dataList.size() == 1) {
                                Object data = dataList.get(0);
                                if (data != null) {
                                    List<Object> currentValues = new ArrayList<Object>();
                                    for (Column column : conditionColumnValues) {
                                        currentValues.add(rdtResolver.getPropertyValue(data, column.getProperty()));
                                    }
                                    isAllowed = fillBuilder.equalsElement(groupValue, currentValues);
                                    if (isAllowed) {
                                        entityData = data;
                                    }
                                }
                            }

                            if (checkValue && !isAllowed) {
                                throwFillNotAllowedDataException(fillManyKeyModel, 1, entityClass.getName() + " can't find one data with " + fillBuilder.getConditionMark(conditionColumnValues, groupValue));
                            }
                        }

                        if (clear || entityData != null) {
                            String conditionMark = fillBuilder.getConditionMark(conditionColumnValues, groupValue);
                            Map<ModifyDescribe, List<Object>> describeMap = detail.getDescribeMap(groupValue);
                            for (ModifyDescribe describe : describeMap.keySet()) {
                                List<Object> waitFillData = describeMap.get(describe);
                                if (waitFillData != null && !waitFillData.isEmpty()) {
                                    ClassModel modifyClassModel = configuration.getClassModel(describe.getEntityClass());
                                    fillBuilder.setFillManyKeyData(entityData, entityClass, waitFillData, modifyClassModel, describe, clear, conditionMark, fillType);
                                }
                            }

                            Map<ModifyRelyDescribe, List<Object>> relyDescribeMap = detail.getRelyDescribeMap(groupValue);
                            for (ModifyRelyDescribe describe : relyDescribeMap.keySet()) {
                                List<Object> waitFillData = relyDescribeMap.get(describe);
                                if (waitFillData != null && !waitFillData.isEmpty()) {
                                    ClassModel modifyClassModel = configuration.getClassModel(describe.getEntityClass());
                                    fillBuilder.setFillManyKeyData(entityData, entityClass, waitFillData, modifyClassModel, describe, clear, conditionMark, fillType);
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<Class, List<FillOneKeyModel>> fillKeyModelListMap = fillRSModel.getFillKeyModelListMap();
        if (!fillKeyModelListMap.isEmpty()) {
            for (Class entityClass : fillKeyModelListMap.keySet()) {
                List<FillOneKeyModel> fillOneKeyModelList = fillKeyModelListMap.get(entityClass);
                for (FillOneKeyModel fillOneKeyModel : fillOneKeyModelList) {
                    List<Object> entityList = findByFillKeyModel(fillOneKeyModel);
                    String key = fillOneKeyModel.getKey();
                    Map<Object, Object> entityDataMap = configuration.transferMap(entityList, key);

                    if (checkValue) {
                        int keyValuesSize = fillOneKeyModel.getKeyValues().size();

                        //查找的结果值必须与key value的个数相同
                        if (keyValuesSize != entityDataMap.size()) {
                            throwFillNotAllowedDataException(fillOneKeyModel, keyValuesSize, entityClass.getName() + " can't find all data with property " + fillOneKeyModel.getKey() + " value in " + rdtResolver.toJson(fillOneKeyModel.getKeyValues()));
                        }
                    }
                    fillBuilder.setFillOneKeyData(fillOneKeyModel, entityClass, entityDataMap, checkValue, clear, fillType);
                }
            }
        }
    }


    /**
     * 根据FillGroupKeyModel数据进行填充对应数据的groupKeys值,并将对应的关系进行处理
     * @param fillRSModel
     * @param collection
     * @param allowedNullValue
     * @param checkValue
     * @param clear
     * @param fillType
     */
    protected void fillGroupKeysRelationshipHandle(final FillRSModel fillRSModel, Collection<?> collection, final boolean allowedNullValue, final boolean checkValue, final boolean clear, final FillType fillType) {
        Map<Class, FillGroupKeyModel> fillGroupKeyModelMap = fillRSModel.getFillGroupKeyModelMap();
        if (!fillGroupKeyModelMap.isEmpty()) {
            for (Class gainClass : fillGroupKeyModelMap.keySet()) {
                FillGroupKeyModel fillGroupKeyModel = fillGroupKeyModelMap.get(gainClass);
                List<FillGroupKeyDetail> groupKeyDetails = fillGroupKeyModel.getGroupKeyDetails();
                for (FillGroupKeyDetail fillGroupKeyDetail : groupKeyDetails) {
                    List<String> conditionPropertys = getPropertyList(fillGroupKeyDetail.getConditionColumnValues());
                    Column selectColumnValue = fillGroupKeyDetail.getSelectColumnValue();
                    //获取条件数据值所对应的groupKeys列表值
                    Map<List<Object>, List<Object>> groupKeysMap = getGroupKeysMap(gainClass, conditionPropertys, fillGroupKeyDetail.getConditionValueList(), selectColumnValue.getProperty());

                    Map<List<Object>, Map<ModifyGroupDescribe, List<Object>>> conditionValueGroupDataMap = fillGroupKeyDetail.getConditionValueGroupDataMap();
                    for (List<Object> conditionValue : conditionValueGroupDataMap.keySet()) {

                        //获取当前条件所对应的groupKeys值列表
                        List<Object> groupKeysValueList = groupKeysMap.get(conditionValue);

                        if (groupKeysValueList != null) {
                            Map<ModifyGroupDescribe, List<Object>> groupDataListMap = conditionValueGroupDataMap.get(conditionValue);

                            for (ModifyGroupDescribe modifyGroupDescribe : groupDataListMap.keySet()) {
                                List<Object> dataList = groupDataListMap.get(modifyGroupDescribe);
                                ClassModel dataModel = getClassModel(modifyGroupDescribe.getEntityClass());

                                ModifyGroupKeysColumn modifyGroupKeysColumn = modifyGroupDescribe.getModifyGroupKeysColumn();
                                Column column = modifyGroupKeysColumn.getColumn();

                                //获取当前列实际类型的数据值列表
                                List<Object> columnPropertyValueList = rdtResolver.getColumnPropertyValueList(groupKeysValueList, modifyGroupKeysColumn, selectColumnValue);

                                //获取groupKey所对应列的数据值列表
                                List<Object> groupKeysExpectValueList = rdtResolver.getGroupKeysExpectValueList(groupKeysValueList, modifyGroupKeysColumn, selectColumnValue);

                                List<ModifyGroupConcatColumn> modifyGroupConcatColumnList = modifyGroupDescribe.getModifyGroupConcatColumnList();
                                boolean hasModifyGroupConcatColumnList = !modifyGroupConcatColumnList.isEmpty();

                                //当前列最终的数据值
                                Object groupKeysColumnValue = rdtResolver.getGroupColumnPropertyValue(columnPropertyValueList, modifyGroupKeysColumn);
                                ClassModel targetClassModel = getClassModel(modifyGroupDescribe.getTargetClass());
                                for (Object data : dataList) {
                                    //设置groupKeys列属性值
                                    rdtResolver.setPropertyValue(data, column.getProperty(), groupKeysColumnValue);

                                    if (hasModifyGroupConcatColumnList) {
                                        //设置关系
                                        fillBuilder.initOneKeyModelData(fillRSModel, targetClassModel, dataModel, modifyGroupDescribe, data, groupKeysExpectValueList, allowedNullValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * 未满足匹配数据个数时抛出
     * @param fillKeyModel
     * @param expectSize 期望个数
     * @param msg 默认提示信息
     */
    @Override
    public void throwFillNotAllowedDataException(FillKeyModel fillKeyModel, int expectSize, String msg) {
        Class entityClass = fillKeyModel.getEntityClass();
        ClassModel classModel = fillKeyModel.getClassModel();
        String tips = classModel.getNotFoundTips();
        if (expectSize > 1) {
            String notFoundMoreTips = classModel.getNotFoundMoreTips();
            if (StringUtils.isNotBlank(notFoundMoreTips)) {
                tips = notFoundMoreTips;
            }
        }
        //获取要提示的信息
        tips = StringUtils.isNotBlank(tips) ? tips : msg;
        throw new FillNotAllowedDataException(fillKeyModel, entityClass, tips);
    }

    @Override
    public void throwFillNotAllowedValueException(ClassModel dataClassModel, ModifyDescribe modifyDescribe, ModifyCondition modifyCondition, Object data, String msg) {
        String tips = null;
        String property = null;
        if (modifyCondition != null) {
            //条件优先
            property = modifyCondition.getColumn().getProperty();
            tips = modifyCondition.getNotAllowedNullTips();
            if (StringUtils.isEmpty(tips)) {
                tips = modifyCondition.getColumn().getNotAllowedNullTips();
            }
        } else {
            if (modifyDescribe instanceof ModifyRelyDescribe) {
                //依赖列
                ModifyRelyDescribe modifyRelyDescribe = ((ModifyRelyDescribe) modifyDescribe);
                tips = modifyRelyDescribe.getNotAllowedTypeTips();
                property = modifyRelyDescribe.getRelyColumn().getProperty();
            }
        }
        tips = StringUtils.isNotEmpty(tips) ? tips : msg;
        throw new FillNotAllowedValueException(tips, dataClassModel, data, property, modifyDescribe);
    }

    @Override
    public void throwFillNotAllowedValueException(ClassModel dataClassModel, ModifyGroupDescribe modifyGroupDescribe, ModifyGroupKeysColumn modifyGroupKeysColumn, Object data, String msg) {
        String tips = null;
        String property = null;
        if (modifyGroupKeysColumn != null) {
            //条件优先
            property = modifyGroupKeysColumn.getColumn().getProperty();
            tips = modifyGroupKeysColumn.getNotAllowedNullTips();
            if (StringUtils.isEmpty(tips)) {
                tips = modifyGroupKeysColumn.getColumn().getNotAllowedNullTips();
            }
        }
        tips = StringUtils.isNotEmpty(tips) ? tips : msg;
        throw new FillNotAllowedValueException(tips, dataClassModel, data, property, modifyGroupDescribe);
    }

    @Override
    public void throwFillNotAllowedValueException(String msg) {
        throw new FillNotAllowedValueException(msg);
    }

    /**
     * 处理关系
     */
    protected void fillRelationshipHandle(FillRSModel fillRSModel, Collection<?> collection, final boolean withNullKeyValue, final boolean checkValue, final boolean clear, FillType fillType) {
        fillBuilder.fillRelationshipHandle(fillRSModel, collection, withNullKeyValue, checkValue, clear, fillType);
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

    public Boolean getIsUpdateLogDetail() {
        return properties.getIsUpdateLogDetail();
    }

}