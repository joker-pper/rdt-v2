package com.joker17.redundant.model;

import com.joker17.redundant.annotation.RdtFillType;


public class ModifyGroupConcatColumn extends ModifyGroupBaseColumn {

    private RdtFillType fillShowType;

    private RdtFillType fillSaveType;

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
