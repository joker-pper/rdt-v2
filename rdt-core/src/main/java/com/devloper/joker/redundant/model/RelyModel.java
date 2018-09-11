package com.devloper.joker.redundant.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RelyModel {

    //被依赖的描述,属于classModel当前class的属性相关数据
    private String property;//被依赖的属性字段
    private Field propertyField;
    private int group;//第几组
    private Class valType;//值所对应的class,比如依赖字段为枚举类时,该值可能为int.class/String.class
    private List<Object> valList = new ArrayList<Object>();//被依赖的值

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Field getPropertyField() {
        return propertyField;
    }

    public void setPropertyField(Field propertyField) {
        this.propertyField = propertyField;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public Class getValType() {
        return valType;
    }

    public void setValType(Class valType) {
        this.valType = valType;
    }

    public List<Object> getValList() {
        return valList;
    }

    public void setValList(List<Object> valList) {
        this.valList = valList;
    }
}
