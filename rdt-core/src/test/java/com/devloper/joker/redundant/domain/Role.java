package com.devloper.joker.redundant.domain;

import com.devloper.joker.redundant.annotation.RdtId;
import com.devloper.joker.redundant.annotation.base.RdtBaseEntity;
import java.util.Date;

@RdtBaseEntity
public class Role {

    @RdtId
    private Long id;

    private String name;

    private Date createTime;
}
