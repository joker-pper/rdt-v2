package com.joker17.rdt_jpa_test;

import com.joker17.rdt_jpa_test.repository.RoleRepository;
import com.joker17.rdt_jpa_test.repository.UserRepository;
import com.joker17.rdt_jpa_test.service.IUserService;
import com.joker17.redundant.operation.RdtOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final Integer USER_TYPE_USER = 1;

	@Resource
	protected UserRepository userRepository;

	@Resource
	protected RoleRepository roleRepository;

	@Resource
	protected RdtOperation rdtOperation;

	@Resource
	protected IUserService userService;

	@Test
	public void contextLoads() {
	}

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
}
