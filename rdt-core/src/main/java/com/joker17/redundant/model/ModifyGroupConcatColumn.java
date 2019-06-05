package com.joker17.redundant.model;

import com.joker17.redundant.annotation.RdtFillType;


public class ModifyGroupConcatColumn extends ModifyGroupBaseColumn {

    private RdtFillType fillShowType;

    private RdtFillType fillSaveType;

    private boolean startBasicConnector;

    private boolean basicNotConnectorOptFirst;

    public RdtFillType getFillShowType() {
        return fillShowType;
    }

    public void setFillShowType(RdtFillType fillShowType) {
        this.fillShowType = fillShowType;
    }

    public RdtFillType getFillSaveType() {
        return fillSaveType;
    }

    public void setFillSaveType(RdtFillType fillSaveType) {
        this.fillSaveType = fillSaveType;
    }

    public boolean isStartBasicConnector() {
        return startBasicConnector;
    }

    public void setStartBasicConnector(boolean startBasicConnector) {
        this.startBasicConnector = startBasicConnector;
    }

    public boolean isBasicNotConnectorOptFirst() {
        return basicNotConnectorOptFirst;
    }

    public void setBasicNotConnectorOptFirst(boolean basicNotConnectorOptFirst) {
        this.basicNotConnectorOptFirst = basicNotConnectorOptFirst;
    }
}
