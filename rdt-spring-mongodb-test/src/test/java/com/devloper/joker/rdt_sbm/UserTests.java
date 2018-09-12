package com.devloper.joker.rdt_sbm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.devloper.joker.rdt_sbm.domain.Article;
import com.devloper.joker.rdt_sbm.domain.User;
import com.devloper.joker.rdt_sbm.repository.ArticleProgressRepository;
import com.devloper.joker.rdt_sbm.repository.ArticleRepository;
import com.devloper.joker.rdt_sbm.repository.UserRepository;
import com.devloper.joker.redundant.operation.MongoRdtOperation;
import com.devloper.joker.redundant.support.Prototype;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class UserTests extends ApplicationTests {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private UserRepository userRepository;

    @Resource
    private MongoRdtOperation rdtOperation;

    @Resource
    private ArticleRepository articleRepository;

    @Resource
    private ArticleProgressRepository articleProgressRepository;

    private String[] userNames = new String[]{"张三", "李四", "王五"};
    private int[] userAges = new int[]{22, 25, 23};
    private boolean[] userShows = new boolean[]{true, false, false};
    private String[] userIds = new String[]{"1", "2", "3"};


    public <T> T clone(T model) {
        if (model != null)  {
            String json = JSON.toJSONString(model);
            model = (T) JSONObject.parseObject(json, model.getClass());
        }
        return model;

    }

    @Test
    public void updateUserNameWithChanged() {
        Optional<User> userOptional = userRepository.findById("1");
        if (userOptional.isPresent()) {
            User before = userOptional.get();
            User current = clone(before);

            //修改userName
            current.setUserName("joker");
            userRepository.save(current);

            //依据前后数据更新相关字段
            rdtOperation.updateMulti(current, before);
        }

    }

    /**
     * 直接更新该数据的全部属性
     */
    @Test
    public void updateUserPropertyAll() {
        Optional<User> userOptional = userRepository.findById("1");
        if (userOptional.isPresent()) {
            User before = userOptional.get();
            User current = clone(before);

            //修改userName
            current.setUserName("joker");

            userRepository.save(current);
            //将会更新被使用的冗余字段
            rdtOperation.updateMulti(current);
        }

    }

    @Test
    public void updateArticleFirst() {
        Article first = articleRepository.findTopBy();
        if (first != null) {
            Article before = articleRepository.findTopBy();
            int random = new Random().nextInt(userIds.length);
            first.setAuthor(userNames[random]);
            first.setUserId(userIds[random]);
            articleRepository.save(first);
            if (first.getUserId().equals(before.getUserId())) {
                log.info("{}", "article的userId未发生变化");
            } else {
                log.info("{}", "修改第一个article的数据,articleProgress表中第一条记录的相关值也将更新");
            }

            rdtOperation.updateMulti(first, before);
        }


    }


}
