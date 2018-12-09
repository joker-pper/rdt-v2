package com.joker17.redundant.model;

import com.joker17.redundant.model.commons.RdtRelyModel;

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

    /**
     *
     * 为target class时依赖字段所处已存在的值
     *
     */
    private List<Object> valList = new ArrayList<Object>();

    /**
     * 依赖字段未处于这些值时为target class(size > 1)
     */
    private List<Object> notInValList = new ArrayList<Object>();

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

    public List<Object> getNotInValList() {
        return notInValList;
    }

    public void setNotInValList(List<Object> notInValList) {
        this.notInValList = notInValList;
    }

}
