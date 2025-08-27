package com.booking.client;

import com.booking.model.BookingRequest;
import com.booking.model.BookingResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;

public class BookingClient {
    private static final String BASE_URL = "https://automationintesting.online/api/booking";

    public Response createBooking(BookingRequest bookingRequest) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(bookingRequest)
                .post(BASE_URL)
                .then()
                .extract()
                .response();
    }
}

