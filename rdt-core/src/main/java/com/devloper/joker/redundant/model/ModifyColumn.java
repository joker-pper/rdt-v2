package com.devloper.joker.redundant.model;

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
}
