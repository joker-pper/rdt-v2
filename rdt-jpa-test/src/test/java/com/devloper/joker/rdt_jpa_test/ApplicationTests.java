package com.devloper.joker.rdt_jpa_test;

import com.devloper.joker.rdt_jpa_test.rdt.RdtJpaCoreResolver;
import com.devloper.joker.rdt_jpa_test.repository.RoleRepository;
import com.devloper.joker.rdt_jpa_test.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

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
	protected RdtJpaCoreResolver coreResolver;

	@Test
	public void contextLoads() {
	}

}
