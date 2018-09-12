package com.devloper.joker.redundant.resolver;

import com.devloper.joker.redundant.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

public abstract class RdtOperationResolver {

    protected final static Logger logger = LoggerFactory.getLogger(RdtOperationResolver.class);

    protected RdtProperties properties;

    protected RdtResolver rdtResolver;

    protected RdtSupport rdtSupport;

    private String symbol = "->";
    private Boolean logDetail = true;

    public void setRdtSupport(RdtSupport rdtSupport) {
        this.rdtSupport = rdtSupport;
        this.rdtResolver = rdtSupport.getRdtResolver();
        this.properties = rdtSupport.getProperties();
    }

    public abstract Object findById(Class entityClass, Object id);

    public abstract <T> Collection<T> findByIdIn(Class<T> entityClass, String idKey, Collection<Object> ids);

    public abstract Object save(Object o);

    public abstract Object saveAll(Collection<Object> o);

    public ClassModel getClassModel(Class entityClass) {
        return properties.getClassModel(entityClass);
    }

    public String getPrimaryId(Class entityClass) {
        ClassModel classModel = getClassModel(entityClass);
        if (classModel == null) throw new IllegalArgumentException("not found classModel with type " + entityClass);
        if (!classModel.getBaseClass()) throw new IllegalArgumentException("type " + entityClass + " has no primary id");
        return classModel.getPrimaryId();
    }

    /**
     * 获取之前的model数据
     *
     * @param entity
     * @return key: id, val: entity
     */

