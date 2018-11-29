package com.devloper.joker.redundant.domain;


import com.devloper.joker.redundant.annotation.RdtId;
import com.devloper.joker.redundant.annotation.base.RdtBaseEntity;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.rely.KeyTarget;
import com.devloper.joker.redundant.annotation.rely.RdtFieldConditionRely;
import com.devloper.joker.redundant.annotation.rely.RdtFieldRely;
import com.devloper.joker.redundant.annotation.rely.RdtRely;

import java.util.Date;

import java.util.Date;

@RdtBaseEntity
public class User {
    public final static int TYPE_USER = 1;
    public final static int TYPE_ROLE = 2;

    /**
     * 用户类型
     */
    public enum AccountType {
        USER,
        ROLE;
    }


    @RdtId
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @RdtFieldCondition(property = "id", target = Role.class)
    private Long roleId;

    @RdtField(property = "name", target = Role.class)
    private String roleName;

    @RdtField(property = "createTime", target = Role.class)
    private Date roleCreateTime;

    private Date createTime;

    /**
     * 1: user 2:role
     */
    @RdtRely(value = {@KeyTarget(target = User.class, value = {TYPE_USER + "", "3"}), @KeyTarget(target = Role.class, value = TYPE_ROLE + "")})
    private Integer type;

    @RdtFieldConditionRely(property = "type", targetPropertys = "id")
    private Long createById;

    @RdtFieldRely(property = "type", targetPropertys = {"username", "name"})
    private String createByName;

    @RdtRely(value = {@KeyTarget(target = User.class, value = "0"), @KeyTarget(target = Role.class, value = "ROLE")}, nullType = User.class)
    private AccountType accountType;

    @RdtFieldConditionRely(property = "accountType", targetPropertys = "id")
    private Long createById2;

    @RdtFieldRely(property = "accountType", targetPropertys = {"username", "name"})
    private String createByName2;
}
