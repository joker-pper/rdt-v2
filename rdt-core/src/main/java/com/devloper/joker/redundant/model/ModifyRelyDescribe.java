package com.devloper.joker.redundant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于描述满足带依赖某字段的修改条件时修改相关字段的类
 */
public class ModifyRelyDescribe implements Serializable {

    private int index;
    private Column relyColumn;

    private Class valType;//值所对应的class,比如依赖字段为枚举类时,该值可能为int.class/String.class
    private List<Object> valList = new ArrayList<Object>();//target class所要依赖的值

    private List<Object> unknowNotExistValList = new ArrayList<Object>();//非这些值时为target class

    private List<ModifyCondition> conditionList = new ArrayList<ModifyCondition>();  //修改条件(可能多个)
    private List<ModifyColumn> columnList = new ArrayList<ModifyColumn>();  //修改相关的数据信息(可能多个)

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Column getRelyColumn() {
        return relyColumn;
    }

    public void setRelyColumn(Column relyColumn) {
        this.relyColumn = relyColumn;
    }

    public Class getValType() {
        return valType;
    }

    public void setValType(Class valType) {
        this.valType = valType;
    }

    public List<Object> getValList() {
        return valList;
    }

    public void setValList(List<Object> valList) {
        this.valList = valList;
    }

    public List<Object> getUnknowNotExistValList() {
        return unknowNotExistValList;
    }

    public void setUnknowNotExistValList(List<Object> unknowNotExistValList) {
        this.unknowNotExistValList = unknowNotExistValList;
    }

    public List<ModifyCondition> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<ModifyCondition> conditionList) {
        this.conditionList = conditionList;
    }

    public List<ModifyColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ModifyColumn> columnList) {
        this.columnList = columnList;
    }
}
