package com.joker17.rdt_jpa_test;

import com.alibaba.fastjson.JSON;
import com.joker17.rdt_jpa_test.domain.User;
import com.joker17.rdt_jpa_test.support.JsonUtils;
import com.joker17.rdt_jpa_test.vo.UserRoleComplexVO;
import com.joker17.rdt_jpa_test.vo.UserRoleVO;
import com.joker17.redundant.fill.FillNotAllowedValueException;
import com.joker17.redundant.fill.FillType;
import org.junit.Assert;
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
        rdtOperation.fillForShow(results, false, FillType.ALL);
        logger.info("results: {}", JsonUtils.toJson(results));
    }


    /**
     * allowedNullValue为false时,作为条件列的值必须存在
     * @throws Exception
     */
    @Test
    public void fillUserRoleVOWithNotAllowedNullValue() throws Exception {
        UserRoleVO vo = new UserRoleVO();
        vo.setId(getUserRandomId());
        //vo.setRoleId(getRoleRandomId());
        vo.setRoleId2(getRoleRandomId());
        vo.setUsername2(getUserRandomName());

        List<UserRoleVO> results = Arrays.asList(vo);
        try {
            rdtOperation.fillForSave(results, false);
            logger.info("results: {}", JsonUtils.toJson(results));
        } catch (Exception e) {
            if (e instanceof FillNotAllowedValueException) {
                FillNotAllowedValueException valueException = (FillNotAllowedValueException) e;
                if (valueException.getDataType() == UserRoleVO.class) {
                    switch (valueException.getProperty()) {
                        case "username2":
                            logger.error("用户名不能为空");
                            break;
                        case "roleId":
                        case "roleId2":
                            logger.error("角色不能为空");
                            break;

                    }

                }
            }
            e.printStackTrace();
        }
    }


    /**
     * checkValue为true时,条件值必须匹配到对应的结果
     * @throws Exception
     */
    @Test
    public void fillUserRoleVOWithCheckValue() throws Exception {
        UserRoleVO vo = new UserRoleVO();
        vo.setId(getUserRandomId());
        //vo.setRoleId(getRoleRandomId());
        vo.setRoleId2(3L);
        vo.setUsername2(getUserRandomName());

        List<UserRoleVO> results = Arrays.asList(vo);
        rdtOperation.fill(results, true, true, true);
        logger.info("results: {}", JsonUtils.toJson(results));
    }


    /**
     *
     * @throws Exception
     */
    @Test
    public void fillUserRoleVOWithClear() throws Exception {
        UserRoleVO vo = new UserRoleVO();
        vo.setId(getUserRandomId());
        //vo.setRoleId(getRoleRandomId());
        vo.setRoleId2(3L); //不存在的id值
        vo.setRoleName2("测试不存在的角色2");
        vo.setUsername2(getUserRandomName());

        List<UserRoleVO> results = Arrays.asList(vo);
        rdtOperation.fill(results, true, false, true);

        Assert.assertNull("roleId2应该为null", vo.getRoleId2());
        Assert.assertNull("roleName2应该为null", vo.getRoleName2());
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

        rdtOperation.fillForShow(results, false, FillType.ALL);
        logger.info("results: {}", JsonUtils.toJson(results));
    }


    @Test
    public void fill() throws Exception {
        List<UserRoleComplexVO> results = getUserRoleVoList();
        rdtOperation.fillForShow(results, false,FillType.ALL);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

    @Test
    public void fill2() throws Exception {
        List<User> users = userService.findAll();
        rdtOperation.fillForShow(users, false,FillType.ALL);
        logger.info("results: {}", JsonUtils.toJson(users));
    }

    @Test
    public void fill3() throws Exception {
        List<User> users = userService.findAll();
        List<Object> objectList = new ArrayList<>();
        users.get(0).setType(99);
        objectList.addAll(users);
        objectList.addAll(getUserRoleVoList());
        rdtOperation.fillForShow(objectList, false,FillType.ALL);
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

        rdtOperation.fillForShow(results, false,FillType.ALL);
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
