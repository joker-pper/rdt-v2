package com.devloper.joker.rdt_sbm.model;

import com.devloper.joker.rdt_sbm.domain.User;
import com.devloper.joker.redundant.annotation.RdtOne;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;

public class Reply {

    private String id;

    @RdtFieldCondition(target = User.class, property = "id", index = 1)
    private String answerUserId;  //当前回复的用户id

    @RdtField(target = User.class, property = "username", index = 1)
    private String answerUserName;  //当前回复的用户名称

    @RdtFieldCondition(target = User.class, property = "id")
    private String userId;

    @RdtField(target = User.class, property = "username")
    private String userName;

    private String detail;

    @RdtOne
    private RelyVo relyVo;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAnswerUserId() {
        return answerUserId;
    }

    public void setAnswerUserId(String answerUserId) {
        this.answerUserId = answerUserId;
    }

    public String getAnswerUserName() {
        return answerUserName;
    }

    public void setAnswerUserName(String answerUserName) {
        this.answerUserName = answerUserName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public RelyVo getRelyVo() {
        return relyVo;
    }

    public void setRelyVo(RelyVo relyVo) {
        this.relyVo = relyVo;
    }
}
