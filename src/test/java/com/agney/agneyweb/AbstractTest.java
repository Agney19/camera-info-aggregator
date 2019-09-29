package com.agney.agneyweb;

import com.agney.agneyweb.config.MockitoConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Configuration()
@Import(Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MockitoConfig.class)
public abstract class AbstractTest {
	protected MockMvc mockMvc;

	@Autowired
	public void setContext(WebApplicationContext context) {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}
}
