package de.weimarnetz.registrator

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.event.annotation.BeforeTestClass
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import java.time.Duration


@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
@DirtiesContext
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
abstract class IntegrationTestConfig {

    @LocalServerPort
    protected var port: Int = 0

    protected lateinit var webTestClient: WebTestClient

    @BeforeEach
    open fun setUp(restDocumentation: RestDocumentationContextProvider) {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port")
            .filter(WebTestClientRestDocumentation.documentationConfiguration(restDocumentation))
            .responseTimeout(Duration.ofSeconds(10))
            .build()
    }

    companion object {

        @Container
        private var postgreSQLContainer = PostgreSQLContainer("postgres:15.0")

        @JvmStatic
        @BeforeTestClass
        @DynamicPropertySource
        fun setContainer(registry: DynamicPropertyRegistry) {
            postgreSQLContainer.start()
            registry.add("spring.datasource.url") { "jdbc:postgresql://localhost:${postgreSQLContainer.firstMappedPort}/${postgreSQLContainer.databaseName}" }
            registry.add("spring.datasource.username") { postgreSQLContainer.username }
            registry.add("spring.datasource.password") { postgreSQLContainer.password }
        }

        @JvmStatic
        @AfterAll
        fun closeContainer() {
            postgreSQLContainer.stop()
        }
    }

}