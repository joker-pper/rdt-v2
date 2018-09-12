package com.devloper.joker.rdt_sbm.domain;

import com.devloper.joker.redundant.annotation.field.RdtField;
import com.devloper.joker.redundant.annotation.field.RdtFieldCondition;
import com.devloper.joker.redundant.annotation.field.RdtFields;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ArticleProgress {

    @Id
    private String id;

    @RdtFieldCondition(target = Article.class, property = "id")
    private String articleId;

    @RdtField(target = Article.class)
    @RdtFieldCondition(target = User.class, property = "id")
    private String userId;

    @RdtFields(
            {
              @RdtField(target = Article.class),
              @RdtField(target = User.class, property = "username")
            }
    )
    private String author;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
