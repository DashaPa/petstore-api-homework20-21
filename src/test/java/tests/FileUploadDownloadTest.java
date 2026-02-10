package tests;

import config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
public class FileUploadDownloadTest {@BeforeEach
public void setup() {
    io.restassured.RestAssured.baseURI = TestConfig.BASE_URL;
}

    @Test
    public void testUploadFileToPet() {

        File testFile = new File("test-upload.txt");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write("Test content for upload".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            given()
                    .multiPart("file", testFile, "text/plain")
                    .formParam("additionalMetadata", "Test metadata")
                    .pathParam("petId", 12345)
                    .when()
                    .post("/pet/{petId}/uploadImage")
                    .then()
                    .statusCode(200)
                    .body("code", equalTo(200))
                    .body("type", equalTo("unknown"))
                    .body("message", containsString("test-upload.txt"));
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testUploadImageWithApiKey() {
        File imageFile = new File("test-image.jpg");
        // фейковый JPEG
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(new byte[]{(byte)0xFF, (byte)0xD8}); // JPEG заголовок
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            given()
                    .header("api_key", TestConfig.API_TOKEN)
                    .multiPart("file", imageFile, "image/jpeg")
                    .formParam("additionalMetadata", "Pet photo")
                    .pathParam("petId", 999)
                    .when()
                    .post("/pet/{petId}/uploadImage")
                    .then()
                    .statusCode(200)
                    .body("message", containsString(".jpg"));
        } finally {
            imageFile.delete();
        }
    }
}

