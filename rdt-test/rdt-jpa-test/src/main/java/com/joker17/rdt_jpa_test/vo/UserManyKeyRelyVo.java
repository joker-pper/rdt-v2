package com.joker17.rdt_jpa_test.vo;

import com.joker17.rdt_jpa_test.domain.Role;
import com.joker17.rdt_jpa_test.domain.User;
import com.joker17.redundant.annotation.fill.RdtVO;
import com.joker17.redundant.annotation.rely.KeyTarget;
import com.joker17.redundant.annotation.rely.RdtFieldConditionRely;
import com.joker17.redundant.annotation.rely.RdtFieldRely;
import com.joker17.redundant.annotation.rely.RdtRely;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RdtVO
public class UserManyKeyRelyVo {

    /**
     * 基于依赖的多组条件列的数据
     */
    @RdtFieldConditionRely(property = "type", targetPropertys = "id")
    private Long currentId;

    @RdtFieldConditionRely(property = "type", targetPropertys = {"username", "name"})
    private String currentName;

    //不同类型时: user -> roleName role -> createTime
    @Transient
    @RdtFieldRely(property = "type", targetPropertys = {"roleName", "createTime"})
    private String currentValue;


    @RdtRely({@KeyTarget(target = User.class, value = "1"), @KeyTarget(target = Role.class, value = "2")})
    private Integer type;

}
