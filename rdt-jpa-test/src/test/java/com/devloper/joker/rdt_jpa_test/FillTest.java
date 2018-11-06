package com.devloper.joker.rdt_jpa_test;

import com.alibaba.fastjson.JSON;
import com.devloper.joker.rdt_jpa_test.domain.User;
import com.devloper.joker.rdt_jpa_test.support.JsonUtils;
import com.devloper.joker.rdt_jpa_test.vo.UserRoleComplexVO;
import com.devloper.joker.rdt_jpa_test.vo.UserRoleVO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FillTest extends ApplicationTests  {


    public Long getRoleRandomId() {
        return (long) new Random().nextInt(2) + 1;
    }

    public Long getUserRandomId() {
        return (long) new Random().nextInt(5) + 1;
    }

    public String getUserRandomName() {
        long id = (long) new Random().nextInt(5) + 1;
        if (id == 1) {
            id = 2;
        }
        return "用户" + id;

    }

    @Test
    public void fillUserRoleVO() throws Exception {
        UserRoleVO vo = new UserRoleVO();
        vo.setId(getUserRandomId());
        vo.setRoleId(getRoleRandomId());
        vo.setRoleId2(getRoleRandomId());
        vo.setUsername2(getUserRandomName());

        List<UserRoleVO> results = Arrays.asList(vo);
        coreResolver.fill(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }




    @Test
    public void fillUserRoleVOS() throws Exception {
        List<UserRoleVO> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            UserRoleVO vo = new UserRoleVO();
            vo.setId(getUserRandomId());
            vo.setRoleId(getRoleRandomId());
            vo.setRoleId2(getRoleRandomId());
            vo.setUsername2(getUserRandomName());
            results.add(vo);
        }

        coreResolver.fill(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }


    @Test
    public void fill() throws Exception {
        List<UserRoleComplexVO> results = getUserRoleVoList();
        coreResolver.fill(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

    @Test
    public void fill2() throws Exception {
        List<User> users = userService.findAll();
        coreResolver.fill(users);
        logger.info("results: {}", JsonUtils.toJson(users));
    }

    @Test
    public void fill3() throws Exception {
        List<User> users = userService.findAll();
        List<Object> objectList = new ArrayList<>();
        users.get(0).setType(99);
        objectList.addAll(users);
        objectList.addAll(getUserRoleVoList());
        coreResolver.fill(objectList);
        logger.info("results: {}", JsonUtils.toJson(objectList));
    }

    @Test
    public void fill4() throws Exception {
        List<User> users = userService.findAll();
        List<UserRoleComplexVO> results = getUserRoleVoList();
        results.get(0).setParent(results.get(2));
        results.get(3).setUserList(users);
        results.get(2).setUserArray(JSON.parseArray(JsonUtils.toJson(users), User.class).toArray(new User[users.size()]));
        results.get(0).setComplexVOList(getUserRoleVoList());

        coreResolver.fill(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

    public List<UserRoleComplexVO> getUserRoleVoList() {
        List<UserRoleComplexVO> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UserRoleComplexVO vo = new UserRoleComplexVO();
            vo.setId((long)i + 1);
            vo.setRoleId(getRoleRandomId());
            if (i == 4) {
                vo.setRoleId2((long)2);
            } else if (i == 3) {
                vo.setRoleId2(null);
            } else {
                vo.setRoleId2((long)1);
            }
            vo.setUsername2("用户" + (new Random().nextInt(2) + 2));
            results.add(vo);
        }
        return results;
    }


}
