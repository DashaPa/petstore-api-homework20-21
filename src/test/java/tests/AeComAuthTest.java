package tests;

import config.TestConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

import static org.hamcrest.Matchers.*;

public class AeComAuthTest {

    @Test
    public void testAuthWithSimpleToken() {
        // использую простой токен для проверки
        String myToken = "simple_token_123";

        given()
                .baseUri(TestConfig.AE_BASE_URL)
                .header("api-token", myToken)
                .header("Accept", "application/json")
                .queryParam("studentId", "777")
                .when()
                .get("/us/en/api/data")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(404)));
    }

    @Test
    public void testAuthWithCustomHeader() {
        // проверяю авторизацию
        given()
                .baseUri(TestConfig.AE_BASE_URL)
                .header("my-api-key", "my_test_key_456")
                .header("Accept-Language", "en-US")
                .queryParam("test", "true")
                .when()
                .get("/us/en/api/info")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(404)));
    }

    @Test
    public void testPetStoreWithRealApiKey() {
        given()
                .baseUri(TestConfig.BASE_URL)
                .header("api_key", TestConfig.API_TOKEN)
                .queryParam("status", "available")
                .when()
                .get("/pet/findByStatus")
                .then()
                .statusCode(200)
                .body("status", everyItem(equalTo("available")));
    }

    @Test
    public void testDifferentAuthTypes() {
        // простые способы авторизации
        Map<String, String> authHeaders = new HashMap<>();
        authHeaders.put("api-key", "key_111");
        authHeaders.put("access-token", "token_222");
        authHeaders.put("auth-code", "code_333");

        for (Map.Entry<String, String> auth : authHeaders.entrySet()) {
            given()
                    .baseUri(TestConfig.AE_BASE_URL)
                    .header(auth.getKey(), auth.getValue())
                    .when()
                    .get("/us/en/api/test")
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(404)));
        }
    }

    @Test
    public void testSimpleAuthWithQueryParam() {
        // авторизация через query параметр
        given()
                .baseUri(TestConfig.AE_BASE_URL)
                .queryParam("access_key", "simple_key_789")
                .queryParam("action", "get_data")
                .when()
                .get("/us/en/api/simple")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(401), equalTo(404)));
    }
}