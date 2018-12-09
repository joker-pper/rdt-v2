package com.joker17.redundant.fill;

import com.joker17.redundant.model.ModifyDescribe;

/**
 * 不允许参数值时的异常
 */
public class FillNotAllowedValueException extends IllegalArgumentException {

    private FillKeyModel fillKeyModel;

    private Object data;

    private String property;

    private Class dataType;

    private ModifyDescribe describe;

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


    public FillNotAllowedValueException(FillKeyModel fillKeyModel, Class dataType, Object data, ModifyDescribe describe, String property, String message) {
        super(message);
        this.fillKeyModel = fillKeyModel;
        this.data = data;
        this.describe = describe;
        this.property = property;
        this.dataType = dataType;
    }


    public FillKeyModel getFillKeyModel() {
        return fillKeyModel;
    }

    public void setFillKeyModel(FillKeyModel fillKeyModel) {
        this.fillKeyModel = fillKeyModel;
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

    public Class getDataType() {
        return dataType;
    }

    public void setDataType(Class dataType) {
        this.dataType = dataType;
    }

    public ModifyDescribe getDescribe() {
        return describe;
    }

    public void setDescribe(ModifyDescribe describe) {
        this.describe = describe;
    }
}
