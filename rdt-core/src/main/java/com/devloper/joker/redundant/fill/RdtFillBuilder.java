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


    public void setFillKeyData(Map<Object, Object> entityDataMap, Class entityClass, String key, Object keyValue, Set<Object> waitFillSet, Object describe) {

        List<ModifyColumn> columnList;
        Column relyColumn = null;
        if (describe instanceof ModifyDescribe) {
            columnList = ((ModifyDescribe) describe).getColumnList();
        } else {
            ModifyRelyDescribe modifyRelyDescribe = (ModifyRelyDescribe) describe;
            columnList = modifyRelyDescribe.getColumnList();
            relyColumn = modifyRelyDescribe.getRelyColumn();
        }
        //获取基于当前key值所对应的base class实体数据
        Object entityData = entityDataMap.get(keyValue);
        if (entityData != null) {
            //依据列信息进行赋值
            for (Object waitFillData : waitFillSet) {
                for (ModifyColumn modifyColumn : columnList) {
                    Column column = modifyColumn.getColumn();
                    String property = column.getProperty();
                    Class propertyClass = column.getPropertyClass();

                    Column targetColumn = modifyColumn.getTargetColumn();
                    String targetProperty = targetColumn.getProperty();
                    Class targetPropertyClass = targetColumn.getPropertyClass();

                    //获取要填充列字段所对应的值
                    Object value = rdtResolver.getPropertyValue(entityData, targetProperty);
                    try {
                        //转换值
                        value = rdtResolver.cast(value, propertyClass);
                    } catch (Exception e) {
                        logger.warn("rdt cast val error", e);
                    }

                    //设置当前填充列字段的值
                    rdtResolver.setPropertyValue(waitFillData, property, value);

                    if (logger.isDebugEnabled()) {
                        ClassModel waitClassModel = support.getClassModel(waitFillData.getClass());
                        String text = "";
                        String primaryId = waitClassModel.getPrimaryId();
                        if (StringUtils.isNotEmpty(primaryId)) {
                            text = "[" + primaryId + "=" + rdtResolver.getPropertyValue(waitFillData, primaryId) + "]";
                        }
                        if (relyColumn != null) {
                            String relyProperty = relyColumn.getProperty();
                            logger.debug("rdt fill with rely property {}({}-[{}]) about {}{} set [{}({})->{}({})={}] from [{}[{}={}]", relyProperty, relyColumn.getPropertyClass().getName(), rdtResolver.getPropertyValue(waitFillData, relyProperty), waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), key, keyValue);
                        } else {
                            logger.debug("rdt fill about {}{} set [{}({})->{}({})={}] from [{}[{}={}]", waitClassModel.getClassName(), text, property, propertyClass.getName(), targetProperty, targetPropertyClass.getName(), value, entityClass.getName(), key, keyValue);
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

    private void initManyKeyModelData(FillRSModel fillRSModel, Class entityClass, Object describe, Object data) {
        ModifyDescribe modifyDescribe = null;
        ModifyRelyDescribe modifyRelyDescribe = null;
        List<ModifyCondition> modifyConditionList;
        List<ModifyColumn> modifyColumnList;

        if (describe instanceof ModifyDescribe) {
            modifyDescribe = (ModifyDescribe) describe;
            modifyColumnList = modifyDescribe.getColumnList();
            modifyConditionList = modifyDescribe.getConditionList();

        } else {
            modifyRelyDescribe = (ModifyRelyDescribe) describe;
            modifyColumnList = modifyRelyDescribe.getColumnList();
            modifyConditionList = modifyRelyDescribe.getConditionList();
        }

        Map<Class, FillManyKeyModel> fillManyKeyModelMap = fillRSModel.getFillManyKeyModelMap();

        FillManyKeyModel fillManyKeyModel = fillManyKeyModelMap.get(entityClass);

        if (fillManyKeyModel == null) {
            fillManyKeyModel = new FillManyKeyModel();
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

        //获取当前对应的条件列值
        List<Object> currentConditionGroupValue = new ArrayList<Object>(16);
        List<List<Object>> conditionGroupValues = currentKeyDetail.getConditionGroupValues();
        for (ModifyCondition modifyCondition : columnModifyConditionMap.values()) {
            Column column = modifyCondition.getColumn();
            currentConditionGroupValue.add(rdtResolver.getPropertyValue(data, column.getProperty()));
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
        if (modifyDescribe != null) {
            Map<List<Object>, Map<ModifyDescribe, Set<Object>>> describeDataMap = currentKeyDetail.getConditionGroupValueDescribeDataMap();
            Map<ModifyDescribe, Set<Object>> describeSetMap = describeDataMap.get(currentConditionGroupValue);
            if (describeSetMap == null) {
                describeSetMap = new HashMap<ModifyDescribe, Set<Object>>(16);
                describeDataMap.put(currentConditionGroupValue, describeSetMap);
            }

            dataSet = describeSetMap.get(modifyDescribe);
            if (dataSet == null) {
                dataSet = new HashSet<Object>(16);
                describeSetMap.put(modifyDescribe, dataSet);
            }
        } else {
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
        }
        dataSet.add(data);

    }

    /**
     * 处理关系
     * @param fillRSModel
     * @param collection
     * @param withNullKeyValue
     * @param checkValue
     * @param clear
     */
    public void fillRelationshipHandle(final FillRSModel fillRSModel, Collection<?> collection, final boolean withNullKeyValue, final boolean checkValue, final boolean clear) {
        if (collection != null && !collection.isEmpty()) {
            final Map<Class, List<FillKeyModel>> fillKeyModelListMap = fillRSModel.getFillKeyModelListMap();

            for (final Object data : collection) {
                if (data != null) {
                    //获取当前数据的类型
                    Class dataClass = data.getClass();
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
                        try {
                            //基于entity data 修改 data model的数据
                            support.doModifyDescribeHandle(entityClassModel, dataClassModel, new RdtSupport.ModifyDescribeCallBack() {
                                @Override
                                public void execute(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe) throws Exception {
                                    List<ModifyCondition> conditionList = describe.getConditionList();
                                    List<ModifyColumn> columnList = describe.getColumnList();

                                    if (!columnList.isEmpty()) {
                                        int conditionSize = conditionList.size();
                                        if (conditionSize == 0) {
                                            logger.warn("rdt fill relationship handle continued : {} target about [{}(index={})] has no condition.", modifyClassModel.getClassName(), classModel.getClassName(), describe.getIndex());
                                        } else if (conditionSize == 1) {
                                            ModifyCondition modifyCondition = conditionList.get(0);
                                            //处理当前entityClass某单列作为key查询条件的数据
                                            FillKeyModel fillKeyModel = fillRSModel.getFillKeyModel(entityClass, modifyCondition, fillKeyModelListMap);

                                            for (ModifyColumn modifyColumn : columnList) {
                                                fillKeyModel.addColumnValue(modifyColumn.getTargetColumn());
                                            }
                                            //获取关于entity class的key val
                                            Object keyVal = rdtResolver.getPropertyValue(data, modifyCondition.getColumn().getProperty());
                                            fillKeyModel.addDescribeKeyValueData(describe, keyVal, data, withNullKeyValue);
                                        } else {
                                            initManyKeyModelData(fillRSModel, entityClass, describe, data);
                                        }

                                    }
                                }
                            });
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }

                    for (final Class entityClass : dataClassModel.getTargetRelyModifyClassSet()) {
                        ClassModel entityClassModel = support.getClassModel(entityClass);

                        try {
                            support.doModifyRelyDescribeHandle(entityClassModel, dataClassModel, new RdtSupport.ModifyRelyDescribeCallBack() {
                                @Override
                                public void execute(ClassModel classModel, ClassModel modifyClassModel, Column relyColumn, int group, ModifyRelyDescribe describe) throws Exception {
                                    List<ModifyCondition> conditionList = describe.getConditionList();
                                    List<ModifyColumn> columnList = describe.getColumnList();

                                    if (!columnList.isEmpty()) {
                                        int conditionSize = conditionList.size();
                                        if (conditionSize == 0) {
                                            logger.warn("rdt fill relationship handle continued : {} with rely property {} target about [{}(group={}&index={})] has no condition.", modifyClassModel.getClassName(), relyColumn.getProperty(), classModel.getClassName(), describe.getGroup(), describe.getIndex());
                                        } else {
                                            //获取当前依赖字段的值
                                            Object relyColumnValue = rdtResolver.getPropertyValue(data, relyColumn.getProperty());
                                            if (isMatchedType(describe, relyColumnValue)) {
                                                if (conditionSize == 1) {
                                                    //处理当前entityClass某单列作为key查询条件的数据
                                                    ModifyCondition modifyCondition = conditionList.get(0);
                                                    FillKeyModel fillKeyModel = fillRSModel.getFillKeyModel(entityClass, modifyCondition, fillKeyModelListMap);
                                                    for (ModifyColumn modifyColumn : columnList) {
                                                        fillKeyModel.addColumnValue(modifyColumn.getTargetColumn());
                                                    }
                                                    Object keyVal = rdtResolver.getPropertyValue(data, modifyCondition.getColumn().getProperty());
                                                    fillKeyModel.addDescribeKeyValueData(describe, keyVal, data, withNullKeyValue);
                                                } else {
                                                    initManyKeyModelData(fillRSModel, entityClass, describe, data);
                                                }
                                            } else {
                                                RdtRelyModel relyModel = describe.getRdtRelyModel();
                                                boolean isAllowed = relyModel.isValueAllowed(relyColumnValue);
                                                if (!isAllowed) {
                                                    logger.warn("rdt fill relationship handle check macth type error : " + modifyClassModel.getClassName() + " has no rely property " + relyColumn.getProperty() + " value " + relyColumnValue + " to appoint type : [{}(group={}&index={})], and current type values is {}", classModel.getClassName(), describe.getGroup(), describe.getIndex(), relyModel.getExplicitValueList());
                                                    if (checkValue) {
                                                        //验证数据合法性
                                                        throw new IllegalArgumentException(modifyClassModel.getClassName() + " has no rely property " + relyColumn.getProperty() + " value " + relyColumnValue + " to appoint type : " + classModel.getClassName());
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
                        fillRelationshipHandle(fillRSModel, toHandleList, withNullKeyValue, checkValue, clear);
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
