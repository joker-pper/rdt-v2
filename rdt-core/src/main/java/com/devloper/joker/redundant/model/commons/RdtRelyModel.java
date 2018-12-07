package com.devloper.joker.redundant.model.commons;

import java.io.Serializable;
import java.util.*;

/**
 * @RdtRely所解析的模型
 */
public class RdtRelyModel implements Serializable {
    /**
     * 多个@KeyTarget注解顺序的target class
     */
    private List<Class> keyTargetClassList = new ArrayList<Class>();
    /**
     * 为target class时所需要对应的值
     */
    private Map<Class, List<Object>> targetClassValueMap = new LinkedHashMap<Class, List<Object>>();
    private Class valType;
    private Class nullType;//存在时最后添加到targetClassValueMap中
    private Class unknownType;

    /**
     * 除unknownType外所存在的类型值
     */
    private List<Object> unknownNotExistValues = new ArrayList<Object>();

    /**
     * 除value配置外允许的值列表,仅用于通过填充时的验证(既不影响fill,又不影响update,等同于略过状态值)
     */
    private List<Object> allowValues = new ArrayList<Object>();

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

    public Class getUnknownType() {
        return unknownType;
    }

    public void setUnknownType(Class unknownType) {
        this.unknownType = unknownType;
    }

    public List<Object> getUnknownNotExistValues() {
        return unknownNotExistValues;
    }

    public void setUnknownNotExistValues(List<Object> unknownNotExistValues) {
        this.unknownNotExistValues = unknownNotExistValues;
    }

    public List<Object> getAllowValues() {
        return allowValues;
    }

    public void setAllowValues(List<Object> allowValues) {
        this.allowValues = allowValues;
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
            values.addAll(allowValues);
            explicitValueList = new ArrayList<Object>(values);
        }
        return explicitValueList;
    }


    public boolean isValueAllowed(Object value) {
        if (unknownType != null) {
            return true;
        }
        return getExplicitValueList().contains(value);
    }
}
