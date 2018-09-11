package de.weimarnetz.integrationtests;


import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.operation.preprocess.UriModifyingOperationPreprocessor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import de.weimarnetz.registrator.RegistratorApplication;
import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.model.NodeResponse;
import de.weimarnetz.registrator.repository.RegistratorRepository;
import de.weimarnetz.registrator.services.PasswordService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RegistratorApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
public class RestMvcTest {

    private static final ResponseFieldsSnippet NODE_RESPONSE_SNIPPET = responseFields(
            fieldWithPath("status").description("HTTP Status code").type(Integer.class),
            fieldWithPath("message").description("Response message").type(String.class),
            fieldWithPath("result.key").description("Internal key").type(Long.class),
            fieldWithPath("result.last_seen").description("Unix timestamp of last occurence").type(Long.class),
            fieldWithPath("result.created_at").description("Unix timestamp of creation").type(Long.class),
            fieldWithPath("result.location").description("Path of this resource").type(String.class),
            fieldWithPath("result.mac").description("Mac address of this node").type(String.class),
            fieldWithPath("result.network").description("Network name").type(String.class),
            fieldWithPath("result.number").description("Node numnber").type(Integer.class)
    );
    private static final ResponseFieldsSnippet NODES_RESPONSE_SNIPPET = responseFields(
            fieldWithPath("status").description("HTTP Status code").type(Integer.class),
            fieldWithPath("message").description("Response message").type(String.class),
            fieldWithPath("result[0].key").description("Internal key").type(Long.class),
            fieldWithPath("result[0].last_seen").description("Unix timestamp of last occurence").type(Long.class),
            fieldWithPath("result[0].created_at").description("Unix timestamp of creation").type(Long.class),
            fieldWithPath("result[0].location").description("Path of this resource").type(String.class),
            fieldWithPath("result[0].mac").description("Mac address of this node").type(String.class),
            fieldWithPath("result[0].network").description("Network name").type(String.class),
            fieldWithPath("result[0].number").description("Node numnber").type(Integer.class)
    );
    private static final UriModifyingOperationPreprocessor STANDARD_URI = modifyUris().host("reg.weimarnetz.de").removePort().scheme("http");
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @LocalServerPort
    protected int port;

    @Inject
    private RegistratorRepository registratorRepository;
    @Inject
    private PasswordService passwordService;

    private WebTestClient webTestClient;

