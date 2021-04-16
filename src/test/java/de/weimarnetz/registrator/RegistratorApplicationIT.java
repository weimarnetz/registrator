package de.weimarnetz.registrator;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.weimarnetz.registrator.services.NodeNumberService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RegistratorApplication.class)
public class RegistratorApplicationIT {

    @Inject
    private ApplicationContext applicationContext;

    @Test
    public void contextLoads() {
        assertThat(applicationContext.getBean(NodeNumberService.class)).isNotNull();
    }


}
