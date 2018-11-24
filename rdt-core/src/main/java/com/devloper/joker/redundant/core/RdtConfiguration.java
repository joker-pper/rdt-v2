package com.devloper.joker.redundant.core;


import com.devloper.joker.redundant.fill.FillType;
import com.devloper.joker.redundant.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RdtConfiguration {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private RdtProperties properties;

    private RdtResolver rdtResolver;

    private RdtPropertiesBuilder propertiesBuilder;

    private Map<Class, List<List<ComplexModel>>> complexResultListMap = new HashMap<Class, List<List<ComplexModel>>>(16);

    private Map<Class, List<ComplexAnalysis>> complexAnalysisResultListMap = new HashMap<Class, List<ComplexAnalysis>>(16);

    /**
     * 储存当前describe只保留transient列的信息
     */
    private Map<ModifyDescribe, ModifyDescribe> transientModifyDescribeCacheMap = new HashMap<ModifyDescribe, ModifyDescribe>(16);

    /**
     * 储存当前describe只保留persistent列的信息
     */
    private Map<ModifyDescribe, ModifyDescribe> persistentModifyDescribeCacheMap = new HashMap<ModifyDescribe, ModifyDescribe>(16);

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

    public void builderClass(Class currentClass) {
        propertiesBuilder.builderClass(currentClass);
    }


    public ClassModel getClassModel(Class entityClass) {
        return properties.getClassModel(entityClass);
    }

    public String getPrimaryId(Class entityClass) {
        ClassModel classModel = getClassModel(entityClass);
        if (classModel == null) throw new IllegalArgumentException("not found classModel with type " + entityClass);
        return classModel.getPrimaryId();
    }


    /**
     * 获取以key值为key的map数据
     * @param data
     * @param key
     * @param <T>
     * @return
     */
    public <T> Map<Object, T> getKeyMap(Collection<T> data, String key) {
        Map<Object, T> result = new LinkedHashMap<Object, T>(16);
        if (data != null && !data.isEmpty()) {
            for (T t : data) {
                result.put(rdtResolver.getPropertyValue(t, key), t);
            }
        }
        return result;
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
            if (!result && !unknowNotExistValList.isEmpty()) {
                //未包含valList判断是否在非unknowNotExistValList中
                result = !unknowNotExistValList.contains(relyColumnValue);
            }

        } else {
            if (!unknowNotExistValList.isEmpty()) {
                //满足非unknowNotExistValList时
                result = !unknowNotExistValList.contains(relyColumnValue);
            }
        }
        return result;
    }


    /**
     * 获取classModel中关于entityClass的修改信息
     * @param classModel
     * @param entityClass
     * @return
     */
    public List<ModifyDescribe> getModifyDescribeData(ClassModel classModel, Class entityClass) {
        List<ModifyDescribe> result = classModel.getTargetClassModifyDescribes(entityClass);
        if (result == null) result = new ArrayList<ModifyDescribe>();
        return result;
    }

    /**
     * 根据发生改变的属性列表返回当前的修改条件,如果返回为空时则无需修改
     *
     * @param modifyDescribe
     * @param changedPropertys 发生改变的属性列表
     * @return
     */
    public ModifyDescribe getModifyDescribe(ModifyDescribe modifyDescribe, List<String> changedPropertys) {
        ModifyDescribe temp = null;
        List<ModifyColumn> columnList = new ArrayList<ModifyColumn>();  //当前值发生变化所要修改的列
        for (ModifyColumn modifyColumn : modifyDescribe.getColumnList()) {
            //如果包含列时加入
            if (!modifyColumn.getColumn().getIsTransient() && changedPropertys.contains(modifyColumn.getTargetColumn().getProperty())) {
                columnList.add(modifyColumn);
            }
        }
        if (!columnList.isEmpty()) {
            temp = new ModifyDescribe();
            temp.setIndex(modifyDescribe.getIndex());
            temp.setConditionList(modifyDescribe.getConditionList());
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
        for (ModifyColumn modifyColumn : describe.getColumnList()) {
            if (!modifyColumn.getColumn().getIsTransient() && changedPropertys.contains(modifyColumn.getTargetColumn().getProperty())) { //值变化所要修改的列
                columnList.add(modifyColumn);
            }
        }

        if (!columnList.isEmpty()) {

            temp = new ModifyRelyDescribe();
            temp.setIndex(describe.getIndex());
            temp.setConditionList(describe.getConditionList());
            temp.setColumnList(columnList);

            temp.setGroup(describe.getGroup());
            temp.setValType(describe.getValType());
            temp.setRdtRelyModel(describe.getRdtRelyModel());
            temp.setRelyColumn(describe.getRelyColumn());
            temp.setValList(describe.getValList());
            temp.setUnknowNotExistValList(describe.getUnknowNotExistValList());
        }
        return getDeepCloneModifyRelyDescribe(temp);
    }

    //将未被序列话的field进行处理
    private void handleModifyColumnAndConditionDeepClone(List<ModifyColumn> clonedColumnList, List<ModifyColumn> columnList, List<ModifyCondition> clonedConditionList, List<ModifyCondition> conditionList) {
        for (int i = 0; i < clonedColumnList.size(); i++) {
            ModifyColumn column = columnList.get(i);
            ModifyColumn clonedColumn = clonedColumnList.get(i);

            clonedColumn.getColumn().setField(column.getColumn().getField());
            clonedColumn.getTargetColumn().setField(column.getTargetColumn().getField());
        }

        for (int i = 0; i < clonedConditionList.size(); i++) {
            ModifyCondition condition = conditionList.get(i);
            ModifyCondition clonedCondition = clonedConditionList.get(i);

            clonedCondition.getColumn().setField(condition.getColumn().getField());
            clonedCondition.getTargetColumn().setField(condition.getTargetColumn().getField());
        }
    }

    public ModifyRelyDescribe getDeepCloneModifyRelyDescribe(ModifyRelyDescribe describe, boolean withConfig) {
        if (describe != null) {
            if (!withConfig || properties.getDeepCloneChangedModify()) {
                ModifyRelyDescribe cloned = rdtResolver.deepClone(describe);
                cloned.getRelyColumn().setField(describe.getRelyColumn().getField());
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


    //获取当前复杂对象的组合,返回数组按照倒序的属性方式
    public List<List<ComplexModel>> getComplexModelParseResult(Class complexClass) {
        List<List<ComplexModel>> results = complexResultListMap.get(complexClass);
        if (results == null) {
            synchronized (RdtConfiguration.class) {
                results = complexResultListMap.get(complexClass);
                boolean flag = results == null;
                if (flag) {
                    results = new ArrayList<List<ComplexModel>>();
                    complexResultListMap.put(complexClass, results);
                    List<ComplexModel> complexObjects = properties.getClassComplexModelsMap().get(complexClass);
                    if (complexObjects != null) {
                        for (ComplexModel complexObject : complexObjects) {
                            if (complexObject.getOwnerBase()) {
                                List<ComplexModel> result = new ArrayList<ComplexModel>();
                                result.add(complexObject);
                                results.add(result);
                            } else {
                                List<List<ComplexModel>> currents = getComplexModelParseResult(complexObject.getOwnerType());
                                for (List<ComplexModel> current : currents) {
                                    List<ComplexModel> result = new ArrayList<ComplexModel>();
                                    result.add(complexObject);
                                    result.addAll(current);
                                    results.add(result);
                                }
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * 获取当前非base类所对应的所有关联关系数据集合
     * @param complexClass
     * @return
     */
    public List<ComplexAnalysis> getComplexAnalysisList(Class complexClass) {
        List<ComplexAnalysis> result = complexAnalysisResultListMap.get(complexClass);
        if (result == null) {
            synchronized (RdtConfiguration.class) {
                result = complexAnalysisResultListMap.get(complexClass);
                if (result == null) {
                    result = new ArrayList<ComplexAnalysis>(16);
                    complexAnalysisResultListMap.put(complexClass, result);
                    List<List<ComplexModel>> complexResults = getComplexModelParseResult(complexClass);
                    for (List<ComplexModel> complexResult : complexResults) {
                        ComplexAnalysis complexAnalysis = getComplexAnalysis(complexResult);
                        result.add(complexAnalysis);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 解析当前关系列表的结果,即base类的关联关系
     * @param complexModelList
     * @return e.g
     *  {"currentTypeList": ["com.devloper.joker.rdt_sbm.model.Reply"],"hasMany":false,"oneList":[true],"prefix":"reply","propertyList":["reply"],"rootClass":"com.devloper.joker.rdt_sbm.domain.Article"}
     *
     */
    public ComplexAnalysis getComplexAnalysis(List<ComplexModel> complexModelList) {
        ComplexAnalysis complexAnalysis = new ComplexAnalysis();
        StringBuilder sb = new StringBuilder();
        int size = complexModelList.size();
        for (int i = size - 1; i >= 0; i--) {
            ComplexModel complexModel = complexModelList.get(i);
            if (i == size - 1) {
                complexAnalysis.setRootClass(complexModel.getOwnerType());
                complexAnalysis.setHasMany(false);
            }
            List<Boolean> oneList = complexAnalysis.getOneList();
            List<String> propertyList = complexAnalysis.getPropertyList();
            List<Class> currentTypeList = complexAnalysis.getCurrentTypeList();

            String property = complexModel.getProperty();
            Boolean isOne = complexModel.getIsOne();
            if (!isOne) complexAnalysis.setHasMany(true);
            sb.append(property);
            sb.append(".");

            currentTypeList.add(complexModel.getCurrentType());
            propertyList.add(property);
            oneList.add(isOne);
        }

        int length = sb.length();
        if (length > 0) {
            sb.delete(length - 1, length);
            complexAnalysis.setPrefix(sb.toString());
        }
        return complexAnalysis;
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

    /**
     * 获取和onlyTransient配置处理后的ModifyDescribe,用于填充
     * @param describe
     * @param type 填充类型
     */
    public ModifyDescribe getModifyDescribeForFill(ModifyDescribe describe, FillType type) {
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
                    ModifyDescribe current = getDeepCloneModifyDescribe(describe, false);
                    List<ModifyColumn> columnList = current.getColumnList();
                    for (Iterator<ModifyColumn> columnIterator = columnList.iterator(); columnIterator.hasNext();) {
                        boolean isTransient = columnIterator.next().getColumn().getIsTransient();
                        //为只填充持久化列时,移除非持久化的列,反正移除持久化的列
                        boolean remove = isPersistentType ? isTransient : !isTransient;
                        if (remove) {
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
                        boolean isTransient = columnIterator.next().getColumn().getIsTransient();
                        //为只填充持久化列时,移除非持久化的列,反正移除持久化的列
                        boolean remove = isPersistentType ? isTransient : !isTransient;
                        if (remove) {
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
}
