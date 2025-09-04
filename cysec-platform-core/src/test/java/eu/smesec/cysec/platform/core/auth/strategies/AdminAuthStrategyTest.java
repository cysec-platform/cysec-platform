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
package eu.smesec.cysec.platform.core.auth.strategies;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.config.Config;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import java.util.List;

public class AdminAuthStrategyTest {

    @Mock
    private CacheAbstractionLayer cal;
    @Mock
    private Config config;
    @Mock
    private ServletContext context;

    private AdminAuthStrategy authStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authStrategy = new AdminAuthStrategy(cal, config, context);
    }

    @Test
    void constructor_shouldSetProxyAuthToFalse() {
        assertThat(authStrategy.isProxyAuth()).isFalse();
    }

    @Test
    void getHeaderNames_shouldReturnAuthorizationHeader() {
        List<String> headerNames = authStrategy.getHeaderNames();
        
        assertThat(headerNames).containsExactly("authorization");
    }

    @Test
    void authenticate_shouldThrowForEmptyHeaders() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid auth header");
    }

    @Test
    void authenticate_shouldThrowForInvalidAuthHeader() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "invalidheader");
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid auth header");
    }

    @Test
    void authenticate_shouldThrowForInvalidAuthFormat() throws Exception {
        String invalidAuth = "testuser:password"; // missing company/
        String encodedAuth = Base64.encodeAsString(invalidAuth);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("invalid auth format");
    }

    @Test
    void authenticate_shouldReturnFalseForNonAdminPrefix() throws Exception {
        String companyUserPass = "normalcompany/admin:password";
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isFalse();
    }

    @Test
    void authenticate_shouldThrowForInvalidUsername() throws Exception {
        String companyUserPass = "admincompany/invalid-user!:password";
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username pattern does not match");
    }

    @Test
    void authenticate_shouldThrowForEmptyPassword() throws Exception {
        String companyUserPass = "admincompany/admin:";
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Password is null or empty");
    }

    @Test
    void authenticate_shouldReturnFalseForUnknownAdminUser() throws Exception {
        String companyUserPass = "admincompany/unknownadmin:password";
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin1 admin2");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PWS)).thenReturn("pass1 pass2");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isFalse();
    }

    @Test
    void authenticate_shouldReturnFalseForWrongPassword() throws Exception {
        String companyUserPass = "admincompany/admin1:wrongpassword";
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin1 admin2");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PWS)).thenReturn("pass1 pass2");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isFalse();
    }

    @Test
    void authenticate_shouldSucceedWithValidAdminCredentials() throws Exception {
        String companyUserPass = "admincompany/admin1:pass1";
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin1 admin2");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PWS)).thenReturn("pass1 pass2");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
    }

    @Test
    void authenticate_shouldSucceedWithSecondAdminUser() throws Exception {
        String companyUserPass = "admincompany/admin2:pass2";
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin1 admin2");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PWS)).thenReturn("pass1 pass2");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
    }

    @Test
    void authenticate_shouldHandleCaseInsensitivePrefix() throws Exception {
        String companyUserPass = "ADMINCOMPANY/admin1:pass1"; // uppercase prefix
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin1");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PWS)).thenReturn("pass1");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
    }

    @Test
    void authenticate_shouldHandleCaseInsensitivePassword() throws Exception {
        String companyUserPass = "admincompany/admin1:PASS1"; // uppercase password
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin1");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PWS)).thenReturn("pass1");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
    }

    @Test
    void authenticate_shouldHandleMismatchedNamePasswordCount() throws Exception {
        String companyUserPass = "admincompany/admin1:pass1";
        String encodedAuth = Base64.encodeAsString(companyUserPass);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("authorization", "Basic " + encodedAuth);
        Method method = TestResource.class.getMethod("get");
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("admincompany");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin1 admin2");
        when(config.getStringValue("cysec", AdminAuthStrategy.ADMIN_PWS)).thenReturn("pass1"); // only one password
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue(); // Should still succeed for first admin
    }

    @Test
    void constants_shouldHaveExpectedValues() {
        assertThat(AdminAuthStrategy.ADMIN_PREFIX).isEqualTo("cysec_admin_prefix");
        assertThat(AdminAuthStrategy.ADMIN_NAMES).isEqualTo("cysec_admin_users");
        assertThat(AdminAuthStrategy.ADMIN_PWS).isEqualTo("cysec_admin_passwords");
    }

    // Test resource class to simulate JAX-RS endpoints
    private static class TestResource {
        @GET
        public String get() {
            return "GET";
        }
    }
}
