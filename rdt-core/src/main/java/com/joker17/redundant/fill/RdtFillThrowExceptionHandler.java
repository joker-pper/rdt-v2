package com.joker17.redundant.fill;

import com.joker17.redundant.model.ClassModel;
import com.joker17.redundant.model.ModifyCondition;
import com.joker17.redundant.model.ModifyDescribe;

public interface RdtFillThrowExceptionHandler {
    void throwFillNotAllowedValueException(ClassModel dataClassModel, ModifyDescribe modifyDescribe, ModifyCondition modifyCondition, Object data, String msg);

    void throwFillNotAllowedDataException(FillKeyModel fillKeyModel, int expectSize, String msg);

}
