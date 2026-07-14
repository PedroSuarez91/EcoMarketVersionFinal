package com.example.envio_service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EnvioServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainClassExist() {
		assertNotNull(EnvioServiceApplication.class);
	}
}