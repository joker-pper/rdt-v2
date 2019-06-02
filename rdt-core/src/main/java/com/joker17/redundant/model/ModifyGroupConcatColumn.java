package com.joker17.redundant.model;

import com.joker17.redundant.annotation.RdtFillType;

import java.io.Serializable;


public class ModifyGroupConcatColumn implements Serializable {

    private Column column;

    private Column targetColumn;

    private RdtFillType fillShowType;

    private RdtFillType fillSaveType;


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
