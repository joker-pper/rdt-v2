package com.devloper.joker.rdt_jpa_test;

import com.devloper.joker.rdt_jpa_test.domain.Role;
import com.devloper.joker.rdt_jpa_test.domain.User;
import com.devloper.joker.rdt_jpa_test.support.JsonUtils;
import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public class DataTest extends ApplicationTests {

    @Test
    public void findByIdIn() {
        Object results = coreResolver.findByIdIn(User.class, Arrays.asList(new Long[]{1L}));
        logger.info("results: {}", JsonUtils.toJson(results));

        results = coreResolver.findByIdIn(User.class, Arrays.asList(new Long[]{1L, 2L}));
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
        coreResolver.updateMulti(role);

    }


    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false) //测试不回滚
    @Test
    public void userModify() throws Exception {
        User user = userRepository.findById(1L).get();
        user.setUsername("joker");
        userRepository.save(user);
        coreResolver.updateMulti(user);
    }
}
