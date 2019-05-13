package com.joker17.redundant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LogicalModel implements Serializable {

    private Column column;

    /**
     * 正常值
     */
    private List<Object> values = new ArrayList<Object>(16);

    private Class type;


    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

}
