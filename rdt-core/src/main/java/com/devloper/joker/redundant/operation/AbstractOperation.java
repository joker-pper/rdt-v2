package com.devloper.joker.redundant.operation;

import com.devloper.joker.redundant.fill.*;
import com.devloper.joker.redundant.model.*;
import com.devloper.joker.redundant.resolver.RdtResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

public abstract class AbstractOperation implements RdtOperation {

    protected final Logger logger = LoggerFactory.getLogger(AbstractOperation.class);

    protected RdtProperties properties;

    protected RdtResolver rdtResolver;

    protected RdtSupport rdtSupport;

    protected String symbol = "->";

    protected Boolean logDetail = true;

    protected RdtFillBuilder fillBuilder;


    public AbstractOperation(RdtSupport rdtSupport) {
        if (rdtSupport == null) {
            throw new IllegalArgumentException("rdt support must not be null.");
        }
        this.rdtSupport = rdtSupport;
        this.rdtResolver = rdtSupport.getRdtResolver();
        this.properties = rdtSupport.getProperties();
        this.fillBuilder = RdtFillBuilder.of(rdtSupport);
    }



    @Override
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


    protected abstract <T> List<T> findByIdIn(Class<T> entityClass, String idKey, Collection<Object> ids);

    protected abstract <T> T save(T entity, Class<T> entityClass);

    protected abstract <T> Collection<T> saveAll(Collection<T> data, Class<T> entityClass);


    @Override
    public <T> Map<Object, T> getKeyMap(Collection<T> data, String key) {
        return rdtSupport.getKeyMap(data, key);
    }

    @Override
    public ClassModel getClassModel(Class entityClass) {
        return rdtSupport.getClassModel(entityClass);
    }

    @Override
    public String getPrimaryId(Class entityClass) {
        return rdtSupport.getPrimaryId(entityClass);
    }

    @Override
    public Map<Object, Object> getCurrentMapData(Object data) {
        return getCurrentMapData(data, true);
    }


