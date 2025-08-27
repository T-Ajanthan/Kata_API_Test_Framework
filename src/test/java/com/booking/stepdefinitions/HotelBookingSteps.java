package com.booking.stepdefinitions;

import io.cucumber.datatable.DataTable;
//import io.cucumber.java.en.Background;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

import org.assertj.core.api.Assertions;
import com.booking.utils.ConfigReader;



import java.util.*;
import java.util.stream.Collectors;

public class HotelBookingSteps {

    String baseUrl = ConfigReader.get("api.baseUrl");
    private String endpoint;
    private String endpointPath = "/api/booking";
    private Response response;
    private Map<String, Object> lastRequestPayload;
    private static final String DEFAULT_BASE_URI = "https://automationintesting.online";

//    @Given("the user has access to the hotel reservation endpoint {string}")
//    public void the_user_has_access_to_the_hotel_reservation_endpoint(String endpointPath) {
//        // allow feature to pass endpoint e.g. "/api/booking"
//        if (endpointPath != null && !endpointPath.isBlank()) {
//            this.basePath = endpointPath;
//        }
//
//        String baseUri = System.getProperty("api.base.uri", DEFAULT_BASE_URI);
//        RestAssured.baseURI = baseUri;
//        RestAssured.basePath = ""; // we'll append basePath in requests explicitly
//
//        // simple sanity check: baseUri not null
//        Assertions.assertThat(RestAssured.baseURI).isNotBlank();
//    }

