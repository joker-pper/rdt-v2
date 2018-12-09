package com.joker17.redundant.domain;


import com.joker17.redundant.annotation.RdtId;
import com.joker17.redundant.annotation.base.RdtBaseEntity;
import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import com.joker17.redundant.annotation.rely.KeyTarget;
import com.joker17.redundant.annotation.rely.RdtFieldConditionRely;
import com.joker17.redundant.annotation.rely.RdtFieldRely;
import com.joker17.redundant.annotation.rely.RdtRely;

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

    @RdtRely(value = {@KeyTarget(target = User.class, value = {"0", "null"}), @KeyTarget(target = Role.class, value = "ROLE")})
    private AccountType accountType;

    @RdtFieldConditionRely(property = "accountType", targetPropertys = "id")
    private Long createById2;

    @RdtFieldRely(property = "accountType", targetPropertys = {"username", "name"})
    private String createByName2;
}
