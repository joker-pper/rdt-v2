package com.devloper.joker.redundant.domain;

import com.devloper.joker.redundant.annotation.RdtId;
import com.devloper.joker.redundant.annotation.base.RdtBaseEntity;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.rely.KeyTarget;
import com.devloper.joker.redundant.annotation.rely.RdtFieldRely;
import com.devloper.joker.redundant.annotation.rely.RdtRely;

@RdtBaseEntity
public class Article {

    @RdtId
    private Long id;

    @RdtFieldCondition(property = "id", target = User.class)
    private Long userId;

    @RdtField(target = User.class, property = "username")
    private String author;


    @RdtFieldCondition(property = "id", target = Role.class)
    private Long roleId;

    @RdtField(property = "name", target = Role.class)
    private String roleName;

    /**
     * 使用target属性限定唯一,不再根据@KeyTarget中存在的class动态指定多个
     */
    @RdtFieldRely(property = "hasReply", targetPropertys = "username", target = User.class)
    private String layerUsername;

    @RdtFieldRely(property = "hasReply", targetPropertys = {"name"}, target = Role.class)
    private String layerRoleName;

    @RdtRely(value = {@KeyTarget(target = Role.class, value = "1"), @KeyTarget(target = User.class, value = {"1", "2"})}, unique = false)
    private Integer hasReply;


    @RdtFieldRely(property = "hasReply2", targetPropertys =  {"name", "username"})
    private String layerUsername2;

    @RdtFieldRely(property = "hasReply2", targetPropertys = {"name", "username"})
    private String layerRoleName2;

    @RdtRely(value = {@KeyTarget(target = Role.class, value = "true"), @KeyTarget(target = User.class, value = "true")}, unique = false)
    private boolean hasReply2;



}
