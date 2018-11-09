package com.devloper.joker.redundant.model.commons;

import java.io.Serializable;
import java.util.*;

/**
 * @RdtRely所解析的模型
 */
public class RdtRelyModel implements Serializable {
    private List<Class> keyTargetClassList = new ArrayList<Class>();//多个KeyTarget注解顺序的target class
    private Map<Class, List<Object>> targetClassValueMap = new LinkedHashMap<Class, List<Object>>();//target class所需要对应的值
    private Class valType;
    private Class nullType;//存在时最后添加到targetClassValueMap中
    private Class unknowType;//不为null时不处于unknowNotExistValues中所对应的target class
    private List<Object> unknowNotExistValues = new ArrayList<Object>();//记录的为非unknowType类型时已存在的值

    /**
     * 当前字段所拥有的指定值列表
     */
    private List<Object> explicitValueList;

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

    //获取当前类型所拥有的明确值列表
    public List<Object> getExplicitValueList() {
        if (explicitValueList == null) {
            Set<Object> values = new HashSet<Object>(16);
            for (List<Object> list : targetClassValueMap.values()) {
                values.addAll(list);
            }
            if (nullType != null) {
                values.add(null);
            }
            explicitValueList = new ArrayList<Object>(values);
        }
        return explicitValueList;
    }


    public boolean isValueAllowed(Object value) {
        if (unknowType != null) {
            return true;
        }
        return getExplicitValueList().contains(value);
    }
}
