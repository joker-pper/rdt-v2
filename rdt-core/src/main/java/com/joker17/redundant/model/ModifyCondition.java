package com.joker17.redundant.model;


/**
 * 修改条件
 */
public class ModifyCondition extends ModifyColumn {

    /**
     * 不允许为空的提示
     */
    private String notAllowedNullTips;

    public String getNotAllowedNullTips() {
        return notAllowedNullTips;
    }

    public void setNotAllowedNullTips(String notAllowedNullTips) {
        this.notAllowedNullTips = notAllowedNullTips;
    }
}
