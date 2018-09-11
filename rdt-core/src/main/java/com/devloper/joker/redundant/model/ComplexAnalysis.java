package com.devloper.joker.redundant.model;

import java.util.ArrayList;
import java.util.List;

public class ComplexAnalysis {

    private Boolean hasMany;//是否包含many
    private List<Boolean> oneList = new ArrayList<Boolean>();
    private List<String> propertyList = new ArrayList<String>();
    private List<Class> currentTypeList = new ArrayList<Class>();//真实类型
    private Class rootClass;//所属根类
    private String prefix;//前缀

    public Boolean getHasMany() {
        return hasMany;
    }

    public void setHasMany(Boolean hasMany) {
        this.hasMany = hasMany;
    }

    public List<Boolean> getOneList() {
        return oneList;
    }

    public void setOneList(List<Boolean> oneList) {
        this.oneList = oneList;
    }

    public List<String> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<String> propertyList) {
        this.propertyList = propertyList;
    }

    public List<Class> getCurrentTypeList() {
        return currentTypeList;
    }

    public void setCurrentTypeList(List<Class> currentTypeList) {
        this.currentTypeList = currentTypeList;
    }

    public Class getRootClass() {
        return rootClass;
    }

    public void setRootClass(Class rootClass) {
        this.rootClass = rootClass;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
