package com.joker17.mp.test.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.joker17.redundant.annotation.RdtFillType;
import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import com.joker17.redundant.annotation.field.RdtLogicalField;
import com.joker17.redundant.annotation.rely.KeyTarget;
import com.joker17.redundant.annotation.rely.RdtFieldConditionRely;
import com.joker17.redundant.annotation.rely.RdtFieldRely;
import com.joker17.redundant.annotation.rely.RdtRely;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "user")
public class User implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String username;

    private String password;

    @RdtFieldCondition(target = Role.class, property = "id")
    private Long roleId;

    @RdtField(target = Role.class, property = "name")
    private String roleName;

    @RdtField(target = Role.class, property = "name")
    @TableField(exist = false)
    private String roleTransientName;


    @RdtField(target = Role.class, property = "createTime")
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date roleTransientCreateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    //模拟rely配置,relyType及createRelyById随机生成,createRelyByName启用填充

    /**
     * user: 1、3
     * role: 2
     */
    @RdtRely(value = {@KeyTarget(target = User.class, value = {"1", "3"}), @KeyTarget(target = Role.class, value = "2")}, unknownType = User.class)
    private Integer relyType = new Random().nextInt(4) + 1;

    @RdtFieldConditionRely(property = "relyType", targetPropertys = "id")
    private Long createRelyById = (long) new Random().nextInt(2) + 1;

    @RdtFieldRely(property = "relyType", targetPropertys = {"username", "name"}, fillShow = RdtFillType.ENABLE)
    private String createRelyByName;

    /**
     * 逻辑列 正常值: null
     */
    @RdtLogicalField(value = "null")
    private Integer status;


}
