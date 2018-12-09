package com.joker17.rdt_jpa_test.service;

import com.joker17.rdt_jpa_test.base.BaseService;
import com.joker17.rdt_jpa_test.domain.User;
import org.springframework.stereotype.Service;

import java.util.Random;


@Service
public class UserService extends BaseService<User, Long> implements IUserService {

    @Override
    public void updateUser(Long id, String username){
        User user = getOne(id);
        user.setUsername(username);
        save(user);
        if (new Random().nextInt(100) > 88) {
            logger.warn("抛出异常~~~~~~~~");
            throw new IllegalArgumentException("抛出异常");
        }
    }
}
