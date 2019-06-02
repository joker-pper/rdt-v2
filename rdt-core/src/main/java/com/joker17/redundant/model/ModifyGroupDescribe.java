package com.joker17.redundant.model;

import java.io.Serializable;

public class ModifyGroupDescribe implements Comparable, Serializable {

    private Class entityClass;
    private Class targetClass;

    private int index;

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

    @Override
    public int compareTo(Object o) {
        int x = this.getIndex();
        int y = ((ModifyGroupDescribe)o).getIndex();
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
