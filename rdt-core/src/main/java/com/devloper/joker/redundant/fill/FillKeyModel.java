package com.devloper.joker.redundant.fill;

import com.devloper.joker.redundant.model.Column;
import com.devloper.joker.redundant.model.ModifyDescribe;
import com.devloper.joker.redundant.model.ModifyRelyDescribe;

import java.util.*;

/**
 * 作为base class(即被引用的字段所处的实体类)某单项key列的实体
 */

public class FillKeyModel {
    /**
     * 当前key名称
     */
    private String key;

    /**
     * 是否为id key
     */
    private boolean isPrimaryKey;

    private Class entityClass;

    /**
     * key所对应的值
     */
    private Set<Object> keyValues = new LinkedHashSet<Object>(16);
    /**
     * 所涉及的查询列数据(要填充到的数据中所被引用的列)
     */
    private Set<Column> columnValues = new LinkedHashSet<Column>(16);
    /**
     * modifyDesc情况下对应key值所对应要修改的数据列表
     */
    private Map<ModifyDescribe, Map<Object, Set<Object>>> describeKeyDataMap = new HashMap<ModifyDescribe, Map<Object, Set<Object>>>(16);

    /**
     * relyDescribe情况下对应key值所对应要修改的数据列表
     */
    private Map<ModifyRelyDescribe, Map<Object, Set<Object>>> relyDescribeKeyDataMap = new HashMap<ModifyRelyDescribe, Map<Object, Set<Object>>>(16);

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public Set<Object> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(Set<Object> keyValues) {
        this.keyValues = keyValues;
    }

    public void addKeyValue(Object value) {
        keyValues.add(value);
    }

    public Set<Column> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(Set<Column> columnValues) {
        this.columnValues = columnValues;
    }

    public void addColumnValue(Column value) {
        columnValues.add(value);
    }

    public Map<ModifyDescribe, Map<Object, Set<Object>>> getDescribeKeyDataMap() {
        return describeKeyDataMap;
    }

    public void setDescribeKeyDataMap(Map<ModifyDescribe, Map<Object, Set<Object>>> describeKeyDataMap) {
        this.describeKeyDataMap = describeKeyDataMap;
    }

    public Map<Object, Set<Object>> getDescribeKeyData(ModifyDescribe describe) {
        Map<Object, Set<Object>> result = describeKeyDataMap.get(describe);
        if (result == null) {
            result = new HashMap<Object, Set<Object>>(16);
            describeKeyDataMap.put(describe, result);
        }
        return result;
    }

    public void addDescribeKeyValueData(ModifyDescribe describe, Object keyValue, Object data, boolean withNullKeyValue) {
        if (keyValue != null || withNullKeyValue) {
            addKeyValue(keyValue);
        }
        Map<Object, Set<Object>> result = getDescribeKeyData(describe);
        Set<Object> datas = result.get(keyValue);
        if (datas == null) {
            datas = new HashSet<Object>(16);
            result.put(keyValue, datas);
        }
        datas.add(data);
    }

    public Map<ModifyRelyDescribe, Map<Object, Set<Object>>> getRelyDescribeKeyDataMap() {
        return relyDescribeKeyDataMap;
    }

    public void setRelyDescribeKeyDataMap(Map<ModifyRelyDescribe, Map<Object, Set<Object>>> relyDescribeKeyDataMap) {
        this.relyDescribeKeyDataMap = relyDescribeKeyDataMap;
    }

    public Map<Object, Set<Object>> getDescribeKeyData(ModifyRelyDescribe describe) {
        Map<Object, Set<Object>> result = relyDescribeKeyDataMap.get(describe);
        if (result == null) {
            result = new HashMap<Object, Set<Object>>(16);
            relyDescribeKeyDataMap.put(describe, result);
        }
        return result;
    }

    public void addDescribeKeyValueData(ModifyRelyDescribe describe, Object keyValue, Object data, boolean withNullKeyValue) {
        if (keyValue != null || withNullKeyValue) {
            addKeyValue(keyValue);
        }
        Map<Object, Set<Object>> result = getDescribeKeyData(describe);
        Set<Object> datas = result.get(keyValue);
        if (datas == null) {
            datas = new HashSet<Object>(16);
            result.put(keyValue, datas);
        }
        datas.add(data);
    }

}
