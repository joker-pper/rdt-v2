package com.devloper.joker.rdt_sbm;

import com.devloper.joker.rdt_sbm.domain.Article;
import com.devloper.joker.rdt_sbm.domain.ArticleProgress;
import com.devloper.joker.rdt_sbm.domain.User;
import com.devloper.joker.rdt_sbm.model.ComplexVo;
import com.devloper.joker.rdt_sbm.model.RelyVo;
import com.devloper.joker.rdt_sbm.model.Reply;
import com.devloper.joker.rdt_sbm.repository.ArticleProgressRepository;
import com.devloper.joker.rdt_sbm.repository.ArticleRepository;
import com.devloper.joker.rdt_sbm.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 用于初始化数据
 */
public class DataInitTests extends ApplicationTests {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private UserRepository userRepository;

    @Resource
    private ArticleRepository articleRepository;

    @Resource
    private ArticleProgressRepository articleProgressRepository;


    private String[] userNames = new String[]{"张三", "李四", "王五"};
    private int[] userAges = new int[]{22, 25, 23};
    private boolean[] userShows = new boolean[]{true, false, false};
    private String[] userIds = new String[]{"1", "2", "3"};


    /**
     * 运行时初始化数据
     */
    @Before
    public void before() {
        userRepository.deleteAll();
        articleRepository.deleteAll();
        articleProgressRepository.deleteAll();
    }


    @Test
    public void init() {
        List<User> userList = getUsers();
        //初始化8条article和progress
        List<Article> articleList = getArticles(8);
        List<ArticleProgress> articleProgressList = new ArrayList<>();
        for (int i = 0; i < articleList.size(); i++) {
            articleProgressList.add(getArticleProgress(articleList.get(i)));
        }
        userRepository.saveAll(userList);
        articleRepository.saveAll(articleList);
        articleProgressRepository.saveAll(articleProgressList);
    }


    public int getRandomUserIndex() {
        return new Random().nextInt(userIds.length);
    }


    public ArticleProgress getArticleProgress(Article article) {
        ArticleProgress progress = new ArticleProgress();
        progress.setId(UUID.randomUUID().toString().replace("-", ""));
        progress.setAuthor(article.getAuthor());
        progress.setArticleId(article.getId());
        progress.setUserId(article.getUserId());
        return progress;
    }


    public List<Article> getArticles(int size) {
        List<Article> articleList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Article article = new Article();
            article.setId(UUID.randomUUID().toString());

            int currentUserIndex = getRandomUserIndex();

            //设置用户id和作者
            article.setUserId(userIds[currentUserIndex]);
            article.setAuthor(userNames[currentUserIndex]);
            article.setAge(userAges[currentUserIndex] + "");
            article.setContent(article.getAge());

            int randomInt = new Random().nextInt(5);
            Integer type = randomInt == 0 ? null : randomInt;
            article.setType(type);

            if (randomInt == 4) {
                //此类型时为article

                article.setFirstParentId(article.getId());
                article.setFirstParentText(article.getContent());

                Article secondArticle = article;
                if (articleList.size() > 0) {
                    secondArticle = articleList.get(new Random().nextInt(articleList.size()));
                }

                article.setSecondParentId(secondArticle.getId());
                article.setSecondParentText(secondArticle.getContent());
            } else {
                currentUserIndex = getRandomUserIndex();
                article.setFirstParentId(userIds[currentUserIndex]);
                article.setFirstParentText(userNames[currentUserIndex]);

                currentUserIndex = getRandomUserIndex();
                article.setSecondParentId(userIds[currentUserIndex]);
                article.setSecondParentText(userNames[currentUserIndex]);
            }


            article.setReplyList(getReplyList(6, true));
            article.setReplyArray(article.getReplyList().toArray(new Reply[article.getReplyList().size()]));
            article.setReply(getReply());
            article.setComplex(getComplexVo());

            articleList.add(article);
        }

        return articleList;
    }

    public List<User> getUsers() {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < userNames.length; i++) {
            User user = new User();
            user.setUserName(userNames[i]);
            user.setId(userIds[i]);
            user.setAge(userAges[i]);
            user.setShow(userShows[i]);
            userList.add(user);
        }
        return userList;
    }


    public ComplexVo getComplexVo() {
        ComplexVo vo = new ComplexVo();
        int index = getRandomUserIndex();
        vo.setUserId(userIds[index]);
        vo.setUserName(userNames[index]);
        vo.setReply(getReply());
        vo.setReplyList(getReplyList(6, true));

        return vo;
    }

    public List<Reply> getReplyList(int size, boolean random) {
        List<Reply> replies = new ArrayList<>();
        if (random) size = new Random().nextInt(size);
        for (int j = 0; j < size; j++) {
            replies.add(getReply());
        }
        return replies;
    }


    public Reply getReply() {
        int first = getRandomUserIndex();
        int second = getRandomUserIndex();

        Reply reply = new Reply();
        reply.setId(UUID.randomUUID().toString());
        reply.setUserId(userIds[first]);
        reply.setUserName(userNames[first]);
        if (first != second) {
            reply.setAnswerUserId(userIds[second]);
            reply.setAnswerUserName(userNames[second]);
        }

        RelyVo relyVo = new RelyVo();
        relyVo.setId(UUID.randomUUID().toString());
        relyVo.setType(1);
        relyVo.setParentId(reply.getUserId());
        relyVo.setValue(reply.getUserName());
        relyVo.setUserId(reply.getUserId());
        relyVo.setUserAge(userAges[first] + "");

        reply.setRelyVo(relyVo);

        return reply;
    }
}