    @Given("the user has access to the hotel reservation endpoint {string}")
    public void setHotelBookingEndpoint(String endpointPath) {
        // Save the endpoint path from the feature file
        this.endpoint = ConfigReader.get("api.baseUrl") + endpointPath;
    }

//    @When("the user submits a reservation request with the following details:")
//    public void the_user_submits_a_reservation_request_with_the_following_details(DataTable table) {
//        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
//        // Expect at least one row (the test uses a single-row table in scenario)
//        Assertions.assertThat(rows).isNotEmpty();
//        Map<String, String> first = rows.get(0);
//        Map<String, Object> payload = buildPayloadFromMap(first);
//
//        this.lastRequestPayload = payload;
//
//        this.response = RestAssured
//                .given()
//                .contentType(ContentType.JSON)
//                .body(payload)
//                .when()
//                .post(basePath)
//                .andReturn();
//    }
@When("the user submits a reservation request with the following details:")
public void submitReservation(io.cucumber.datatable.DataTable dataTable) {
    // Convert the table row to a Map
    Map<String, String> reservation = dataTable.asMaps(String.class, String.class).get(0);

// Prepare nested bookingdates map
    Map<String, String> bookingDates = new HashMap<>();
    bookingDates.put("checkin", reservation.get("checkin"));
    bookingDates.put("checkout", reservation.get("checkout"));

// Prepare main request body
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("roomid", Integer.parseInt(reservation.get("roomid")));
    requestBody.put("firstname", reservation.get("firstname"));
    requestBody.put("lastname", reservation.get("lastname"));
    requestBody.put("depositpaid", Boolean.parseBoolean(reservation.get("depositpaid")));
    requestBody.put("bookingdates", bookingDates);
    requestBody.put("email", reservation.get("email"));
    requestBody.put("phone", reservation.get("phone"));


    // Send POST request
    response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(requestBody).log().all()
            .post(this.endpoint);
}

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        Assertions.assertThat(response.getStatusCode()).isEqualTo(int1);
    }

    @Then("the response status code should be {int} and the error messages should include {string}")
    public void validateStatusCodeAndErrors(int expectedStatusCode, String expectedErrors) {
        // Validate status code
        Assertions.assertThat(response.getStatusCode())
                .as("Check response status code")
                .isEqualTo(expectedStatusCode);

        // Extract actual error messages from JSON array
        List<String> actualMessages = response.jsonPath().getList("errors");

        // Null or empty check
        if (actualMessages == null || actualMessages.isEmpty()) {
            Assertions.fail("No error messages found in the response.");
        }

        // Split expected errors by semicolon
        String[] expectedMessages = expectedErrors.split(";");

        for (String expectedMessage : expectedMessages) {
            expectedMessage = expectedMessage.trim();
            boolean found = false;

            for (String actualMessage : actualMessages) {
                if (actualMessage.trim().contains(expectedMessage)) {
                    found = true;
                    break;
                }
            }

            Assertions.assertThat(found)
                    .as("Expected error message to be present: " + expectedMessage)
                    .isTrue();
        }
    }

    @When("the user submits a reservation request missing the field")
    public void submitInvalidReservationRequest(DataTable dataTable) {
        Map<String, String> reservation = dataTable.asMaps(String.class, String.class).get(0);

        // Build bookingdates map safely
        Map<String, String> bookingDates = new HashMap<>();
        bookingDates.put("checkin", safeValue(reservation.get("checkin")));
        bookingDates.put("checkout", safeValue(reservation.get("checkout")));

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();

        // Handle roomid (use "" if missing or empty, else parse)
        String roomIdValue = safeValue(reservation.get("roomid"));
        if (!roomIdValue.isEmpty()) {
            requestBody.put("roomid", Integer.parseInt(roomIdValue));
        } else {
            requestBody.put("roomid", ""); // or remove if the API requires
        }

        requestBody.put("firstname", safeValue(reservation.get("firstname")));
        requestBody.put("lastname", safeValue(reservation.get("lastname")));
        requestBody.put("depositpaid", Boolean.parseBoolean(safeValue(reservation.get("depositpaid"))));
        requestBody.put("bookingdates", bookingDates);
        requestBody.put("email", safeValue(reservation.get("email")));
        requestBody.put("phone", safeValue(reservation.get("phone")));

        // Send request
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .log().all()
                .post(this.endpoint);
    }

    // Helper method to ensure no nulls are passed
    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }


    // Utility methods to handle empty/null values
    private Integer parseIntegerOrNull(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseBooleanOrNull(String value) {
        if (value == null || value.isEmpty()) return null;
        return Boolean.parseBoolean(value);
    }


//    @When("the user submits a reservation request with boundary value for {string} as {string}")
//    public void the_user_submits_a_reservation_request_with_boundary_value_for_as(String fieldName, String value, DataTable table) {
//        // The feature provides a table with a single row for this step — reuse same payload builder
//        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
//        Assertions.assertThat(rows).isNotEmpty();
//        Map<String, String> first = rows.get(0);
//
//        // override the particular field with the provided boundary value (if present)
//        if (fieldName != null && !fieldName.isBlank()) {
//            // if the table contains the same column, we override it; otherwise add it
//            first.put(fieldName, value);
//        }
//
//        Map<String, Object> payload = buildPayloadFromMap(first);
//        this.lastRequestPayload = payload;
//
//        this.response = RestAssured
//                .given()
//                .contentType(ContentType.JSON)
//                .body(payload)
//                .when()
//                .post(basePath)
//                .andReturn();
//    }
//
//    @Then("the response status code should be {int}")
//    public void the_response_status_code_should_be(Integer expected) {
//        Assertions.assertThat(response)
//                .withFailMessage("No response captured yet.")
//                .isNotNull();
//
//        int actual = lastResponse.getStatusCode();
//        Assertions.assertThat(actual)
//                .withFailMessage("Expected status code %d but got %d. Response body: %s", expected, actual, lastResponse.asString())
//                .isEqualTo(expected.intValue());
//    }
//
//    @Then("the response status code should be {int} and error message should contain {string}")
//    public void the_response_status_code_should_be_and_error_message_should_contain(Integer expectedCode, String expectedMessage) {
//        Assertions.assertThat(lastResponse).isNotNull();
//        int actual = lastResponse.getStatusCode();
//        Assertions.assertThat(actual)
//                .withFailMessage("Expected status code %d but got %d. Response body: %s", expectedCode, actual, lastResponse.asString())
//                .isEqualTo(expectedCode.intValue());
//
//        String body = lastResponse.asString();
//        Assertions.assertThat(body)
//                .withFailMessage("Expected response body to contain [%s] but was [%s]", expectedMessage, body)
//                .contains(expectedMessage);
//    }
//
//    @Then("the response should contain a booking id")
//    public void the_response_should_contain_a_booking_id() {
//        Assertions.assertThat(lastResponse).isNotNull();
//        // Many booking APIs respond with top-level bookingid
//        Integer bookingId = null;
//        try {
//            bookingId = lastResponse.jsonPath().getInt("bookingid");
//        } catch (Exception ignored) {}
//
//        // Fallback: some variants return "{ id: ..., booking: { ... } }" or "id" field
//        if (bookingId == null || bookingId == 0) {
//            try {
//                bookingId = lastResponse.jsonPath().getInt("id");
//            } catch (Exception ignored) {}
//        }
//
//        Assertions.assertThat(bookingId)
//                .withFailMessage("Response does not contain a booking id. Response body: %s", lastResponse.asString())
//                .isNotNull();
//    }
//
//    @Then("the response should contain booking details matching the request")
//    public void the_response_should_contain_booking_details_matching_the_request() {
//        Assertions.assertThat(lastResponse).isNotNull();
//        Assertions.assertThat(lastRequestPayload).isNotNull();
//
//        // We expect the booking details to be inside "booking" object in the response
//        Map<String, Object> bookingFromResponse;
//        try {
//            bookingFromResponse = lastResponse.jsonPath().getMap("booking");
//        } catch (Exception e) {
//            // fallback: the response may directly return the booking object (no wrapper)
//            bookingFromResponse = lastResponse.jsonPath().getMap("");
//        }
//
//        Assertions.assertThat(bookingFromResponse)
//                .withFailMessage("Response does not contain booking details. Response body: %s", lastResponse.asString())
//                .isNotNull();
//
//        // Normalize and compare each property from lastRequestPayload against the returned booking
//        // Compare primitive fields: roomid, firstname, lastname, depositpaid, email, phone
//        compareField(bookingFromResponse, lastRequestPayload, "roomid");
//        compareField(bookingFromResponse, lastRequestPayload, "firstname");
//        compareField(bookingFromResponse, lastRequestPayload, "lastname");
//        compareField(bookingFromResponse, lastRequestPayload, "depositpaid");
//        compareField(bookingFromResponse, lastRequestPayload, "email");
//        compareField(bookingFromResponse, lastRequestPayload, "phone");
//
//        // Compare bookingdates (nested object)
//        Object expectedDatesObj = lastRequestPayload.get("bookingdates");
//        if (expectedDatesObj instanceof Map) {
//            @SuppressWarnings("unchecked")
//            Map<String, Object> expectedDates = (Map<String, Object>) expectedDatesObj;
//
//            Object actualDatesObj = bookingFromResponse.get("bookingdates");
//            Assertions.assertThat(actualDatesObj)
//                    .withFailMessage("Returned booking does not contain bookingdates. Booking: %s", bookingFromResponse)
//                    .isNotNull();
//
//            @SuppressWarnings("unchecked")
//            Map<String, Object> actualDates = (Map<String, Object>) actualDatesObj;
//
//            compareSimpleValue(expectedDates.get("checkin"), actualDates.get("checkin"), "bookingdates.checkin");
//            compareSimpleValue(expectedDates.get("checkout"), actualDates.get("checkout"), "bookingdates.checkout");
//        }
//    }
//
//    // -------------------- Helpers --------------------
//
//    /**
//     * Build the JSON-ready payload map expected by the booking API from a map of strings
//     * extracted from DataTable (all values are strings).
//     */
//    private Map<String, Object> buildPayloadFromMap(Map<String, String> input) {
//        Map<String, Object> payload = new HashMap<>();
//
//        // roomid
//        if (input.containsKey("roomid")) {
//            String roomidStr = input.get("roomid");
//            if (roomidStr != null && !roomidStr.isBlank()) {
//                try {
//                    payload.put("roomid", Integer.parseInt(roomidStr));
//                } catch (NumberFormatException e) {
//                    // keep as string if cannot parse
//                    payload.put("roomid", roomidStr);
//                }
//            }
//        }
//
//        if (input.containsKey("firstname")) payload.put("firstname", input.get("firstname"));
//        if (input.containsKey("lastname")) payload.put("lastname", input.get("lastname"));
//
//        if (input.containsKey("depositpaid")) {
//            String dp = input.get("depositpaid");
//            if (dp != null) {
//                if ("true".equalsIgnoreCase(dp) || "false".equalsIgnoreCase(dp)) {
//                    payload.put("depositpaid", Boolean.parseBoolean(dp));
//                } else {
//                    // try numeric 1/0
//                    if ("1".equals(dp)) payload.put("depositpaid", true);
//                    else if ("0".equals(dp)) payload.put("depositpaid", false);
//                    else payload.put("depositpaid", dp);
//                }
//            }
//        }
//
//        // dates -> nested bookingdates object
//        Map<String, Object> bookingDates = new HashMap<>();
//        if (input.containsKey("checkin")) bookingDates.put("checkin", input.get("checkin"));
//        if (input.containsKey("checkout")) bookingDates.put("checkout", input.get("checkout"));
//        if (!bookingDates.isEmpty()) {
//            payload.put("bookingdates", bookingDates);
//        }
//
//        if (input.containsKey("email")) payload.put("email", input.get("email"));
//        if (input.containsKey("phone")) payload.put("phone", input.get("phone"));
//
//        return payload;
//    }
//
//    private void compareField(Map<String, Object> actualBooking, Map<String, Object> expectedRequest, String field) {
//        Object expected = expectedRequest.get(field);
//        Object actual = actualBooking.get(field);
//
//        compareSimpleValue(expected, actual, field);
//    }
//
//    private void compareSimpleValue(Object expected, Object actual, String name) {
//        // null-handling
//        if (expected == null && actual == null) return;
//        if (expected == null) {
//            Assertions.assertThat(actual)
//                    .withFailMessage("Expected null for field '%s' but actual was [%s]", name, actual)
//                    .isNull();
//            return;
//        }
//        // Normalize to string comparison after reasonable conversion
//        String expStr = String.valueOf(expected).trim();
//        String actStr = actual != null ? String.valueOf(actual).trim() : null;
//
//        // If expected is boolean-like, compare ignoring case
////        if ("true".equalsIgnoreCase(expStr) || "false".equalsIgnoreCase(expStr)) {
////            Assertions.assertThat(actStr)
////                    .withFailMessage("Mismatch for field %s: expected [%s] but was [%s]", name, expStr, actStr)
////                    .isEqualToIgnoreCase(expStr);
////            return;
////        }
//
//        // Numeric comparison where both are numbers
//        if (isIntegerString(expStr) && actStr != null && isIntegerString(actStr)) {
//            Integer eInt = Integer.parseInt(expStr);
//            Integer aInt = Integer.parseInt(actStr);
//            Assertions.assertThat(aInt)
//                    .withFailMessage("Mismatch for field %s: expected [%d] but was [%d]", name, eInt, aInt)
//                    .isEqualTo(eInt);
//            return;
//        }
//
//        // default: string equality
//        Assertions.assertThat(actStr)
//                .withFailMessage("Mismatch for field %s: expected [%s] but was [%s]", name, expStr, actStr)
//                .isEqualTo(expStr);
//    }
//
//    private boolean isIntegerString(String s) {
//        if (s == null) return false;
//        try {
//            Integer.parseInt(s);
//            return true;
//        } catch (NumberFormatException ex) {
//            return false;
//        }
//    }
}
