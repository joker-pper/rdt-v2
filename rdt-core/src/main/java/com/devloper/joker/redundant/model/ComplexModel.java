package com.devloper.joker.redundant.model;

//关联对象的信息,一个类中可能有多个关联对象@RdtOne/@RdtMany,且仅支持该类非修改基本类
public class ComplexModel {

    private Class currentType;  //当前class类型,one时与属性类型一致,反之为集合中的类型

    /**在所属类中的信息**/

    private Class ownerType;  //所属类

    private Boolean isOne;  //是否为单个普通关联对象,反之为集合元素

    private Boolean ownerBase;  //所属类是否为base类

    private String property;  //属性名称

    private String alias;  //别名

    private String column;  //列名

    private Class propertyType;  //属性的类型

    public Class getCurrentType() {
        return currentType;
    }

    public void setCurrentType(Class currentType) {
        this.currentType = currentType;
    }

    public Class getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(Class ownerType) {
        this.ownerType = ownerType;
    }

    public Boolean getIsOne() {
        return isOne;
    }

    public void setIsOne(Boolean isOne) {
        this.isOne = isOne;
    }

    public Boolean getOwnerBase() {
        return ownerBase;
    }

    public void setOwnerBase(Boolean ownerBase) {
        this.ownerBase = ownerBase;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Class getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(Class propertyType) {
        this.propertyType = propertyType;
    }

}
