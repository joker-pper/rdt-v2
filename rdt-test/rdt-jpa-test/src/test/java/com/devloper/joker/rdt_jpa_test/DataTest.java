package com.devloper.joker.rdt_jpa_test;

import com.devloper.joker.rdt_jpa_test.domain.Role;
import com.devloper.joker.rdt_jpa_test.domain.User;
import com.devloper.joker.rdt_jpa_test.support.JsonUtils;
import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class DataTest extends ApplicationTests {


    @Test
    public void findByIdIn() {
        Object results = rdtOperation.findByIdIn(User.class, Arrays.asList(new Long[]{1L}));
        logger.info("results: {}", JsonUtils.toJson(results));

        results = rdtOperation.findByIdIn(User.class, Arrays.asList(new Long[]{1L, 2L}));
        logger.info("results: {}", JsonUtils.toJson(results));
    }

    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false) //测试不回滚
    @Test
    public void roleModify() throws Exception {
        Role role = roleRepository.findById(1L).get();
        String name = role.getName();
        if (name.matches("[a-z]+")) {
            name = name.toUpperCase();
        } else {
            name = name.toLowerCase();
        }
        role.setName(name);
        role.setCreateTime(new Date());
        roleRepository.save(role);
        rdtOperation.updateMulti(role);

    }


    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false) //测试不回滚
    @Test
    public void userModify() throws Exception {
        User user = userRepository.findById(1L).get();
        user.setUsername("jokers");
        userRepository.save(user);
        rdtOperation.updateMulti(user);
    }


    @Test
    public void userModifyWithService() throws Exception {
        User user = userService.getOne(1L);
        user.setUsername("jokerw");
        userService.save(user);
        //rdtOperation.updateMulti(user);
       //throw new NullPointerException("xxxxx");
    }

    @Test
    public void updateUser() throws Exception {
        userService.updateUser(1L, "O(∩_∩)O哈哈~");
    }


    @Test
    public void userFindAll() throws Exception {
        List<User> users = userService.findAll();
        userService.saveAll(users);
        logger.info("results: {}", JsonUtils.toJson(users));
    }

}
