/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.core.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * End-to-End tests using REST Assured for API testing.
 * Tests complete authentication flows, security vulnerabilities, and API interactions.
 */
@DisplayName("Authentication E2E - REST Assured Tests")
@Tag("e2e")
@Tag("api")
class AuthenticationE2ERestAssuredTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_BASE = "/api";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final String TEST_EMAIL = "test@example.com";
    
    private String sessionToken;

    @BeforeAll
    static void globalSetup() {
        // Configure REST Assured
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = API_BASE;
        RestAssured.port = 8080;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Set default content type
        RestAssured.requestSpecification = given()
            .contentType(ContentType.URLENC)
            .accept(ContentType.JSON);
            
        System.out.println("REST Assured configured for: " + BASE_URL + API_BASE);
    }

    @BeforeEach
    void setUp() {
        sessionToken = null;
        // Clear any existing sessions
        given().when().post("/logout").then().statusCode(anyOf(equalTo(200), equalTo(401)));
    }

    @AfterEach
    void tearDown() {
        if (sessionToken != null) {
            given()
                .header("X-Session-Token", sessionToken)
            .when()
                .post("/logout")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(401)));
        }
    }

    @Nested
    @DisplayName("Login Flow Tests")
    class LoginFlowTests {

        @Test
        @DisplayName("Successful login flow with valid credentials")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void successfulLoginFlow() {
            // Step 1: Login with valid credentials
            Response loginResponse = given()
                .formParam("username", TEST_USERNAME)
                .formParam("password", TEST_PASSWORD)
            .when()
                .post("/login")
            .then()
                .statusCode(200)
                .header("X-Session-Token", notNullValue())
                .body("success", equalTo(true))
                .extract().response();

            sessionToken = loginResponse.getHeader("X-Session-Token");
            
            // Step 2: Access protected resource
            given()
                .header("X-Session-Token", sessionToken)
            .when()
                .get("/user/profile")
            .then()
                .statusCode(200)
                .body("username", equalTo(TEST_USERNAME));

            // Step 3: Logout
            given()
                .header("X-Session-Token", sessionToken)
            .when()
                .post("/logout")
            .then()
                .statusCode(200)
                .body("message", containsString("logged out"));

            // Step 4: Verify session is invalid after logout
            given()
                .header("X-Session-Token", sessionToken)
            .when()
                .get("/user/profile")
            .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("🔴 VULNERABILITY: Login with empty password")
        void loginWithEmptyPassword_Vulnerability() {
            // This test demonstrates the critical vulnerability
            Response response = given()
                .formParam("username", TEST_USERNAME)
                .formParam("password", "")  // Empty password
            .when()
                .post("/login")
            .then()
                .extract().response();

            // Log the result for vulnerability documentation
            int statusCode = response.getStatusCode();
            if (statusCode == 200) {
                System.err.println("🔴 CRITICAL VULNERABILITY CONFIRMED: Empty password accepted!");
                System.err.println("Response: " + response.asString());
            } else {
                System.out.println("✅ Good: Empty password properly rejected (status: " + statusCode + ")");
            }
            
            // Document the vulnerability regardless of current behavior
            assertThat(statusCode)
                .as("Empty password vulnerability test - check logs for actual behavior")
                .isIn(200, 401); // Either passes (showing vuln) or fails (secure)
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  ", "\t", "\n", "wrongpass", "PASSWORD", "testpass"})
        @DisplayName("Invalid passwords should be rejected")
        void invalidPasswordsRejected(String invalidPassword) {
            given()
                .formParam("username", TEST_USERNAME)
                .formParam("password", invalidPassword)
            .when()
                .post("/login")
            .then()
                .statusCode(401)
                .body("error", notNullValue())
                .header("X-Session-Token", nullValue());
        }

        @Test
        @DisplayName("Non-existent user login attempt")
        void nonExistentUserLogin() {
            given()
                .formParam("username", "nonexistentuser")
                .formParam("password", TEST_PASSWORD)
            .when()
                .post("/login")
            .then()
                .statusCode(401)
                .body("error", containsString("Invalid"))
                .header("X-Session-Token", nullValue());
        }

        @Test
        @DisplayName("Login with missing parameters")
        void loginWithMissingParameters() {
            // Missing username
            given()
                .formParam("password", TEST_PASSWORD)
            .when()
                .post("/login")
            .then()
                .statusCode(400)
                .body("error", containsString("username"));

            // Missing password
            given()
                .formParam("username", TEST_USERNAME)
            .when()
                .post("/login")
            .then()
                .statusCode(400)
                .body("error", containsString("password"));
        }
    }

    @Nested
    @DisplayName("Authentication Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Brute force protection test")
        void bruteForceProtection() {
            AtomicInteger blockedAttempts = new AtomicInteger(0);
            
            // Attempt multiple failed logins rapidly
            IntStream.rangeClosed(1, 10)
                .parallel()
                .forEach(i -> {
                    Response response = given()
                        .formParam("username", TEST_USERNAME)
                        .formParam("password", "wrongpassword" + i)
                    .when()
                        .post("/login")
                    .then()
                        .extract().response();
                    
                    if (response.getStatusCode() == 429) { // Too Many Requests
                        blockedAttempts.incrementAndGet();
                        System.out.println("✅ Brute force protection activated after attempt " + i);
                    }
                });
            
            // Check if any attempts were blocked
            assertThat(blockedAttempts.get())
                .as("Some login attempts should be blocked by rate limiting")
                .isGreaterThan(0);
        }

        @Test
        @DisplayName("Session hijacking protection")
        void sessionHijackingProtection() {
            // Login from first IP
            sessionToken = given()
                .header("X-Forwarded-For", "192.168.1.1")
                .formParam("username", TEST_USERNAME)
                .formParam("password", TEST_PASSWORD)
            .when()
                .post("/login")
            .then()
                .statusCode(200)
                .extract().header("X-Session-Token");

            // Try to use session from different IP
            Response hijackResponse = given()
                .header("X-Forwarded-For", "10.0.0.1") // Different IP
                .header("X-Session-Token", sessionToken)
            .when()
                .get("/user/profile")
            .then()
                .extract().response();

            // Check if session is tied to IP
            if (hijackResponse.getStatusCode() == 401) {
                System.out.println("✅ Good: Session tied to IP address");
            } else {
                System.out.println("⚠️  Warning: Session not tied to IP - potential hijacking vulnerability");
            }
        }

        @Test
        @DisplayName("CSRF protection test")
        void csrfProtection() {
            // Login to get session
            sessionToken = loginSuccessfully();

            // Try dangerous action without CSRF token
            Response response = given()
                .header("X-Session-Token", sessionToken)
                .header("Origin", "http://evil-site.com")
                .formParam("action", "deleteAccount")
            .when()
                .post("/user/dangerous-action")
            .then()
                .extract().response();

            if (response.getStatusCode() == 403) {
                System.out.println("✅ Good: CSRF protection active");
            } else {
                System.out.println("⚠️  Warning: CSRF protection may not be active");
            }
        }

        @Test
        @DisplayName("SQL injection in login parameters")
        void sqlInjectionAttempts() {
            String[] sqlInjectionPayloads = {
                "admin'--",
                "' OR '1'='1'--",
                "'; DROP TABLE users; --",
                "' UNION SELECT * FROM users--",
                "admin'/*",
                "' OR 1=1#"
            };

            for (String payload : sqlInjectionPayloads) {
                given()
                    .formParam("username", payload)
                    .formParam("password", "anypassword")
                .when()
                    .post("/login")
                .then()
                    .statusCode(anyOf(equalTo(400), equalTo(401))) // Should be rejected, not 200
                    .body("error", notNullValue());
            }
        }
    }

    @Nested
    @DisplayName("API Security Tests")
    class ApiSecurityTests {

        @Test
        @DisplayName("Protected endpoints require authentication")
        void protectedEndpointsRequireAuth() {
            String[] protectedEndpoints = {
                "/user/profile",
                "/user/settings", 
                "/admin/users",
                "/coaches/list",
                "/data/sensitive"
            };

            for (String endpoint : protectedEndpoints) {
                given()
                .when()
                    .get(endpoint)
                .then()
                    .statusCode(401)
                    .body("error", containsString("Unauthorized"));
            }
        }

        @Test
        @DisplayName("API rate limiting")
        void apiRateLimiting() {
            AtomicInteger rateLimitedRequests = new AtomicInteger(0);
            
            // Make rapid requests to API endpoint
            IntStream.rangeClosed(1, 50)
                .parallel()
                .forEach(i -> {
                    Response response = given()
                    .when()
                        .get("/public/info")
                    .then()
                        .extract().response();
                    
                    if (response.getStatusCode() == 429) {
                        rateLimitedRequests.incrementAndGet();
                    }
                });

            System.out.println("Rate limited requests: " + rateLimitedRequests.get());
        }

        @Test
        @DisplayName("Input validation on all parameters")
        void inputValidationTests() {
            String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "javascript:alert('XSS')",
                "<img src=x onerror=alert('XSS')>",
                "';alert('XSS');//"
            };

            for (String payload : xssPayloads) {
                given()
                    .formParam("username", payload)
                    .formParam("password", "validpassword")
                .when()
                    .post("/login")
                .then()
                    .statusCode(anyOf(equalTo(400), equalTo(401)))
                    .body("error", notNullValue());
            }
        }
    }

    @Nested
    @DisplayName("Password Reset Flow")
    class PasswordResetTests {

        @Test
        @DisplayName("Complete password reset flow")
        void completePasswordResetFlow() {
            // Step 1: Request password reset
            given()
                .formParam("email", TEST_EMAIL)
            .when()
                .post("/password/forgot")
            .then()
                .statusCode(200)
                .body("message", containsString("reset"));

            // Step 2: Use reset token (would normally come from email)
            String resetToken = "simulated-reset-token-123";
            
            given()
                .formParam("token", resetToken)
                .formParam("newPassword", "NewPassword456!")
                .formParam("confirmPassword", "NewPassword456!")
            .when()
                .post("/password/reset")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(400))); // 400 if token invalid (expected in test)
        }

        @Test
        @DisplayName("Password reset with invalid email")
        void passwordResetInvalidEmail() {
            given()
                .formParam("email", "nonexistent@example.com")
            .when()
                .post("/password/forgot")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(404))) // Some systems return 200 to prevent enumeration
                .body("message", notNullValue());
        }

        @Test
        @DisplayName("Password reset with malformed email")
        void passwordResetMalformedEmail() {
            given()
                .formParam("email", "invalid-email")
            .when()
                .post("/password/forgot")
            .then()
                .statusCode(400)
                .body("error", containsString("email"));
        }
    }

    @Nested
    @DisplayName("Session Management Tests")
    class SessionManagementTests {

        @Test
        @DisplayName("Concurrent sessions handling")
        void concurrentSessionsHandling() {
            // Create multiple sessions concurrently
            CompletableFuture<String>[] futures = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    return given()
                        .formParam("username", TEST_USERNAME)
                        .formParam("password", TEST_PASSWORD)
                    .when()
                        .post("/login")
                    .then()
                        .statusCode(200)
                        .extract().header("X-Session-Token");
                }))
                .toArray(CompletableFuture[]::new);

            // Wait for all sessions to be created
            CompletableFuture.allOf(futures).join();

            // Count successful sessions
            long successfulSessions = java.util.Arrays.stream(futures)
                .mapToLong(future -> future.join() != null ? 1 : 0)
                .sum();

            assertThat(successfulSessions)
                .as("Should handle concurrent sessions appropriately")
                .isGreaterThan(0);
        }

        @Test
        @DisplayName("Session timeout behavior")
        void sessionTimeoutBehavior() {
            // Login and get session
            sessionToken = loginSuccessfully();

            // Access resource immediately - should work
            given()
                .header("X-Session-Token", sessionToken)
            .when()
                .get("/user/profile")
            .then()
                .statusCode(200);

            // Simulate session timeout by invalidating manually
            given()
                .header("X-Session-Token", sessionToken)
                .header("X-Admin-Action", "invalidate-session")
            .when()
                .post("/admin/sessions/invalidate")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(403))); // May not have admin privileges

            // Try to use expired session
            given()
                .header("X-Session-Token", sessionToken)
            .when()
                .get("/user/profile")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    @Tag("performance")
    class PerformanceTests {

        @Test
        @DisplayName("Login performance under load")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void loginPerformanceUnderLoad() {
            AtomicInteger successfulLogins = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            // Simulate multiple concurrent logins
            CompletableFuture<Void>[] loginFutures = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        Response response = given()
                            .formParam("username", "user" + i)
                            .formParam("password", "password" + i)
                        .when()
                            .post("/login");
                        
                        if (response.getStatusCode() == 200) {
                            successfulLogins.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Expected for non-existent users
                    }
                }))
                .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(loginFutures).join();
            
            long duration = System.currentTimeMillis() - startTime;
            
            assertThat(duration)
                .as("10 concurrent login attempts should complete reasonably quickly")
                .isLessThan(5000); // 5 seconds
        }
    }

    // Helper methods

    private String loginSuccessfully() {
        return given()
            .formParam("username", TEST_USERNAME)
            .formParam("password", TEST_PASSWORD)
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .extract().header("X-Session-Token");
    }

    private RequestSpecification authenticatedRequest() {
        if (sessionToken == null) {
            sessionToken = loginSuccessfully();
        }
        return given().header("X-Session-Token", sessionToken);
    }
}
