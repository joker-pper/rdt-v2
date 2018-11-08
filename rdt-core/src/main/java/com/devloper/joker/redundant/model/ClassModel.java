package com.devloper.joker.redundant.model;

import com.devloper.joker.redundant.model.commons.RdtRelyModel;
import com.devloper.joker.redundant.model.commons.RdtRelyTargetColumnModel;
import java.lang.reflect.Field;
import java.util.*;

public class ClassModel {

    //类的描述信息
    private Class currentClass;  //当前类的class
    private String className;
    private String simpleName;
    private String entityName;  //数据库表名
    private Boolean baseClass;  //是否为基本类(类有注解@RdtBaseEntity或通过实现方法进行判断)
    private String primaryId;  //当前类的id字段(基本类时必须存在)

    /**
     * 标志 0: 初始化 1: 进行中 2: 已完成
     */
    private Integer builderMark;

    private List<Field> fieldList = new ArrayList<Field>(16);
    private Map<String, Field> propertyFieldMap = new HashMap<String, Field>(16);
    private Map<String, String> aliasPropertyMap = new LinkedHashMap<String, String>(16);  //该类属性别名对应的该类属性名称
    private Map<String, Column> propertyColumnMap = new LinkedHashMap<String, Column>(16);  //该类属性所对应的信息

    private Map<String, Map<Integer, RdtRelyModel>> propertyRelyDataMap = new LinkedHashMap<String, Map<Integer, RdtRelyModel>>(16);  //该属性的指定group索引所依赖数据信息

    //当前属性所对应的依赖列的所对应的index对应的RdtRelyTargetColumnModel
    private Map<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>> propertyTargetRelyMap = new LinkedHashMap<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>>(16);
    private Map<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>> propertyTargetConditionRelyMap = new LinkedHashMap<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>>(16);

    private Set<String> usedPropertySet = new HashSet<String>(16);  //被其他持久化使用(所保存)的冗余字段

    //当前类数据修改后所要修改的基本类列表(该类属性做为直接冗余字段的相关基本类,即target class为当前类的类列表)
    private Set<Class> changedRelaxedClassSet = new LinkedHashSet<Class>(16);

    //当前类中存在的target class所对应的修改信息
    private Map<Class, List<ModifyDescribe>> targetClassModifyDescribeMap = new LinkedHashMap<Class, List<ModifyDescribe>>(16);

    //当前类中存在依赖列字段下的target class所对应的修改信息 (integer key 对应的为 group)
    private Map<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>> targetClassModifyRelyDescribeMap = new LinkedHashMap<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>>(16) ;

    /**
     * 当前类中依赖字段所对应的class列表
     */
    private Set<Class> targetRelyModifyClassSet = new LinkedHashSet<Class>(16);

    /**
     * 当前类拥有的关联对象列表,依赖于注解@RdtOne,@RdtMany(用于文档型更新/子对象填充)
     */
    private List<ComplexModel> complexModelList = new ArrayList<ComplexModel>(16);

    /**
     * 其他非持久化类中字段target class为当前类的class集合
     */
    private Set<Class> changedComplexClassSet = new LinkedHashSet<Class>(16);

    public Class getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(Class currentClass) {
        this.currentClass = currentClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Boolean getBaseClass() {
        return baseClass;
    }

    public void setBaseClass(Boolean baseClass) {
        this.baseClass = baseClass;
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public Integer getBuilderMark() {
        return builderMark;
    }

    public void setBuilderMark(Integer builderMark) {
        this.builderMark = builderMark;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<Field> fieldList) {
        this.fieldList = fieldList;
    }

    public Map<String, Field> getPropertyFieldMap() {
        return propertyFieldMap;
    }

    public void setPropertyFieldMap(Map<String, Field> propertyFieldMap) {
        this.propertyFieldMap = propertyFieldMap;
    }

    public Map<String, String> getAliasPropertyMap() {
        return aliasPropertyMap;
    }

    public void setAliasPropertyMap(Map<String, String> aliasPropertyMap) {
        this.aliasPropertyMap = aliasPropertyMap;
    }

    public Map<String, Column> getPropertyColumnMap() {
        return propertyColumnMap;
    }

    public void setPropertyColumnMap(Map<String, Column> propertyColumnMap) {
        this.propertyColumnMap = propertyColumnMap;
    }

    public Map<String, Map<Integer, RdtRelyModel>> getPropertyRelyDataMap() {
        return propertyRelyDataMap;
    }

    public void setPropertyRelyDataMap(Map<String, Map<Integer, RdtRelyModel>> propertyRelyDataMap) {
        this.propertyRelyDataMap = propertyRelyDataMap;
    }

    public Map<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>> getPropertyTargetRelyMap() {
        return propertyTargetRelyMap;
    }

    public void setPropertyTargetRelyMap(Map<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>> propertyTargetRelyMap) {
        this.propertyTargetRelyMap = propertyTargetRelyMap;
    }

    public Map<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>> getPropertyTargetConditionRelyMap() {
        return propertyTargetConditionRelyMap;
    }

    public void setPropertyTargetConditionRelyMap(Map<String, Map<Column, Map<Integer, RdtRelyTargetColumnModel>>> propertyTargetConditionRelyMap) {
        this.propertyTargetConditionRelyMap = propertyTargetConditionRelyMap;
    }

    public Set<String> getUsedPropertySet() {
        return usedPropertySet;
    }

    public void setUsedPropertySet(Set<String> usedPropertySet) {
        this.usedPropertySet = usedPropertySet;
    }

    public Set<Class> getChangedRelaxedClassSet() {
        return changedRelaxedClassSet;
    }

    public void setChangedRelaxedClassSet(Set<Class> changedRelaxedClassSet) {
        this.changedRelaxedClassSet = changedRelaxedClassSet;
    }

    public Map<Class, List<ModifyDescribe>> getTargetClassModifyDescribeMap() {
        return targetClassModifyDescribeMap;
    }

    public void setTargetClassModifyDescribeMap(Map<Class, List<ModifyDescribe>> targetClassModifyDescribeMap) {
        this.targetClassModifyDescribeMap = targetClassModifyDescribeMap;
    }

    public Map<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>> getTargetClassModifyRelyDescribeMap() {
        return targetClassModifyRelyDescribeMap;
    }

    public void setTargetClassModifyRelyDescribeMap(Map<Class, Map<Column, Map<Integer, List<ModifyRelyDescribe>>>> targetClassModifyRelyDescribeMap) {
        this.targetClassModifyRelyDescribeMap = targetClassModifyRelyDescribeMap;
    }

    public Set<Class> getTargetRelyModifyClassSet() {
        return targetRelyModifyClassSet;
    }

    public void setTargetRelyModifyClassSet(Set<Class> targetRelyModifyClassSet) {
        this.targetRelyModifyClassSet = targetRelyModifyClassSet;
    }

    public List<ComplexModel> getComplexModelList() {
        return complexModelList;
    }

    public void setComplexModelList(List<ComplexModel> complexModelList) {
        this.complexModelList = complexModelList;
    }

    public Set<Class> getChangedComplexClassSet() {
        return changedComplexClassSet;
    }

    public void setChangedComplexClassSet(Set<Class> changedComplexClassSet) {
        this.changedComplexClassSet = changedComplexClassSet;
    }

    public List<ModifyDescribe> getTargetClassModifyDescribes(Class targetClass) {
        return targetClassModifyDescribeMap.get(targetClass);
    }

    /**
     * 添加被引用的字段(非)
     * @param property
     */
    public void addUsedProperty(String property) {
        usedPropertySet.add(property);
    }


}
