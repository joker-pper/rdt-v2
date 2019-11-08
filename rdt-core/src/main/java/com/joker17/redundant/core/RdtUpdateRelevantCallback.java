package com.joker17.redundant.core;

public interface RdtUpdateRelevantCallback {

    Object castToEntityMapKeyValue(Object idKeyValue);

    Object castToEntity(Class entityClass, Object data);

}
