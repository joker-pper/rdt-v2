package com.devloper.joker.redundant.model;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 修改条件
 */
public class ModifyCondition implements Serializable {

    private String targetAlias; //对应的别名
    private String targetProperty;  //对应的属性名称
    private Class targetPropertyClass;  //对应的属性类型
    private String targetName;  //对应列名称(实际列名,即数据库列名)

    private transient Field targetField;

    private String alias; //别名
    private Class propertyClass;  //属性类型
    private String property;//属性名称
    private String name;  //列名称(实际列名,即数据库列名)

    private transient Field field;

    public String getTargetAlias() {
        return targetAlias;
    }

    public void setTargetAlias(String targetAlias) {
        this.targetAlias = targetAlias;
    }

    public String getTargetProperty() {
        return targetProperty;
    }

    public void setTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
    }

    public Class getTargetPropertyClass() {
        return targetPropertyClass;
    }

    public void setTargetPropertyClass(Class targetPropertyClass) {
        this.targetPropertyClass = targetPropertyClass;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Field getTargetField() {
        return targetField;
    }

    public void setTargetField(Field targetField) {
        this.targetField = targetField;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Class getPropertyClass() {
        return propertyClass;
    }

    public void setPropertyClass(Class propertyClass) {
        this.propertyClass = propertyClass;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
