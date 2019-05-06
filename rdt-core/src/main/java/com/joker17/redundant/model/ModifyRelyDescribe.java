package com.joker17.redundant.model;

import com.joker17.redundant.model.commons.RdtRelyModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于描述满足带依赖某字段的修改条件时修改相关字段的类
 *
 * 当valList.length = updateIgnoresValList.length && notInValList.length = 1时 更新中不包含该对象
 */
public class ModifyRelyDescribe extends ModifyDescribe {

    private int group;
    /**
     * 依赖列
     */
    private Column relyColumn;

    private RdtRelyModel rdtRelyModel;

    /**
     * 类型值所对应的类型,比如依赖字段为枚举类时,该值可能为int.class/String.class
     */
    private Class valType;

    /**
     *
     * 为target class时的所有类型值
     *
     */
    private List<Object> valList = new ArrayList<Object>();

    /**
     * 依赖字段未处于这些值时为target class(size > 1)
     */
    private List<Object> notInValList = new ArrayList<Object>();

    /**
     * 更新时所忽略的类型值
     */
    private List<Object> updateIgnoresValList = new ArrayList<Object>();

    /**
     * 是否全局禁用更新
     */
    private Boolean disableUpdate;


    /**
     * 未知的依赖值时的提示
     */
    private String notAllowedTypeTips;

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

    public List<Object> getUpdateIgnoresValList() {
        return updateIgnoresValList;
    }

    public void setUpdateIgnoresValList(List<Object> updateIgnoresValList) {
        this.updateIgnoresValList = updateIgnoresValList;
    }

    public Boolean getDisableUpdate() {
        return disableUpdate;
    }

    public void setDisableUpdate(Boolean disableUpdate) {
        this.disableUpdate = disableUpdate;
    }

    public String getNotAllowedTypeTips() {
        return notAllowedTypeTips;
    }

    public void setNotAllowedTypeTips(String notAllowedTypeTips) {
        this.notAllowedTypeTips = notAllowedTypeTips;
    }

}
