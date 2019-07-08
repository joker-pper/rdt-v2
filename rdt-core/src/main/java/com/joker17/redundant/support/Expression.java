package com.joker17.redundant.support;

import java.io.Serializable;

public class Expression implements Serializable {
    /**
     * 验证的属性
     */
    private String key;

    /**
     * 所对应的值
     */
    private Object value;

    /**
     * 是否通过value值为集合时进行contains比较(两种策略,contains/eq)
     */
    private boolean isContains;

    public Expression() {
    }

    public Expression(String key, Object value, boolean isContains) {
        this.key = key;
        this.value = value;
        this.isContains = isContains;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isContains() {
        return isContains;
    }

    public void setContains(boolean contains) {
        isContains = contains;
    }
}