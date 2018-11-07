package com.devloper.joker.rdt_jpa_test;

import com.devloper.joker.rdt_jpa_test.support.JsonUtils;
import com.devloper.joker.rdt_jpa_test.vo.UserManyKeyVo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserManyKeyFillTest extends ApplicationTests  {

    @Test
    public void fill() throws Exception {
        List<UserManyKeyVo> results = new ArrayList<>();
        UserManyKeyVo vo = new UserManyKeyVo();
        vo.setUserId(2L);
        vo.setUsername("用户2");
        results.add(vo);
        coreResolver.fill(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

    /**
     * 4组数据,其中2组相同条件的数据,应该只进行3次查询
     * @throws Exception
     */
    @Test
    public void fills() throws Exception {
        List<UserManyKeyVo> results = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            UserManyKeyVo vo = new UserManyKeyVo();
            long val = 2;
            if (i != 3) {
                val += i;
            }
            vo.setUserId(val);
            vo.setUsername("用户" + val);
            results.add(vo);
        }
        coreResolver.fill(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

    @Test
    public void fills2() throws Exception {
        List<UserManyKeyVo> results = new ArrayList<>();
        Set<UserManyKeyVo> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            UserManyKeyVo vo = new UserManyKeyVo();
            long val = 2;
            vo.setUserId(val);
            vo.setUsername("用户" + val);
            results.add(vo);
            set.add(vo);
        }
        coreResolver.fill(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

}