    public Map<Object, Object> getBeforeEntitys(Object entity) {
        Map<Object, Object> result = new HashMap<Object, Object>();
        if (entity != null) {
            entity = parseEntityData(entity);

            Class entityClass = null;
            boolean flag = false;

            String idKey = null;
            ClassModel classModel = null;

            for (Object current : (Collection) entity) {
                if (current == null) continue;
                if (entityClass == null) entityClass = current.getClass();
                else {
                    if (!entityClass.equals(current.getClass())) {
                        throw new IllegalArgumentException("the entity object args must be the same class type");
                    }
                }
                //判断class是否为base class
                if (classModel == null) {
                    classModel = getClassModel(entityClass);
                    if (classModel != null) flag = classModel.getBaseClass();
                }
                if (flag) {
                    //获取idKey
                    idKey = classModel.getPrimaryId();
                    Object idKeyVal = rdtResolver.getPropertyValue(current, idKey);
                    result.put(idKeyVal, null);
                } else break;
            }

            if (classModel != null) {
                Collection<Object> dataList = findByIdIn(entityClass, idKey, result.keySet());
                if (dataList != null) {
                    for (Object data : dataList) {
                        Object idKeyVal = rdtResolver.getPropertyValue(data, idKey);
                        result.put(idKeyVal, data);
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
    public void updateRelevant(Object entity) {
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
        for (Object data : dataList) {
            Object before = null;
            if (beforeKeyDataMap != null && !beforeKeyDataMap.isEmpty()) {
                if (idKey == null) idKey = getPrimaryId(data.getClass());
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
        updateMulti(current, before);
    }


    /**
     * 根据当前对象与之前对象数据对比进行更新相关字段
     * @param current
     * @param before
     */
    public void updateMulti(Object current, Object before) {
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
                if (before == null) {
                    logger.debug("{} 【{}】not exist before data, continue modify", entityClassName, idKeyVal);
                } else {
                    if (!before.getClass().equals(entityClass)) {
                        logger.warn("{} 【{}】 before data type is {}, continue modify", entityClassName, idKeyVal, before.getClass().getName());
                    } else {
                        Set<String> usedPropertys = classModel.getUsedPropertySet();
                        if (!usedPropertys.isEmpty()) {
                            //获取当前实体所使用的字段中发生改变的数据
                            ChangedVo changedVo = new ChangedVo();
                            changedVo.setBefore(before);
                            changedVo.setCurrent(current);
                            changedVo.setPrimaryId(idKey);
                            changedVo.setPrimaryIdVal(idKeyVal);

                            for (String property : usedPropertys) {
                                Object currentVal = rdtResolver.getPropertyValue(current, property);
                                Object beforeVal = rdtResolver.getPropertyValue(before, property);
                                boolean changed = false;
                                if (currentVal != null) {
                                    changed = !currentVal.equals(beforeVal);
                                } else {
                                    if (beforeVal != null) {
                                        changed = !beforeVal.equals(currentVal);
                                    }
                                }
                                if (changed) changedVo.addChangedProperty(property);
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

                                logger.info("{} 【{}={}】changed propertys {}, will to modify", entityClassName, idKey, idKeyVal, changedPropertys);

                                updateModifyDescribeSimple(classModel, changedVo);

                                updateModifyRelyDescribeSimple(classModel, changedVo);

                                updateModifyComplex(classModel, changedVo);
                            } else {
                                logger.debug("{} 【{}={}】has no changed propertys, continue modify", entityClassName, idKey, idKeyVal);
                            }
                        }
                    }
                }
            }
        }

    }


    private void updateModifyDescribeSimple(final ClassModel classModel, final ChangedVo vo) {
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
                String property = modifyCondition.getProperty();
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
                String property = modifyColumn.getProperty();
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
            logger.info("{} modify about {}【{}={}】data, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    describe.getIndex(), rdtResolver.toJson(conditionMap), rdtResolver.toJson(updateLogMap));
        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    describe.getIndex(), rdtResolver.toJson(conditionMap), rdtResolver.toJson(updateLogMap));

            logger.warn("rdt update field has error", e);
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


   
    private void updateModifyRelyDescribeSimple(final ClassModel classModel, final ChangedVo vo) {
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
                String property = modifyCondition.getProperty();
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
                updateDataMap.put(modifyColumn.getProperty(), targetPropertyVal);
                if (logDetail) {
                    updateLogMap.put(modifyColumn.getProperty() + symbol + targetProperty, targetPropertyVal);
                } else {
                    updateLogMap.put(modifyColumn.getProperty(), targetPropertyVal);
                }
            }
        });

        RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

        try {
           updateModifyRelyDescribeSimpleImpl(classModel, modifyClassModel, vo, conditionDataMap, updateDataMap, relyColumn, group, describe, rdtLog);

            logger.info("{} modify about {}【{}={}】data with rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

        } catch (Exception e) {
            logger.warn("{} modify about {}【{}={}】data with rely column error - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                    relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

            logger.warn("rdt update field with rely has error", e);
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



    /**
     * 复杂数据的处理
     * @param classModel
     * @param vo
     */
    protected void updateModifyComplex(final ClassModel classModel, final ChangedVo vo) {
        Set<Class> complexClassSet = classModel.getChangedComplexClassSet(); //获取相关的子文档关联类
        if (!complexClassSet.isEmpty()) {
            final List<String> changedPropertys = vo.getChangedPropertys();

            for (Class complexClass : complexClassSet) {
                //获取要修改的复杂数据对应类的指定修改属性列表
                ClassModel complexClassModel = getClassModel(complexClass);
                List<List<ComplexModel>> complexResults = rdtSupport.getComplexModelParseResult(complexClass);

                for (List<ComplexModel> currents : complexResults) {
                    final ComplexAnalysis complexAnalysis = rdtSupport.getComplexAnalysis(currents);

                    if (complexAnalysis.getHasMany()) { //包含many时

                        rdtSupport.doModifyDescribeHandle(classModel, complexClassModel, new RdtSupport.ModifyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel currentClassModel, ModifyDescribe describe) {
                                ModifyDescribe currentDescribe = rdtSupport.getModifyDescribe(describe, changedPropertys); //获取当前的修改条件
                                if (currentDescribe != null) {
                                    updateModifyDescribeMany(classModel, currentClassModel, complexAnalysis, currentDescribe, vo);
                                }
                            }
                        });

                        rdtSupport.doModifyRelyDescribeHandle(classModel, complexClassModel, new RdtSupport.ModifyRelyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel currentClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {
                                ModifyRelyDescribe currentDescribe = rdtSupport.getModifyRelyDescribe(describe, changedPropertys);
                                if (currentDescribe != null) {
                                    updateModifyRelyDescribeMany(classModel, currentClassModel, complexAnalysis, currentDescribe, vo, relyColumn, group);
                                }
                            }
                        });

                    } else { //全部为one时

                        rdtSupport.doModifyDescribeHandle(classModel, complexClassModel, new RdtSupport.ModifyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel currentClassModel, ModifyDescribe describe) {
                                ModifyDescribe currentDescribe = rdtSupport.getModifyDescribe(describe, changedPropertys); //获取当前的修改条件
                                if (currentDescribe != null) {
                                    updateModifyDescribeOne(classModel, currentClassModel, complexAnalysis, currentDescribe, vo);
                                }
                            }
                        });

                        rdtSupport.doModifyRelyDescribeHandle(classModel, complexClassModel, new RdtSupport.ModifyRelyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel classModel, ClassModel currentClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {
                                ModifyRelyDescribe currentDescribe = rdtSupport.getModifyRelyDescribe(describe, changedPropertys);
                                if (currentDescribe != null) {
                                    updateModifyRelyDescribeOne(classModel, currentClassModel, complexAnalysis, currentDescribe, vo, relyColumn, group);
                                }
                            }
                        });

                    }
                }


            }
        }
    }


    /**
     * 处理当前保存实体值变化时所要修改相关实体类的字段数据的业务逻辑
     *
     * @param classModel        触发更新的实体
     * @param complexClassModel 当前处理的complexClassModel
     * @param complexAnalysis
     * @param describe          对应的修改信息
     * @param vo
     */
    protected void updateModifyDescribeOne(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyDescribe describe, final ChangedVo vo) {

        try {
            ClassModel modifyClassModel = getModifyDescribeOneModifyClassModel(complexClassModel, complexAnalysis); //获取当前要修改的base model

            final Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
            final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

            final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
            final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

            rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
                @Override
                public void execute(ModifyCondition modifyCondition, String targetProperty, Object targetPropertyVal) {
                    String property = getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
                    conditionDataMap.put(property, targetPropertyVal); //用作查询条件

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
                    String property = getModifyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyColumn);
                    updateDataMap.put(property, targetPropertyVal); //用作更新值
                    if (logDetail) {
                        updateLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                    } else {
                        updateLogMap.put(property + targetProperty, targetPropertyVal);
                    }
                }
            });

