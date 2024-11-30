package de.weimarnetz.registrator

import de.weimarnetz.registrator.services.NodeNumberService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class RegistratorApplicationIT(
    @Autowired
    private val applicationContext: ApplicationContext
) : IntegrationTestConfig() {

    @Test
    fun contextLoads() {
        Assertions.assertThat(
            applicationContext.getBean(
                NodeNumberService::class.java
            )
        ).isNotNull
    }
}