    @Before
    public void setUp() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .filter(documentationConfiguration(restDocumentation))
                .build();
        if (registratorRepository.count() < 2) {
            Node node1 = Node.builder().network("ffweimar").createdAt(123L).lastSeen(456L).mac("02caffeebabe").number(2).pass(passwordService.encryptPassword("test")).location("/ffweimar/knoten/2").build();
            Node node2 = Node.builder().network("ffweimar").createdAt(123L).lastSeen(456L).mac("03caffeebabe").number(3).pass(passwordService.encryptPassword("test")).location("/ffweimar/knoten/3").build();
            registratorRepository.save(node1);
            registratorRepository.save(node2);
        }
    }

    @After
    public void tearDown() {
        registratorRepository.deleteAll();
    }


    @Test
    public void testTimeEndpoint() {
        webTestClient.get().uri("/time").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("now")
                .isNumber()
                .consumeWith(document("time",
                        responseFields(
                                fieldWithPath("now").description("current time")
                        )));
    }

    @Test
    public void testQueryKnownNodeNumber() {
        webTestClient.get().uri("/ffweimar/knoten/2").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(NodeResponse.class)
                .consumeWith(document("queryNodes", preprocessRequest(STANDARD_URI), NODE_RESPONSE_SNIPPET));
    }

    @Test
    public void testQueryUnknownNodeNumber() {
        webTestClient.get().uri("/ffweimar/knoten/999").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void testAddNewNodeNumber() {
        webTestClient.post().uri("/ffweimar/knoten?mac=04caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("result.number")
                .isEqualTo(4)
                .consumeWith(document("addNode", preprocessRequest(STANDARD_URI), NODE_RESPONSE_SNIPPET));
    }

    @Test
    public void testAddNewNodeNumberViaGet() {
        webTestClient.get().uri("/POST/ffweimar/knoten?mac=04caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("result.number")
                .isEqualTo(4)
                .consumeWith(document("addNodeViaGet", preprocessRequest(STANDARD_URI), NODE_RESPONSE_SNIPPET));
    }

    @Test
    public void testNoMoreNodenumbers() {
        for (int mac = 66; mac < 75; mac++) {
            webTestClient.post().uri("/testnet/knoten?mac=" + mac + "caffeebabe&pass=test")
                    .accept(MediaType.APPLICATION_JSON_UTF8)
                    .exchange()
                    .expectStatus()
                    .isCreated();
        }
        webTestClient.post().uri("/testnet/knoten?mac=75caffeebabe&pass=test")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    public void testAddAlreadyExistingNodeNumber() {
        webTestClient.post().uri("/ffweimar/knoten?mac=03caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("result.number")
                .isEqualTo(3).returnResult();
    }

    @Test
    public void testUpdateNodeNumber() {
        webTestClient.put().uri("/ffweimar/knoten/2?mac=02caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("updateNode", preprocessRequest(STANDARD_URI), NODE_RESPONSE_SNIPPET))
                .jsonPath("result.number")
                .isEqualTo(2)
                .jsonPath("[?($.result.last_seen > 456)]")
                .hasJsonPath();
    }

    @Test
    public void testUpdateNodeNumberViaGet() {
        webTestClient.get().uri("/PUT/ffweimar/knoten/2?mac=02caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("updateNodeViaGet", preprocessRequest(STANDARD_URI), NODE_RESPONSE_SNIPPET))
                .jsonPath("result.number")
                .isEqualTo(2)
                .jsonPath("[?($.result.last_seen > 456)]")
                .hasJsonPath();
    }

    @Test
    public void testCreateNodeNumberWithPut() {
        webTestClient.put().uri("/ffweimar/knoten/10?mac=05caffeebabe&pass=test").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .consumeWith(document("addGivenNodeNumber", preprocessRequest(STANDARD_URI), NODE_RESPONSE_SNIPPET))
                .jsonPath("result.number")
                .isEqualTo(10);
    }

    @Test
    public void testUpdateNodeNumberInvalidPasswordWithPost() {
        webTestClient.post().uri("/ffweimar/knoten?mac=02caffeebabe&pass=test1").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
                .expectBody(NodeResponse.class);
    }

    @Test
    public void testUpdateNodeNumberWrongPassword() {
        webTestClient.put().uri("/ffweimar/knoten/2?mac=02caffeebabe&pass=test123").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(NodeResponse.class);
    }

    @Test
    public void testListAllNodeNumbers() {
        webTestClient.get().uri("/ffweimar/knoten").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(document("queryAllNodes", preprocessRequest(STANDARD_URI), NODES_RESPONSE_SNIPPET));
    }

    @Test
    public void testUpdateNodeNumberInvalidNetwork() {
        webTestClient.put().uri("/NOT_OUR_NETWORK/knoten/2?mac=02caffeebabe&pass=test123").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void testUpdateNodeNumberInvalidPass() {
        int nodenumber = webTestClient.post().uri("/ffweimar/knoten?mac=05caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(NodeResponse.class)
                .getResponseBody().blockFirst().getNode().getNumber();

        webTestClient.put().uri("/ffweimar/knoten/" + nodenumber + "?mac=05caffeebabe&pass=54322").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testUpdateNodeNumberWrongMac() {
        int nodenumber = webTestClient.post().uri("/ffweimar/knoten?mac=07caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(NodeResponse.class)
                .getResponseBody().blockFirst().getNode().getNumber();

        webTestClient.put().uri("/ffweimar/knoten/" + nodenumber + "?mac=07caffeebabf&pass=54322").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isUnauthorized();

    }

    @Test
    public void testUpdateNodeNumberInvalidMac() {
        webTestClient.put().uri("/ffweimar/knoten/24?mac=caffeebabf&pass=54321").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void testCreateNodeNumberInvalidMac() {
        webTestClient.post().uri("/ffweimar/knoten?mac=caffeebabf&pass=54321").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void testUpdatePassword() {
        webTestClient.put().uri("/ffweimar/updatepassword/2?mac=02caffeebabe&oldPass=test&newPass=test123").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(NodeResponse.class)
                .consumeWith(document("updatePassword", preprocessRequest(STANDARD_URI), NODE_RESPONSE_SNIPPET));
    }

    @Test
    public void testUpdatePasswordWrongOldPassword() {
        webTestClient.put().uri("/ffweimar/updatepassword/2?mac=02caffeebabe&oldPass=test333&newPass=test123").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(NodeResponse.class);
    }

    @Test
    public void testUpdatePasswordInvalidMac() {
        webTestClient.put().uri("/ffweimar/updatepassword/2?mac=caffeebabe&oldPass=test&newPass=test123").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void testUpdatePasswordWrongMac() {
        webTestClient.put().uri("/ffweimar/updatepassword/2?mac=02caffeebabf&oldPass=test&newPass=test123").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testUpdatePasswordInvalidNodeNumber() {
        webTestClient.put().uri("/ffweimar/updatepassword/3?mac=02caffeebabe&oldPass=test&newPass=test123").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testUpdateNodeNumberInvalidNodenumber() {
        int nodeNumber = webTestClient.post().uri("/ffweimar/knoten?mac=06caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(NodeResponse.class).getResponseBody().blockFirst().getNode().getNumber();

        webTestClient.put().uri("/ffweimar/knoten/" + nodeNumber + 23 + "?mac=06caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testDeleteNodeNumberFromDatabase() {
        int nodeNumber = webTestClient.post().uri("/ffweimar/knoten?mac=07caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(NodeResponse.class).getResponseBody().blockFirst().getNode().getNumber();

        webTestClient.mutate().filter(basicAuthentication("deleteuser", "deletepass")).build()
                .delete().uri("/ffweimar/knoten/" + nodeNumber)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .consumeWith(document("deleteNode", preprocessRequest(STANDARD_URI)));
    }

    @Test
    public void testDeleteNodeNumberFromDatabaseWithInvalidCredentials() {
        int nodeNumber = webTestClient.post().uri("/ffweimar/knoten?mac=07caffeebabe&pass=54321").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(NodeResponse.class).getResponseBody().blockFirst().getNode().getNumber();

        webTestClient.mutate().filter(basicAuthentication("trolluser", "trollpass")).build()
                .delete().uri("/ffweimar/knoten/" + nodeNumber)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void testDeleteUnknownNodeNumberFromDatabase() {
        webTestClient.mutate().filter(basicAuthentication("deleteuser", "deletepass")).build()
                .delete().uri("/ffweimar/knoten/512")
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
