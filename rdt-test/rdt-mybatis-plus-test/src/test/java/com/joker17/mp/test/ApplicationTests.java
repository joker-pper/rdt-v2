package com.joker17.mp.test;

import com.joker17.mp.test.domain.Role;
import com.joker17.mp.test.domain.User;
import com.joker17.mp.test.mapper.RoleMapper;
import com.joker17.mp.test.mapper.UserMapper;
import com.joker17.mp.test.support.JackSonUtils;
import com.joker17.redundant.operation.RdtOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RdtOperation rdtOperation;


    @Test
    public void contextLoads() {
    }

    public String toJson(Object o) {
        return JackSonUtils.toJson(o);
    }




    @Test
    public void testFillForShow() {
        List<User> userList = userMapper.selectList(null);
        rdtOperation.fillForShow(userList);
        logger.info("result: {}", toJson(userList));
    }


    @Test
    public void testUserUpdate() {
        List<User> userList = userMapper.selectList(null);
        rdtOperation.updateRelevant(userList);
    }

    @Test
    public void testRoleUpdate() {
        List<Role> roleList = roleMapper.selectList(null);
        rdtOperation.updateRelevant(roleList);
    }

}
