package de.weimarnetz.registrator.controller

import de.weimarnetz.registrator.IntegrationTestConfig
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

internal class RegistratorControllerIT(
    @Autowired
    private val registratorController: RegistratorController
) : IntegrationTestConfig() {
    @Test
    fun getTime() {
        // when
        val time = registratorController.getTime()

        // then
        Assertions.assertThat(time.statusCode).isEqualTo(HttpStatus.OK)
    }
}