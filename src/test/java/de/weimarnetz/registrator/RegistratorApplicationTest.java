package de.weimarnetz.registrator;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import de.weimarnetz.registrator.services.NodeNumberService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RegistratorApplication.class)
public class RegistratorApplicationTest {

	@Inject
	private ApplicationContext applicationContext;

	@Test
	public void beansloaded() {
		assertThat(applicationContext.getBean(NodeNumberService.class)).isNotNull();
	}



}
