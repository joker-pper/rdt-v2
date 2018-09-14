package com.devloper.joker.rdt_sbm.domain;

import com.devloper.joker.rdt_sbm.model.ComplexVo;
import com.devloper.joker.rdt_sbm.model.Reply;
import com.devloper.joker.redundant.annotation.RdtMany;
import com.devloper.joker.redundant.annotation.RdtOne;
import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.field.RdtFields;
import com.devloper.joker.redundant.annotation.rely.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document
public class Article {

    //@Id
    private String id;

    @RdtField(target = User.class, property = "username")
    private String author;

    @RdtFieldCondition(target = User.class, property = "id")
    private String userId;

    @RdtField(target = User.class, property = "age")
    private String age;

    @RdtFields({
            @RdtField(target = User.class, property = "age")
    })
    private String content;  //内容

    @RdtRelys({
            @RdtRely(nullType = User.class, unknowType = User.class, value = {@KeyTarget(target = User.class, value = {"1", "2", "3"}),
                    @KeyTarget(target = Article.class, value = "4")})
    })
    private Integer type;

    @RdtFieldConditionRely(property = "type", targetPropertys = "id"/*, nullTypeProperty = "id"*/)
    private String firstParentId;

    @RdtFieldRely(property = "type", targetPropertys = {"username", "content"})
    private String firstParentText;

    @RdtFieldConditionRely(property = "type", targetPropertys = "id", /*nullTypeProperty = "id",*/ index = 1)
    private String secondParentId;

    @RdtFieldRely(property = "type", targetPropertys = {"username", "content"}, index = 1)
    private String secondParentText;

    //子文档更新----依赖于子文档的注解
    @RdtMany
    private List<Reply> replyList;

    @RdtMany
    private Reply[] replyArray;

    @RdtOne
    private Reply reply;

    @RdtOne
    private ComplexVo complex;//复杂对象

    public Article() {
    }

    public Article(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getFirstParentId() {
        return firstParentId;
    }

    public void setFirstParentId(String firstParentId) {
        this.firstParentId = firstParentId;
    }

    public String getFirstParentText() {
        return firstParentText;
    }

    public void setFirstParentText(String firstParentText) {
        this.firstParentText = firstParentText;
    }

    public String getSecondParentId() {
        return secondParentId;
    }

    public void setSecondParentId(String secondParentId) {
        this.secondParentId = secondParentId;
    }

    public String getSecondParentText() {
        return secondParentText;
    }

    public void setSecondParentText(String secondParentText) {
        this.secondParentText = secondParentText;
    }

    public List<Reply> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<Reply> replyList) {
        this.replyList = replyList;
    }

    public Reply[] getReplyArray() {
        return replyArray;
    }

    public void setReplyArray(Reply[] replyArray) {
        this.replyArray = replyArray;
    }

    public Reply getReply() {
        return reply;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
    }

    public ComplexVo getComplex() {
        return complex;
    }

    public void setComplex(ComplexVo complex) {
        this.complex = complex;
    }
}
