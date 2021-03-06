package com.joker17.redundant.core;


import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.fill.FillType;
import com.joker17.redundant.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RdtConfiguration {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private RdtProperties properties;

    private RdtResolver rdtResolver;

    private RdtPropertiesBuilder propertiesBuilder;

    /**
     * 储存当前describe只保留transient列的信息
     */
    private Map<ModifyDescribe, ModifyDescribe> transientModifyDescribeCacheMap = new HashMap<ModifyDescribe, ModifyDescribe>(16);

    /**
     * 储存当前describe只保留persistent列的信息
     */
    private Map<ModifyDescribe, ModifyDescribe> persistentModifyDescribeCacheMap = new HashMap<ModifyDescribe, ModifyDescribe>(16);

    private Map<ModifyGroupDescribe, ModifyGroupDescribe> transientModifyGroupDescribeCacheMap = new HashMap<ModifyGroupDescribe, ModifyGroupDescribe>(16);

    private Map<ModifyGroupDescribe, ModifyGroupDescribe> persistentModifyGroupDescribeCacheMap = new HashMap<ModifyGroupDescribe, ModifyGroupDescribe>(16);

    public RdtConfiguration(RdtProperties properties, RdtPropertiesBuilder propertiesBuilder, RdtResolver rdtResolver) {
        this.properties = properties;
        this.rdtResolver = rdtResolver;
        this.propertiesBuilder = propertiesBuilder;
    }

    public RdtProperties getProperties() {
        return properties;
    }

    public void setProperties(RdtProperties properties) {
        this.properties = properties;
    }

    public RdtResolver getRdtResolver() {
        return rdtResolver;
    }

    public void setRdtResolver(RdtResolver rdtResolver) {
        this.rdtResolver = rdtResolver;
    }

    public ClassModel builderClass(Class currentClass) {
        return propertiesBuilder.builderClass(currentClass);
    }


    public ClassModel getClassModel(Class entityClass) {
        return rdtResolver.getClassModel(entityClass);
    }

    public String getPrimaryId(Class entityClass) {
        ClassModel classModel = getClassModel(entityClass);
        if (classModel == null) throw new IllegalArgumentException("not found classModel with type " + entityClass);
        return classModel.getPrimaryId();
    }


    public <R, T> Map<R, T> transferMap(Collection<T> data, String key) {
        return transferMap(data, key, new HashMap<R, T>());
    }

    /**
     * 将data集合转换成以实体属性的值为key的map
     * @param data
     * @param key
     * @param sourceMap
     * @param <T>
     * @return
     */
    public <R, T> Map<R, T> transferMap(Collection<T> data, String key, Map<R, T> sourceMap) {
        if (sourceMap == null) {
            sourceMap = new HashMap<R, T>();
        }
        if (data != null) {
            for (T t : data) {
                if (t != null) {
                    R keyValue = (R) rdtResolver.getPropertyValue(t, key);
                    sourceMap.put(keyValue, t);
                }
            }
        }
        return sourceMap;
    }


    public interface MatchedTypeCallback {
        /**
         * 满足在list中
         */
        void in(List<Object> inValList);

        /**
         * or
         */
        void or(List<Object> inValList, List<Object> notInValList);

        /**
         * 不处于该list中
         */
        void notIn(List<Object> notInValList);
    }


    /**
     * 将匹配类型值条件进行统一处理,通过回调函数在对应的方法中进行筛选
     * @param describe
     * @param callback
     * @param isUpdate
     */
    public void matchedTypeHandle(ModifyRelyDescribe describe, MatchedTypeCallback callback, boolean isUpdate) {

        if (callback == null || describe == null) {
            throw new IllegalArgumentException("matchedTypeCallback and modifyRelyDescribe must be not null!");
        }

        List<Object> notInValList = rdtResolver.getNewList(describe.getNotInValList());
        List<Object> valList = rdtResolver.getNewList(describe.getValList());

        if (isUpdate) {
            //为更新时移除需要忽略的列表值
            List<Object> updateIgnoresValList = rdtResolver.getNewList(describe.getUpdateIgnoresValList());
            if (!updateIgnoresValList.isEmpty()) {
                valList.removeAll(updateIgnoresValList);
            }
        }

        if (!valList.isEmpty()) {
            if (notInValList.isEmpty()) {
                callback.in(valList);
            } else {
                //满足在valList 或 非notInValList时
                callback.or(valList, notInValList);
            }
        } else {
            if (!notInValList.isEmpty()) {
                callback.notIn(notInValList);
            }
        }

    }

    /**
     * 依赖值是否满足匹配条件
     *
     * @param describe
     * @param relyColumnValue
     * @param isUpdate
     * @return
     */
    public boolean isMatchedType(ModifyRelyDescribe describe, final Object relyColumnValue, boolean isUpdate) {
        final boolean[] results = new boolean[]{false};
        matchedTypeHandle(describe, new MatchedTypeCallback() {
            @Override
            public void in(List<Object> inValList) {
                results[0] = inValList.contains(relyColumnValue);
            }

            @Override
            public void or(List<Object> inValList, List<Object> notInValList) {
                results[0] = inValList.contains(relyColumnValue) || !notInValList.contains(relyColumnValue);
            }

            @Override
            public void notIn(List<Object> notInValList) {
                results[0] = !notInValList.contains(relyColumnValue);
            }
        }, isUpdate);
        return results[0];
    }

    /**
     * 匹配数据是否通过验证
     * @param describe
     * @param data
     * @param classModel
     * @return
     */
    public boolean isMatchedType(ModifyDescribe describe, Object data, ClassModel classModel) {
        return isMatchedType(describe, data, classModel, true);
    }

    /**
     * 匹配数据是否通过验证
     * @param describe
     * @param data
     * @param classModel
     * @param isUpdate 是否为更新
     * @return
     */
    public boolean isMatchedType(ModifyDescribe describe, Object data, ClassModel classModel, boolean isUpdate) {
        boolean result = true;
        if (describe != null) {
            if (describe instanceof ModifyRelyDescribe) {
                ModifyRelyDescribe relyDescribe = (ModifyRelyDescribe) describe;
                String relyProperty = relyDescribe.getRelyColumn().getProperty();
                Object relyPropertyVal = rdtResolver.getPropertyValue(data, relyProperty);
                result = isMatchedType(relyDescribe, relyPropertyVal, isUpdate);
            }
        }
        return result;
    }


    /**
     * 判断两个值是否匹配
     * @param current
     * @param target
     * @return
     */
    public boolean isMatchedValue(Object current, Object target) {
        if (current == null && target == null) {
            return true;
        }

        if (current != null) {
            return current.equals(target);
        }

        return false;
    }

    public boolean isMatchedContainsValue(Object data, Object target) {
        if (data == null && target == null) {
            return true;
        }
        if (data != null) {
            if (data instanceof Collection) {
                if (target instanceof Collection) {
                    return data.equals(target) || ((Collection) data).containsAll((Collection<?>) target);
                }
                return ((Collection) data).contains(target);
            }
            return data.equals(target);
        }
        return false;
    }


    /**
     * 获取classModel中关于entityClass的修改信息
     * @param classModel
     * @param entityClass
     * @return
     */
    public List<ModifyDescribe> getModifyDescribeData(ClassModel classModel, Class entityClass) {
        List<ModifyDescribe> result = classModel.getTargetClassModifyDescribes(entityClass);
        if (result == null) {
            result = new ArrayList<ModifyDescribe>();
        }
        return result;
    }


    public List<ModifyGroupDescribe> getModifyGroupDescribeData(ClassModel classModel, Class entityClass) {
        List<ModifyGroupDescribe> result = classModel.getTargetClassModifyGroupDescribeMap().get(entityClass);
        if (result == null) {
            result = new ArrayList<ModifyGroupDescribe>();
        }
        return result;
    }


    /**
     * 根据发生改变的属性列表返回当前的修改条件,如果返回为空时则无需修改
     *
     * @param describe
     * @param changedPropertys 发生改变的属性列表
     * @return
     */
    public ModifyDescribe getModifyDescribe(ModifyDescribe describe, List<String> changedPropertys) {
        ModifyDescribe temp = null;
        List<ModifyColumn> columnList = new ArrayList<ModifyColumn>();  //当前值发生变化所要修改的列
        for (ModifyColumn modifyColumn : describe.getColumnList()) {
            //如果包含列时加入
            if (!modifyColumn.getColumn().getIsTransient() && changedPropertys.contains(modifyColumn.getTargetColumn().getProperty())) {
                columnList.add(modifyColumn);
            }
        }
        if (!columnList.isEmpty()) {
            temp = new ModifyDescribe();
            temp.setEntityClass(describe.getEntityClass());
            temp.setTargetClass(describe.getTargetClass());
            temp.setIndex(describe.getIndex());
            temp.setConditionList(describe.getConditionList());
            temp.setColumnList(columnList);
        }
        return getDeepCloneModifyDescribe(temp);
    }


    public ModifyDescribe getDeepCloneModifyDescribe(ModifyDescribe describe) {
        return getDeepCloneModifyDescribe(describe, true);
    }

    public ModifyDescribe getDeepCloneModifyDescribe(ModifyDescribe describe, boolean withConfig) {
        if (describe != null) {
            if (!withConfig || properties.getDeepCloneChangedModify()) {
                ModifyDescribe cloned = rdtResolver.deepClone(describe);

                List<ModifyColumn> clonedColumnList = cloned.getColumnList();
                List<ModifyColumn> columnList = describe.getColumnList();

                List<ModifyCondition> clonedConditionList = cloned.getConditionList();
                List<ModifyCondition> conditionList = describe.getConditionList();

                handleModifyColumnAndConditionDeepClone(clonedColumnList, columnList, clonedConditionList, conditionList);

                return cloned;
            }
        }
        return describe;
    }

    /**
     * 获取更新时关于changed property的modifyRelyDescribe数据
     * @param describe
     * @param changedPropertys
     * @return
     */
    public ModifyRelyDescribe getModifyRelyDescribe(ModifyRelyDescribe describe, List<String> changedPropertys) {
        ModifyRelyDescribe temp = null;
        List<ModifyColumn> columnList = new ArrayList<ModifyColumn>();

        List<Object> updateIgnoresValList = describe.getUpdateIgnoresValList();
        List<Object> valList = describe.getValList();

        //当忽略更新值不等同于所处值列表时才进行更新
        boolean willUpdate = !(valList.size() == updateIgnoresValList.size() && updateIgnoresValList.containsAll(valList));

        if (willUpdate) {
            for (ModifyColumn modifyColumn : describe.getColumnList()) {
                if (!modifyColumn.getColumn().getIsTransient() && changedPropertys.contains(modifyColumn.getTargetColumn().getProperty())) {
                    //值变化所要修改的列
                    columnList.add(modifyColumn);
                }
            }
        }

        if (!columnList.isEmpty()) {
            temp = new ModifyRelyDescribe();
            temp.setEntityClass(describe.getEntityClass());
            temp.setTargetClass(describe.getTargetClass());

            temp.setIndex(describe.getIndex());
            temp.setConditionList(describe.getConditionList());
            temp.setColumnList(columnList);

            temp.setGroup(describe.getGroup());
            temp.setValType(describe.getValType());
            temp.setRdtRelyModel(describe.getRdtRelyModel());
            temp.setRelyColumn(describe.getRelyColumn());
            temp.setValList(describe.getValList());
            temp.setNotInValList(describe.getNotInValList());
            temp.setUpdateIgnoresValList(describe.getUpdateIgnoresValList());
            temp.setNotAllowedTypeTips(describe.getNotAllowedTypeTips());
        }
        return getDeepCloneModifyRelyDescribe(temp);
    }

    //进行处理column
    private void handleModifyColumnAndConditionDeepClone(List<ModifyColumn> clonedColumnList, List<ModifyColumn> columnList, List<ModifyCondition> clonedConditionList, List<ModifyCondition> conditionList) {
        for (int i = 0; i < clonedColumnList.size(); i++) {
            ModifyColumn column = columnList.get(i);
            ModifyColumn clonedColumn = clonedColumnList.get(i);
            handleColumnDeepClone(clonedColumn, column);
     }

        for (int i = 0; i < clonedConditionList.size(); i++) {
            ModifyCondition condition = conditionList.get(i);
            ModifyCondition clonedCondition = clonedConditionList.get(i);
            handleColumnDeepClone(clonedCondition, condition);
        }
    }

    /**
     * 保持column地址不变
     * @param clonedColumn
     * @param sourceColumn
     */
    private void handleColumnDeepClone(ModifyColumn clonedColumn, ModifyColumn sourceColumn) {
        if (clonedColumn != null && sourceColumn != null) {
            clonedColumn.setColumn(sourceColumn.getColumn());
            clonedColumn.setTargetColumn(sourceColumn.getTargetColumn());
        }
    }


    public ModifyRelyDescribe getDeepCloneModifyRelyDescribe(ModifyRelyDescribe describe, boolean withConfig) {
        if (describe != null) {
            if (!withConfig || properties.getDeepCloneChangedModify()) {
                ModifyRelyDescribe cloned = rdtResolver.deepClone(describe);

                cloned.setRelyColumn(describe.getRelyColumn());

                List<ModifyColumn> clonedColumnList = cloned.getColumnList();
                List<ModifyColumn> columnList = describe.getColumnList();

                List<ModifyCondition> clonedConditionList = cloned.getConditionList();
                List<ModifyCondition> conditionList = describe.getConditionList();

                handleModifyColumnAndConditionDeepClone(clonedColumnList, columnList, clonedConditionList, conditionList);

                return cloned;
            }
        }
        return describe;
    }


    public ModifyRelyDescribe getDeepCloneModifyRelyDescribe(ModifyRelyDescribe describe) {
        return getDeepCloneModifyRelyDescribe(describe, true);
    }

    private void handleModifyGroupConcatColumnAndModifyGroupKeysColumnDeepClone(List<ModifyGroupConcatColumn> clonedColumnList, List<ModifyGroupConcatColumn> columnList, ModifyGroupKeysColumn clonedKeysColumn, ModifyGroupKeysColumn keysColumn) {
        for (int i = 0; i < clonedColumnList.size(); i++) {
            ModifyGroupConcatColumn column = columnList.get(i);
            ModifyGroupConcatColumn clonedColumn = clonedColumnList.get(i);
            handleColumnDeepClone(clonedColumn, column);
        }
        handleColumnDeepClone(clonedKeysColumn, keysColumn);
    }

    private void handleColumnDeepClone(ModifyGroupKeysColumn clonedColumn, ModifyGroupKeysColumn sourceColumn) {
        if (clonedColumn != null && sourceColumn != null) {
            clonedColumn.setColumn(sourceColumn.getColumn());
            clonedColumn.setTargetColumn(sourceColumn.getTargetColumn());
            clonedColumn.setGainSelectColumn(sourceColumn.getGainSelectColumn());
            clonedColumn.setGainConditionColumnList(sourceColumn.getGainConditionColumnList());
            clonedColumn.setGainConditionValueRelyColumnList(sourceColumn.getGainConditionValueRelyColumnList());
        }
    }

    private void handleColumnDeepClone(ModifyGroupBaseColumn clonedColumn, ModifyGroupBaseColumn sourceColumn) {
        if (clonedColumn != null && sourceColumn != null) {
            clonedColumn.setColumn(sourceColumn.getColumn());
            clonedColumn.setTargetColumn(sourceColumn.getTargetColumn());
        }
    }


    public ModifyGroupDescribe getDeepCloneModifyGroupDescribe(ModifyGroupDescribe describe, boolean withConfig) {
        if (describe != null) {
            if (!withConfig || properties.getDeepCloneChangedModify()) {
                ModifyGroupDescribe cloned = rdtResolver.deepClone(describe);
                List<ModifyGroupConcatColumn> clonedColumnList = cloned.getModifyGroupConcatColumnList();
                List<ModifyGroupConcatColumn> columnList = describe.getModifyGroupConcatColumnList();

                ModifyGroupKeysColumn clonedKeysColumn = cloned.getModifyGroupKeysColumn();
                ModifyGroupKeysColumn keysColumn = describe.getModifyGroupKeysColumn();

                handleModifyGroupConcatColumnAndModifyGroupKeysColumnDeepClone(clonedColumnList, columnList, clonedKeysColumn, keysColumn);
                return cloned;
            }
        }
        return describe;
    }

    /**
     * 获取当前非base类所对应的所有关联关系数据集合
     * @param complexClass
     * @return
     */
    public List<ComplexAnalysis> getComplexAnalysisList(Class complexClass) {
       return rdtResolver.getComplexAnalysisList(complexClass);
    }

    public static abstract class ModifyRelyDescribeCallBack {
        /**
         * 回调函数
         * @param classModel 更新的实体classModel
         * @param modifyClassModel 当前处理的classModel
         * @param relyColumn 依赖列
         * @param group group index
         * @param describe
         */
        public abstract void execute(ClassModel classModel, ClassModel modifyClassModel, Column relyColumn, int group, ModifyRelyDescribe describe);
    }

    public void doModifyRelyDescribeHandle(ClassModel classModel, ClassModel modifyClassModel, ModifyRelyDescribeCallBack callBack) {
        Class entityClass = classModel.getCurrentClass();
        Map<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>> describeMap = modifyClassModel.getTargetClassModifyRelyDescribeMap();
        //获取当前实体所相关的依赖列及修改相关的数据
        Map<Column, Map<Integer, List<ModifyRelyDescribe>>> entityClassRelyMap = describeMap.get(entityClass);
        if (entityClassRelyMap != null && !entityClassRelyMap.isEmpty()) {
            for (Column relyColumn : entityClassRelyMap.keySet()) {
                Map<Integer, List<ModifyRelyDescribe>> groupDescribeList = entityClassRelyMap.get(relyColumn);
                if (groupDescribeList != null) {
                    for (Integer group : groupDescribeList.keySet()) {
                        List<ModifyRelyDescribe> modifyDescribeList = groupDescribeList.get(group);
                        if (modifyDescribeList != null && !modifyDescribeList.isEmpty()) {
                            for (ModifyRelyDescribe describe : modifyDescribeList) {
                                callBack.execute(classModel, modifyClassModel, relyColumn, group, describe);
                            }
                        }
                    }
                }
            }
        }
    }

    public static abstract class ModifyDescribeCallBack {
        /**
         * 回调函数
         * @param classModel 更新的实体classModel
         * @param modifyClassModel 当前要修改的classModel
         * @param describe
         */
        public abstract void execute(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribe describe);
    }

    //获取modifyClassModel中关于classModel的modifyDescribe信息
    public void doModifyDescribeHandle(ClassModel classModel, ClassModel modifyClassModel, ModifyDescribeCallBack callBack) {
        Class entityClass = classModel.getCurrentClass();
        List<ModifyDescribe> modifyDescribeList = getModifyDescribeData(modifyClassModel, entityClass);
        if (!modifyDescribeList.isEmpty()) {
            for (ModifyDescribe modifyDescribe : modifyDescribeList) {
                callBack.execute(classModel, modifyClassModel, modifyDescribe);
            }
        }
    }




    public static abstract class ModifyGroupDescribeCallBack {
        public abstract void execute(ClassModel classModel, ClassModel modifyClassModel, ModifyGroupDescribe describe);
    }


    public void doModifyGroupDescribeHandle(ClassModel classModel, ClassModel modifyClassModel, ModifyGroupDescribeCallBack callBack) {
        Class entityClass = classModel.getCurrentClass();
        List<ModifyGroupDescribe> modifyDescribeList = getModifyGroupDescribeData(modifyClassModel, entityClass);
        if (!modifyDescribeList.isEmpty()) {
            for (ModifyGroupDescribe modifyDescribe : modifyDescribeList) {
                callBack.execute(classModel, modifyClassModel, modifyDescribe);
            }
        }
    }




    public static abstract class ModifyColumnCallBack {

        public abstract void execute(ModifyColumn modifyColumn, int position, String targetProperty, Object targetPropertyVal);
    }

    /**
     * 回调处理modifyColumn的值
     * @param vo
     * @param describe
     * @param callBack
     */
    public void doModifyColumnHandle(ChangedVo vo, ModifyDescribe describe, ModifyColumnCallBack callBack) {
        List<ModifyColumn> modifyColumnList = describe.getColumnList();
        int index = 0;

        for (ModifyColumn modifyColumn : modifyColumnList) {
            String targetProperty = modifyColumn.getTargetColumn().getProperty();
            Object val = vo.getCurrentVal(targetProperty);
            try {
                val = rdtResolver.cast(val, modifyColumn.getColumn().getPropertyClass()); //转换值
            } catch (Exception e) {
                logger.warn("rdt cast val error", e);
            }
            callBack.execute(modifyColumn, index ++, targetProperty, val);
        }
    }


    public static abstract class ModifyConditionCallBack {

        public abstract void execute(ModifyCondition modifyCondition, int position, String targetProperty, Object targetPropertyVal);
    }

    /**
     * 回调处理modifyCondition的值
     * @param vo
     * @param describe
     * @param callBack
     */
    public void doModifyConditionHandle(ChangedVo vo, ModifyDescribe describe, ModifyConditionCallBack callBack) {
        List<ModifyCondition> conditionList = describe.getConditionList();
        int index = 0;
        for (ModifyCondition modifyCondition : conditionList) {
            String targetProperty = modifyCondition.getTargetColumn().getProperty();
            Object val = vo.getCurrentVal(targetProperty);
            callBack.execute(modifyCondition, index ++, targetProperty, val);
        }

    }

    public static abstract class LogicalModelCallBack {
        public abstract void execute(ClassModel dataModel, LogicalModel logicalModel);
    }

    public void doLogicalModelHandle(Class dataClass, LogicalModelCallBack callBack) {
        doLogicalModelHandle(getClassModel(dataClass), callBack);
    }


    public void doLogicalModelHandle(ClassModel dataModel, LogicalModelCallBack callBack) {
        doLogicalModelHandle(dataModel, true, callBack);
    }

    /**
     * 处理逻辑属性列
     * @param dataModel
     * @param logical 是否启用逻辑值
     * @param callBack
     */
    public void doLogicalModelHandle(ClassModel dataModel, boolean logical, LogicalModelCallBack callBack) {
        if (logical && dataModel != null) {
            LogicalModel logicalMode = dataModel.getLogicalModel();
            if (logicalMode != null) {
                //逻辑值对象存在时进行处理
                callBack.execute(dataModel, logicalMode);
            }
        } else {
            callBack = null;
        }
    }


    /**
     * 是否移除当前列避免填充(依据是否为填充持久化模式)
     * @param modifyColumn
     * @param isPersistentType
     * @return
     */
    public boolean isModifyColumnRemove(ModifyColumn modifyColumn, boolean isPersistentType) {
        RdtFillType fillType;
        if (isPersistentType) {
            //填充持久化列时(save)
            fillType = modifyColumn.getFillSaveType();
        } else {
            fillType = modifyColumn.getFillShowType();
        }
        return isColumnRemove(modifyColumn.getColumn(), fillType, isPersistentType);
    }

    public boolean isModifyGroupConcatColumnRemove(ModifyGroupConcatColumn modifyGroupConcatColumn, boolean isPersistentType) {
        RdtFillType fillType;
        if (isPersistentType) {
            fillType = modifyGroupConcatColumn.getFillSaveType();
        } else {
            fillType = modifyGroupConcatColumn.getFillShowType();
        }
        return isColumnRemove(modifyGroupConcatColumn.getColumn(), fillType, isPersistentType);
    }


    protected boolean isColumnRemove(Column column, RdtFillType fillType, boolean isPersistentType) {
        boolean isTransient = column.getIsTransient();
        switch (fillType) {
            case DEFAULT:
                if (isPersistentType) {
                    //持久化列时,如果是非持久化时移除
                    return isTransient;
                }
                return !isTransient;
            case ENABLE:
                //跟随填充不移除
                return false;
            case DISABLE:
                //不跟随填充移除
                return true;
        }
        return true;

    }
    /**
     * 获取根据填充模式所处理后的ModifyDescribe
     * @param describe
     * @param type 填充类型
     */
    public ModifyDescribe getModifyDescribeForFill(ModifyDescribe describe, FillType type) {
        if (FillType.ALL == type) {
            return describe;
        }
        //获取当前describe关于其他两种FillType处理后的结果(column是否为非持久化字段不会改变,即对应结果是唯一的.)
        boolean isPersistentType = FillType.PERSISTENT == type;
        Map<ModifyDescribe, ModifyDescribe> describeMap = isPersistentType ? persistentModifyDescribeCacheMap : transientModifyDescribeCacheMap;
        ModifyDescribe result = describeMap.get(describe);

        if (result == null) {
            synchronized (RdtConfiguration.class) {
                result = describeMap.get(describe);
                if (result == null) {
                    ModifyDescribe current = getDeepCloneModifyDescribe(describe, false);
                    List<ModifyColumn> columnList = current.getColumnList();
                    for (Iterator<ModifyColumn> columnIterator = columnList.iterator(); columnIterator.hasNext();) {
                        if (isModifyColumnRemove(columnIterator.next(), isPersistentType)) {
                            columnIterator.remove();
                        }
                    }
                    result = current;
                    describeMap.put(describe, result);
                }
            }
        }
        return result;
    }

    public ModifyRelyDescribe getModifyRelyDescribeForFill(ModifyRelyDescribe describe, FillType type) {
        if (FillType.ALL == type) {
            return describe;
        }
        boolean isPersistentType = FillType.PERSISTENT == type;
        Map<ModifyDescribe, ModifyDescribe> describeMap = isPersistentType ? persistentModifyDescribeCacheMap : transientModifyDescribeCacheMap;
        ModifyDescribe result = describeMap.get(describe);

        if (result == null) {
            synchronized (RdtConfiguration.class) {
                result = describeMap.get(describe);
                if (result == null) {
                    ModifyRelyDescribe current = getDeepCloneModifyRelyDescribe(describe, false);
                    List<ModifyColumn> columnList = current.getColumnList();
                    for (Iterator<ModifyColumn> columnIterator = columnList.iterator(); columnIterator.hasNext();) {
                        if (isModifyColumnRemove(columnIterator.next(), isPersistentType)) {
                            columnIterator.remove();
                        }
                    }
                    result = current;
                    describeMap.put(describe, result);
                }
            }
        }
        return (ModifyRelyDescribe) result;
    }



    public ModifyGroupDescribe getModifyGroupDescribeForFill(ModifyGroupDescribe describe, FillType type) {
        if (FillType.ALL == type) {
            return describe;
        }
        boolean isPersistentType = FillType.PERSISTENT == type;
        Map<ModifyGroupDescribe, ModifyGroupDescribe> describeMap = isPersistentType ? persistentModifyGroupDescribeCacheMap : transientModifyGroupDescribeCacheMap;
        ModifyGroupDescribe result = describeMap.get(describe);
        if (result == null) {
            synchronized (RdtConfiguration.class) {
                result = describeMap.get(describe);
                if (result == null) {
                    ModifyGroupDescribe current = getDeepCloneModifyGroupDescribe(describe, false);
                    List<ModifyGroupConcatColumn> columnList = current.getModifyGroupConcatColumnList();
                    for (Iterator<ModifyGroupConcatColumn> columnIterator = columnList.iterator(); columnIterator.hasNext();) {
                        if (isModifyGroupConcatColumnRemove(columnIterator.next(), isPersistentType)) {
                            columnIterator.remove();
                        }
                    }
                    result = current;
                    describeMap.put(describe, result);
                }
            }
        }
        return result;
    }

}
