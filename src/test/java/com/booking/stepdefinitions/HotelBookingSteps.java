package com.booking.stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import org.assertj.core.api.Assertions;
import com.booking.utils.ConfigReader;

import java.util.*;

public class HotelBookingSteps {
    private String endpoint;
    private Response response;
    private Map<String, Object> lastRequestPayload;

    @Given("the user has access to the hotel reservation endpoint {string}")
    public void setHotelBookingEndpoint(String endpointPath) {
        this.endpoint = ConfigReader.get("api.baseUrl") + endpointPath;
    }

    @When("the user submits a reservation request with the following details:")
    @When("the user submits a reservation request missing for the optional field")
    public void submitReservation(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> reservation = dataTable.asMaps(String.class, String.class).get(0);

        Map<String, String> bookingDates = new HashMap<>();
        bookingDates.put("checkin", reservation.get("checkin"));
        bookingDates.put("checkout", reservation.get("checkout"));

        Map<String, Object> requestBody = new HashMap<>();
        String roomIdValue = safeValue(reservation.get("roomid"));
        if (!roomIdValue.isEmpty()) {
            requestBody.put("roomid", Integer.parseInt(roomIdValue));
        } else {
            requestBody.put("roomid", "");
        }
//    requestBody.put("roomid", Integer.parseInt(reservation.get("roomid")));
        requestBody.put("firstname", reservation.get("firstname"));
        requestBody.put("lastname", reservation.get("lastname"));
        requestBody.put("depositpaid", Boolean.parseBoolean(reservation.get("depositpaid")));
        requestBody.put("bookingdates", bookingDates);
        requestBody.put("email", reservation.get("email"));
        requestBody.put("phone", reservation.get("phone"));

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody).log().all()
                .post(this.endpoint);
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer int1) {
        Assertions.assertThat(response.getStatusCode()).isEqualTo(int1);
    }

    @Then("the response status code should be {int} and the error messages should include {string}")
    public void validateStatusCodeAndErrors(int expectedStatusCode, String expectedErrors) {
        assertStatusCode(expectedStatusCode);

        List<String> actualMessages = response.jsonPath().getList("errors");
        if (actualMessages == null || actualMessages.isEmpty()) {
            Assertions.fail("No error messages found in the response.");
        }

        String[] expectedMessages = expectedErrors.split(";");
        for (String expectedMessage : expectedMessages) {
            assertErrorMessagePresent(actualMessages, expectedMessage.trim());
        }
    }

    private void assertStatusCode(int expectedStatusCode) {
        Assertions.assertThat(response.getStatusCode())
                .as("Check response status code")
                .isEqualTo(expectedStatusCode);
    }

    private void assertErrorMessagePresent(List<String> actualMessages, String expectedMessage) {
        for (String actualMessage : actualMessages) {
            if (actualMessage.trim().contains(expectedMessage)) {
                return; // Found match — pass
            }
        }
        Assertions.fail("Expected error message not found: " + expectedMessage);
    }


    @When("the user submits a reservation request missing the field")
    public void submitInvalidReservationRequest(DataTable dataTable) {
        Map<String, String> reservation = dataTable.asMaps(String.class, String.class).get(0);
        Map<String, String> bookingDates = new HashMap<>();
        bookingDates.put("checkin", safeValue(reservation.get("checkin")));
        bookingDates.put("checkout", safeValue(reservation.get("checkout")));
        Map<String, Object> requestBody = new HashMap<>();
        String roomIdValue = safeValue(reservation.get("roomid"));
        if (!roomIdValue.isEmpty()) {
            requestBody.put("roomid", Integer.parseInt(roomIdValue));
        } else {
            requestBody.put("roomid", "");
        }
        requestBody.put("firstname", safeValue(reservation.get("firstname")));
        requestBody.put("lastname", safeValue(reservation.get("lastname")));
        requestBody.put("depositpaid", Boolean.parseBoolean(safeValue(reservation.get("depositpaid"))));
        requestBody.put("bookingdates", bookingDates);
        requestBody.put("email", safeValue(reservation.get("email")));
        requestBody.put("phone", safeValue(reservation.get("phone")));
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .log().all()
                .post(this.endpoint);
    }

    private String safeValue(String value) {
        return isNullOrEmpty(value) ? "" : value.trim();
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
