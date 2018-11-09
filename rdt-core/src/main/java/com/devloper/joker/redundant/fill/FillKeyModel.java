package com.devloper.joker.redundant.fill;

import java.io.Serializable;

public class FillKeyModel implements Serializable {

    private Class entityClass;

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }
}
