package com.devloper.joker.rdt_jpa_test;

import com.devloper.joker.rdt_jpa_test.domain.Role;
import com.devloper.joker.rdt_jpa_test.domain.User;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DataInitTest extends ApplicationTests {

    private List<Role> roleList = new ArrayList<>();

    @Before
    public void before() {
        roleRepository.deleteAll();
        userRepository.deleteAll();

        roleList.add(new Role(1L, "SA", new Date()));
        roleList.add(new Role(2L, "ADMIN", new Date()));
    }


    @Test
    public void init() {

        List<User> userList = new ArrayList<>();
        roleRepository.saveAll(roleList);

        for (int i = 0; i < 5; i++) {
            Role role = getRole(new Random().nextInt(2));
            int userType;
            if (i % 2 == 0) {
                userType = User.TYPE_ROLE;
            } else {
                userType = User.TYPE_USER;
            }
            long createById;
            String createByName;
            if (userType ==  User.TYPE_ROLE) {
                createById = new Random().nextInt(2) + 1;
                createByName = getRole((int) createById - 1).getName();
            } else {
                createById = new Random().nextInt(i) + 1;
                createByName = userList.get((int) createById - 1).getUsername();
            }

            userList.add(new User((long) i + 1, "用户" + (i + 1),
                    role.getId(), role.getName(), role.getCreateTime(),
                    new Date(), userType, createById, createByName));
        }


        userRepository.saveAll(userList);
    }

    private Role getRole(int id) {
        return roleList.get(id);
    }



}
