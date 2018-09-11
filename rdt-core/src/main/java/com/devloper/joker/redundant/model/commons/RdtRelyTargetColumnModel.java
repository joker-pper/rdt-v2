package com.devloper.joker.redundant.model.commons;

import com.devloper.joker.redundant.model.Column;

import java.util.LinkedHashMap;
import java.util.Map;

public class RdtRelyTargetColumnModel {
    private Map<Class, Column> classTargetColumnMap = new LinkedHashMap<Class, Column>();  //每个class所对应的列信息
    private int group;
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

}
