@createReservation
Feature: Create a room reservation with valid

  Background:
    Given the user has access to the hotel reservation endpoint "/api/booking"

  @createBooking
  Scenario Outline: create a hotel reservation with all valid inputs
    When the user submits a reservation request with the following details:
      | roomid   | firstname   | lastname   | depositpaid | checkin    | checkout   | email                  | phone        |
      | <roomid> | <firstname> | <lastname> | <depositpaid> | <checkin> | <checkout> | <email>                | <phone>      |
    Then the response status code should be 200
#    Then the response should contain a booking id
#    Then the response should contain booking details matching the request

    Examples:
      | roomid | firstname | lastname | depositpaid | checkin     | checkout    | email                  | phone        |
      | 3      | Lucas     | Martinez | true        | 2025-12-10  | 2025-12-15  | lucas.martinez@test.com | 55512345678  |
      | 2      | Aisha     | Patel    | false       | 2025-11-05  | 2025-11-10  | aisha.patel@test.com    | 55587654321  |

  @negativeBooking
  Scenario Outline: Create booking with missing fields
    When the user submits a reservation request missing the field
      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email         | phone      |
      | <roomid> | <firstname> | <lastname> | <depositpaid> | <checkin> | <checkout> | <email> | <phone> |
    Then the response status code should be <statusCode> and the error messages should include "<expectedErrors>"

    Examples:
      | roomid | firstname | lastname | depositpaid | checkin     | checkout     | email                  | phone        | statusCode | expectedErrors                                                                                     |
      | 1      | Maria     | Kim      | false       | 2025-08-27  | 2025-08-28   | maria.kim@test.com     | 0465491111   | 400        | size must be between 11 and 21                                    |
      | 2      |           | Silva    | true        | 2025-08-29  | 2025-08-30   | silva@test.com         | 046549100022 | 400        | Firstname should not be blank; size must be between 3 and 18                                                                |
      | 3      | Anna      |          | false       |             |              | anna@test.com          | 046549100022 | 400        | Lastname should not be blank; size must be between 3 and 30                                                              |
      | 0      | Sarah     | Lee      | true        | 2025-09-01  | 2025-09-02   | sarah.lee@dummy.com    | 046549100033 | 400        | must be greater than or equal to 1                                                                   |
      | 1      | Alice     | Smith    | true        |             | 2025-09-03   | alice.smith@dummy.com  | 046549100033 | 400        | must not be null                                                               |
      | 1      | Robert    | Langdon  | false       | 2025-09-04  |              | robert.lang@dummy.com  | 046549100033 | 400        | must not be null                                                             |
      | 3      | Angelin   | karun    | true        | 2025-09-04  | 2025-09-06   | test                   | 046549100033 | 400        | must be a well-formed email address                                                            |


#  Scenario: Create booking with invalid roomid
#    When the user submits a reservation request with invalid "roomid" value "abc"
#      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email         | phone      |
#      | abc    | John      | Doe      | true        | 2025-08-27 | 2025-08-28 | john@doe.com  | 1234567890 |
#    Then the response status code should be 400 and error message should contain "roomid must be a number"
#
#  Scenario: Create booking with checkout before checkin
#    When the user submits a reservation request with checkout before checkin
#      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email         | phone      |
#      | 1      | John      | Doe      | true        | 2025-08-28 | 2025-08-27 | john@doe.com  | 1234567890 |
#    Then the response status code should be 400 and error message should contain "checkout must be after checkin"
#
#  Scenario: Create booking with invalid email
#    When the user submits a reservation request with invalid email "not-an-email"
#      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email         | phone      |
#      | 1      | John      | Doe      | true        | 2025-08-27 | 2025-08-28 | not-an-email  | 1234567890 |
#    Then the response status code should be 400 and error message should contain "email is invalid"
#
#  Scenario: Create booking with boundary value for firstname as "A"
#    When the user submits a reservation request with boundary value for "firstname" as "A"
#      | roomid | firstname | lastname | depositpaid | checkin    | checkout   | email         | phone      |
#      | 1      | A         | Doe      | true        | 2025-08-27 | 2025-08-28 | john@doe.com  | 1234567890 |
#    Then the response status code should be 200
#    Then the response should contain booking details matching the request

#  @message
#
#Feature: Get messages
#  Scenario: Message
#    When I want to read the messages
#    Then I should receive all existing messages