    @Override
    public Map<Object, Object> getCurrentMapData(Object data, boolean check) {
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
                result.put(idKeyVal, null);
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
                            resultDataList = findByIdIn(entityClass, idKey, result.keySet());
                        } else {
                            resultDataList = new ArrayList<Object>();
                            Object modelData = findById(entityClass, resultKeys.iterator().next());
                            if (modelData != null) {
                                resultDataList.add(modelData);
                            }
                        }
                    }
                    if (resultDataList != null) {
                        for (Object resultData : resultDataList) {
                            Object idKeyVal = rdtResolver.getPropertyValue(resultData, idKey);
                            result.put(idKeyVal, resultData);
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
    public void updateRelevant(Object multiData, Map<Object, Object> beforeKeyDataMap) {
        Collection<Object> dataList = parseEntityData(multiData);
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

        if (logger.isDebugEnabled()) {
            rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
                @Override
                public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                    String property = modifyCondition.getColumn().getProperty();
                    if (logDetail) {
                        conditionMap.put(property + symbol + targetProperty, targetPropertyVal);
                    } else {
                        conditionMap.put(property, targetPropertyVal);
                    }
                }
            });

            rdtSupport.doModifyColumnHandle(vo, describe, new RdtSupport.ModifyColumnCallBack() {
                @Override
                public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                    String property = modifyColumn.getColumn().getProperty();
                    if (logDetail) {
                        updateLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                    } else {
                        updateLogMap.put(property, targetPropertyVal);
                    }
                }
            });
        }

        try {
            updateModifyDescribeSimpleImpl(classModel, modifyClassModel, describe, vo);
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
     */
    protected abstract void updateModifyDescribeSimpleImpl(final ClassModel classModel, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo);


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

    protected Map getModelTypeProcessingCriteriaMap(ModifyRelyDescribe describe, String relyProperty) {
        List<Object> unknowNotExistValList = describe.getUnknowNotExistValList();
        List<Object> valList = describe.getValList();
        Map allMap = new HashMap(16);
        if (!valList.isEmpty()) {
            if (unknowNotExistValList.isEmpty()) {
                allMap.put(relyProperty, valList);
            } else {
                //满足在valList 或 非unknowNotExistValList时

                Map notValMap = new HashMap(16);
                notValMap.put(relyProperty, unknowNotExistValList);

                Map notMap = new HashMap(16);
                notMap.put("not", notValMap);

                Map inMap = new HashMap(16);
                inMap.put(relyProperty, valList);

                allMap.put("or", Arrays.asList(inMap, notMap));

            }
        } else {
            if (!unknowNotExistValList.isEmpty()) {
                Map notMap = new HashMap(16);
                notMap.put(relyProperty, unknowNotExistValList);
                allMap.put("not", notMap);
            }
        }
        return allMap;
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

        RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

        if (logger.isDebugEnabled()) {
            rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
                @Override
                public void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal) {
                    String property = modifyCondition.getColumn().getProperty();
                    if (logDetail) {
                        conditionLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                    } else {
                        conditionLogMap.put(property, targetPropertyVal);
                    }
                }
            });

            rdtSupport.doModifyColumnHandle(vo, describe, new RdtSupport.ModifyColumnCallBack() {
                @Override
                public void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal) {
                    if (logDetail) {
                        updateLogMap.put(modifyColumn.getColumn().getProperty() + symbol + targetProperty, targetPropertyVal);
                    } else {
                        updateLogMap.put(modifyColumn.getColumn().getProperty(), targetPropertyVal);
                    }
                }
            });

            String relyProperty = relyColumn.getProperty();
            rdtLog.putConditionTop(getModelTypeProcessingCriteriaMap(describe, relyProperty));
        }


        try {
            updateModifyRelyDescribeSimpleImpl(classModel, modifyClassModel, vo, relyColumn, group, describe);

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
     * @param relyColumn 依赖列,modifyClassModel中的column
     * @param group
     * @param describe
     * @return
     */
    protected abstract void updateModifyRelyDescribeSimpleImpl(final ClassModel classModel, final ClassModel modifyClassModel, final ChangedVo vo, final Column relyColumn, final int group, final ModifyRelyDescribe describe);



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


    protected <T> List<T> findByFillManyKey(Class<T> entityClass, List<Column> conditionColumnValues, Set<Column> columnValues, List<Object> conditionGroupValue) {
        List<T> result = findByFillManyKeyExecute(entityClass, conditionColumnValues, columnValues, conditionGroupValue);
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * 获取entity class当前条件组的数据
     * @param entityClass
     * @param conditionColumnValues 条件列
     * @param columnValues 需要加载的列字段
     * @param conditionGroupValue 条件列对应的值
     * @param <T>
     * @return
     */
    protected abstract <T> List<T> findByFillManyKeyExecute(Class<T> entityClass, List<Column> conditionColumnValues, Set<Column> columnValues, List<Object> conditionGroupValue);


    @Override
    public void fillForShow(Collection<?> collection) {
        fillForShow(collection, false);
    }
    @Override
    public void fillForShow(Collection<?> collection, boolean clear) {
        fill(collection, true, false, clear);
    }

    @Override
    public void fillForSave(Collection<?> collection) {
        fillForSave(collection, false);
    }

    @Override
    public void fillForSave(Collection<?> collection, boolean allowedNullValue) {
        fill(collection, allowedNullValue, true, true);
    }


    @Override
    public void fill(Collection<?> collection, boolean allowedNullValue, boolean checkValue, boolean clear) {
        FillRSModel fillRSModel = new FillRSModel();
        //处理数据关系
        fillRelationshipHandle(fillRSModel, collection, allowedNullValue, checkValue, clear);

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
                            //获取当前条件组的数据
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
                                throw new FillNotAllowedDataException(fillManyKeyModel, entityClass, entityClass.getName() + " can't find one data with " + fillBuilder.getConditionMark(conditionColumnValues, groupValue));
                            }

                        }

                        if (clear || entityData != null) {
                            String conditionMark = fillBuilder.getConditionMark(conditionColumnValues, groupValue);
                            Map<ModifyDescribe, List<Object>> describeMap = detail.getDescribeMap(groupValue);
                            for (ModifyDescribe describe : describeMap.keySet()) {
                                List<Object> waitFillData = describeMap.get(describe);
                                fillBuilder.setFillManyKeyData(entityData, entityClass, waitFillData, describe, clear, conditionMark);
                            }

                            Map<ModifyRelyDescribe, List<Object>> relyDescribeMap = detail.getRelyDescribeMap(groupValue);
                            for (ModifyRelyDescribe describe : relyDescribeMap.keySet()) {
                                List<Object> waitFillData = relyDescribeMap.get(describe);
                                fillBuilder.setFillManyKeyData(entityData, entityClass, waitFillData, describe, clear, conditionMark);
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