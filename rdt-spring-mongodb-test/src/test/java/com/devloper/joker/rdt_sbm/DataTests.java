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
     * 保存一组关于userId为1的复杂数据,观察值修改结果
     */
    @Test
    public void saveArticleForTest() {

        Article article = JSONObject.parseObject("{\n" +
                "    \"id\":\"test_1\",\n" +
                "    \"age\":\"25\",\n" +
                "    \"author\":\"李四\",\n" +
                "    \"complex\":{\n" +
                "        \"reply\":{\n" +
                "            \"answerUserId\":\"1\",\n" +
                "            \"answerUserName\":\"张三\",\n" +
                "            \"id\":\"c3bdfd70-f325-4784-adbf-87802cdcc6bb\",\n" +
                "            \"relyVo\":{\n" +
                "                \"id\":\"cd143356-4618-4269-80d1-6c0f19b10c58\",\n" +
                "                \"parentId\":\"1\",\n" +
                "                \"type\":1,\n" +
                "                \"userAge\":\"123\",\n" +
                "                \"userId\":\"1\",\n" +
                "                \"value\":\"王五\"\n" +
                "            },\n" +
                "            \"userId\":\"1\",\n" +
                "            \"userName\":\"王五\"\n" +
                "        },\n" +
                "        \"replyList\":[\n" +
                "            {\n" +
                "                \"answerUserId\":\"1\",\n" +
                "                \"answerUserName\":\"张三\",\n" +
                "                \"id\":\"dfb27fa0-67af-475d-bfd6-0180462ccece\",\n" +
                "                \"relyVo\":{\n" +
                "                    \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                    \"parentId\":\"1\",\n" +
                "                    \"type\":1,\n" +
                "                    \"userAge\":\"123\",\n" +
                "                    \"userId\":\"1\",\n" +
                "                    \"value\":\"李四\"\n" +
                "                },\n" +
                "                \"userId\":\"1\",\n" +
                "                \"userName\":\"李四\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"answerUserId\":\"1\",\n" +
                "                \"answerUserName\":\"张三\",\n" +
                "                \"id\":\"dfb27fa0-67af-475d-bfd6-0180462ccece\",\n" +
                "                \"relyVo\":{\n" +
                "                    \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                    \"parentId\":\"1\",\n" +
                "                    \"type\":1,\n" +
                "                    \"userAge\":\"123\",\n" +
                "                    \"userId\":\"1\",\n" +
                "                    \"value\":\"李四\"\n" +
                "                },\n" +
                "                \"userId\":\"1\",\n" +
                "                \"userName\":\"李四\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"answerUserId\":\"1\",\n" +
                "                \"answerUserName\":\"张三\",\n" +
                "                \"id\":\"dfb27fa0-67af-475d-bfd6-0180413\",\n" +
                "                \"relyVo\":{\n" +
                "                    \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                    \"parentId\":\"1\",\n" +
                "                    \"type\":1,\n" +
                "                    \"userAge\":\"123\",\n" +
                "                    \"userId\":\"1\",\n" +
                "                    \"value\":\"李四\"\n" +
                "                },\n" +
                "                \"userId\":\"1\",\n" +
                "                \"userName\":\"李四\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"userId\":\"1\",\n" +
                "        \"userName\":\"张三\"\n" +
                "    },\n" +
                "    \"content\":\"25\",\n" +
                "    \"firstParentId\":\"1\",\n" +
                "    \"firstParentText\":\"张三\",\n" +
                "    \"reply\":{\n" +
                "        \"id\":\"e3618964-dec6-4ed2-8b1a-98caa02ad7f6\",\n" +
                "        \"relyVo\":{\n" +
                "            \"id\":\"3ef08f63-921d-4c83-9b0b-8f0cb4154952\",\n" +
                "            \"parentId\":\"1\",\n" +
                "            \"type\":1,\n" +
                "            \"userAge\":\"123\",\n" +
                "            \"userId\":\"1\",\n" +
                "            \"value\":\"张三\"\n" +
                "        },\n" +
                "        \"userId\":\"1\",\n" +
                "        \"userName\":\"张三\"\n" +
                "    },\n" +
                "    \"replyArray\":[\n" +
                "        {\n" +
                "            \"answerUserId\":\"1\",\n" +
                "            \"answerUserName\":\"张三\",\n" +
                "            \"id\":\"dfb27fa0-67af-475d-bfd6-0180462ccece\",\n" +
                "            \"relyVo\":{\n" +
                "                \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                \"parentId\":\"1\",\n" +
                "                \"type\":1,\n" +
                "                \"userAge\":\"123\",\n" +
                "                \"userId\":\"1\",\n" +
                "                \"value\":\"李四\"\n" +
                "            },\n" +
                "            \"userId\":\"1\",\n" +
                "            \"userName\":\"李四\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"answerUserId\":\"1\",\n" +
                "            \"answerUserName\":\"张三\",\n" +
                "            \"id\":\"dfb27fa0-67af-475d-bfd6-0180462ccece\",\n" +
                "            \"relyVo\":{\n" +
                "                \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                \"parentId\":\"1\",\n" +
                "                \"type\":1,\n" +
                "                \"userAge\":\"123\",\n" +
                "                \"userId\":\"1\",\n" +
                "                \"value\":\"李四\"\n" +
                "            },\n" +
                "            \"userId\":\"1\",\n" +
                "            \"userName\":\"李四\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"answerUserId\":\"1\",\n" +
                "            \"answerUserName\":\"张三\",\n" +
                "            \"id\":\"dfb27fa0-67af-475d-bfd6-0180413\",\n" +
                "            \"relyVo\":{\n" +
                "                \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                \"parentId\":\"1\",\n" +
                "                \"type\":1,\n" +
                "                \"userAge\":\"123\",\n" +
                "                \"userId\":\"1\",\n" +
                "                \"value\":\"李四\"\n" +
                "            },\n" +
                "            \"userId\":\"1\",\n" +
                "            \"userName\":\"李四\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"replyList\":[\n" +
                "        {\n" +
                "            \"answerUserId\":\"1\",\n" +
                "            \"answerUserName\":\"张三\",\n" +
                "            \"id\":\"dfb27fa0-67af-475d-bfd6-0180462ccece\",\n" +
                "            \"relyVo\":{\n" +
                "                \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                \"parentId\":\"1\",\n" +
                "                \"type\":1,\n" +
                "                \"userAge\":\"123\",\n" +
                "                \"userId\":\"1\",\n" +
                "                \"value\":\"李四\"\n" +
                "            },\n" +
                "            \"userId\":\"1\",\n" +
                "            \"userName\":\"李四\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"answerUserId\":\"1\",\n" +
                "            \"answerUserName\":\"张三\",\n" +
                "            \"id\":\"dfb27fa0-67af-475d-bfd6-0180462ccece\",\n" +
                "            \"relyVo\":{\n" +
                "                \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                \"parentId\":\"1\",\n" +
                "                \"type\":1,\n" +
                "                \"userAge\":\"123\",\n" +
                "                \"userId\":\"1\",\n" +
                "                \"value\":\"李四\"\n" +
                "            },\n" +
                "            \"userId\":\"1\",\n" +
                "            \"userName\":\"李四\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"answerUserId\":\"1\",\n" +
                "            \"answerUserName\":\"张三\",\n" +
                "            \"id\":\"dfb27fa0-67af-475d-bfd6-0180413\",\n" +
                "            \"relyVo\":{\n" +
                "                \"id\":\"8b97c30b-0b2f-4889-a763-699df7e05058\",\n" +
                "                \"parentId\":\"1\",\n" +
                "                \"type\":1,\n" +
                "                \"userAge\":\"123\",\n" +
                "                \"userId\":\"1\",\n" +
                "                \"value\":\"李四\"\n" +
                "            },\n" +
                "            \"userId\":\"1\",\n" +
                "            \"userName\":\"李四\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"secondParentId\":\"1\",\n" +
                "    \"secondParentText\":\"王五\",\n" +
                "    \"type\":2,\n" +
                "    \"userId\":\"1\"\n" +
                "}", Article.class);
        articleRepository.save(article);
    }

    /**
     * 直接更新该数据的全部属性
     *
     *
     * o.s.data.mongodb.core.MongoTemplate      : Calling update using query: { "_id" : "1d318229-feba-4691-b950-41ae104334a7", "replyArray.0.relyVo._id" : "8b97c30b-0b2f-4889-a763-699df7e05058", "replyArray.1.relyVo._id" : "8b97c30b-0b2f-4889-a763-699df7e05058", "replyArray.2.relyVo._id" : "8b97c30b-0b2f-4889-a763-699df7e05058" }
     *    and update: { "$set" : { "replyArray.0.relyVo.userAge" : "0", "replyArray.1.relyVo.userAge" : "0", "replyArray.2.relyVo.userAge" : "0" } } in collection: article
     *
     */
    @Test
    public void updateUserPropertyAll() throws Exception {
        Optional<User> userOptional = userRepository.findById("1");
        if (userOptional.isPresent()) {
            User before = userOptional.get();
            User current = clone(before);

            //修改userName
            current.setUserName("joker");
            current.setAge(221);

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
            Map<Object, Object> beforeMap = rdtOperation.getCurrentMapData(user);
            userRepository.save(user);
            rdtOperation.updateRelevant(user, beforeMap);
        }
    }

    /**
     * 通过getCurrentMapData获取之前的数据,用于更新实体(当前实体未存在被使用的冗余字段,将会减少查询操作)
     */
    @Test
    public void updateByBeforeDataAndNotUsedProperty() throws Exception {
        ArticleProgress articleProgress = articleProgressRepository.findTopBy();
        if (articleProgress != null) {
            Map<Object, Object> beforeMap = rdtOperation.getCurrentMapData(articleProgress);
            articleProgressRepository.save(articleProgress);
            rdtOperation.updateRelevant(articleProgress, beforeMap);
        }
    }

    @Test
    public void getCurrentMapDataTest() throws Exception {
        rdtOperation.getCurrentMapData(new User("1"));
        rdtOperation.getCurrentMapData(new User("12333"));
        rdtOperation.getCurrentMapData(new Article("123123"));
        rdtOperation.getCurrentMapData(new ArticleProgress("123123123"));

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

    @Test
    public void fill() throws Exception {
        Collection<Article> results = articleRepository.findAll();
        //rdtOperation.fillForShow(results);
        log.info("results: {}", JSON.toJSONString(results));
    }

}
