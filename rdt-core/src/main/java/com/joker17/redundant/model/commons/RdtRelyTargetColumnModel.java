package com.joker17.redundant.model.commons;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析 @RdtFieldRely关于class所对于列的数据
 */
public class RdtRelyTargetColumnModel {

    private int group;

    /**
     * 处于不同target class所对应的列信息
     */
    private Map<Class, RdtRelyTargetColumnDetailModel> classTargetColumnDetailMap = new HashMap<Class, RdtRelyTargetColumnDetailModel>();

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public Map<Class, RdtRelyTargetColumnDetailModel> getClassTargetColumnDetailMap() {
        return classTargetColumnDetailMap;
    }

    public void setClassTargetColumnDetailMap(Map<Class, RdtRelyTargetColumnDetailModel> classTargetColumnDetailMap) {
        this.classTargetColumnDetailMap = classTargetColumnDetailMap;
    }
}
