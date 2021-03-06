package com.joker17.rdt_jpa_test.vo;

import com.joker17.rdt_jpa_test.domain.Role;
import com.joker17.rdt_jpa_test.domain.User;
import java.util.Date;
import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import com.joker17.redundant.annotation.fill.RdtGroupConcatField;
import com.joker17.redundant.annotation.fill.RdtGroupKeys;
import com.joker17.redundant.annotation.fill.RdtVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@RdtVO
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


    @RdtField(property = "roleId", target = User.class)
    private String userRoleId;


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

    @RdtGroupKeys(property = "id", target = Role.class)
    private String roleIds;

    @RdtGroupConcatField(property = "name", target = Role.class)
    private String roleNames;

    @RdtGroupConcatField(property = "name", target = Role.class)
    private List<String> roleNameList;

    @RdtGroupConcatField(property = "name", target = Role.class)
    private String[] roleNameArray;


    @RdtGroupConcatField(property = "createTime", target = Role.class)
    private String roleCreateTimes;


    @RdtGroupConcatField(property = "createTime", target = Role.class)
    private List<Date> roleCreateTimeList;

    @RdtGroupConcatField(property = "createTime", target = Role.class)
    private String[] roleCreateTimeArray;


    @RdtGroupKeys(property = "id", target = Role.class, index = 1)
    private List<String> roleId2List;

    @RdtGroupConcatField(property = "name", target = Role.class, index = 1)
    private List<Object> role2NameList;

    @RdtGroupConcatField(property = "createTime", target = Role.class, index = 1)
    private String role2CreateTimes;

    @RdtGroupConcatField(property = "name", target = Role.class, index = 1, startBasicConnector = false, basicNotConnectorOptFirst = true)
    private String role2FirstName;

    @RdtGroupConcatField(property = "name", target = Role.class, index = 1, startBasicConnector = false)
    private String role2LastName;

    @RdtGroupConcatField(property = "createTime", target = Role.class, index = 1, basicNotConnectorOptFirst = true)
    private Date role2FirstCreateTime;

    @RdtGroupConcatField(property = "createTime", target = Role.class, index = 1)
    private Date role2LastCreateTime;

    /**
     * 配置中间表机制动态获取当前groupKeys值(即roleIds)
     */
    @RdtGroupKeys(property = "id", target = Role.class, index = 2, gain = User.class, gainConditionPropertys = "id", gainConditionValueRelyPropertys = "id", gainProperty = "roleId")
    private String roleIdsByGainStrategy;

    @RdtGroupConcatField(property = "name", target = Role.class, index = 2)
    private String roleNamesByGainStrategy;

    @RdtGroupConcatField(property = "name", target = Role.class, index = 2)
    private String[] roleNameArrayByGainStrategy;

    @RdtGroupConcatField(property = "name", target = Role.class, index = 2)
    private List<String> roleNameListByGainStrategy;

    /**
     * 仅动态加载groupKeys值
     */
    @RdtGroupKeys(property = "id", target = Role.class, index = 3, gain = User.class, gainConditionPropertys = "id", gainConditionValueRelyPropertys = "id", gainProperty = "roleId")
    private String roleIdsByGainStrategyOnlyHasGroupKey;

    @RdtGroupKeys(property = "id", target = Role.class, index = 4, gain = User.class, gainConditionPropertys = "id", gainConditionValueRelyPropertys = "id", gainProperty = "roleId")
    private String[] roleIdsByGainStrategyOnlyHasGroupKeyArray;

}
