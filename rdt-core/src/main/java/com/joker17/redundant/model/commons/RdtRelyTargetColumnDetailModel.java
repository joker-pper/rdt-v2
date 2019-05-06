package com.joker17.redundant.model.commons;

import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.model.Column;

import java.util.ArrayList;
import java.util.List;


public class RdtRelyTargetColumnDetailModel {

    private Column targetColumn;

    private RdtFillType fillShowType;

    private RdtFillType fillSaveType;

    private String notAllowedNullTips;

    /**
     * 该列是否禁用更新
     */
    private boolean disableUpdate;

    /**
     * fillShow忽略的依赖列类型值列表
     */
    private List<Object> fillShowIgnoresType = new ArrayList<Object>();

    /**
     * fillSave忽略的依赖列类型值列表
     */
    private List<Object> fillSaveIgnoresType = new ArrayList<Object>();


    public Column getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(Column targetColumn) {
        this.targetColumn = targetColumn;
    }

    public RdtFillType getFillShowType() {
        return fillShowType;
    }

    public void setFillShowType(RdtFillType fillShowType) {
        this.fillShowType = fillShowType;
    }

    public RdtFillType getFillSaveType() {
        return fillSaveType;
    }

    public void setFillSaveType(RdtFillType fillSaveType) {
        this.fillSaveType = fillSaveType;
    }

    public String getNotAllowedNullTips() {
        return notAllowedNullTips;
    }

    public void setNotAllowedNullTips(String notAllowedNullTips) {
        this.notAllowedNullTips = notAllowedNullTips;
    }

    public boolean getDisableUpdate() {
        return disableUpdate;
    }

    public void setDisableUpdate(boolean disableUpdate) {
        this.disableUpdate = disableUpdate;
    }

    public List<Object> getFillShowIgnoresType() {
        return fillShowIgnoresType;
    }

    public void setFillShowIgnoresType(List<Object> fillShowIgnoresType) {
        this.fillShowIgnoresType = fillShowIgnoresType;
    }

    public List<Object> getFillSaveIgnoresType() {
        return fillSaveIgnoresType;
    }

    public void setFillSaveIgnoresType(List<Object> fillSaveIgnoresType) {
        this.fillSaveIgnoresType = fillSaveIgnoresType;
    }


}
