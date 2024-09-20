package com.stream.app;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

import com.stream.app.service.impl.VideoServiceImpl;

@SpringBootTest
class SpringStreamApplicationTests {
	
	@InjectMocks
	private VideoServiceImpl serviceImpl;

	@Test
	void contextLoads() {
		serviceImpl.processVideo("23b0693b-84e3-4235-b169-f05d3342bad8");
	}

}
