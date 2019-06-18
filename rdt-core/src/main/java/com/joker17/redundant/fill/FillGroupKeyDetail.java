package com.joker17.redundant.fill;

import com.joker17.redundant.model.Column;
import com.joker17.redundant.model.ModifyGroupDescribe;

import java.io.Serializable;
import java.util.*;

/**
 * 以条件列及查询key列作为一组数据
 */
public class FillGroupKeyDetail implements Serializable {

    /**
     * 条件列
     */
    private List<Column> conditionColumnValues = new ArrayList<Column>(16);

    /**
     * 要查询的key列
     */
    private Column selectColumnValue;

    /**
     * 每组条件列值所对应的数据
     */
    private Map<List<Object>, Map<ModifyGroupDescribe, List<Object>>> conditionValueGroupDataMap = new HashMap<List<Object>, Map<ModifyGroupDescribe, List<Object>>>(16);

    /**
     * 每个条件列值集合的列表
     */
    private List<Set<Object>> conditionValueList = new ArrayList<Set<Object>>(16);


    public List<Column> getConditionColumnValues() {
        return conditionColumnValues;
    }

    public void setConditionColumnValues(List<Column> conditionColumnValues) {
        this.conditionColumnValues = conditionColumnValues;
    }

    public Column getSelectColumnValue() {
        return selectColumnValue;
    }

    public void setSelectColumnValue(Column selectColumnValue) {
        this.selectColumnValue = selectColumnValue;
    }

    public Map<List<Object>, Map<ModifyGroupDescribe, List<Object>>> getConditionValueGroupDataMap() {
        return conditionValueGroupDataMap;
    }

    public void setConditionValueGroupDataMap(Map<List<Object>, Map<ModifyGroupDescribe, List<Object>>> conditionValueGroupDataMap) {
        this.conditionValueGroupDataMap = conditionValueGroupDataMap;
    }

    public List<Set<Object>> getConditionValueList() {
        return conditionValueList;
    }

    public void initFillGroupKeyDetailData(List<Object> gainConditionValueList, ModifyGroupDescribe groupDescribe, Object data) {
        Map<ModifyGroupDescribe, List<Object>> groupDescribeListMap = conditionValueGroupDataMap.get(gainConditionValueList);
        if (groupDescribeListMap == null) {
            groupDescribeListMap = new HashMap<ModifyGroupDescribe, List<Object>>(16);
            conditionValueGroupDataMap.put(gainConditionValueList, groupDescribeListMap);
        }
        List<Object> dataList = groupDescribeListMap.get(groupDescribe);
        if (dataList == null) {
            dataList = new ArrayList<Object>(16);
            groupDescribeListMap.put(groupDescribe, dataList);
        }
        dataList.add(data);

        int conditionValueListSize = conditionValueList.size();

        List<Set<Object>> tempList = new ArrayList<Set<Object>>(16);

        for (int i = 0; i < gainConditionValueList.size(); i++) {
            Set<Object> gainConditionSet;
            if (conditionValueListSize > i) {
                gainConditionSet = conditionValueList.get(i);
            } else {
                gainConditionSet = new HashSet<Object>(16);
                tempList.add(gainConditionSet);
            }
            gainConditionSet.add(gainConditionValueList.get(i));
        }
        if (!tempList.isEmpty()) {
            conditionValueList.addAll(tempList);
        }
    }


}
