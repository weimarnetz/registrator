package de.weimarnetz.registrator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import de.weimarnetz.registrator.services.NodeNumberService;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RegistratorApplication.class)
public class RegistratorApplicationTest {

	@Inject
	private ApplicationContext applicationContext;

	@Test
    public void contextLoads() {
        assertThat(applicationContext.getBean(NodeNumberService.class)).isNotNull();
	}



}
