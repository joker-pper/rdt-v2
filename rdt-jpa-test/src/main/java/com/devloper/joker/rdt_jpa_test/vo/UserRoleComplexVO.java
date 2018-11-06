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
public class UserRoleComplexVO {

    @RdtFieldCondition(property = "id", target = User.class)
    private Long id;

    @RdtField(target = User.class)
    private String username;

    @RdtField(target = User.class)
    private String createTime;

    @RdtField(property = "roleName", target = User.class)
    private String userRoleName;


    @RdtFieldCondition(property = "id", target = Role.class)
    private Long roleId;

    @RdtField(property = "name", target = Role.class)
    private String roleName;

    @RdtFieldCondition(property = "id", target = Role.class, index = 1)
    private Long roleId2;

    @RdtField(property = "name", target = Role.class, index = 1)
    private String roleName2;


    @RdtFieldCondition(target = User.class, property = "id", index = 1)
    private Long userId;

    @RdtFieldCondition(target = User.class, property = "username", index = 1)
    private String username2;

    @RdtField(target = User.class, property = "createTime", index = 1)
    private String createTime2;

    @RdtOne
    private UserRoleComplexVO parent;

    @RdtOne
    private User user;

    @RdtMany
    private User[] userArray;

    @RdtMany
    private List<User> userList;

    @RdtMany
    private List<UserRoleComplexVO> complexVOList;

}
