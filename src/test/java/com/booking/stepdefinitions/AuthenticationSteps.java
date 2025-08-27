package com.booking.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import org.assertj.core.api.Assertions;
import com.booking.utils.ConfigReader;

import java.util.Map;

public class AuthenticationSteps {
    private String endpoint;
    private Response response;

    @Given("the user has access to the auth endpoint {string}")
    public void setHotelBookingEndpoint(String endpointPath) {
        this.endpoint = ConfigReader.get("api.baseUrl") + endpointPath;
    }

    @When("the user attempts to log in with the following credentials")
    public void userAttemptsLogin(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> credentials = dataTable.asMaps(String.class, String.class).get(0);
        Map<String, String> requestBody = Map.of(
                "username", credentials.get("username"),
                "password", credentials.get("password")
        );
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post(endpoint);
    }

    @Then("the authentication response status code should be {int}")
    public void validateStatusCode(Integer int1) {
        Assertions.assertThat(response.getStatusCode()).isEqualTo(int1);
    }

    @Then("if authentication is successful then the response should contain a {string}")
    public void validateResponseContainsToken(String key) {
        Assertions.assertThat(response.getStatusCode())
                .as("Verify status code is 200")
                .isEqualTo(200);
        String token = response.jsonPath().getString(key);
        Assertions.assertThat(token)
                .as("Check that response contains " + key)
                .isNotNull()
                .isNotEmpty();
    }

    @Then("the response status code should be {int} and the error messages should have {string}")
    public void validateLoginErrorResponse(int expectedStatusCode, String expectedErrorMessage) {
        Assertions.assertThat(response.getStatusCode())
                .as("Check response status code")
                .isEqualTo(expectedStatusCode);
        String actualError = response.jsonPath().getString("error");
        Assertions.assertThat(actualError)
                .as("Check if error message contains expected text")
                .isNotNull()
                .contains(expectedErrorMessage);
    }

}
