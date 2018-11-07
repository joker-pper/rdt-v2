package com.devloper.joker.rdt_jpa_test.vo;

import com.devloper.joker.rdt_jpa_test.domain.Role;
import com.devloper.joker.rdt_jpa_test.domain.User;
import com.devloper.joker.redundant.annotation.RdtMany;
import com.devloper.joker.redundant.annotation.RdtOne;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleVO {
    /**
     * 基于 user id的字段列
     */
    @RdtFieldCondition(property = "id", target = User.class)
    private Long id;

    @RdtField(target = User.class)
    private String username;

    @RdtField(target = User.class)
    private String createTime;

    @RdtField(property = "roleName", target = User.class)
    private String userRoleName;

    /**
     * 两组基于roleId的字段列
     */

    @RdtFieldCondition(property = "id", target = Role.class)
    private Long roleId;

    @RdtField(property = "name", target = Role.class)
    private String roleName;

    @RdtFieldCondition(property = "id", target = Role.class, index = 1)
    private Long roleId2;

    @RdtField(property = "name", target = Role.class, index = 1)
    private String roleName2;


    /**
     * 基于username填充对应的role name及 user id
     */

    @RdtFieldCondition(property = "username", target = User.class, index = 1)
    private String username2;

    @RdtField(property = "roleName", target = User.class, index = 1)
    private String userRoleName2;


    @RdtField(property = "id", target = User.class, index = 1)
    private String userId2;
}
