package com.joker17.redundant.model;

import com.joker17.redundant.model.commons.ClassTypeEnum;

import java.io.Serializable;

public class ModifyGroupBaseColumn implements Serializable {

    private Column column;
    private Column targetColumn;

    private String connector;

    private Class targetColumnClass;

    private Class columnClass;

    private Class columnBasicClass;

    private ClassTypeEnum columnClassType;

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

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public Class getTargetColumnClass() {
        return targetColumnClass;
    }

    public void setTargetColumnClass(Class targetColumnClass) {
        this.targetColumnClass = targetColumnClass;
    }

    public Class getColumnClass() {
        return columnClass;
    }

    public void setColumnClass(Class columnClass) {
        this.columnClass = columnClass;
    }

    public Class getColumnBasicClass() {
        return columnBasicClass;
    }

    public void setColumnBasicClass(Class columnBasicClass) {
        this.columnBasicClass = columnBasicClass;
    }

    public ClassTypeEnum getColumnClassType() {
        return columnClassType;
    }

    public void setColumnClassType(ClassTypeEnum columnClassType) {
        this.columnClassType = columnClassType;
    }
}
