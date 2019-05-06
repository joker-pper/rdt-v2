package com.joker17.rdt_jpa_test.vo;

import com.joker17.rdt_jpa_test.domain.User;
import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserManyKeyVo {

    /**
     * 基于多组条件列的数据
     */
    @RdtFieldCondition(property = "id", target = User.class, nullTips = "userId不能为空")
    private Long userId;

    @RdtFieldCondition(target = User.class)
    private String username;

    @RdtField(target = User.class)
    private String createTime;

    @RdtField(property = "roleName", target = User.class)
    private String userRoleName;


}
