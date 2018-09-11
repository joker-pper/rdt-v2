package com.devloper.joker.redundant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于描述满足修改条件时修改相关字段的类
 */

public class ModifyDescribe implements Comparable, Serializable {

    private List<ModifyCondition> conditionList = new ArrayList<ModifyCondition>();  //修改条件(可能多个)
    private int index;  //索引
    private List<ModifyColumn> columnList = new ArrayList<ModifyColumn>();  //修改相关的数据信息(可能多个)

    public List<ModifyCondition> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<ModifyCondition> conditionList) {
        this.conditionList = conditionList;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<ModifyColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ModifyColumn> columnList) {
        this.columnList = columnList;
    }

    @Override
    public int compareTo(Object o) {
        int x = this.getIndex();
        int y = ((ModifyDescribe)o).getIndex();
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
