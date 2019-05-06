package com.joker17.redundant.fill;

import com.joker17.redundant.model.ClassModel;

import java.io.Serializable;

public class FillKeyModel implements Serializable {

    private Class entityClass;

    private ClassModel classModel;

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public void setClassModel(ClassModel classModel) {
        this.classModel = classModel;
    }
}
