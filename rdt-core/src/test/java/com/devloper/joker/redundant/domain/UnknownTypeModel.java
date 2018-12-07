package com.devloper.joker.redundant.domain;

import com.devloper.joker.redundant.annotation.RdtId;
import com.devloper.joker.redundant.annotation.base.RdtBaseEntity;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.rely.KeyTarget;
import com.devloper.joker.redundant.annotation.rely.RdtFieldConditionRely;
import com.devloper.joker.redundant.annotation.rely.RdtFieldRely;
import com.devloper.joker.redundant.annotation.rely.RdtRely;

@RdtBaseEntity
public class UnknownTypeModel {

    @RdtId
    private Long id;


    @RdtRely(value = {@KeyTarget(target = Role.class/*, value = {"3"}*/), @KeyTarget(target = User.class, value = {"1", "2"})}, unknownType = Role.class)
    private Integer type;


    @RdtFieldConditionRely(property = "type", targetPropertys =  {"id"}, target = Role.class)
    private Long roleId;

    @RdtFieldRely(property = "type", targetPropertys =  {"name"}, target = Role.class)
    private String roleName;

    @RdtFieldConditionRely(property = "type", targetPropertys =  {"id"}, target = User.class)
    private Long userId;

    @RdtFieldRely(property = "type", targetPropertys =  {"username"}, target = User.class)
    private String userName;

    @RdtFieldConditionRely(property = "type", targetPropertys =  {"id"}, target = User.class, index = 1)
    private Long userId2;

    @RdtFieldRely(property = "type", targetPropertys =  {"username"}, target = User.class, index = 1)
    private String userName2;


    /**
     * 由于根据持久化类型 + index进行匹配当前持久化类的条件和修改列,当前为新的一组,设置不同的index
     */
    @RdtFieldConditionRely(property = "type", targetPropertys =  {"id"}, index = 2)
    private Long dynamicId;

    @RdtFieldRely(property = "type", targetPropertys =  {"name", "username"}, index = 2)
    private String dynamicText;


}
