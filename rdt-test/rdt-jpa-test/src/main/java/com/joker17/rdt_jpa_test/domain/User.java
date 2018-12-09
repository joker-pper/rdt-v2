package com.joker17.rdt_jpa_test.domain;


import com.alibaba.fastjson.annotation.JSONField;
import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import com.joker17.redundant.annotation.rely.KeyTarget;
import com.joker17.redundant.annotation.rely.RdtFieldConditionRely;
import com.joker17.redundant.annotation.rely.RdtFieldRely;
import com.joker17.redundant.annotation.rely.RdtRely;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
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


    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @RdtFieldCondition(property = "id", target = Role.class)
    private Long roleId;

    @RdtField(property = "name", target = Role.class)
    private String roleName;

    @RdtField(property = "createTime", target = Role.class)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date roleCreateTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
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
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @RdtFieldConditionRely(property = "accountType", targetPropertys = "id")
    private Long createById2;

    @RdtFieldRely(property = "accountType", targetPropertys = {"username", "name"})
    private String createByName2;

}
