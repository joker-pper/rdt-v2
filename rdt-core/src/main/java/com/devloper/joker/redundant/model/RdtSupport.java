package com.devloper.joker.redundant.model;


import com.devloper.joker.redundant.resolver.RdtResolver;
import com.devloper.joker.redundant.support.Prototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RdtSupport {

    protected final  Logger logger = LoggerFactory.getLogger(getClass());

    private RdtProperties properties;

    private RdtResolver rdtResolver;

    private Map<Class, List<List<ComplexModel>>> complexResultListMap = new HashMap<Class, List<List<ComplexModel>>>();

    public RdtSupport(RdtProperties properties, RdtResolver rdtResolver) {
        this.properties = properties;
        this.rdtResolver = rdtResolver;
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
            if (changedPropertys.contains(modifyColumn.getTargetProperty())) {
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
        if (describe != null) {
            if (properties.getDeepCloneChangedModify()) {
                ModifyDescribe cloned = Prototype.of(describe).deepClone().getModel();

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

    public ModifyRelyDescribe getModifyRelyDescribe(ModifyRelyDescribe describe, List<String> changedPropertys) {
        ModifyRelyDescribe temp = null;
        List<ModifyColumn> columnList = new ArrayList<ModifyColumn>();
        for (ModifyColumn modifyColumn : describe.getColumnList()) {
            if (changedPropertys.contains(modifyColumn.getTargetProperty())) { //值变化所要修改的列
                columnList.add(modifyColumn);
            }
        }

        if (!columnList.isEmpty()) {
            temp = new ModifyRelyDescribe();

            temp.setIndex(describe.getIndex());
            temp.setConditionList(describe.getConditionList());
            temp.setValType(describe.getValType());
            temp.setValList(describe.getValList());
            temp.setUnknowNotExistValList(describe.getUnknowNotExistValList());
            temp.setColumnList(columnList);
        }
        return getDeepCloneModifyRelyDescribe(temp);
    }

    //将未被序列话的field进行处理
    private void handleModifyColumnAndConditionDeepClone(List<ModifyColumn> clonedColumnList, List<ModifyColumn> columnList, List<ModifyCondition> clonedConditionList, List<ModifyCondition> conditionList) {
        for (int i = 0; i < clonedColumnList.size(); i++) {
            ModifyColumn column = columnList.get(i);
            ModifyColumn clonedColumn = clonedColumnList.get(i);
            clonedColumn.setField(column.getField());
            clonedColumn.setTargetField(column.getTargetField());
        }

        for (int i = 0; i < clonedConditionList.size(); i++) {
            ModifyCondition condition = conditionList.get(i);
            ModifyCondition clonedCondition = clonedConditionList.get(i);
            clonedCondition.setField(condition.getField());
            clonedCondition.setTargetField(condition.getTargetField());
        }
    }

    public ModifyRelyDescribe getDeepCloneModifyRelyDescribe(ModifyRelyDescribe describe) {
        if (describe != null) {
            if (properties.getDeepCloneChangedModify()) {
                ModifyRelyDescribe cloned = Prototype.of(describe).deepClone().getModel();

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


    //获取当前复杂对象的组合,返回数组按照倒序的属性方式
    public List<List<ComplexModel>> getComplexModelParseResult(Class complexClass) {
        List<List<ComplexModel>> results = complexResultListMap.get(complexClass);
        if (results == null) {
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
        return results;
    }

    /**
     * @param complexModelList
     * @return
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
         * @param currentClassModel 当前处理的classModel
         * @param relyColumn 依赖列
         * @param group group index
         * @param describe
         */
        public abstract void execute(ClassModel classModel, ClassModel currentClassModel, Column relyColumn, int group, ModifyRelyDescribe describe);
    }

    public void doModifyRelyDescribeHandle(ClassModel classModel, ClassModel currentClassModel, ModifyRelyDescribeCallBack callBack) {
        Class entityClass = classModel.getCurrentClass();
        Map<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>> describeMap = currentClassModel.getTargetClassModifyRelyDescribeMap();
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
                                callBack.execute(classModel, currentClassModel, relyColumn, group, describe);
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
         * @param currentClassModel 当前处理的classModel
         * @param describe
         */
        public abstract void execute(ClassModel classModel, ClassModel currentClassModel, ModifyDescribe describe);
    }

    public void doModifyDescribeHandle(ClassModel classModel, ClassModel currentClassModel, ModifyDescribeCallBack callBack) {
        Class entityClass = classModel.getCurrentClass();
        List<ModifyDescribe> modifyDescribeList = getModifyDescribeData(currentClassModel, entityClass); //获取currentClassModel中关于classModel的modifyDescribe信息
        if (!modifyDescribeList.isEmpty()) {
            for (ModifyDescribe modifyDescribe : modifyDescribeList) {
                callBack.execute(classModel, currentClassModel, modifyDescribe);
            }
        }
    }

    public static abstract class ModifyColumnCallBack {

        public abstract void execute(ModifyColumn modifyColumn, String targetProperty, Object targetPropertyVal);
    }

    /**
     * 回调处理modifyColumn的值
     * @param vo
     * @param describe
     * @param callBack
     */
    public void doModifyColumnHandle(ChangedVo vo, Object describe, ModifyColumnCallBack callBack) {
        List<ModifyColumn> modifyColumnList;
        if (describe instanceof ModifyDescribe) modifyColumnList = ((ModifyDescribe) describe).getColumnList();
        else if (describe instanceof ModifyRelyDescribe) modifyColumnList = ((ModifyRelyDescribe) describe).getColumnList();
        else throw new IllegalArgumentException("not allowed describe instance");
        for (ModifyColumn modifyColumn : modifyColumnList) {
            String targetProperty = modifyColumn.getTargetProperty();
            Object val = vo.getCurrentVal(targetProperty);
            try {
                val = rdtResolver.cast(val, modifyColumn.getPropertyClass()); //转换值
            } catch (Exception e) {
                logger.warn("rdt cast val error", e);
            }
            callBack.execute(modifyColumn, targetProperty, val);
        }
    }


    public static abstract class ModifyConditionCallBack {

        public abstract void execute(ModifyCondition modifyCondition, String targetProperty, Object targetPropertyVal);
    }

    /**
     * 回调处理modifyCondition的值
     * @param vo
     * @param describe
     * @param callBack
     */
    public void doModifyConditionHandle(ChangedVo vo, Object describe, ModifyConditionCallBack callBack) {
        List<ModifyCondition> conditionList;
        if (describe instanceof ModifyDescribe) conditionList = ((ModifyDescribe) describe).getConditionList();
        else if (describe instanceof ModifyRelyDescribe) conditionList = ((ModifyRelyDescribe) describe).getConditionList();
        else throw new IllegalArgumentException("not allowed describe instance");

        for (ModifyCondition modifyCondition : conditionList) {
            String targetProperty = modifyCondition.getTargetProperty();
            Object val = vo.getCurrentVal(targetProperty);
            callBack.execute(modifyCondition, targetProperty, val);
        }

    }
}