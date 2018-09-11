package com.devloper.joker.redundant.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存放属性所对应的值
 */
public class ChangedVo {

    private Map<String, Object> beforeValMap = new HashMap<String, Object>();
    private Map<String, Object> currentValMap = new HashMap<String, Object>();

    private List<String> changedPropertys = new ArrayList<String>();

    private Object current;
    private Object before;

    private String primaryId;
    private Object primaryIdVal;

    public Object getCurrent() {
        return current;
    }

    public void setCurrent(Object current) {
        this.current = current;
    }

    public Object getBefore() {
        return before;
    }

    public void setBefore(Object before) {
        this.before = before;
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public Object getPrimaryIdVal() {
        return primaryIdVal;
    }

    public void setPrimaryIdVal(Object primaryIdVal) {
        this.primaryIdVal = primaryIdVal;
    }

    public void addChangedProperty(String property) {
        changedPropertys.add(property);
    }

    public void setVal(String property, Object currentVal, Object beforeVal) {
        currentValMap.put(property, currentVal);
        beforeValMap.put(property, beforeVal);
    }

    public Object getBeforeVal(String property) {
        return beforeValMap.get(property);
    }


    public Object getCurrentVal(String property) {
        if (!currentValMap.containsKey(property)) throw new IllegalArgumentException("has no key " + property + " val");
        return currentValMap.get(property);
    }

    //获取属性值改变的属性列表
    public List<String> getChangedPropertys() {
        return changedPropertys;
    }
}
