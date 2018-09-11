package com.devloper.joker.rdt_sbm.model;

import com.devloper.joker.rdt_sbm.domain.User;
import com.devloper.joker.redundant.annotation.RdtMany;
import com.devloper.joker.redundant.annotation.RdtOne;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;

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
}
