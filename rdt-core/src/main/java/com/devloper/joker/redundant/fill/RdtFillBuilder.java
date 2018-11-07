package com.devloper.joker.redundant.fill;

import com.devloper.joker.redundant.model.*;
import com.devloper.joker.redundant.model.commons.RdtRelyModel;
import com.devloper.joker.redundant.resolver.RdtResolver;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RdtFillBuilder {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private RdtSupport support;
    private RdtResolver rdtResolver;

    private RdtFillBuilder(RdtSupport support) {
        this.support = support;
        this.rdtResolver = support.getRdtResolver();
    }

    public static RdtFillBuilder of(RdtSupport support) {
        return new RdtFillBuilder(support);
    }

    public void setFillKeyData(FillOneKeyModel fillOneKeyModel, Class entityClass, List<Object> entityList, boolean checkValue, boolean clear) {
        int keyValuesSize = fillOneKeyModel.getKeyValues().size();
        String key = fillOneKeyModel.getKey();
        Map<Object, Object> entityDataMap = support.getKeyMap(entityList, key);

        if (checkValue) {
            //查找的结果值必须与key value的个数相同
            if (keyValuesSize != entityDataMap.size()) {
                throw new IllegalArgumentException(fillOneKeyModel.getEntityClass().getName() + " can't find all data with property " + fillOneKeyModel.getKey() + " value in " + rdtResolver.toJson(fillOneKeyModel.getKeyValues()));
            }
        }

        if (!entityDataMap.isEmpty() || clear) {
            //存在数据或clear时进行处理

            for (ModifyDescribe modifyDescribe : fillOneKeyModel.getDescribeKeyDataMap().keySet()) {
                Map<Object, Set<Object>> keyValueData = fillOneKeyModel.getDescribeKeyData(modifyDescribe);
                for (Object keyValue : keyValueData.keySet()) {
                    //获取当前key要修改的数据列表
                    Set<Object> modifyDataSet = keyValueData.get(keyValue);
                    if (!modifyDataSet.isEmpty()) {
                        //获取当前值所对应的数据
                        Object entityData = entityDataMap.get(keyValue);
                        setFillKeyData(entityData, entityClass, key, keyValue, modifyDataSet, modifyDescribe, clear);
                    }
                }
            }

            for (ModifyRelyDescribe modifyDescribe : fillOneKeyModel.getRelyDescribeKeyDataMap().keySet()) {
                Map<Object, Set<Object>> keyValueData = fillOneKeyModel.getDescribeKeyData(modifyDescribe);
                for (Object keyValue : keyValueData.keySet()) {
                    Set<Object> modifyDataSet = keyValueData.get(keyValue);
                    if (!modifyDataSet.isEmpty()) {
                        Object entityData = entityDataMap.get(keyValue);
                        setFillKeyData(entityData, entityClass, key, keyValue, modifyDataSet, modifyDescribe, clear);                    }
                }
            }


        }


    }

    private void setFillKeyData(Object entityData, Class entityClass, String key, Object keyValue, Set<Object> waitFillSet, ModifyDescribe describe, boolean clear) {
        if (entityData != null || clear) {
            //当持久化数据存在或者clear时
            List<ModifyColumn> columnList = describe.getColumnList();
            Column relyColumn = null;

            if (describe instanceof ModifyRelyDescribe) {
                ModifyRelyDescribe modifyRelyDescribe = (ModifyRelyDescribe) describe;
                relyColumn = modifyRelyDescribe.getRelyColumn();
            }


            //依据列信息进行赋值
            for (Object waitFillData : waitFillSet) {
                ClassModel waitClassModel = null;
                String text = null;

                if (logger.isDebugEnabled()) {
                    waitClassModel = support.getClassModel(waitFillData.getClass());
                    String primaryId = waitClassModel.getPrimaryId();
                    if (StringUtils.isNotEmpty(primaryId)) {
                        text = "[" + primaryId + "=" + rdtResolver.getPropertyValue(waitFillData, primaryId) + "]";
                    } else {
                        text = "";
                    }
                }
                boolean clearMark = entityData == null;
                if (clearMark) {
                    //清空条件列的值
                    for (ModifyCondition modifyCondition : describe.getConditionList()) {
                        Column column = modifyCondition.getColumn();
                        String property = column.getProperty();
                        Class propertyClass = column.getPropertyClass();

                        Object value = null;
                        try {
                            //转换值
                            value = rdtResolver.cast(null, propertyClass);
                        } catch (Exception e) {
                            logger.warn("rdt cast val error", e);
                        }

                        //设置当前填充列字段的值
                        rdtResolver.setPropertyValue(waitFillData, property, value);

                        if (relyColumn != null) {
                            String relyProperty = relyColumn.getProperty();
                            logger.info("rdt fill clear condition with rely property {}({}-[{}]) about {}{} set [{}({})={}] from [{}[{}={}]", relyProperty, relyColumn.getPropertyClass().getName(), rdtResolver.getPropertyValue(waitFillData, relyProperty), waitClassModel.getClassName(), text, property, propertyClass.getName(), value, entityClass.getName(), key, keyValue);
                        } else {
                            logger.info("rdt fill clear condition about {}{} set [{}({})={}] from [{}[{}={}]", waitClassModel.getClassName(), text, property, propertyClass.getName(), value, entityClass.getName(), key, keyValue);
                        }
                    }
                }


                for (ModifyColumn modifyColumn : columnList) {
                    Column column = modifyColumn.getColumn();
                    String property = column.getProperty();
                    Class propertyClass = column.getPropertyClass();

                    Column targetColumn = modifyColumn.getTargetColumn();
                    String targetProperty = targetColumn.getProperty();
                    Class targetPropertyClass = targetColumn.getPropertyClass();


                    //处理填充列字段所对应的值
                    Object value = null;
                    if (!clearMark) {
                        value = rdtResolver.getPropertyValue(entityData, targetProperty);
                    }
                    try {
                        //转换值
                        value = rdtResolver.cast(value, propertyClass);
                    } catch (Exception e) {
                        logger.warn("rdt cast val error", e);
                    }

                    //设置当前填充列字段的值
                    rdtResolver.setPropertyValue(waitFillData, property, value);


                    if (relyColumn != null) {
                        String relyProperty = relyColumn.getProperty();
                        if (clearMark) {
                            logger.info("rdt fill clear column with rely property {}({}-[{}]) about {}{} set [{}({})->{}({})={}] from [{}[{}={}]", relyProperty, relyColumn.getPropertyClass().getName(), rdtResolver.getPropertyValue(waitFillData, relyProperty), waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), key, keyValue);
                        } else {
                            logger.debug("rdt fill column with rely property {}({}-[{}]) about {}{} set [{}({})->{}({})={}] from [{}[{}={}]", relyProperty, relyColumn.getPropertyClass().getName(), rdtResolver.getPropertyValue(waitFillData, relyProperty), waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), key, keyValue);
                        }
                    } else {
                        if (clearMark) {
                            logger.info("rdt fill clear column about {}{} set [{}({})->{}({})={}] from [{}[{}={}]", waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), key, keyValue);
                        } else {
                            logger.debug("rdt fill column about {}{} set [{}({})->{}({})={}] from [{}[{}={}]", waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), key, keyValue);
                        }
                    }



                }
            }
        }
    }

    public static boolean equalsElement(Collection collection1, Collection collection2) {
        boolean result = false;
        if (collection1 != null && collection2 != null) {
            if (collection1 == collection2) {
                result = true;
            } else {
                if (collection1.size() == collection2.size()) {
                    //数量一致时
                    boolean equals = true;
                    for (Object data : collection1) {
                        if (!collection2.contains(data)) {
                            equals = false;
                            break;
                        }
                    }
                    result = equals;
                }
            }
        } else {
            if (collection1 == null && collection2 == null) {
                result = true;
            }
        }
        return result;

    }

    private void initManyKeyModelData(FillRSModel fillRSModel, ClassModel entityClassModel, ClassModel dataClassModel, ModifyDescribe describe, Object data, boolean allowedNullValue) {
        List<ModifyCondition> modifyConditionList = describe.getConditionList();
        List<ModifyColumn> modifyColumnList = describe.getColumnList();

        Map<Class, FillManyKeyModel> fillManyKeyModelMap = fillRSModel.getFillManyKeyModelMap();

        Class entityClass = entityClassModel.getCurrentClass();

        FillManyKeyModel fillManyKeyModel = fillManyKeyModelMap.get(entityClass);

        if (fillManyKeyModel == null) {
            fillManyKeyModel = new FillManyKeyModel();
            fillManyKeyModel.setEntityClass(entityClass);
            fillManyKeyModelMap.put(entityClass, fillManyKeyModel);
        }

        //处理当前entity class的条件列
        Map<Column, ModifyCondition> columnModifyConditionMap = new LinkedHashMap<Column, ModifyCondition>(16);

        List<Column> currentConditionColumnValues = new ArrayList<Column>(16);

        for (ModifyCondition modifyCondition : modifyConditionList) {
            Column targetColumn = modifyCondition.getTargetColumn();
            currentConditionColumnValues.add(targetColumn);
            columnModifyConditionMap.put(targetColumn, modifyCondition);
        }

        //获取当前detail
        FillManyKeyDetail currentKeyDetail = null;
        List<FillManyKeyDetail> manyKeyDetails = fillManyKeyModel.getManyKeyDetails();
        for (FillManyKeyDetail detail : manyKeyDetails) {
            List<Column> conditionColumnValues = detail.getConditionColumnValues();
            if (equalsElement(conditionColumnValues, currentConditionColumnValues)) {
                //使用对应顺序列的数据
                currentConditionColumnValues = conditionColumnValues;
                currentKeyDetail = detail;
                break;
            }
        }

        if (currentKeyDetail == null) {
            //不存在时进行初始化
            currentKeyDetail = new FillManyKeyDetail();
            currentKeyDetail.setConditionColumnValues(currentConditionColumnValues);
            manyKeyDetails.add(currentKeyDetail);
        }

        //添加所要查询列
        for (ModifyColumn modifyColumn : modifyColumnList) {
            currentKeyDetail.getColumnValues().add(modifyColumn.getTargetColumn());
        }
        Class dataClass = dataClassModel.getCurrentClass();

        //获取当前对应的条件列值
        List<Object> currentConditionGroupValue = new ArrayList<Object>(16);
        List<List<Object>> conditionGroupValues = currentKeyDetail.getConditionGroupValues();
        for (ModifyCondition modifyCondition : columnModifyConditionMap.values()) {
            Column column = modifyCondition.getColumn();
            String property = column.getProperty();
            Object value = rdtResolver.getPropertyValue(data, property);

            if (!allowedNullValue && value == null) {
                throw new FillNotAllowedValueException(fillManyKeyModel, dataClass, data, describe, property, dataClassModel.getClassName() + " property " + property + " value not allowed null.");
            }

            currentConditionGroupValue.add(value);
        }
        boolean hasContainsConditionGroupValue = false;
        for (List<Object> values : conditionGroupValues) {
            if (equalsElement(currentConditionGroupValue, values)) {
                hasContainsConditionGroupValue = true;
                currentConditionGroupValue = values;
                break;
            }
        }

        //不存在时加入条件值组中
        if (!hasContainsConditionGroupValue) {
            conditionGroupValues.add(currentConditionGroupValue);
        }

        Set<Object> dataSet;

        if (describe instanceof ModifyRelyDescribe) {
            ModifyRelyDescribe modifyRelyDescribe = (ModifyRelyDescribe) describe;
            Map<List<Object>, Map<ModifyRelyDescribe, Set<Object>>> conditionGroupValueRelyDescribeDataMap = currentKeyDetail.getConditionGroupValueRelyDescribeDataMap();
            //获取当前条件值下的relyDescribeSetMap
            Map<ModifyRelyDescribe, Set<Object>> relyDescribeSetMap = conditionGroupValueRelyDescribeDataMap.get(currentConditionGroupValue);
            if (relyDescribeSetMap == null) {
                relyDescribeSetMap = new HashMap<ModifyRelyDescribe, Set<Object>>(16);
                conditionGroupValueRelyDescribeDataMap.put(currentConditionGroupValue, relyDescribeSetMap);
            }

            dataSet = relyDescribeSetMap.get(modifyRelyDescribe);
            if (dataSet == null) {
                dataSet = new HashSet<Object>(16);
                relyDescribeSetMap.put(modifyRelyDescribe, dataSet);
            }
        } else {
            Map<List<Object>, Map<ModifyDescribe, Set<Object>>> describeDataMap = currentKeyDetail.getConditionGroupValueDescribeDataMap();
            Map<ModifyDescribe, Set<Object>> describeSetMap = describeDataMap.get(currentConditionGroupValue);
            if (describeSetMap == null) {
                describeSetMap = new HashMap<ModifyDescribe, Set<Object>>(16);
                describeDataMap.put(currentConditionGroupValue, describeSetMap);
            }

            dataSet = describeSetMap.get(describe);
            if (dataSet == null) {
                dataSet = new HashSet<Object>(16);
                describeSetMap.put(describe, dataSet);
            }
        }
        dataSet.add(data);

    }

    private void initOneKeyModelData(FillRSModel fillRSModel, ClassModel entityClassModel, ClassModel dataClassModel, ModifyDescribe describe, Object data, boolean allowedNullValue) {

        ModifyCondition modifyCondition = describe.getConditionList().get(0);
        List<ModifyColumn> modifyColumnList = describe.getColumnList();

        Map<Class, List<FillOneKeyModel>> fillKeyModelListMap = fillRSModel.getFillKeyModelListMap();
        Class dataClass = dataClassModel.getCurrentClass();

        //处理当前entityClass某单列作为key查询条件的数据
        FillOneKeyModel fillOneKeyModel = fillRSModel.getFillKeyModel(entityClassModel.getCurrentClass(), modifyCondition, fillKeyModelListMap);
        for (ModifyColumn modifyColumn : modifyColumnList) {
            fillOneKeyModel.addColumnValue(modifyColumn.getTargetColumn());
        }
        String property = modifyCondition.getColumn().getProperty();

        Object keyVal = rdtResolver.getPropertyValue(data, property);

        if (!allowedNullValue && keyVal == null) {
            throw new FillNotAllowedValueException(fillOneKeyModel, dataClass, data, describe, property, dataClassModel.getClassName() + " property " + property + " value not allowed null.");
        }
        if (describe instanceof ModifyRelyDescribe) {
            fillOneKeyModel.addDescribeKeyValueData((ModifyRelyDescribe) describe, keyVal, data);
        } else {
            fillOneKeyModel.addDescribeKeyValueData(describe, keyVal, data);
        }
    }

    /**
     * 处理关系
     *
     * @param fillRSModel
     * @param collection
     * @param allowedNullValue 是否允许指定为对应持久化类的字段值为空
     * @param checkValue
     * @param clear
     */
    public void fillRelationshipHandle(final FillRSModel fillRSModel, Collection<?> collection, final boolean allowedNullValue, final boolean checkValue, final boolean clear) {
        if (collection != null && !collection.isEmpty()) {
            final Map<Class, List<FillOneKeyModel>> fillKeyModelListMap = fillRSModel.getFillKeyModelListMap();

            for (final Object data : collection) {
                if (data != null) {
                    //获取当前数据的类型
                    final Class dataClass = data.getClass();
                    ClassModel dataClassModel = support.getClassModel(dataClass);
                    if (dataClassModel == null) {
                        logger.debug("rdt not contains class {}, so builder for fill now.", dataClass.getName());
                        support.builderClass(dataClass);
                        dataClassModel = support.getClassModel(dataClass);
                    }
                    final Map<Class, List<ModifyDescribe>> targetClassModifyDescribeMap = dataClassModel.getTargetClassModifyDescribeMap();

                    for (final Class entityClass : targetClassModifyDescribeMap.keySet()) {
                        //当前的修改信息
                        ClassModel entityClassModel = support.getClassModel(entityClass);
                        //基于entity data 修改 data model的数据
                        support.doModifyDescribeHandle(entityClassModel, dataClassModel, new RdtSupport.ModifyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel entityClassModel, ClassModel dataClassModel, ModifyDescribe describe) {
                                List<ModifyCondition> conditionList = describe.getConditionList();
                                List<ModifyColumn> columnList = describe.getColumnList();

                                if (!columnList.isEmpty()) {
                                    int conditionSize = conditionList.size();
                                    if (conditionSize == 0) {
                                        logger.warn("rdt fill relationship handle continued : {} target about [{}(index={})] has no condition.", dataClassModel.getClassName(), entityClassModel.getClassName(), describe.getIndex());
                                    } else if (conditionSize == 1) {
                                        initOneKeyModelData(fillRSModel, entityClassModel, dataClassModel, describe, data, allowedNullValue);
                                    } else {
                                        initManyKeyModelData(fillRSModel, entityClassModel, dataClassModel, describe, data, allowedNullValue);
                                    }

                                }
                            }
                        });
                    }

                    for (final Class entityClass : dataClassModel.getTargetRelyModifyClassSet()) {
                        ClassModel entityClassModel = support.getClassModel(entityClass);

                        try {
                            support.doModifyRelyDescribeHandle(entityClassModel, dataClassModel, new RdtSupport.ModifyRelyDescribeCallBack() {
                                @Override
                                public void execute(ClassModel entityClassModel, ClassModel dataClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {
                                    List<ModifyCondition> conditionList = describe.getConditionList();
                                    List<ModifyColumn> columnList = describe.getColumnList();

                                    if (!columnList.isEmpty()) {
                                        int conditionSize = conditionList.size();
                                        if (conditionSize == 0) {
                                            logger.warn("rdt fill relationship handle continued : {} with rely property {} target about [{}(group={}&index={})] has no condition.", dataClassModel.getClassName(), relyColumn.getProperty(), entityClassModel.getClassName(), describe.getGroup(), describe.getIndex());
                                        } else {
                                            //获取当前依赖字段的值
                                            Object relyColumnValue = rdtResolver.getPropertyValue(data, relyColumn.getProperty());
                                            if (isMatchedType(describe, relyColumnValue)) {
                                                if (conditionSize == 1) {
                                                    initOneKeyModelData(fillRSModel, entityClassModel, dataClassModel, describe, data, allowedNullValue);
                                                } else {
                                                    initManyKeyModelData(fillRSModel, entityClassModel, dataClassModel, describe, data, allowedNullValue);
                                                }
                                            } else {
                                                RdtRelyModel relyModel = describe.getRdtRelyModel();
                                                boolean isAllowed = relyModel.isValueAllowed(relyColumnValue);
                                                if (!isAllowed) {
                                                    logger.warn("rdt fill relationship handle check macth type error : " + dataClassModel.getClassName() + " has no rely property " + relyColumn.getProperty() + " value " + relyColumnValue + " to appoint type : [{}(group={}&index={})], and current type values is {}", entityClassModel.getClassName(), describe.getGroup(), describe.getIndex(), relyModel.getExplicitValueList());
                                                    if (checkValue) {
                                                        //验证数据合法性
                                                        throw new IllegalArgumentException(dataClassModel.getClassName() + " has no rely property " + relyColumn.getProperty() + " value " + relyColumnValue + " to appoint type : " + entityClassModel.getClassName());
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            });
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }

                    //处理当前数据中的存在的关联字段的数据
                    for (ComplexModel complexModel : dataClassModel.getComplexModelList()) {
                        String complexProperty = complexModel.getProperty();

                        Object complexData = rdtResolver.getPropertyValue(data, complexProperty);
                        Collection<Object> toHandleList = null;
                        if (complexModel.getIsOne()) {
                            //单对象属性时
                            toHandleList = Arrays.asList(complexData);
                        } else {
                            if (complexData != null) {
                                if (complexData.getClass().isArray()) {
                                    toHandleList = Arrays.asList((Object[]) complexData);
                                } else {
                                    toHandleList = (Collection) complexData;
                                }
                            }
                        }
                        fillRelationshipHandle(fillRSModel, toHandleList, allowedNullValue, checkValue, clear);
                    }
                }
            }
        }
    }


    /**
     * 依赖值是否满足匹配条件
     *
     * @param describe
     * @param relyColumnValue
     * @return
     */
    public boolean isMatchedType(ModifyRelyDescribe describe, Object relyColumnValue) {
        boolean result = false;
        List<Object> unknowNotExistValList = describe.getUnknowNotExistValList();
        List<Object> valList = describe.getValList();
        if (!valList.isEmpty()) {
            result = valList.contains(relyColumnValue);
            if (unknowNotExistValList.isEmpty()) {
                //valList 包含当前data的依赖字段值
            } else {
                //满足在valList 或 非unknowNotExistValList时
                if (!result) {
                    result = !unknowNotExistValList.contains(relyColumnValue);
                }
            }
        } else {
            if (!unknowNotExistValList.isEmpty()) {
                //满足非unknowNotExistValList时
                result = !unknowNotExistValList.contains(relyColumnValue);
            }
        }
        return result;
    }

}
