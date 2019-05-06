package com.joker17.redundant.model.commons;

import java.io.Serializable;
import java.util.*;

/**
 * @RdtRely所解析的模型
 */
public class RdtRelyModel implements Serializable {

    /**
     * 为target class时对应的值(class按照@KeyTarget注解顺序)
     */
    private Map<Class, KeyTargetModel> targetClassValueMap = new LinkedHashMap<Class, KeyTargetModel>();
    private Class valType;

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

    private String notAllowedTypeTips;

    public Map<Class, KeyTargetModel> getTargetClassValueMap() {
        return targetClassValueMap;
    }

    public void setTargetClassValueMap(Map<Class, KeyTargetModel> targetClassValueMap) {
        this.targetClassValueMap = targetClassValueMap;
    }

    public Class getValType() {
        return valType;
    }

    public void setValType(Class valType) {
        this.valType = valType;
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
            for (KeyTargetModel keyTargetModel : targetClassValueMap.values()) {
                values.addAll(keyTargetModel.getValueList());
            }

            values.addAll(allowValues);
            explicitValueList = new ArrayList<Object>(values);
        }
        return explicitValueList;
    }

    public String getNotAllowedTypeTips() {
        return notAllowedTypeTips;
    }

    public void setNotAllowedTypeTips(String notAllowedTypeTips) {
        this.notAllowedTypeTips = notAllowedTypeTips;
    }

    public boolean isValueAllowed(Object value) {
        if (unknownType != null) {
            return true;
        }
        return getExplicitValueList().contains(value);
    }
}
