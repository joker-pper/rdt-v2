package com.devloper.joker.rdt_sbm.model;

import com.devloper.joker.rdt_sbm.domain.Article;
import com.devloper.joker.rdt_sbm.domain.User;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.rely.*;

public class RelyVo {

    private String id;

    @RdtFieldCondition(target = User.class, property = "id")
    private String userId;
    @RdtField(target = User.class, property = "age")
    private String userAge;

    @RdtFieldConditionRely(property = "type", targetPropertys = {"id"}, index = 1)
    private String parentId;
    @RdtFieldRely(property = "type", targetPropertys = {"username"}, index = 1)
    private String value;

    @RdtRelys({
            @RdtRely(value = {@KeyTarget(value = {"0", "1"}, target = User.class)
            }, valType = Integer.class, nullType = User.class),
            @RdtRely(value = @KeyTarget(value = {"66", "67"}, target = User.class), valType = String.class, nullType = User.class, group = 66)
    }
    )
    private Object type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAge() {
        return userAge;
    }

    public void setUserAge(String userAge) {
        this.userAge = userAge;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }
}
