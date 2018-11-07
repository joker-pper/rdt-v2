package com.devloper.joker.redundant.model;

import com.devloper.joker.redundant.model.commons.RdtRelyModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于描述满足带依赖某字段的修改条件时修改相关字段的类
 */
public class ModifyRelyDescribe extends ModifyDescribe {

    private int group;
    private Column relyColumn;

    private RdtRelyModel rdtRelyModel;

    private Class valType;//值所对应的class,比如依赖字段为枚举类时,该值可能为int.class/String.class
    private List<Object> valList = new ArrayList<Object>();//target class所要依赖的值

    private List<Object> unknowNotExistValList = new ArrayList<Object>();//非这些值时为target class

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public Column getRelyColumn() {
        return relyColumn;
    }

    public void setRelyColumn(Column relyColumn) {
        this.relyColumn = relyColumn;
    }

    public RdtRelyModel getRdtRelyModel() {
        return rdtRelyModel;
    }

    public void setRdtRelyModel(RdtRelyModel rdtRelyModel) {
        this.rdtRelyModel = rdtRelyModel;
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

}
