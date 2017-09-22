package de.weimarnetz.integrationtests;


import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.test.context.junit4.SpringRunner;

import de.weimarnetz.registrator.RegistratorApplication;
import de.weimarnetz.registrator.model.Node;
import de.weimarnetz.registrator.repository.RegistratorRepository;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RegistratorApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    private static final String NODE_RESPONSE_SCHEMA_JSON = "node-response-schema.json";
    private static final String NODES_RESPONSE_SCHEMA_JSON = "nodes-response-schema.json";
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Inject
    private RegistratorRepository registratorRepository;

    private RequestSpecification spec;

    @LocalServerPort
    private int port;

    @Before
    public void setUp() {
        this.spec = new RequestSpecBuilder().addFilter(
                documentationConfiguration(this.restDocumentation))
                .build();
        if (registratorRepository.count() < 2) {
            Node node1 = Node.builder().network("ffweimar").createdAt(123L).lastSeen(456L).mac("12345").number(2).pass("test").location("here").build();
            Node node2 = Node.builder().network("ffweimar").createdAt(123L).lastSeen(456L).mac("23456").number(3).pass("test").location("here").build();
            registratorRepository.save(node1);
            registratorRepository.save(node2);
        }
    }

    @Test
    public void testTimeEndpoint() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .filter(document("time", responseFields(
                        fieldWithPath("now").description("current time").type(Long.class)
                )))
                .when().get("/time")
                .then().assertThat().statusCode(is(200)).and().body("now", any(Long.class));
    }

    @Test
    public void testQueryKnownNodeNumber() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .filter(document("queryNodes", NODE_RESPONSE_SNIPPET))
                .when().get("/ffweimar/knoten/2")
                .then().assertThat().statusCode(is(200)).and().body(matchesJsonSchemaInClasspath(NODE_RESPONSE_SCHEMA_JSON));
    }

    @Test
    public void testQueryUnknownNodeNumber() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .when().get("/ffweimar/knoten/999")
                .then().assertThat().statusCode(is(404));
    }

    @Test
    public void testAddNewNodeNumber() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .when().post("/ffweimar/knoten?mac=34556&pass=test")
                .then().assertThat().statusCode(is(201))
                .and().body(matchesJsonSchemaInClasspath(NODE_RESPONSE_SCHEMA_JSON))
                .and().body("result.number", is(4));
    }

    @Test
    public void testAddAlreadyExistingNodeNumber() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .when().post("/ffweimar/knoten?mac=23456&pass=test")
                .then().assertThat().statusCode(is(200))
                .and().body(matchesJsonSchemaInClasspath(NODE_RESPONSE_SCHEMA_JSON))
                .and().body("result.number", is(3));
    }

    @Test
    public void testUpdateNodeNumber() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .when().put("/ffweimar/knoten/2?mac=12345&pass=test")
                .then().assertThat().statusCode(is(200))
                .and().body(matchesJsonSchemaInClasspath(NODE_RESPONSE_SCHEMA_JSON))
                .and().body("result.last_seen", greaterThan(456L));
    }

    @Test
    public void testUpdateNodeNumberWrongPassword() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .when().put("/ffweimar/knoten/2?mac=12345&pass=test123")
                .then().assertThat().statusCode(is(401))
                .and().body(matchesJsonSchemaInClasspath(NODE_RESPONSE_SCHEMA_JSON));
    }

    @Test
    public void testListAllNodeNumbers() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .filter(document("queryAllNodes", NODES_RESPONSE_SNIPPET))
                .when().get("/ffweimar/knoten")
                .then().assertThat().statusCode(is(200))
                .and().body(matchesJsonSchemaInClasspath(NODES_RESPONSE_SCHEMA_JSON));
    }

    @Test
    public void testUpdateNodeNumberInvalidNetwork() {
        RestAssured.given(this.spec).port(port)
                .accept("application/json")
                .when().put("/NOT_OUR_NETWORK/knoten/2?mac=12345&pass=test123")
                .then().assertThat().statusCode(is(404));

    }
}
