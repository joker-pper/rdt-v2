package com.joker17.redundant.model.commons;

import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.model.Column;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RdtRelyTargetColumnModel {
    /**
     * 每个class所对应的列信息
     */
    private Map<Class, Column> classTargetColumnMap = new LinkedHashMap<Class, Column>();
    private int group;

    /**
     * class所对应的填充类型
     */
    private Map<Class, RdtFillType> fillShowTypeMap = new HashMap<Class, RdtFillType>();
    private Map<Class, RdtFillType> fillSaveTypeMap = new HashMap<Class, RdtFillType>();


    public Map<Class, Column> getClassTargetColumnMap() {
        return classTargetColumnMap;
    }

    public void setClassTargetColumnMap(Map<Class, Column> classTargetColumnMap) {
        this.classTargetColumnMap = classTargetColumnMap;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public Map<Class, RdtFillType> getFillShowTypeMap() {
        return fillShowTypeMap;
    }

    public void setFillShowTypeMap(Map<Class, RdtFillType> fillShowTypeMap) {
        this.fillShowTypeMap = fillShowTypeMap;
    }

    public Map<Class, RdtFillType> getFillSaveTypeMap() {
        return fillSaveTypeMap;
    }

    public void setFillSaveTypeMap(Map<Class, RdtFillType> fillSaveTypeMap) {
        this.fillSaveTypeMap = fillSaveTypeMap;
    }
}
