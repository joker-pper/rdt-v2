package com.joker17.redundant.model;

import java.io.Serializable;
import java.lang.reflect.Field;

//列所相关属性
public class Column implements Serializable {
    private Class entityClass;
    private String alias; //对应的别名
    private Class propertyClass;  //实体属性类型
    private String property;//实体属性名称
    private String name;  //列名称(实际列名,即数据库列名)
    private transient Field field;
    private Boolean isTransient;
    private Boolean isPrimaryId;

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
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

    public Boolean getIsTransient() {
        return isTransient;
    }

    public void setIsTransient(Boolean isTransient) {
        this.isTransient = isTransient;
    }

    public Boolean getIsPrimaryId() {
        return isPrimaryId;
    }

    public void setIsPrimaryId(Boolean primaryId) {
        isPrimaryId = primaryId;
    }
}
