package com.server.aydede;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.server.aydede.support.AbstractIntegrationTest;

@SpringBootTest
@ActiveProfiles("test")
class AydedeApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}
