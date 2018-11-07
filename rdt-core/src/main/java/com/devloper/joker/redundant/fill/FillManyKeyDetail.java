package com.devloper.joker.redundant.fill;

import com.devloper.joker.redundant.model.Column;
import com.devloper.joker.redundant.model.ModifyDescribe;
import com.devloper.joker.redundant.model.ModifyRelyDescribe;

import java.util.*;

public class FillManyKeyDetail {
    /**
     * 条件列
     */
    private List<Column> conditionColumnValues = new ArrayList<Column>(16);

    /**
     * 处于当前条件列时,所需要加载的列
     */
    private Set<Column> columnValues = new LinkedHashSet<Column>(16);

    /**
     * 按照当前条件列顺序的多组值,每组条件应只对应一个base类的数据值
     */
    private List<List<Object>> conditionGroupValues = new ArrayList<List<Object>>(16);

    /**
     * 条件组值全为null的索引值
     */
    private int groupValueNullIndex = -1;


    /**
     * 基于一组条件列顺序值所对应describe的要修改的数据列表
     */
    private Map<List<Object>, Map<ModifyDescribe, List<Object>>> conditionGroupValueDescribeDataMap = new HashMap<List<Object>, Map<ModifyDescribe, List<Object>>>(16);

    /**
     * 基于一组条件列顺序值所对应rely describe的要修改的数据列表
     */
    private Map<List<Object>, Map<ModifyRelyDescribe, List<Object>>> conditionGroupValueRelyDescribeDataMap = new HashMap<List<Object>, Map<ModifyRelyDescribe, List<Object>>>(16);

    public List<Column> getConditionColumnValues() {
        return conditionColumnValues;
    }

    public void setConditionColumnValues(List<Column> conditionColumnValues) {
        this.conditionColumnValues = conditionColumnValues;
    }

    public Set<Column> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(Set<Column> columnValues) {
        this.columnValues = columnValues;
    }

    public List<List<Object>> getConditionGroupValues() {
        return conditionGroupValues;
    }

    public void setConditionGroupValues(List<List<Object>> conditionGroupValues) {
        this.conditionGroupValues = conditionGroupValues;
    }

    public int getGroupValueNullIndex() {
        return groupValueNullIndex;
    }

    public void setGroupValueNullIndex(int groupValueNullIndex) {
        this.groupValueNullIndex = groupValueNullIndex;
    }

    public Map<List<Object>, Map<ModifyDescribe, List<Object>>> getConditionGroupValueDescribeDataMap() {
        return conditionGroupValueDescribeDataMap;
    }

    public void setConditionGroupValueDescribeDataMap(Map<List<Object>, Map<ModifyDescribe, List<Object>>> conditionGroupValueDescribeDataMap) {
        this.conditionGroupValueDescribeDataMap = conditionGroupValueDescribeDataMap;
    }

    public Map<List<Object>, Map<ModifyRelyDescribe, List<Object>>> getConditionGroupValueRelyDescribeDataMap() {
        return conditionGroupValueRelyDescribeDataMap;
    }

    public void setConditionGroupValueRelyDescribeDataMap(Map<List<Object>, Map<ModifyRelyDescribe, List<Object>>> conditionGroupValueRelyDescribeDataMap) {
        this.conditionGroupValueRelyDescribeDataMap = conditionGroupValueRelyDescribeDataMap;
    }


    public Map<ModifyDescribe, List<Object>> getDescribeMap(List<Object> groupValue) {
        Map<ModifyDescribe, List<Object>> describeListMap = conditionGroupValueDescribeDataMap.get(groupValue);
        if (describeListMap == null) {
            describeListMap = new HashMap<ModifyDescribe, List<Object>>(16);
            conditionGroupValueDescribeDataMap.put(groupValue, describeListMap);
        }
        return describeListMap;
    }

    public Map<ModifyRelyDescribe, List<Object>> getRelyDescribeMap(List<Object> groupValue) {
        Map<ModifyRelyDescribe, List<Object>> relyDescribeListMap = conditionGroupValueRelyDescribeDataMap.get(groupValue);
        if (relyDescribeListMap == null) {
            relyDescribeListMap = new HashMap<ModifyRelyDescribe, List<Object>>(16);
            conditionGroupValueRelyDescribeDataMap.put(groupValue, relyDescribeListMap);
        }
        return relyDescribeListMap;
    }
}
