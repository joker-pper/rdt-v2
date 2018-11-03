package com.devloper.joker.redundant.model;

import java.lang.reflect.Field;

//修改列所相关属性
public class ModifyColumn extends Column {

    private String targetAlias; //对应的别名
    private String targetProperty;  //对应的属性名称
    private Class targetPropertyClass;  //对应的属性类型
    private String targetName;  //对应列名称(实际列名,即数据库列名)
    private transient Field targetField;

    private Boolean targetIsTransient;
    private Boolean targetIsPrimaryId;

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

    public Boolean getTargetIsTransient() {
        return targetIsTransient;
    }

    public void setTargetIsTransient(Boolean targetIsTransient) {
        this.targetIsTransient = targetIsTransient;
    }

    public Boolean getTargetIsPrimaryId() {
        return targetIsPrimaryId;
    }

    public void setTargetIsPrimaryId(Boolean targetIsPrimaryId) {
        this.targetIsPrimaryId = targetIsPrimaryId;
    }
}
