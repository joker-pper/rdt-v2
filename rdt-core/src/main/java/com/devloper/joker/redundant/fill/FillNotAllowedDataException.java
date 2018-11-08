package com.devloper.joker.redundant.fill;

/**
 * 未找到条件所对应数据时的异常
 */
public class FillNotAllowedDataException extends IllegalArgumentException {

    private FillKeyModel fillKeyModel;

    private Class dataType;

    public FillNotAllowedDataException() {
        super();
    }

    public FillNotAllowedDataException(String s) {
        super(s);
    }

    public FillNotAllowedDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FillNotAllowedDataException(Throwable cause) {
        super(cause);
    }

    public FillNotAllowedDataException(FillKeyModel fillKeyModel, Class dataType,  String msg) {
        super(msg);
        this.fillKeyModel = fillKeyModel;
        this.dataType = dataType;
    }

    public FillKeyModel getFillKeyModel() {
        return fillKeyModel;
    }

    public void setFillKeyModel(FillKeyModel fillKeyModel) {
        this.fillKeyModel = fillKeyModel;
    }

    public Class getDataType() {
        return dataType;
    }

    public void setDataType(Class dataType) {
        this.dataType = dataType;
    }
}
