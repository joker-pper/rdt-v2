package com.joker17.redundant.domain;

import com.joker17.redundant.annotation.RdtId;
import com.joker17.redundant.annotation.base.RdtBaseEntity;
import java.util.Date;

@RdtBaseEntity
public class Role {

    @RdtId
    private Long id;

    private String name;

    private Date createTime;
}
