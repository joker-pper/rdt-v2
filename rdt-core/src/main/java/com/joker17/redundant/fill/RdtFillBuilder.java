package com.joker17.redundant.fill;

import com.joker17.redundant.core.RdtConfiguration;

import com.joker17.redundant.model.commons.RdtRelyModel;
import com.joker17.redundant.core.RdtResolver;
import com.joker17.redundant.utils.StringUtils;
import com.joker17.redundant.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RdtFillBuilder {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private RdtConfiguration configuration;
    private RdtResolver rdtResolver;

    private RdtFillBuilder(RdtConfiguration support) {
        this.configuration = support;
        this.rdtResolver = support.getRdtResolver();
    }

    public static RdtFillBuilder of(RdtConfiguration support) {
        return new RdtFillBuilder(support);
    }

    public void setFillKeyData(FillOneKeyModel fillOneKeyModel, Class entityClass, List<Object> entityList, boolean checkValue, boolean clear) {
        int keyValuesSize = fillOneKeyModel.getKeyValues().size();
        String key = fillOneKeyModel.getKey();
        Map<Object, Object> entityDataMap = configuration.transferMap(entityList, key);

        if (checkValue) {
            //查找的结果值必须与key value的个数相同
            if (keyValuesSize != entityDataMap.size()) {
                throw new FillNotAllowedDataException(fillOneKeyModel, entityClass, fillOneKeyModel.getEntityClass().getName() + " can't find all data with property " + fillOneKeyModel.getKey() + " value in " + rdtResolver.toJson(fillOneKeyModel.getKeyValues()));
            }
        }

        if (!entityDataMap.isEmpty() || clear) {
            //存在数据或clear时进行处理

            for (ModifyDescribe modifyDescribe : fillOneKeyModel.getDescribeKeyDataMap().keySet()) {
                Map<Object, List<Object>> keyValueData = fillOneKeyModel.getDescribeKeyData(modifyDescribe);
                for (Object keyValue : keyValueData.keySet()) {
                    //获取当前key要修改的数据列表
                    List<Object> modifyData = keyValueData.get(keyValue);
                    if (!modifyData.isEmpty()) {
                        //获取当前值所对应的数据
                        Object entityData = entityDataMap.get(keyValue);
                        setFillKeyData(entityData, entityClass, getConditionMark(key, keyValue), modifyData, modifyDescribe, clear);
                    }
                }
            }

            for (ModifyRelyDescribe modifyDescribe : fillOneKeyModel.getRelyDescribeKeyDataMap().keySet()) {
                Map<Object, List<Object>> keyValueData = fillOneKeyModel.getDescribeKeyData(modifyDescribe);
                for (Object keyValue : keyValueData.keySet()) {
                    List<Object> modifyData = keyValueData.get(keyValue);
                    if (!modifyData.isEmpty()) {
                        Object entityData = entityDataMap.get(keyValue);
                        setFillKeyData(entityData, entityClass, getConditionMark(key, keyValue), modifyData, modifyDescribe, clear);                    }
                }
            }
        }

    }

    /**
     * 获取条件标识
     * @param key
     * @param value
     * @return
     */
    public String getConditionMark(String key, Object value) {
        return "[" + key + "=" + value + "]";
    }


    /**
     * 获取条件标识
     * @param conditionColumns
     * @param values
     * @return
     */
    public String getConditionMark(Collection<Column> conditionColumns, List<Object> values) {
        StringBuilder sb = new StringBuilder("[");

        if (!conditionColumns.isEmpty()) {
            int index = 0;
            for (Column column : conditionColumns) {
                sb.append(column.getProperty() + "=" + values.get(index ++) + "&");
            }
            int length = sb.length();
            sb.delete(length - 1, length);
        }

        sb.append("]");
        return sb.toString();
    }

    public void setFillManyKeyData(Object entityData, Class entityClass, Collection<Object> waitFillData, ModifyDescribe describe, boolean clear, String conditionMark) {
        setFillKeyData(entityData, entityClass, conditionMark, waitFillData, describe, clear);
    }


    private void setFillKeyData(Object entityData, Class entityClass, String conditionMark, Collection<Object> waitFillDatas, ModifyDescribe describe, boolean clear) {
        if (entityData != null || clear) {
            //当持久化数据存在或者clear时
            List<ModifyColumn> columnList = describe.getColumnList();
            Column relyColumn = null;

            if (describe instanceof ModifyRelyDescribe) {
                ModifyRelyDescribe modifyRelyDescribe = (ModifyRelyDescribe) describe;
                relyColumn = modifyRelyDescribe.getRelyColumn();
            }

            //依据列信息进行赋值
            for (Object waitFillData : waitFillDatas) {
                ClassModel waitClassModel = null;
                String text = null;

                if (logger.isDebugEnabled() || logger.isInfoEnabled()) {
                    waitClassModel = configuration.getClassModel(waitFillData.getClass());
                    String primaryId = waitClassModel.getPrimaryId();
                    text = StringUtils.isNotEmpty(primaryId) ? getConditionMark(primaryId, rdtResolver.getPropertyValue(waitFillData, primaryId)) : "";
                }
                boolean clearMark = entityData == null;
                if (clearMark) {
                    //清空条件列的值
                    for (ModifyCondition modifyCondition : describe.getConditionList()) {
                        Column column = modifyCondition.getColumn();
                        String property = column.getProperty();
                        Class propertyClass = column.getPropertyClass();

                        Object value = rdtResolver.getPropertyValue(waitFillData, property);

                        if (value != null) {
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
                                logger.info("rdt fill clear condition with rely property {}({}-[{}]) about {}{} set [{}({})={}] from [{}{}]", relyProperty, relyColumn.getPropertyClass().getName(), rdtResolver.getPropertyValue(waitFillData, relyProperty), waitClassModel.getClassName(), text, property, propertyClass.getName(), value, entityClass.getName(), conditionMark);
                            } else {
                                logger.info("rdt fill clear condition about {}{} set [{}({})={}] from [{}{}]", waitClassModel.getClassName(), text, property, propertyClass.getName(), value, entityClass.getName(), conditionMark);
                            }
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
                            logger.info("rdt fill clear column with rely property {}({}-[{}]) about {}{} set [{}({})->{}({})={}] from [{}{}]", relyProperty, relyColumn.getPropertyClass().getName(), rdtResolver.getPropertyValue(waitFillData, relyProperty), waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), conditionMark);
                        } else {
                            logger.debug("rdt fill column with rely property {}({}-[{}]) about {}{} set [{}({})->{}({})={}] from [{}{}]", relyProperty, relyColumn.getPropertyClass().getName(), rdtResolver.getPropertyValue(waitFillData, relyProperty), waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), conditionMark);
                        }
                    } else {
                        if (clearMark) {
                            logger.info("rdt fill clear column about {}{} set [{}({})->{}({})={}] from [{}{}]", waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), conditionMark);
                        } else {
                            logger.debug("rdt fill column about {}{} set [{}({})->{}({})={}] from [{}{}]", waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), conditionMark);
                        }
                    }

                }
            }
        }
    }



    public static boolean equalsElement(Collection collection1, Collection collection2) {
        boolean result = false;
        if (collection1 != null && collection2 != null) {
            if (collection1.equals(collection2)) {
                result = true;
            } else {
                if (collection1.size() == collection2.size()) {
                    result = collection1.containsAll(collection2);
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
        Map<Column, Column> targetColumnKeyMap = new HashMap<Column, Column>(16);

        List<Column> currentConditionColumnValues = new ArrayList<Column>(16);

        for (ModifyCondition modifyCondition : modifyConditionList) {
            Column targetColumn = modifyCondition.getTargetColumn();
            currentConditionColumnValues.add(targetColumn);
            targetColumnKeyMap.put(targetColumn, modifyCondition.getColumn());
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


        boolean isGroupValueNull = true;


        List<List<Object>> conditionGroupValues = currentKeyDetail.getConditionGroupValues();

        //根据当前条件列生成对应的值
        List<Object> currentConditionGroupValue = new ArrayList<Object>(16);
        for (Column targetColumn : currentConditionColumnValues) {
            //获取当前data对应的列
            Column column = targetColumnKeyMap.get(targetColumn);
            String property = column.getProperty();
            Object value = rdtResolver.getPropertyValue(data, property);
            if (!allowedNullValue && value == null) {
                throw new FillNotAllowedValueException(fillManyKeyModel, dataClass, data, describe, property, dataClassModel.getClassName() + " property " + property + " value not allowed null.");
            }
            if (value != null) {
                isGroupValueNull = false;
            }
            currentConditionGroupValue.add(value);
        }


        boolean hasContainsConditionGroupValue = false;
        for (List<Object> values : conditionGroupValues) {
            if (values.equals(currentConditionGroupValue)) {
                hasContainsConditionGroupValue = true;
                currentConditionGroupValue = values;
                break;
            }
        }

        //不存在时加入条件值组中
        if (!hasContainsConditionGroupValue) {
            if (isGroupValueNull) {
                currentKeyDetail.setGroupValueNullIndex(conditionGroupValues.size());
            }
            conditionGroupValues.add(currentConditionGroupValue);
        }

        List<Object> dataList;

        if (describe instanceof ModifyRelyDescribe) {
            ModifyRelyDescribe modifyRelyDescribe = (ModifyRelyDescribe) describe;
            //获取当前条件值下的relyDescribeMap
            Map<ModifyRelyDescribe, List<Object>> relyDescribeMap = currentKeyDetail.getRelyDescribeMap(currentConditionGroupValue);
            dataList = relyDescribeMap.get(modifyRelyDescribe);
            if (dataList == null) {
                dataList = new ArrayList<Object>(16);
                relyDescribeMap.put(modifyRelyDescribe, dataList);
            }
        } else {
            Map<ModifyDescribe, List<Object>> describeMap = currentKeyDetail.getDescribeMap(currentConditionGroupValue);

            dataList = describeMap.get(describe);
            if (dataList == null) {
                dataList = new ArrayList<Object>(16);
                describeMap.put(describe, dataList);
            }
        }
        dataList.add(data);

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
     * @param fillType
     */
    public void fillRelationshipHandle(final FillRSModel fillRSModel, Collection<?> collection, final boolean allowedNullValue, final boolean checkValue, final boolean clear, final FillType fillType) {
        if (collection != null && !collection.isEmpty()) {
            for (final Object data : collection) {
                if (data != null) {
                    //获取当前数据的类型
                    final Class dataClass = data.getClass();
                    if (rdtResolver.isIgnoreClass(dataClass)) {
                        continue;
                    }
                    ClassModel dataClassModel = configuration.getClassModel(dataClass);
                    if (dataClassModel == null) {
                        logger.debug("rdt not contains class {}, so builder for fill now.", dataClass.getName());
                        configuration.builderClass(dataClass);
                        dataClassModel = configuration.getClassModel(dataClass);
                    }
                    if (dataClassModel == null) {
                        continue;
                    }

                    final Map<Class, List<ModifyDescribe>> targetClassModifyDescribeMap = dataClassModel.getTargetClassModifyDescribeMap();

                    for (final Class entityClass : targetClassModifyDescribeMap.keySet()) {
                        //当前的修改信息
                        ClassModel entityClassModel = configuration.getClassModel(entityClass);
                        //基于entity data 修改 data model的数据
                        configuration.doModifyDescribeHandle(entityClassModel, dataClassModel, new RdtConfiguration.ModifyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel entityClassModel, ClassModel dataClassModel, ModifyDescribe describe) {
                                describe = configuration.getModifyDescribeForFill(describe, fillType);
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
                        ClassModel entityClassModel = configuration.getClassModel(entityClass);
                        configuration.doModifyRelyDescribeHandle(entityClassModel, dataClassModel, new RdtConfiguration.ModifyRelyDescribeCallBack() {
                            @Override
                            public void execute(ClassModel entityClassModel, ClassModel dataClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) {
                                describe = configuration.getModifyRelyDescribeForFill(describe, fillType);
                                List<ModifyCondition> conditionList = describe.getConditionList();
                                List<ModifyColumn> columnList = describe.getColumnList();
                                if (!columnList.isEmpty()) {
                                    int conditionSize = conditionList.size();
                                    if (conditionSize == 0) {
                                        logger.warn("rdt fill relationship handle continued : {} with rely property {} target about [{}(group={}&index={})] has no condition.", dataClassModel.getClassName(), relyColumn.getProperty(), entityClassModel.getClassName(), describe.getGroup(), describe.getIndex());
                                    } else {
                                        //获取当前依赖字段的值
                                        Object relyColumnValue = rdtResolver.getPropertyValue(data, relyColumn.getProperty());
                                        if (configuration.isMatchedType(describe, relyColumnValue, false)) {
                                            if (conditionSize == 1) {
                                                initOneKeyModelData(fillRSModel, entityClassModel, dataClassModel, describe, data, allowedNullValue);
                                            } else {
                                                initManyKeyModelData(fillRSModel, entityClassModel, dataClassModel, describe, data, allowedNullValue);
                                            }
                                        } else {
                                            RdtRelyModel relyModel = describe.getRdtRelyModel();
                                            boolean isAllowed = relyModel.isValueAllowed(relyColumnValue);
                                            if (!isAllowed) {
                                                logger.warn("rdt fill relationship handle check match type error : " + dataClassModel.getClassName() + " has no rely property " + relyColumn.getProperty() + " value " + relyColumnValue + " to appoint type : [{}(group={}&index={})], and current type values is {}", entityClassModel.getClassName(), describe.getGroup(), describe.getIndex(), relyModel.getExplicitValueList());
                                                if (checkValue) {
                                                    //验证数据合法性
                                                    throw new FillNotAllowedValueException(null, dataClass, data, describe, relyColumn.getProperty(), dataClassModel.getClassName() + " has no rely property " + relyColumn.getProperty() + " value " + relyColumnValue + " to appoint type : " + entityClassModel.getClassName());
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        });
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
                        fillRelationshipHandle(fillRSModel, toHandleList, allowedNullValue, checkValue, clear, fillType);
                    }
                }
            }
        }
    }


}
