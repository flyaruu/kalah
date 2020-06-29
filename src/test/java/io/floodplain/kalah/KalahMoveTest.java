package io.floodplain.kalah;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.CoreMatchers.anything;

@QuarkusTest
public class KalahMoveTest {

    @BeforeEach
    public void setup() {
        given()
                .when()
                .header("Content-Type", ContentType.JSON)
                .header("Accept",ContentType.JSON)
                .post("/games")
                .then()
                .statusCode(201)
                .body("id", greaterThanOrEqualTo("1"));
    }
    @Test
    public void testCreateGame() {
        given()
          .when()
          .header("Content-Type", ContentType.JSON)
          .header("Accept",ContentType.JSON)
          .post("/games")
          .then()
          .statusCode(201)
             .body("id", greaterThanOrEqualTo("1"));
    }

    @Test
    public void testMakeMove() {
        given()
                .when()
                .header("Content-Type", ContentType.JSON)
                .header("Accept",ContentType.JSON)
                .post("/games")
                .then()
                .statusCode(201)
                .body("id", is(anything()));
        given()
                .when()
                .header("Content-Type", ContentType.JSON)
                .header("Accept",ContentType.JSON)
                .put("/games/1/pits/2")
                .then()
                .statusCode(200)
                .body("id", is(anything()));
    }

}