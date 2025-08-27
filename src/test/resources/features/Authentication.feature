@authenticationAPI
Feature: Authenticate with login endpoint

  Background:
    Given the user has access to the auth endpoint "/api/auth/login"

  @validLogin
  Scenario Outline: Authenticate with valid credentials and Validate the token generated
    When the user attempts to log in with the following credentials
      | username | password |
      |<username>|<password>|
    Then the authentication response status code should be 200
    And if authentication is successful then the response should contain a "token"

    Examples:
      | username | password |
      | admin    | password |

  @invalidLogin
  Scenario Outline: Authenticate with invalid credentials and verify error messages
    When the user attempts to log in with the following credentials
      | username | password |
      |<username>|<password>|
    Then the response status code should be <statusCode> and the error messages should have "<expectedErrors>"

    Examples:
      | username | password | statusCode | expectedErrors      |
      | invalid  | password | 401        | Invalid credentials |
      | admin    | invalid  | 401        | Invalid credentials |

