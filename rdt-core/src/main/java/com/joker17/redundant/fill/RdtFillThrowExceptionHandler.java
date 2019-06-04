package com.joker17.redundant.fill;

import com.joker17.redundant.model.*;

public interface RdtFillThrowExceptionHandler {

    void throwFillNotAllowedValueException(String msg);

    void throwFillNotAllowedValueException(ClassModel dataClassModel, ModifyDescribe modifyDescribe, ModifyCondition modifyCondition, Object data, String msg);

    void throwFillNotAllowedValueException(ClassModel dataClassModel, ModifyGroupDescribe modifyGroupDescribe, ModifyGroupKeysColumn modifyGroupKeysColumn, Object data, String msg);

    void throwFillNotAllowedDataException(FillKeyModel fillKeyModel, int expectSize, String msg);

}
