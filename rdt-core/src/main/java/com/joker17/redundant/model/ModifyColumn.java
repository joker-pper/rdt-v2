package com.joker17.redundant.model;

import com.joker17.redundant.annotation.RdtFillType;

import java.io.Serializable;

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

    private RdtFillType fillShowType = RdtFillType.DEFAULT;

    private RdtFillType fillSaveType = RdtFillType.DEFAULT;

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
}
