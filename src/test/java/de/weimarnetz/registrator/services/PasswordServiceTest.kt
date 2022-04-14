package de.weimarnetz.registrator.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PasswordServiceTest {
    private val passwordService: PasswordService = PasswordService()

    @Test
    fun encryptPassword() {
        // when
        val encryptPassword = passwordService.encryptPassword("test")

        // then
        assertThat(encryptPassword).matches("^\\$2a\\$.{56}$")
    }
}