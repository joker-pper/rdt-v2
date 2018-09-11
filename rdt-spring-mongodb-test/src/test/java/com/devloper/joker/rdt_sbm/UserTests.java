package com.devloper.joker.rdt_sbm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.devloper.joker.rdt_sbm.domain.User;
import com.devloper.joker.rdt_sbm.repository.UserRepository;
import com.devloper.joker.redundant.operation.MongoRdtOperation;
import com.devloper.joker.redundant.support.Prototype;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserTests extends ApplicationTests {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private UserRepository userRepository;

    @Resource
    private MongoRdtOperation rdtOperation;

    private String[] userNames = new String[]{"张三", "李四", "王五"};
    private int[] userAges = new int[]{22, 25, 23};
    private String[] userIds = new String[]{"1", "2", "3"};

    private boolean initByZero = true;

    /**
     * 运行时初始化数据
     */
    @Before
    public void initData() {
        List<User> userList = new ArrayList<>();
        if (!initByZero || (initByZero && userRepository.count() == 0)) {
            for (int i = 0; i < userIds.length; i++) {
                User user = new User();
                user.setId(userIds[i]);
                user.setAge(userAges[i]);
                user.setUserName(userNames[i]);
                userList.add(user);
            }
            userRepository.saveAll(userList);
        }

    }


    public <T> T clone(T model) {
        if (model != null)  {
            String json = JSON.toJSONString(model);
            model = (T) JSONObject.parseObject(json, model.getClass());
        }
        return model;

    }

    @Test
    public void updateUserNameWithChanged() {
        Optional<User> userOptional = userRepository.findById("1");
        if (userOptional.isPresent()) {
            User before = userOptional.get();
            User current = clone(before);

            //修改userName
            current.setUserName("joker");
            userRepository.save(current);

            //依据前后数据更新相关字段
            rdtOperation.updateMulti(current, before);
        }

    }

    /**
     * 直接更新该数据的全部属性
     */
    @Test
    public void updateUserPropertyAll() {
        Optional<User> userOptional = userRepository.findById("1");
        if (userOptional.isPresent()) {
            User before = userOptional.get();
            User current = clone(before);

            //修改userName
            current.setUserName("joker");

            userRepository.save(current);
            //将会更新被使用的冗余字段
            rdtOperation.updateMulti(current);
        }

    }

}