            try {
                updateModifyDescribeOneImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, conditionDataMap, updateDataMap);

                logger.info("{} modify about {}【{}={}】data with complex【{}】, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(conditionLogMap), rdtResolver.toJson(updateLogMap));
            } catch (Exception e) {
                logger.warn("{} modify about {}【{}={}】data with complex【{}】 has error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(conditionLogMap), rdtResolver.toJson(updateLogMap));
                logger.warn("rdt update field has error", e);
            }
        } catch (Exception e) {
            logger.warn("rdt has error", e);
        }
    }

    protected abstract ClassModel getModifyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition);

    protected abstract String getModifyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, Column column);

    protected abstract void updateModifyDescribeOneImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo, final Map<String, Object> conditionValMap, final Map<String, Object> updateValMap);

    protected void updateModifyRelyDescribeOne(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group) {
        try {
            ClassModel modifyClassModel = getModifyRelyDescribeOneModifyClassModel(complexClassModel, complexAnalysis);

            final Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
            final Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

            final Map<String, Object> conditionDataMap = new LinkedHashMap<String, Object>(16);
            final Map<String, Object> updateDataMap = new LinkedHashMap<String, Object>(16);

            rdtSupport.doModifyConditionHandle(vo, describe, new RdtSupport.ModifyConditionCallBack() {
                @Override
                public void execute(ModifyCondition modifyCondition, String targetProperty, Object targetPropertyVal) {
                    String property = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyCondition);
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
                    String property = getModifyRelyDescribeOneProperty(classModel, complexClassModel, complexAnalysis, modifyColumn);
                    updateDataMap.put(property, targetPropertyVal);
                    if (logDetail) {
                        updateLogMap.put(property + symbol + targetProperty, targetPropertyVal);
                    } else {
                        updateLogMap.put(property, targetPropertyVal);
                    }
                }
            });

            RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

            try {
                updateModifyRelyDescribeOneImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, conditionDataMap, updateDataMap, relyColumn, group, rdtLog);

                logger.info("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

            } catch (Exception e) {
                logger.warn("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】has error , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

                logger.warn("rdt update field has error", e);
            }
        } catch (Exception e) {
            logger.warn("rdt has error", e);
        }
    }

    protected abstract ClassModel getModifyRelyDescribeOneModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, ModifyCondition modifyCondition);

    protected abstract String getModifyRelyDescribeOneProperty(ClassModel classModel, ClassModel complexClassModel, ComplexAnalysis complexAnalysis, Column column);

    protected abstract void updateModifyRelyDescribeOneImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyRelyDescribe describe, final ChangedVo vo, final Map<String, Object> conditionValMap, final Map<String, Object> updateValMap, final Column relyColumn, final int group, RdtLog rdtLog);

    protected void updateModifyDescribeMany(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyDescribe describe, final ChangedVo vo) {
        try {
            ClassModel modifyClassModel = getModifyDescribeManyModifyClassModel(complexClassModel, complexAnalysis);

            Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
            Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

            RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

            try {
                updateModifyDescribeManyImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, rdtLog);

                logger.info("{} modify about {}【{}={}】data with complex【{}】, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));
            } catch (Exception e) {
                logger.warn("{} modify about {}【{}={}】data with complex【{}】has error, index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

                logger.warn("rdt update field has error", e);
            }
        } catch (Exception e) {
            logger.warn("rdt has error", e);
        }
    }

    protected abstract ClassModel getModifyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract void updateModifyDescribeManyImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyDescribe describe, final ChangedVo vo, RdtLog rdtLog);


    protected void updateModifyRelyDescribeMany(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group) {
        try {
            ClassModel modifyClassModel = getModifyRelyDescribeManyModifyClassModel(complexClassModel, complexAnalysis);

            Map<String, Object> conditionLogMap = new LinkedHashMap<String, Object>(16);
            Map<String, Object> updateLogMap = new LinkedHashMap<String, Object>(16);

            RdtLog rdtLog = new RdtLog(conditionLogMap, updateLogMap);

            try {
                updateModifyRelyDescribeManyImpl(classModel, complexClassModel, complexAnalysis, modifyClassModel, describe, vo, relyColumn, group, rdtLog);

                logger.info("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

            } catch (Exception e) {
                logger.warn("{} modify about {}【{}={}】data with complex【{}】and rely column - 【name: {}, group: {} 】 , index: {}, conditions: {}, updates: {}", modifyClassModel.getClassName(), classModel.getClassName(), vo.getPrimaryId(), vo.getPrimaryIdVal(),
                        complexAnalysis.getPrefix(), relyColumn.getProperty(), group, describe.getIndex(), rdtResolver.toJson(rdtLog.getCondition()), rdtResolver.toJson(rdtLog.getUpdate()));

                logger.warn("rdt update field has error", e);
            }
        } catch (Exception e) {
            logger.warn("rdt has error", e);
        }
    }

    protected abstract ClassModel getModifyRelyDescribeManyModifyClassModel(ClassModel complexClassModel, ComplexAnalysis complexAnalysis);

    protected abstract void updateModifyRelyDescribeManyImpl(final ClassModel classModel, final ClassModel complexClassModel, final ComplexAnalysis complexAnalysis, final ClassModel modifyClassModel, final ModifyRelyDescribe describe, final ChangedVo vo, final Column relyColumn, final int group, RdtLog rdtLog);


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
