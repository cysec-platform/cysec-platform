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
package eu.smesec.cysec.platform.core.utils;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.AbstractAssert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class TestAssertions {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    
    public static void assertValidEmail(String email) {
        assertThat(email)
            .as("Email should not be null or empty")
            .isNotNull()
            .isNotEmpty();
        
        assertThat(EMAIL_PATTERN.matcher(email).matches())
            .as("Email '%s' should have valid format", email)
            .isTrue();
    }
    
    public static void assertStrongPassword(String password) {
        assertThat(password)
            .as("Password should not be null or empty")
            .isNotNull()
            .isNotEmpty();
        
        assertThat(password.length())
            .as("Password should be at least 8 characters")
            .isGreaterThanOrEqualTo(8);
        
        assertThat(STRONG_PASSWORD_PATTERN.matcher(password).matches())
            .as("Password should contain uppercase, lowercase, digit, and special character")
            .isTrue();
    }
    
    public static void assertWeakPassword(String password) {
        boolean isWeak = password == null || 
                        password.length() < 8 || 
                        !STRONG_PASSWORD_PATTERN.matcher(password).matches();
        
        assertThat(isWeak)
            .as("Password '%s' should be considered weak", password)
            .isTrue();
    }
    
    public static void assertValidHexString(String hexString) {
        assertThat(hexString)
            .as("Hex string should not be null or empty")
            .isNotNull()
            .isNotEmpty();
        
        assertThat(HEX_PATTERN.matcher(hexString).matches())
            .as("String '%s' should contain only hex characters", hexString)
            .isTrue();
    }
    
    public static void assertValidUuid(String uuid) {
        assertThat(uuid)
            .as("UUID should not be null or empty")
            .isNotNull()
            .isNotEmpty();
        
        assertThat(UUID_PATTERN.matcher(uuid).matches())
            .as("String '%s' should be valid UUID format", uuid)
            .isTrue();
    }
    
    public static void assertExecutionTime(Runnable task, Duration maxDuration) {
        long startTime = System.nanoTime();
        task.run();
        long executionTime = System.nanoTime() - startTime;
        
        Duration actualDuration = Duration.ofNanos(executionTime);
        assertThat(actualDuration)
            .as("Execution should complete within %s", maxDuration)
            .isLessThanOrEqualTo(maxDuration);
    }
    
    public static void assertRecentTimestamp(LocalDateTime timestamp, Duration tolerance) {
        LocalDateTime now = LocalDateTime.now();
        Duration actualAge = Duration.between(timestamp, now);
        
        assertThat(actualAge)
            .as("Timestamp should be recent within %s tolerance", tolerance)
            .isLessThanOrEqualTo(tolerance);
    }
    
    public static void assertContainsXssProtection(String content) {
        assertThat(content)
            .as("Content should not contain unescaped script tags")
            .doesNotContain("<script>")
            .doesNotContain("javascript:")
            .doesNotContain("onload=")
            .doesNotContain("onerror=");
    }
    
    public static void assertSafeFromSqlInjection(String query) {
        String[] sqlKeywords = {"DROP", "DELETE", "UPDATE", "INSERT", "UNION", "--", "/*", "*/"};
        
        for (String keyword : sqlKeywords) {
            assertThat(query.toUpperCase())
                .as("Query should not contain SQL injection keyword: %s", keyword)
                .doesNotContain(keyword);
        }
    }
    
    public static ResponseAssert assertThatResponse(Response response) {
        return new ResponseAssert(response);
    }
    
    public static HtmlPageAssert assertThatPage(HtmlPage page) {
        return new HtmlPageAssert(page);
    }
    
    public static class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {
        
        public ResponseAssert(Response response) {
            super(response, ResponseAssert.class);
        }
        
        public ResponseAssert hasStatusCode(int expectedStatusCode) {
            isNotNull();
            
            int actualStatusCode = actual.getStatusCode();
            if (actualStatusCode != expectedStatusCode) {
                failWithMessage("Expected status code <%d> but was <%d>. Response body: %s", 
                               expectedStatusCode, actualStatusCode, actual.getBody().asString());
            }
            
            return this;
        }
        
        public ResponseAssert isSuccessful() {
            isNotNull();
            
            int statusCode = actual.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                failWithMessage("Expected successful status code (2xx) but was <%d>", statusCode);
            }
            
            return this;
        }
        
        public ResponseAssert hasContentType(String expectedContentType) {
            isNotNull();
            
            String actualContentType = actual.getContentType();
            if (actualContentType == null || !actualContentType.contains(expectedContentType)) {
                failWithMessage("Expected content type to contain <%s> but was <%s>", 
                               expectedContentType, actualContentType);
            }
            
            return this;
        }
        
        public ResponseAssert containsText(String expectedText) {
            isNotNull();
            
            String body = actual.getBody().asString();
            if (!body.contains(expectedText)) {
                failWithMessage("Expected response body to contain <%s> but it didn't. Body: %s", 
                               expectedText, body);
            }
            
            return this;
        }
        
        public ResponseAssert hasHeader(String headerName) {
            isNotNull();
            
            String headerValue = actual.getHeader(headerName);
            if (headerValue == null) {
                failWithMessage("Expected header <%s> to be present", headerName);
            }
            
            return this;
        }
        
        public ResponseAssert hasSecurityHeaders() {
            return hasHeader("X-Content-Type-Options")
                  .hasHeader("X-Frame-Options")
                  .hasHeader("X-XSS-Protection");
        }
        
        public ResponseAssert hasResponseTimeWithin(Duration maxDuration) {
            isNotNull();
            
            long responseTime = actual.getTime();
            if (responseTime > maxDuration.toMillis()) {
                failWithMessage("Expected response time to be within <%d>ms but was <%d>ms", 
                               maxDuration.toMillis(), responseTime);
            }
            
            return this;
        }
    }
    
    public static class HtmlPageAssert extends AbstractAssert<HtmlPageAssert, HtmlPage> {
        
        public HtmlPageAssert(HtmlPage page) {
            super(page, HtmlPageAssert.class);
        }
        
        public HtmlPageAssert hasTitle(String expectedTitle) {
            isNotNull();
            
            String actualTitle = actual.getTitleText();
            if (!expectedTitle.equals(actualTitle)) {
                failWithMessage("Expected page title <%s> but was <%s>", expectedTitle, actualTitle);
            }
            
            return this;
        }
        
        public HtmlPageAssert containsText(String expectedText) {
            isNotNull();
            
            String pageText = actual.asNormalizedText();
            if (!pageText.contains(expectedText)) {
                failWithMessage("Expected page to contain text <%s> but it didn't", expectedText);
            }
            
            return this;
        }
        
        public HtmlPageAssert hasElementWithId(String elementId) {
            isNotNull();
            
            try {
                actual.getHtmlElementById(elementId);
            } catch (Exception e) {
                failWithMessage("Expected page to have element with id <%s> but it didn't", elementId);
            }
            
            return this;
        }
        
        public HtmlPageAssert hasFormWithName(String formName) {
            isNotNull();
            
            List<HtmlElement> forms = actual.getByXPath("//form[@name='" + formName + "']");
            if (forms.isEmpty()) {
                failWithMessage("Expected page to have form with name <%s> but it didn't", formName);
            }
            
            return this;
        }
        
        public HtmlPageAssert hasNoXssVulnerabilities() {
            isNotNull();
            
            String pageSource = actual.getWebResponse().getContentAsString();
            assertContainsXssProtection(pageSource);
            
            return this;
        }
        
        public HtmlPageAssert hasMetaTag(String name, String expectedContent) {
            isNotNull();
            
            List<HtmlElement> metaTags = actual.getByXPath("//meta[@name='" + name + "']");
            if (metaTags.isEmpty()) {
                failWithMessage("Expected page to have meta tag with name <%s>", name);
            }
            
            String actualContent = metaTags.get(0).getAttribute("content");
            if (!expectedContent.equals(actualContent)) {
                failWithMessage("Expected meta tag <%s> to have content <%s> but was <%s>", 
                               name, expectedContent, actualContent);
            }
            
            return this;
        }
        
        public HtmlPageAssert hasAccessibleForm() {
            isNotNull();
            
            List<HtmlElement> inputs = actual.getByXPath("//input[not(@aria-label) and not(@id)]");
            if (!inputs.isEmpty()) {
                failWithMessage("Found input elements without proper accessibility attributes");
            }
            
            return this;
        }
    }
}
