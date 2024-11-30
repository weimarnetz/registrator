package de.weimarnetz.registrator

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ActiveProfiles("testing")
@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
class TestApplication {
    companion object {
        @Bean
        @ServiceConnection
        fun postgreSQLContainer(): PostgreSQLContainer<out PostgreSQLContainer<*>> {
            return PostgreSQLContainer(DockerImageName.parse("postgres:15.0"))
                .apply { withExposedPorts(5432) }
                .apply { withReuse(true) }
                .also { it.start() }
        }
    }
}

fun main(args: Array<String>) {
    fromApplication<RegistratorApplication>().withAdditionalProfiles("testing").with(
        TestApplication::class.java
    ).run(*args)
}
