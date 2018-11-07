package com.devloper.joker.rdt_jpa_test.vo;

import com.devloper.joker.rdt_jpa_test.domain.Role;
import com.devloper.joker.rdt_jpa_test.domain.User;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.rely.KeyTarget;
import com.devloper.joker.redundant.annotation.rely.RdtFieldConditionRely;
import com.devloper.joker.redundant.annotation.rely.RdtFieldRely;
import com.devloper.joker.redundant.annotation.rely.RdtRely;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserManyKeyRelyVo {

    /**
     * 基于依赖的多组条件列的数据
     */
    @RdtFieldConditionRely(property = "type", targetPropertys = "id")
    private Long currentId;

    @RdtFieldConditionRely(property = "type", targetPropertys = {"username", "name"})
    private String currentName;

    //不同类型时: user -> roleName role -> createTime
    @RdtFieldRely(property = "type", targetPropertys = {"roleName", "createTime"})
    private String currentValue;


    @RdtRely({@KeyTarget(target = User.class, value = "1"), @KeyTarget(target = Role.class, value = "2")})
    private Integer type;

}
