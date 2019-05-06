package com.joker17.redundant.model.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KeyTargetModel implements Serializable {

    private List<Object> valueList = new ArrayList<Object>();

    private String notAllowedTypeTips;

    private Boolean disableUpdate;

    private List<Object> updateIgnoresValueList = new ArrayList<Object>();

    public List<Object> getValueList() {
        return valueList;
    }

    public void setValueList(List<Object> valueList) {
        this.valueList = valueList;
    }

    public String getNotAllowedTypeTips() {
        return notAllowedTypeTips;
    }

    public void setNotAllowedTypeTips(String notAllowedTypeTips) {
        this.notAllowedTypeTips = notAllowedTypeTips;
    }

    public Boolean getDisableUpdate() {
        return disableUpdate;
    }

    public void setDisableUpdate(Boolean disableUpdate) {
        this.disableUpdate = disableUpdate;
    }

    public List<Object> getUpdateIgnoresValueList() {
        return updateIgnoresValueList;
    }

    public void setUpdateIgnoresValueList(List<Object> updateIgnoresValueList) {
        this.updateIgnoresValueList = updateIgnoresValueList;
    }
}
