package de.weimarnetz.registrator

import de.weimarnetz.registrator.services.NodeNumberService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [RegistratorApplication::class])
class RegistratorApplicationIT(
    @Autowired
    private val applicationContext: ApplicationContext
) {

    @Test
    fun contextLoads() {
        Assertions.assertThat(
            applicationContext.getBean(
                NodeNumberService::class.java
            )
        ).isNotNull
    }
}