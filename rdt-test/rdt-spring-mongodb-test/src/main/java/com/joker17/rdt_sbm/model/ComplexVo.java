package com.joker17.rdt_sbm.model;

import com.joker17.rdt_sbm.domain.User;
import com.joker17.redundant.annotation.RdtMany;
import com.joker17.redundant.annotation.RdtOne;
import com.joker17.redundant.annotation.field.RdtField;
import com.joker17.redundant.annotation.field.RdtFieldCondition;
import com.joker17.redundant.annotation.field.RdtLogicalField;

import java.util.List;

public class ComplexVo {

    @RdtFieldCondition(target = User.class, property = "id")
    private String userId;

    @RdtField(target = User.class, property = "username")
    private String userName;

    @RdtMany
    private List<Reply> replyList;

    @RdtOne
    private Reply reply;

    @RdtLogicalField("null")
    private Integer status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Reply> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<Reply> replyList) {
        this.replyList = replyList;
    }

    public Reply getReply() {
        return reply;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
