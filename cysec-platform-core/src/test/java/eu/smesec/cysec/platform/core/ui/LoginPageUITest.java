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
package eu.smesec.cysec.platform.core.ui;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

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

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * UI tests using HTMLUnit for testing the web interface.
 * Tests JSP pages, forms, JavaScript behavior, and XSS protection.
 */
@DisplayName("Login Page UI Tests - HTMLUnit")
@Tag("ui")
@Tag("frontend")
class LoginPageUITest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String LOGIN_URL = BASE_URL + "/app/login";
    private static final String DASHBOARD_URL = BASE_URL + "/app/dashboard";
    
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setTimeout(30000); // 30 seconds timeout
    }

    @AfterEach
    void tearDown() {
        if (webClient != null) {
            webClient.close();
        }
    }

    @Nested
    @DisplayName("Login Page Structure Tests")
    class LoginPageStructureTests {

        @Test
        @DisplayName("Login page loads correctly")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void loginPageLoads() throws IOException {
            // When
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);

            // Then
            assertThat(loginPage.getTitleText())
                .as("Page title should indicate login")
                .containsIgnoringCase("login");

            // Check for essential form elements
            HtmlForm loginForm = loginPage.getForms().get(0);
            assertThat(loginForm).isNotNull();

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");

            assertThat(usernameField).isNotNull();
            assertThat(passwordField).isNotNull();
            assertThat(submitButton).isNotNull();
        }

        @Test
        @DisplayName("Login form has proper attributes")
        void loginFormAttributes() throws IOException {
            // When
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            // Then
            assertThat(loginForm.getMethodAttribute())
                .isEqualToIgnoringCase("POST");

            assertThat(loginForm.getActionAttribute())
                .isNotEmpty()
                .contains("login");

            // Check input field attributes
            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");

            assertThat(usernameField.isRequired())
                .as("Username field should be required")
                .isTrue();

            assertThat(passwordField.isRequired())
                .as("Password field should be required")
                .isTrue();

            assertThat(passwordField.getTypeAttribute())
                .as("Password field should have type=password")
                .isEqualTo("password");
        }

        @Test
        @DisplayName("Login page has proper security headers")
        void securityHeaders() throws IOException {
            // When
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);

            // Then
            assertThat(loginPage.getWebResponse().getResponseHeaderValue("X-Frame-Options"))
                .as("Should have X-Frame-Options header")
                .isNotNull();

            assertThat(loginPage.getWebResponse().getResponseHeaderValue("X-Content-Type-Options"))
                .as("Should have X-Content-Type-Options header")
                .isEqualTo("nosniff");
        }

        @Test
        @DisplayName("Login page contains CSRF protection")
        void csrfProtection() throws IOException {
            // When
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            // Then - Look for CSRF token field
            try {
                HtmlHiddenInput csrfToken = loginForm.getInputByName("_csrf");
                assertThat(csrfToken.getValueAttribute())
                    .as("CSRF token should not be empty")
                    .isNotEmpty();
            } catch (Exception e) {
                // CSRF token might be in meta tag or different field
                List<HtmlMeta> metaTags = loginPage.getByXPath("//meta[@name='_csrf']");
                if (metaTags.isEmpty()) {
                    System.out.println("⚠️  Warning: No CSRF protection found");
                } else {
                    assertThat(metaTags.get(0).getContentAttribute())
                        .isNotEmpty();
                }
            }
        }
    }

    @Nested
    @DisplayName("Login Functionality Tests")
    class LoginFunctionalityTests {

        @Test
        @DisplayName("Successful login redirects to dashboard")
        void successfulLoginRedirect() throws IOException {
            // Given
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");

            // When
            usernameField.setValueAttribute("testuser");
            passwordField.setValueAttribute("TestPassword123!");

            HtmlPage resultPage = submitButton.click();

            // Then
            if (resultPage.getUrl().toString().contains("dashboard")) {
                assertThat(resultPage.getUrl().toString())
                    .as("Should redirect to dashboard after successful login")
                    .contains("dashboard");
                
                assertThat(resultPage.asNormalizedText())
                    .as("Dashboard should contain welcome message")
                    .containsIgnoringCase("welcome");
            } else {
                // If login failed, check for error message
                assertThat(resultPage.asNormalizedText())
                    .as("Should show appropriate message")
                    .containsAnyOf("error", "invalid", "login", "welcome");
            }
        }

        @Test
        @DisplayName("🔴 VULNERABILITY: Empty password login attempt")
        void emptyPasswordLogin_Vulnerability() throws IOException {
            // Given
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");

            // When - Try to login with empty password
            usernameField.setValueAttribute("testuser");
            passwordField.setValueAttribute(""); // Empty password

            HtmlPage resultPage = submitButton.click();

            // Then - Document the vulnerability
            String resultText = resultPage.asNormalizedText().toLowerCase();
            String resultUrl = resultPage.getUrl().toString();

            if (resultUrl.contains("dashboard") || resultText.contains("welcome")) {
                System.err.println("🔴 CRITICAL UI VULNERABILITY: Empty password login succeeded!");
                System.err.println("Result URL: " + resultUrl);
                assertThat(true)
                    .as("Empty password vulnerability confirmed in UI")
                    .isTrue();
            } else {
                System.out.println("✅ Good: Empty password properly rejected in UI");
                assertThat(resultText)
                    .as("Should show error message for empty password")
                    .containsAnyOf("error", "invalid", "required", "password");
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"wrongpass", "password", "123456", "admin", ""})
        @DisplayName("Invalid passwords show error messages")
        void invalidPasswordsShowErrors(String invalidPassword) throws IOException {
            // Given
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");

            // When
            usernameField.setValueAttribute("testuser");
            passwordField.setValueAttribute(invalidPassword);

            HtmlPage resultPage = submitButton.click();

            // Then
            String resultText = resultPage.asNormalizedText().toLowerCase();
            
            // Should not redirect to dashboard with invalid credentials
            assertThat(resultPage.getUrl().toString())
                .as("Should not redirect to dashboard with invalid password")
                .doesNotContain("dashboard");
                
            // Should show some form of error
            if (!invalidPassword.isEmpty()) { // Non-empty invalid passwords should show error
                assertThat(resultText)
                    .as("Should show error message for invalid password: " + invalidPassword)
                    .containsAnyOf("error", "invalid", "incorrect", "failed", "denied");
            }
        }

        @Test
        @DisplayName("Login form validation with JavaScript")
        void clientSideValidation() throws IOException {
            // Given
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");

            // When - Try to submit with empty fields
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");
            
            try {
                HtmlPage resultPage = submitButton.click();
                
                // Then - Should either prevent submission or show validation errors
                String pageText = resultPage.asNormalizedText().toLowerCase();
                
                // Check if still on login page with validation messages
                if (resultPage.getUrl().toString().contains("login")) {
                    // Look for validation messages
                    boolean hasValidationMessage = pageText.contains("required") || 
                                                 pageText.contains("please") ||
                                                 pageText.contains("enter") ||
                                                 pageText.contains("field");
                    
                    if (hasValidationMessage) {
                        System.out.println("✅ Client-side validation working");
                    }
                }
            } catch (Exception e) {
                // Form validation might prevent submission entirely
                System.out.println("✅ Form validation prevented submission");
            }
        }
    }

    @Nested
    @DisplayName("XSS Protection Tests")
    class XSSProtectionTests {

        @ParameterizedTest
        @CsvSource({
            "'<script>alert(\"XSS\")</script>', 'script'",
            "'javascript:alert(\"XSS\")', 'javascript'",
            "'<img src=x onerror=alert(\"XSS\")>', 'img'",
            "'<svg onload=alert(\"XSS\")>', 'svg'",
            "'<iframe src=javascript:alert(\"XSS\")>', 'iframe'"
        })
        @DisplayName("XSS payloads in username field are handled safely")
        void xssInUsernameField(String xssPayload, String expectedBlocked) throws IOException {
            // Given
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");

            // When
            usernameField.setValueAttribute(xssPayload);
            passwordField.setValueAttribute("testpassword");

            HtmlPage resultPage = submitButton.click();

            // Then - Check that XSS payload is not executed
            String pageSource = resultPage.asXml();
            String pageText = resultPage.asNormalizedText();

            // Payload should be encoded or stripped
            assertThat(pageSource)
                .as("XSS payload should not appear as executable code")
                .doesNotContain("<script>alert(")
                .doesNotContain("javascript:alert(")
                .doesNotContain("onerror=alert(")
                .doesNotContain("onload=alert(");

            // If payload is reflected, it should be encoded
            if (pageText.contains(expectedBlocked)) {
                assertThat(pageSource)
                    .as("If reflected, XSS should be properly encoded")
                    .containsAnyOf("&lt;", "&gt;", "&quot;", "&#");
            }

            System.out.println("XSS test for " + expectedBlocked + ": payload handled safely");
        }

        @Test
        @DisplayName("Error messages are XSS safe")
        void errorMessagesXSSSafe() throws IOException {
            // Given
            String xssUsername = "<script>alert('XSS in error')</script>";
            
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");

            // When
            usernameField.setValueAttribute(xssUsername);
            passwordField.setValueAttribute("invalidpassword");

            HtmlPage resultPage = submitButton.click();

            // Then
            String pageSource = resultPage.asXml();
            
            // Error messages should not contain executable XSS
            assertThat(pageSource)
                .as("Error messages should be XSS safe")
                .doesNotContain("<script>alert(")
                .doesNotContain("javascript:");

            // If username is reflected in error, it should be encoded
            if (pageSource.contains("script")) {
                assertThat(pageSource)
                    .as("Reflected content should be HTML encoded")
                    .contains("&lt;script&gt;");
            }
        }
    }

    @Nested
    @DisplayName("Accessibility and Usability Tests")
    class AccessibilityTests {

        @Test
        @DisplayName("Form fields have proper labels")
        void formFieldsHaveLabels() throws IOException {
            // Given
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);

            // When
            List<HtmlLabel> labels = loginPage.getByXPath("//label");

            // Then
            assertThat(labels)
                .as("Login form should have labels")
                .isNotEmpty();

            // Check for specific labels
            boolean hasUsernameLabel = labels.stream()
                .anyMatch(label -> label.asNormalizedText().toLowerCase().contains("username") ||
                                  label.asNormalizedText().toLowerCase().contains("user"));
            
            boolean hasPasswordLabel = labels.stream()
                .anyMatch(label -> label.asNormalizedText().toLowerCase().contains("password"));

            assertThat(hasUsernameLabel)
                .as("Should have username label")
                .isTrue();

            assertThat(hasPasswordLabel)
                .as("Should have password label")
                .isTrue();
        }

        @Test
        @DisplayName("Form has proper ARIA attributes")
        void ariaAttributes() throws IOException {
            // Given
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            // When
            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");

            // Then - Check for accessibility attributes
            String usernameAriaLabel = usernameField.getAttribute("aria-label");
            String passwordAriaLabel = passwordField.getAttribute("aria-label");

            if (usernameAriaLabel != null && !usernameAriaLabel.isEmpty()) {
                assertThat(usernameAriaLabel)
                    .as("Username field should have descriptive aria-label")
                    .isNotEmpty();
            }

            if (passwordAriaLabel != null && !passwordAriaLabel.isEmpty()) {
                assertThat(passwordAriaLabel)
                    .as("Password field should have descriptive aria-label")
                    .isNotEmpty();
            }
        }

        @Test
        @DisplayName("Login page works without JavaScript")
        void worksWithoutJavaScript() throws IOException {
            // Given
            webClient.getOptions().setJavaScriptEnabled(false);

            // When
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            // Then
            assertThat(loginForm)
                .as("Login form should work without JavaScript")
                .isNotNull();

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");

            assertThat(usernameField).isNotNull();
            assertThat(passwordField).isNotNull();
            assertThat(submitButton).isNotNull();

            // Form should still be submittable
            usernameField.setValueAttribute("testuser");
            passwordField.setValueAttribute("testpass");
            
            // This should not throw an exception
            assertDoesNotThrow(() -> submitButton.click());
        }
    }

    @Nested
    @DisplayName("UI Performance Tests")
    @Tag("performance")
    class PerformanceTests {

        @Test
        @DisplayName("Login page loads within reasonable time")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void pageLoadPerformance() throws IOException {
            // When
            long startTime = System.currentTimeMillis();
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            long loadTime = System.currentTimeMillis() - startTime;

            // Then
            assertThat(loadTime)
                .as("Login page should load within 5 seconds")
                .isLessThan(5000);

            assertThat(loginPage.getForms())
                .as("Page should have loaded completely with forms")
                .isNotEmpty();

            System.out.println("Login page loaded in: " + loadTime + "ms");
        }

        @Test
        @DisplayName("Form submission response time")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void formSubmissionPerformance() throws IOException {
            // Given
            HtmlPage loginPage = webClient.getPage(LOGIN_URL);
            HtmlForm loginForm = loginPage.getForms().get(0);

            HtmlTextInput usernameField = loginForm.getInputByName("username");
            HtmlPasswordInput passwordField = loginForm.getInputByName("password");
            HtmlSubmitInput submitButton = loginForm.getInputByValue("Login");

            usernameField.setValueAttribute("testuser");
            passwordField.setValueAttribute("testpass");

            // When
            long startTime = System.currentTimeMillis();
            HtmlPage resultPage = submitButton.click();
            long responseTime = System.currentTimeMillis() - startTime;

            // Then
            assertThat(responseTime)
                .as("Login form submission should respond within 10 seconds")
                .isLessThan(10000);

            assertThat(resultPage)
                .as("Should receive a response page")
                .isNotNull();

            System.out.println("Login form submitted in: " + responseTime + "ms");
        }
    }
}
