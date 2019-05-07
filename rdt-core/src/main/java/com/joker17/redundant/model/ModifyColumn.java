package com.joker17.redundant.model;

import com.joker17.redundant.annotation.RdtFillType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//修改列所相关属性
public class ModifyColumn implements Serializable {

    /**
     * 当前列数据
     */
    private Column column;

    /**
     * 对应列数据
     */
    private Column targetColumn;

    private RdtFillType fillShowType;

    private RdtFillType fillSaveType;

    /**
     * fillShow忽略的依赖列类型值列表
     */
    private List<Object> fillShowIgnoresType = new ArrayList<Object>();

    /**
     * fillSave忽略的依赖列类型值列表
     */
    private List<Object> fillSaveIgnoresType = new ArrayList<Object>();



    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

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
