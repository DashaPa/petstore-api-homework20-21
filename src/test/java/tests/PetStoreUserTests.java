package tests;

import config.ApiConfig;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import models.User;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetStoreUserTests {

    private String testUsername;
    private User testUser;
    //private static final String BASE_URL = "https://petstore.swagger.io/v2";
    private static final String BASE_URL = ApiConfig.BASE_URL;

    @BeforeAll
    void setup() {
        testUsername = "test_user_" + System.currentTimeMillis();

        testUser = new User(
                System.currentTimeMillis(),
                testUsername,
                "Ivan",
                "Petrov",
                testUsername + "@university.test",
                "StudentPass123!",
                "+79161234567",
                1
        );

                System.out.println("Тест настроен для: " + testUsername);
    }

    @Test
    @Order(1)
    @DisplayName("POST /user - создание пользователя")
    void createUserBasicTest() {
        given()
                .baseUri(BASE_URL)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(testUser)
                .when()
                .post("/user")
                .then()
                .log().body()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("type", equalTo("unknown"))
                .body("message", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("GET /user/{username} - получение пользователя")
    void getUserBasicTest() {
        given()
                .baseUri(BASE_URL)
                .pathParam("username", testUsername)
                .when()
                .get("/user/{username}")
                .then()
                .log().body()
                .statusCode(200)
                .body("username", equalTo(testUsername))
                .body("firstName", equalTo("Ivan"))
                .body("lastName", equalTo("Petrov"))
                .body("email", equalTo(testUsername + "@university.test"));
    }

    @Test
    @Order(3)
    @DisplayName("PUT /user/{username} - обновление пользователя")
    void updateUserBasicTest() {
        User updatedUser = new User(
                testUser.getId(),
                testUsername,
                "Lida",
                "Abramova",
                "updated_" + testUsername + "@university.test",
                "Newpassword456",
                "+7876543210",
                0
        );

        given()
                .baseUri(BASE_URL)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(updatedUser)
                .when()
                .put("/user/{username}", testUsername)
                .then()
                .log().body()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("message",notNullValue());

        given()
                .baseUri(BASE_URL)
                .pathParam("username", testUsername)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Lida"))
                .body("email", equalTo("updated_" + testUsername + "@university.test"));
    }

    @Test
    @Order(4)
    @DisplayName("DELETE /user/{username} - удаление пользователя")
    void deleteUserBasicTest() {
        given()
                .baseUri(BASE_URL)
                .pathParam("username", testUsername)
                .when()
                .delete("/user/{username}")
                .then()
                .log().body()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("message", equalTo(testUsername));

        given()
                .baseUri(BASE_URL)
                .pathParam("username", testUsername)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /user - создание с проверкой через jsonPath")
    void createUserWithJsonPathValidation() {
        String uniqueUser = "jsonpath_user_" + System.currentTimeMillis();
        User jsonPathUser = new User(
                999888777L,
                uniqueUser,
                "Anna",
                "Sidorova",
                uniqueUser + "@student.test",
                "Testpass123!",
                "+79165551122",
                1
        );

        Response response = given()
                .baseUri(BASE_URL)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(jsonPathUser)
                .when()
                .post("/user")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode(), "Неверный статус код");
        assertEquals(200, response.jsonPath().getInt("code"), "Неверный код в ответе");
        assertEquals("unknown", response.jsonPath().getString("type"), "Неверный тип");
        assertNotNull(response.jsonPath().getString("message"), "Сообщение не должно быть null");

        given()
                .baseUri(BASE_URL)
                .pathParam("username", uniqueUser)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(200)
                .body("username", equalTo(uniqueUser))
                .body("email", equalTo(uniqueUser + "@student.test"));
    }

    @Test
    @DisplayName("GET /user/{username} - Валидация JSON схемы")
    void getUserWithJsonSchemaValidation() {
        String schemaUser = "schema_user_" + System.currentTimeMillis();
        User userForSchema = new User(
                111222333L,
                schemaUser,
                "Olga",
                "Shulgina",
                schemaUser + "@student.test",
                "Pass123",
                "+7234567890",
                1
        );
        given()
                .baseUri(BASE_URL)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(userForSchema)
                .when()
                .post("/user")
                .then()
                .statusCode(200);

        given()
                .baseUri(BASE_URL)
                .pathParam("username", schemaUser)
                .when()
                .get("/user/{username}")
                .then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/user-schema.json"))
                .statusCode(200);
    }

    @Test
    @DisplayName("Тест: все CRUD операции")
    void complexUserCrudTest() {
        String complexUser = "complex_" + System.currentTimeMillis();

        User createUser = new User(
                1001L,
                complexUser,
                "Dima",
                "Volkov",
                complexUser + "@create.test",
                "Create123",
                "111-222-3333",
                1
        );
        given()
                .baseUri(BASE_URL)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(createUser)
                .when()
                .post("/user")
                .then()
                .statusCode(200);

        given()
                .baseUri(BASE_URL)
                .pathParam("username", complexUser)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Dima"))
                .body("email", matchesPattern("^complex_.*@create\\.test$"));

        User updateUser = new User(
                1001L,
                complexUser,
                "Dima",
                "Volod",
                complexUser + "@update.test",
                "Update456",
                "444-555-6666",
                0
        );
        given()
                .baseUri(BASE_URL)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .body(updateUser)
                .when()
                .put("/user/{username}", complexUser)
                .then()
                .statusCode(200);

        given()
                .baseUri(BASE_URL)
                .pathParam("username", complexUser)
                .when()
                .delete("/user/{username}")
                .then()
                .statusCode(200);
        given()
                .baseUri(BASE_URL)
                .pathParam("username", complexUser)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Получение несуществующего пользователя")
    void getNonExistentUserTest() {
        String nonExistentUsername = "non_existent_user_" + System.currentTimeMillis();
        given()
                .baseUri(BASE_URL)
                .pathParam("username", nonExistentUsername)
                .when()
                .get("/user/{username}")
                .then()
                .log().body()
                .statusCode(404)
                .body("code", equalTo(1))
                .body("type", equalTo("error"))
                .body("message", equalTo("User not found"));
    }

    @AfterAll
    void cleanup() {
        System.out.println("Тестирование завершено для : " + testUsername);
    }
}
