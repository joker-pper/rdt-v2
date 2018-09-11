package com.devloper.joker.redundant.model.commons;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RdtRelyModel {
    private List<Class> keyTargetClassList = new ArrayList<Class>();//多个KeyTarget注解顺序的target class
    private Map<Class, List<Object>> targetClassValueMap = new LinkedHashMap<Class, List<Object>>();//target class所需要对应的值
    private Class valType;
    private Class nullType;//存在时最后添加到targetClassValueMap中
    private Class unknowType;//不为null时不处于unknowNotExistValues中所对应的target class
    private List<Object> unknowNotExistValues = new ArrayList<Object>();

    public List<Class> getKeyTargetClassList() {
        return keyTargetClassList;
    }

    public void setKeyTargetClassList(List<Class> keyTargetClassList) {
        this.keyTargetClassList = keyTargetClassList;
    }

    public Map<Class, List<Object>> getTargetClassValueMap() {
        return targetClassValueMap;
    }

    public void setTargetClassValueMap(Map<Class, List<Object>> targetClassValueMap) {
        this.targetClassValueMap = targetClassValueMap;
    }

    public Class getValType() {
        return valType;
    }

    public void setValType(Class valType) {
        this.valType = valType;
    }

    public Class getNullType() {
        return nullType;
    }

    public void setNullType(Class nullType) {
        this.nullType = nullType;
    }

    public Class getUnknowType() {
        return unknowType;
    }

    public void setUnknowType(Class unknowType) {
        this.unknowType = unknowType;
    }

    public List<Object> getUnknowNotExistValues() {
        return unknowNotExistValues;
    }

    public void setUnknowNotExistValues(List<Object> unknowNotExistValues) {
        this.unknowNotExistValues = unknowNotExistValues;
    }
}
