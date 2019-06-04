package com.joker17.redundant.fill;

import com.joker17.redundant.model.ClassModel;
import com.joker17.redundant.model.ModifyDescribe;
import com.joker17.redundant.model.ModifyGroupDescribe;

/**
 * 不允许参数值时的异常
 */
public class FillNotAllowedValueException extends IllegalArgumentException {

    private ClassModel classModel;

    private Class dataType;

    private Object data;

    private String property;

    private ModifyDescribe describe;

    private ModifyGroupDescribe groupDescribe;

    public FillNotAllowedValueException() {
        super();
    }

    public FillNotAllowedValueException(String s) {
        super(s);
    }

    public FillNotAllowedValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public FillNotAllowedValueException(Throwable cause) {
        super(cause);
    }


    public FillNotAllowedValueException(String message, ClassModel classModel, Object data, String property, ModifyDescribe describe) {
        this(message, classModel, data, property, describe, null);
    }

    public FillNotAllowedValueException(String message, ClassModel classModel, Object data, String property, ModifyGroupDescribe groupDescribe) {
        this(message, classModel, data, property, null, groupDescribe);
    }

    private FillNotAllowedValueException(String message, ClassModel classModel, Object data, String property, ModifyDescribe describe, ModifyGroupDescribe groupDescribe) {
        super(message);
        this.classModel = classModel;
        this.dataType = classModel.getCurrentClass();
        this.data = data;
        this.property = property;
        this.describe = describe;
        this.groupDescribe = groupDescribe;
    }

    public ClassModel getClassModel() {
        return classModel;
    }

    public void setClassModel(ClassModel classModel) {
        this.classModel = classModel;
    }

    public Class getDataType() {
        return dataType;
    }

    public void setDataType(Class dataType) {
        this.dataType = dataType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public ModifyDescribe getDescribe() {
        return describe;
    }

    public void setDescribe(ModifyDescribe describe) {
        this.describe = describe;
    }

    public ModifyGroupDescribe getGroupDescribe() {
        return groupDescribe;
    }

    public void setGroupDescribe(ModifyGroupDescribe groupDescribe) {
        this.groupDescribe = groupDescribe;
    }
}
