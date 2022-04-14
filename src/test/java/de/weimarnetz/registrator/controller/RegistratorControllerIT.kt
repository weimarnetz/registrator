package de.weimarnetz.registrator.controller

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class RegistratorControllerIT(
    @Autowired
    private val registratorController: RegistratorController
) {
    @Test
    fun getTime() {
        // when
        val time = registratorController.getTime()

        // then
        Assertions.assertThat(time.statusCode).isEqualByComparingTo(HttpStatus.OK)
    }
}