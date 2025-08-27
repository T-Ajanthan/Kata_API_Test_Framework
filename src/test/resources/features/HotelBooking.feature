@createReservation
Feature: Create a room reservation with valid

  Background:
    Given the user has access to the hotel reservation endpoint "/api/booking"

  @createBooking
  Scenario Outline: create a hotel reservation with all valid inputs
    When the user submits a reservation request with the following details:
      | roomid   | firstname   | lastname   | depositpaid   | checkin   | checkout   | email   | phone   |
      | <roomid> | <firstname> | <lastname> | <depositpaid> | <checkin> | <checkout> | <email> | <phone> |
    Then the response status code should be 200
#    Then the response should contain a booking id
#    Then the response should contain booking details matching the request

    Examples:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email                   | phone       |
      | 2      | Lucas     | Martinez | true        | 2025-12-12 | 2025-12-16 | lucas.martinez@test.com | 55512345678 |
      | 3      | Aisha     | Patel    | false       | 2025-11-06 | 2025-11-11 | aisha.patel@test.com    | 55587654321 |

  @negativeBooking
  Scenario Outline: Create booking with missing fields
    When the user submits a reservation request missing the field
      | roomid   | firstname   | lastname   | depositpaid   | checkin   | checkout   | email   | phone   |
      | <roomid> | <firstname> | <lastname> | <depositpaid> | <checkin> | <checkout> | <email> | <phone> |
    Then the response status code should be <statusCode> and the error messages should include "<expectedErrors>"

    Examples:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email                 | phone        | statusCode | expectedErrors                                               |
      | 1      | Maria     | Kim      | false       | 2025-08-27 | 2025-08-28 | maria.kim@test.com    | 0465491111   | 400        | size must be between 11 and 21                               |
      | 2      |           | Silva    | true        | 2025-08-29 | 2025-08-30 | silva@test.com        | 046549100022 | 400        | Firstname should not be blank; size must be between 3 and 18 |
      | 3      | Anna      |          | false       |            |            | anna@test.com         | 046549100022 | 400        | Lastname should not be blank; size must be between 3 and 30  |
      | 0      | Sarah     | Lee      | true        | 2025-09-01 | 2025-09-02 | sarah.lee@dummy.com   | 046549100033 | 400        | must be greater than or equal to 1                           |
      | 1      | Alice     | Smith    | true        |            | 2025-09-03 | alice.smith@dummy.com | 046549100033 | 400        | must not be null                                             |
      | 1      | Robert    | Langdon  | false       | 2025-09-04 |            | robert.lang@dummy.com | 046549100033 | 400        | must not be null                                             |
      | 3      | Angelin   | karun    | true        | 2025-09-04 | 2025-09-06 | test                  | 046549100033 | 400        | must be a well-formed email address                          |

  @optionalBooking
  Scenario Outline: Create a booking with missing optional fields
    When the user submits a reservation request missing for the optional field
      | roomid   | firstname   | lastname   | depositpaid   | checkin   | checkout   | email   | phone   |
      | <roomid> | <firstname> | <lastname> | <depositpaid> | <checkin> | <checkout> | <email> | <phone> |
    Then the response status code should be 200

    Examples:
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email              | phone      |
      |        | Maria     | Kim      | false       | 2025-08-27 | 2025-08-28 | maria.kim@test.com | 0465491111 |
