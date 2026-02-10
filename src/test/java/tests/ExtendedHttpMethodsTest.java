package tests;

import config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ExtendedHttpMethodsTest {

    @BeforeEach
    public void setupForTests() {
        io.restassured.RestAssured.baseURI = TestConfig.BASE_URL;
    }

    @Test
    public void createCatWithDetails() {
        String myCatJson = """
            {
                "id": 55501,
                "category": {"id": 2, "name": "Cats"},
                "name": "Tom",
                "photoUrls": ["tom_cat_1.jpg", "tom_cat_2.jpg"],
                "tags": [{"id": 5, "name": "playful"}],
                "status": "available"
            }
            """;

        given()
                .contentType("application/json")
                .body(myCatJson)
                .when()
                .post("/pet")
                .then()
                .statusCode(200)
                .body("id", equalTo(55501))
                .body("name", equalTo("Tom"))
                .body("category.name", equalTo("Cats"))
                .body("tags[0].name", equalTo("playful"));
    }

    @Test
    public void createAndUpdateParrot() {

        Map<String, Object> myParrot = new HashMap<>();
        myParrot.put("id", 77701);
        myParrot.put("name", "Kesha");
        myParrot.put("status", "available");

        given().contentType("application/json").body(myParrot).post("/pet");

        // меняю данные попугая
        myParrot.put("name", "Kesha Updated");
        myParrot.put("status", "sold");
        myParrot.put("photoUrls", new String[]{"parrot_new.jpg"});

        given()
                .contentType("application/json")
                .body(myParrot)
                .when()
                .put("/pet")
                .then()
                .statusCode(200)
                .body("name", equalTo("Kesha Updated"))
                .body("status", equalTo("sold"));
    }

    @Test
    public void createAndPatchRabbit() {
        given()
                .contentType("application/json")
                .body("{\"id\": 88801, \"name\": \"Boris\", \"status\": \"available\"}")
                .post("/pet");

        // меняю кролика через форму
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("name", "Boris Patched")
                .formParam("status", "pending")
                .pathParam("petId", 88801)
                .when()
                .post("/pet/{petId}")
                .then()
                .statusCode(200)
                .body("code", equalTo(200));
    }

    @Test
    public void createFishWithPut() {
        // создаю рыбку
        String fishJson = """
            {
                "id": 99901,
                "name": "Goldie",
                "status": "available",
                "tags": [{"id": 3, "name": "aquatic"}]
            }
            """;

        given()
                .contentType("application/json")
                .body(fishJson)
                .post("/pet");

        // обновляю рыбку
        given()
                .contentType("application/json")
                .body("{\"id\": 99901, \"name\": \"Goldie Updated\", \"status\": \"pending\"}")
                .when()
                .put("/pet")
                .then()
                .statusCode(200)
                .body("name", equalTo("Goldie Updated"));
    }

    @Test
    public void createDogAndTest() {
        // дополнительный тест с собачкой
        Map<String, Object> dog = new HashMap<>();
        dog.put("id", 66601);
        dog.put("name", "Teddy");
        dog.put("status", "available");
        dog.put("photoUrls", new String[]{"teddy_photo.jpg"});

        given()
                .contentType("application/json")
                .body(dog)
                .when()
                .post("/pet")
                .then()
                .statusCode(200)
                .body("name", equalTo("Teddy"))
                .body("photoUrls[0]", containsString("teddy_photo"));
    }

    @ParameterizedTest
    @CsvSource({
            "available, pending, Tom",
            "pending, sold, Mimi",
            "sold, available, Dima"
    })
    public void changePetStatusTest(String oldStatus, String newStatus, String petName) {
        long petId = System.currentTimeMillis() % 10000;

        // создаю питомца
        given()
                .contentType("application/json")
                .body(String.format("{\"id\": %d, \"name\": \"%s\", \"status\": \"%s\"}", petId, petName, oldStatus))
                .post("/pet");

        // меняю статус
        given()
                .contentType("application/json")
                .body(String.format("{\"id\": %d, \"name\": \"%s\", \"status\": \"%s\"}", petId, petName, newStatus))
                .when()
                .put("/pet")
                .then()
                .statusCode(200)
                .body("status", equalTo(newStatus))
                .body("name", equalTo(petName));
    }

    @Test
    public void createMultiplePetsTest() {
        // тест с несколькими питомцами
        String[] petNames = {"Tom", "Kesha", "Boris", "Goldie", "Teddy", "Mimi", "Dima"};
        String[] petTypes = {"cat", "parrot", "rabbit", "fish", "dog", "cat", "rabbit"};

        for (int i = 0; i < petNames.length; i++) {
            int petId = 1000 + i;
            given()
                    .contentType("application/json")
                    .body(String.format("{\"id\": %d, \"name\": \"%s\", \"tags\": [{\"name\": \"%s\"}]}",
                            petId, petNames[i], petTypes[i]))
                    .post("/pet")
                    .then()
                    .statusCode(200);
        }

        // проверяю что создались
        for (int i = 0; i < petNames.length; i++) {
            int petId = 1000 + i;
            given()
                    .pathParam("petId", petId)
                    .when()
                    .get("/pet/{petId}")
                    .then()
                    .statusCode(200)
                    .body("name", equalTo(petNames[i]));
        }
    }
}

