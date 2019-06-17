package com.joker17.redundant.model;

import java.util.ArrayList;
import java.util.List;

public class ModifyGroupKeysColumn extends ModifyGroupBaseColumn {

    private String notAllowedNullTips;

    /**
     * 不为空时用于获取相应数据的base class (即中间表)
     */
    private Class gainClass;

    /**
     * 中间表的数据列 (用于获取当前ModifyGroupKeysColumn所对应的key值)
     */
    private Column gainSelectColumn;

    /**
     * 中间表条件列
     */
    private List<Column> gainConditionColumnList = new ArrayList<Column>(3);

    /**
     * 中间表条件列值所依赖当前model的列(用于获取条件值)
     */
    private List<Column> gainConditionValueRelyColumnList = new ArrayList<Column>(3);


    public String getNotAllowedNullTips() {
        return notAllowedNullTips;
    }

    public void setNotAllowedNullTips(String notAllowedNullTips) {
        this.notAllowedNullTips = notAllowedNullTips;
    }


    public Class getGainClass() {
        return gainClass;
    }

    public void setGainClass(Class gainClass) {
        this.gainClass = gainClass;
    }

    public Column getGainSelectColumn() {
        return gainSelectColumn;
    }

    public void setGainSelectColumn(Column gainSelectColumn) {
        this.gainSelectColumn = gainSelectColumn;
    }

    public List<Column> getGainConditionColumnList() {
        return gainConditionColumnList;
    }

    public void setGainConditionColumnList(List<Column> gainConditionColumnList) {
        this.gainConditionColumnList = gainConditionColumnList;
    }

    public List<Column> getGainConditionValueRelyColumnList() {
        return gainConditionValueRelyColumnList;
    }

    public void setGainConditionValueRelyColumnList(List<Column> gainConditionValueRelyColumnList) {
        this.gainConditionValueRelyColumnList = gainConditionValueRelyColumnList;
    }
}
