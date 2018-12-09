package com.joker17.rdt_sbm.domain;

import com.joker17.redundant.annotation.base.RdtBaseField;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User {

    @Id
    private String id;

    @RdtBaseField(alias = "username")  //设置别名后,property应为该别名
    private String userName;

    private int age;

    private boolean show;

    @Transient
    private String userAgeText;

    public User() {
    }

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean getShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public String getUserAgeText() {
        return userAgeText;
    }

    public void setUserAgeText(String userAgeText) {
        this.userAgeText = userAgeText;
    }
}
