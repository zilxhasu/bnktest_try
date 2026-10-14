package com.example.bnk.gateway;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class GatewayTest {

    private String login() {
        return given().contentType("application/json")
                .body("{\"userId\":\"alice\",\"password\":\"demo1234\"}")
                .post("/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    @Test
    void loginSuccessReturnsToken() {
        given().contentType("application/json")
                .body("{\"userId\":\"alice\",\"password\":\"demo1234\"}")
                .post("/auth/login")
                .then().statusCode(200)
                .body("returnCode", equalTo("0000"))
                .body("token", notNullValue());
    }

    @Test
    void wrongPasswordRejected() {
        given().contentType("application/json")
                .body("{\"userId\":\"alice\",\"password\":\"nope\"}")
                .post("/auth/login")
                .then().statusCode(401).body("returnCode", equalTo("E002"));
    }

    @Test
    void apiWithoutTokenRejected() {
        given().contentType("application/json").body("{}")
                .post("/api/account/accounts/list")
                .then().statusCode(401);
    }

    @Test
    void tamperedTokenRejected() {
        String token = login();
        given().contentType("application/json").body("{}")
                .header("Authorization", "Bearer " + token.substring(0, token.length() - 4) + "abcd")
                .post("/api/account/accounts/list")
                .then().statusCode(401);
    }

    @Test
    void unknownServiceIs404() {
        given().contentType("application/json").body("{}")
                .header("Authorization", "Bearer " + login())
                .post("/api/nope/x")
                .then().statusCode(404);
    }

    @Test
    void healthIsUp() {
        given().get("/q/health/ready").then().statusCode(200);
    }
}
