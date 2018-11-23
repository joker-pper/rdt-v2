package com.devloper.joker.rdt_jpa_test;

import com.devloper.joker.rdt_jpa_test.support.JsonUtils;
import com.devloper.joker.rdt_jpa_test.vo.UserManyKeyRelyVo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UserManyKeyRelyFillTest extends ApplicationTests  {

    @Test
    public void fillUserTypeData() throws Exception {
        List<UserManyKeyRelyVo> results = new ArrayList<>();
        UserManyKeyRelyVo vo = new UserManyKeyRelyVo();
        vo.setType(1);
        vo.setCurrentId(2L);
        vo.setCurrentName("用户2");
        results.add(vo);
        rdtOperation.fillForShow(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

    //currentValue输出的为role对应的createTime
    @Test
    public void fillRoleTypeData() throws Exception {
        List<UserManyKeyRelyVo> results = new ArrayList<>();
        UserManyKeyRelyVo vo = new UserManyKeyRelyVo();
        vo.setType(2);
        vo.setCurrentId(1L);
        vo.setCurrentName("SA");
        results.add(vo);
        rdtOperation.fillForShow(results);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

    //匹配不到数据会清除掉无效的数据
    @Test
    public void fillRoleTypeDataWithClear() throws Exception {
        List<UserManyKeyRelyVo> results = new ArrayList<>();
        UserManyKeyRelyVo vo = new UserManyKeyRelyVo();
        vo.setType(2);
        vo.setCurrentId(2L);
        vo.setCurrentName("SA");
        results.add(vo);

        UserManyKeyRelyVo vo2 = new UserManyKeyRelyVo();
        vo2.setType(2);
        vo2.setCurrentId(2L);
        vo2.setCurrentName("SA");
        vo2.setCurrentValue("SA");
        results.add(vo2);

        UserManyKeyRelyVo vo3 = new UserManyKeyRelyVo();
        vo3.setType(2);
        vo3.setCurrentId(null);
        vo3.setCurrentName("SA");
        vo3.setCurrentValue("SA");
        results.add(vo3);

        //添加一组为符合的数据
        UserManyKeyRelyVo vo4 = new UserManyKeyRelyVo();
        vo4.setType(2);
        vo4.setCurrentId(1L);
        vo4.setCurrentName("SA");
        results.add(vo4);

        //添加三组null条件为null的数据
        UserManyKeyRelyVo vo5 = new UserManyKeyRelyVo();
        vo5.setType(2);
        vo5.setCurrentId(null);
        vo5.setCurrentName(null);
        vo5.setCurrentValue("SA");
        results.add(vo5);

        UserManyKeyRelyVo vo6 = new UserManyKeyRelyVo();
        vo6.setType(2);
        vo6.setCurrentId(null);
        vo6.setCurrentName(null);
        vo6.setCurrentValue("SA");
        results.add(vo6);


        UserManyKeyRelyVo vo7= new UserManyKeyRelyVo();
        vo7.setType(1);
        vo7.setCurrentId(null);
        vo7.setCurrentName(null);
        vo7.setCurrentValue("adadaad");
        results.add(vo7);

        rdtOperation.fillForShow(results, true, true);
        logger.info("results: {}", JsonUtils.toJson(results));
    }

}
