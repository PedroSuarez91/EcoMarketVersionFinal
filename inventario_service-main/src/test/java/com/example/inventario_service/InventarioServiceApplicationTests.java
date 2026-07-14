package com.example.inventario_service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = InventarioServiceApplication.class)
@ActiveProfiles("test")
class InventarioServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainClassExist() {
		assertNotNull(InventarioServiceApplication.class);
	}
}