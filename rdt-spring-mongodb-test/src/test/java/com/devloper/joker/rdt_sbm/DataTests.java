package com.devloper.joker.rdt_sbm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.devloper.joker.rdt_sbm.domain.Article;
import com.devloper.joker.rdt_sbm.domain.ArticleProgress;
import com.devloper.joker.rdt_sbm.domain.User;
import com.devloper.joker.rdt_sbm.repository.ArticleProgressRepository;
import com.devloper.joker.rdt_sbm.repository.ArticleRepository;
import com.devloper.joker.rdt_sbm.repository.UserRepository;
import com.devloper.joker.redundant.operation.MongoRdtOperation;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataTests extends ApplicationTests {

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
    public void updateUserNameWithChanged() throws Exception {
        Optional<User> userOptional = userRepository.findById("1");
        if (userOptional.isPresent()) {
            User before = userOptional.get();
            User current = clone(before);

            //修改userName
            current.setUserName("joker");
            userRepository.save(current);

            //依据前后数据更新相关字段(字段值未发生变化时不会更新该字段相关值)
            rdtOperation.updateMulti(current, before);
        }

    }




    /**
     * 直接更新该数据的全部属性
     */
    @Test
    public void updateUserPropertyAll() throws Exception {
        Optional<User> userOptional = userRepository.findById("1");
        if (userOptional.isPresent()) {
            User before = userOptional.get();
            User current = clone(before);

            //修改userName
            current.setUserName("joker");
            current.setAge(233);

            userRepository.save(current);
            //将会更新被使用的冗余字段
            rdtOperation.updateMulti(current);

           /* for (int i = 0; i < 100; i++) {
                new Thread(() -> {
                    try {
                        rdtOperation.updateMulti(current);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }*/


            Thread.sleep(20000);

        }

    }

    @Test
    public void updateArticleFirst() throws Exception {
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


    @Test
    public void updateByBeforeData() throws Exception {
        Optional<User> userOptional = userRepository.findById("1");
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUserName("joker" + "_" + "yyc(power by " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ")");
            Map<Object, Object> beforeMap = rdtOperation.getBeforeData(user);
            userRepository.save(user);
            rdtOperation.updateRelevant(user, beforeMap);
        }
    }

    /**
     * 通过getBeforeData获取之前的数据,用于更新实体(当前实体未存在被使用的冗余字段,将会减少查询操作)
     */
    @Test
    public void updateByBeforeDataAndNotUsedProperty() throws Exception {
        ArticleProgress articleProgress = articleProgressRepository.findTopBy();
        if (articleProgress != null) {
            Map<Object, Object> beforeMap = rdtOperation.getBeforeData(articleProgress);
            articleProgressRepository.save(articleProgress);
            rdtOperation.updateRelevant(articleProgress, beforeMap);
        }
    }

    @Test
    public void getBeforeDataTest() throws Exception {
        rdtOperation.getBeforeData(new User("1"));
        rdtOperation.getBeforeData(new User("12333"));
        rdtOperation.getBeforeData(new Article("123123"));
        rdtOperation.getBeforeData(new ArticleProgress("123123123"));

    }

    /**
     * 更新type为4的数据,将会修改对应article表中的相关数据
     */
    @Test
    public void updateArticleWithType() throws Exception {
        Article model = articleRepository.findTopByType(4);
        if (model != null) {
            int random = new Random().nextInt(userIds.length);
            model.setAuthor(userNames[random]);
            model.setUserId(userIds[random]);
            model.setContent(userAges[random] + "_更新");
            articleRepository.save(model);
            rdtOperation.updateMulti(model);
        }
    }

    @Test
    public void findByIdIn() throws Exception {
        Collection<User> results = rdtOperation.findByIdIn(User.class, Arrays.asList(userIds[0]));
        log.info("results: {}", JSON.toJSONString(results));

        results = rdtOperation.findByIdIn(User.class, Arrays.asList(userIds));
        log.info("results: {}", JSON.toJSONString(results));
    }

    /*@Test
    public void fill() throws Exception {
        Collection<Article> results = articleRepository.findAll();
        rdtOperation.fill(results);
        log.info("results: {}", JSON.toJSONString(results));
    }*/

}
