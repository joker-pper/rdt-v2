package com.joker17.redundant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModifyGroupDescribe implements Comparable, Serializable {

    private Class entityClass;
    private Class targetClass;

    private int index;

    private ModifyGroupKeysColumn modifyGroupKeysColumn;
    private List<ModifyGroupConcatColumn> modifyGroupConcatColumnList = new ArrayList<ModifyGroupConcatColumn>(16);

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public ModifyGroupKeysColumn getModifyGroupKeysColumn() {
        return modifyGroupKeysColumn;
    }

    public void setModifyGroupKeysColumn(ModifyGroupKeysColumn modifyGroupKeysColumn) {
        this.modifyGroupKeysColumn = modifyGroupKeysColumn;
    }

    public List<ModifyGroupConcatColumn> getModifyGroupConcatColumnList() {
        return modifyGroupConcatColumnList;
    }

    public void setModifyGroupConcatColumnList(List<ModifyGroupConcatColumn> modifyGroupConcatColumnList) {
        this.modifyGroupConcatColumnList = modifyGroupConcatColumnList;
    }

    @Override
    public int compareTo(Object o) {
        int x = this.getIndex();
        int y = ((ModifyGroupDescribe)o).getIndex();
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
