package de.weimarnetz.registrator.integrationtests

import de.weimarnetz.registrator.model.Node
import de.weimarnetz.registrator.model.NodeResponse
import de.weimarnetz.registrator.repository.RegistratorRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import java.time.Duration

@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
class RestMvcIT(
    @Autowired
    private val registratorRepository: RegistratorRepository
) {
    @LocalServerPort
    protected lateinit var port: String

    protected lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider?) {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port")
            .filter(WebTestClientRestDocumentation.documentationConfiguration(restDocumentation))
            .responseTimeout(Duration.ofSeconds(10))
            .build()
        if (registratorRepository.count() < 2) {
            val node1 = Node(
                network = "ffweimar",
                createdAt = 123L,
                lastSeen = 456L,
                mac = "02caffeebabe",
                number = 2,
                location = "/ffweimar/knoten/2"
            )
            val node2 = Node(
                network = "ffweimar",
                createdAt = 123L,
                lastSeen = 456L,
                mac = "03caffeebabe",
                number = 3,
                location = "/ffweimar/knoten/3"
            )
            registratorRepository.saveAll(listOf(node1, node2))
        }
    }

    @AfterEach
    fun tearDown() {
        registratorRepository.deleteAll()
    }

    @Test
    fun testTimeEndpoint() {
        webTestClient.get().uri("/time").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .jsonPath("now")
            .isNumber
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "time",
                    PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("now").description("current time")
                    )
                )
            )
    }

    @Test
    fun testQueryKnownNodeNumber() {
        webTestClient.get().uri("/ffweimar/knoten/2").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(NodeResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "queryNodes",
                    Preprocessors.preprocessRequest(STANDARD_URI),
                    NODE_RESPONSE_SNIPPET
                )
            )
    }

    @Test
    fun testQueryNodenumberByMac() {
        webTestClient.get().uri("/ffweimar/knotenByMac?mac=02:ca:ff:ee:ba:be").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(NodeResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "queryNodeByMac", Preprocessors.preprocessRequest(
                        STANDARD_URI
                    ), NODE_RESPONSE_SNIPPET
                )
            )
    }

    @Test
    fun testQueryUnknownNodeNumber() {
        webTestClient.get().uri("/ffweimar/knoten/999").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun testAddNewNodeNumber() {
        webTestClient.post().uri("/ffweimar/knoten?mac=04caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("result.number")
            .isEqualTo(4)
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "addNode",
                    Preprocessors.preprocessRequest(STANDARD_URI),
                    NODE_RESPONSE_SNIPPET
                )
            )
    }

    @Test
    fun testAddNewNodeNumberViaGet() {
        webTestClient.get().uri("/POST/ffweimar/knoten?mac=04caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .jsonPath("result.number")
            .isEqualTo(4)
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "addNodeViaGet", Preprocessors.preprocessRequest(
                        STANDARD_URI
                    ), NODE_RESPONSE_SNIPPET
                )
            )
    }

    @Test
    fun testNoMoreNodenumbers() {
        for (mac in 66..74) {
            webTestClient.post().uri("/testnet/knoten?mac=" + mac + "caffeebabe&pass=test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated
        }
        webTestClient.post().uri("/testnet/knoten?mac=75caffeebabe&pass=test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    @Test
    fun testAddAlreadyExistingNodeNumber() {
        webTestClient.post().uri("/ffweimar/knoten?mac=03caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.SEE_OTHER)
            .expectBody()
            .jsonPath("result.number")
            .isEqualTo(3).returnResult()
    }

    @Test
    fun testUpdateNodeNumber() {
        webTestClient.put().uri("/ffweimar/knoten/2?mac=02caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "updateNode",
                    Preprocessors.preprocessRequest(STANDARD_URI),
                    NODE_RESPONSE_SNIPPET
                )
            )
            .jsonPath("result.number")
            .isEqualTo(2)
            .jsonPath("[?($.result.last_seen > 456)]")
            .hasJsonPath()
    }

    @Test
    fun testUpdateNodeNumberViaGet() {
        webTestClient.get().uri("/PUT/ffweimar/knoten/2?mac=02caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "updateNodeViaGet", Preprocessors.preprocessRequest(
                        STANDARD_URI
                    ), NODE_RESPONSE_SNIPPET
                )
            )
            .jsonPath("result.number")
            .isEqualTo(2)
            .jsonPath("[?($.result.last_seen > 456)]")
            .hasJsonPath()
    }

    @Test
    fun testCreateNodeNumberWithPut() {
        webTestClient.put().uri("/ffweimar/knoten/10?mac=05caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody()
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "addGivenNodeNumber", Preprocessors.preprocessRequest(
                        STANDARD_URI
                    ), NODE_RESPONSE_SNIPPET
                )
            )
            .jsonPath("result.number")
            .isEqualTo(10)
    }

    @Test
    fun testUpdateNodeNumberWithPost() {
        webTestClient.post().uri("/ffweimar/knoten?mac=02caffeebabe&pass=test1").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.SEE_OTHER)
            .expectBody(NodeResponse::class.java)
    }

    @Test
    fun testUpdateNodeNumberWrongPassword() {
        webTestClient.put().uri("/ffweimar/knoten/2?mac=02caffeebabe&pass=test123").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(NodeResponse::class.java)
    }

    @Test
    fun testListAllNodeNumbers() {
        webTestClient.get().uri("/ffweimar/knoten").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "queryAllNodes", Preprocessors.preprocessRequest(
                        STANDARD_URI
                    ), NODES_RESPONSE_SNIPPET
                )
            )
    }

    @Test
    fun testUpdateNodeNumberInvalidNetwork() {
        webTestClient.put().uri("/NOT_OUR_NETWORK/knoten/2?mac=02caffeebabe&pass=test123")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun testUpdateNodeNumberInvalidPass() {
        val nodenumber =
            webTestClient.post().uri("/ffweimar/knoten?mac=05caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated
                .returnResult(NodeResponse::class.java)
                .responseBody.blockFirst()?.node?.number

        webTestClient.put().uri("/ffweimar/knoten/$nodenumber?mac=05caffeebabe&pass=54322")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun testUpdateNodeNumberWrongMac() {
        val nodenumber =
            webTestClient.post().uri("/ffweimar/knoten?mac=07caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated
                .returnResult(NodeResponse::class.java)
                .responseBody.blockFirst()?.node?.number
        webTestClient.put().uri("/ffweimar/knoten/$nodenumber?mac=07caffeebabf&pass=54322")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun testUpdateNodeNumberInvalidMac() {
        webTestClient.put().uri("/ffweimar/knoten/24?mac=caffeebabf&pass=54321").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun testCreateNodeNumberInvalidMac() {
        webTestClient.post().uri("/ffweimar/knoten?mac=caffeebabf&pass=54321").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun testUpdateNodeNumberInvalidNodenumber() {
        val nodeNumber =
            webTestClient.post().uri("/ffweimar/knoten?mac=06caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated
                .returnResult(NodeResponse::class.java).responseBody.blockFirst()?.node?.number
        webTestClient.put().uri("/ffweimar/knoten/" + nodeNumber + 23 + "?mac=06caffeebabe&pass=54321")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun testDeleteNodeNumberFromDatabase() {
        val nodeNumber =
            webTestClient.post().uri("/ffweimar/knoten?mac=07caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated
                .returnResult(NodeResponse::class.java).responseBody.blockFirst()?.node?.number
        webTestClient.mutate().filter(ExchangeFilterFunctions.basicAuthentication("adminuser", "adminpass")).build()
            .delete().uri("/ffweimar/knoten/$nodeNumber")
            .exchange()
            .expectStatus()
            .isNoContent
            .expectBody()
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "deleteNode",
                    Preprocessors.preprocessRequest(STANDARD_URI)
                )
            )
    }

    @Test
    fun testDeleteNodeNumberFromDatabaseWithInvalidCredentials() {
        val nodeNumber =
            webTestClient.post().uri("/ffweimar/knoten?mac=07caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated
                .returnResult(NodeResponse::class.java).responseBody.blockFirst()?.node?.number
        webTestClient.mutate().filter(ExchangeFilterFunctions.basicAuthentication("trolluser", "trollpass")).build()
            .delete().uri("/ffweimar/knoten/$nodeNumber")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun testDeleteUnknownNodeNumberFromDatabase() {
        webTestClient.mutate().filter(ExchangeFilterFunctions.basicAuthentication("adminuser", "adminpass")).build()
            .delete().uri("/ffweimar/knoten/512")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun testDumpDatabase() {
        val nodeNumber =
            webTestClient.post().uri("/ffweimar/knoten?mac=08caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated
                .returnResult(NodeResponse::class.java).responseBody.blockFirst()?.node?.number
        webTestClient.mutate().filter(ExchangeFilterFunctions.basicAuthentication("adminuser", "adminpass")).build()
            .get().uri("/dumpDatabase")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "dumpDatabase", Preprocessors.preprocessRequest(
                        STANDARD_URI
                    ), NODES_RESPONSE_SNIPPET
                )
            )
            .jsonPath("$.result[?(@.number == $nodeNumber)]").exists()
    }

    @Test
    fun testDumpDatabaseFromDatabaseWithInvalidCredentials() {
        webTestClient.mutate().filter(ExchangeFilterFunctions.basicAuthentication("trolluser", "trollpass")).build()
            .get().uri("/dumpDatabase")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    companion object {
        private val NODE_RESPONSE_SNIPPET = PayloadDocumentation.responseFields(
            PayloadDocumentation.fieldWithPath("status").description("HTTP Status code").type(Int::class.java),
            PayloadDocumentation.fieldWithPath("message").description("Response message").type(String::class.java),
            PayloadDocumentation.fieldWithPath("result.key").description("Internal key").type(Long::class.java),
            PayloadDocumentation.fieldWithPath("result.last_seen").description("Unix timestamp of last occurence")
                .type(Long::class.java),
            PayloadDocumentation.fieldWithPath("result.created_at").description("Unix timestamp of creation")
                .type(Long::class.java),
            PayloadDocumentation.fieldWithPath("result.location").description("Path of this resource")
                .type(String::class.java),
            PayloadDocumentation.fieldWithPath("result.mac").description("Mac address of this node")
                .type(String::class.java),
            PayloadDocumentation.fieldWithPath("result.network").description("Network name").type(String::class.java),
            PayloadDocumentation.fieldWithPath("result.number").description("Node numnber").type(Int::class.java)
        )
        private val NODES_RESPONSE_SNIPPET = PayloadDocumentation.responseFields(
            PayloadDocumentation.fieldWithPath("status").description("HTTP Status code").type(Int::class.java),
            PayloadDocumentation.fieldWithPath("message").description("Response message").type(String::class.java),
            PayloadDocumentation.fieldWithPath("result[0].key").description("Internal key").type(Long::class.java),
            PayloadDocumentation.fieldWithPath("result[0].last_seen").description("Unix timestamp of last occurence")
                .type(
                    Long::class.java
                ),
            PayloadDocumentation.fieldWithPath("result[0].created_at").description("Unix timestamp of creation")
                .type(Long::class.java),
            PayloadDocumentation.fieldWithPath("result[0].location").description("Path of this resource")
                .type(String::class.java),
            PayloadDocumentation.fieldWithPath("result[0].mac").description("Mac address of this node")
                .type(String::class.java),
            PayloadDocumentation.fieldWithPath("result[0].network").description("Network name")
                .type(String::class.java),
            PayloadDocumentation.fieldWithPath("result[0].number").description("Node numnber").type(Int::class.java)
        )
        private val STANDARD_URI = Preprocessors.modifyUris().host("reg.weimarnetz.de").removePort().scheme("http")
    }
}