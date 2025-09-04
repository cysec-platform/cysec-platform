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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Locks;
import eu.smesec.cysec.platform.bridge.generated.User;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.config.Config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class HeaderAuthStrategyTest {

    @Mock
    private CacheAbstractionLayer cal;
    @Mock
    private Config config;
    @Mock
    private ServletContext context;

    private HeaderAuthStrategy authStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(context.getContextPath()).thenReturn("/cysec");
        when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_NAME)).thenReturn("x-username");
        when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_MAIL)).thenReturn("x-email");
        when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_COMPANY)).thenReturn("x-company");
        when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_FIRSTNAME)).thenReturn("x-firstname");
        when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_LASTNAME)).thenReturn("x-lastname");
        when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_LOCALE)).thenReturn("x-locale");
        
        authStrategy = new HeaderAuthStrategy(cal, config, context);
    }

    @Test
    void constructor_shouldSetProxyAuthToTrue() {
        assertThat(authStrategy.isProxyAuth()).isTrue();
    }

    @Test
    void getHeaderNames_shouldReturnFirstThreeHeaders() {
        List<String> headerNames = authStrategy.getHeaderNames();
        
        assertThat(headerNames).containsExactly("x-username", "x-email", "x-company");
    }

    @Test
    void authenticate_shouldThrowForMissingUsernameHeader() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-email", "user@example.com");
        headers.add("x-company", "testcompany");
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("missing oidc fields x-username");
    }

    @Test
    void authenticate_shouldThrowForEmptyUsernameHeader() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-username", "");
        headers.add("x-email", "user@example.com");
        headers.add("x-company", "testcompany");
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("missing oidc fields x-username");
    }

    @Test
    void authenticate_shouldThrowForMissingEmailHeader() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-username", "testuser");
        headers.add("x-company", "testcompany");
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("missing oidc fields x-email");
    }

    @Test
    void authenticate_shouldThrowForMissingCompanyHeader() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-username", "testuser");
        headers.add("x-email", "user@example.com");
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("missing oidc fields x-company");
    }

    @Test
    void authenticate_shouldSucceedWithValidHeaders() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-username", "testuser");
        headers.add("x-email", "user@example.com");
        headers.add("x-company", "testcompany");
        headers.add("x-firstname", "Test");
        headers.add("x-lastname", "User");
        headers.add("x-locale", "en");
        Method method = TestResource.class.getMethod("get");
        
        User existingUser = mock(User.class);
        when(existingUser.getLock()).thenReturn(Locks.NONE);
        when(existingUser.getRole()).thenReturn(Collections.emptyList());
        when(existingUser.getLocale()).thenReturn("en");
        when(existingUser.getUsername()).thenReturn("testuser");
        
        when(cal.existsCompany("testcompany")).thenReturn(true);
        when(cal.getUserByEmail("testcompany", "user@example.com")).thenReturn(existingUser);
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
        verify(context).setAttribute("company", "testcompany");
        verify(context).setAttribute("user", "testuser");
        verify(context).setAttribute("locale", "en");
    }

    @Test
    void authenticate_shouldCreateCompanyForNewUser() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-username", "newuser");
        headers.add("x-email", "newuser@example.com");
        headers.add("x-company", "newcompany");
        headers.add("x-firstname", "New");
        headers.add("x-lastname", "User");
        headers.add("x-locale", "de");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.existsCompany("newcompany")).thenReturn(false);
        
        // Extract credentials should succeed and trigger company creation
        String[] credentials = authStrategy.extractCredentials(headers);
        
        assertThat(credentials).hasSize(4);
        assertThat(credentials[0]).isEqualTo("newcompany");
        assertThat(credentials[1]).isEqualTo("newuser");
        assertThat(credentials[2]).isNull(); // No password for header auth
        assertThat(credentials[3]).isEqualTo("de");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(cal).createCompany(eq("newcompany"), eq("newcompany"), userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("newuser");
        assertThat(capturedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(capturedUser.getFirstname()).isEqualTo("New");
        assertThat(capturedUser.getLocale()).isEqualTo("de");
        assertThat(capturedUser.getRole()).contains("Admin");
    }

    @Test
    void authenticate_shouldCreateUserInExistingCompany() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-username", "newuser");
        headers.add("x-email", "newuser@example.com");
        headers.add("x-company", "existingcompany");
        headers.add("x-firstname", "New");
        headers.add("x-lastname", "User");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.existsCompany("existingcompany")).thenReturn(true);
        when(cal.getUserByEmail("existingcompany", "newuser@example.com")).thenReturn(null);
        
        String[] credentials = authStrategy.extractCredentials(headers);
        
        assertThat(credentials[0]).isEqualTo("existingcompany");
        assertThat(credentials[1]).isEqualTo("newuser");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(cal).createUser(eq("existingcompany"), userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("newuser");
        assertThat(capturedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(capturedUser.getFirstname()).isEqualTo("New");
        assertThat(capturedUser.getLock()).isEqualTo(Locks.NONE);
        assertThat(capturedUser.getRole()).doesNotContain("Admin");
    }

    @Test
    void authenticate_shouldHandleOptionalHeaders() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-username", "testuser");
        headers.add("x-email", "user@example.com");
        headers.add("x-company", "testcompany");
        // Not providing optional headers (firstname, lastname, locale)
        Method method = TestResource.class.getMethod("get");
        
        when(cal.existsCompany("testcompany")).thenReturn(true);
        when(cal.getUserByEmail("testcompany", "user@example.com")).thenReturn(null);
        
        String[] credentials = authStrategy.extractCredentials(headers);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(cal).createUser(eq("testcompany"), userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("testuser");
        assertThat(capturedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(capturedUser.getFirstname()).isNull();
        assertThat(capturedUser.getLocale()).isNull();
    }

    @Test
    void extractCredentials_shouldReturnCorrectFormat() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("x-username", "testuser");
        headers.add("x-email", "user@example.com");
        headers.add("x-company", "testcompany");
        headers.add("x-locale", "fr");
        
        when(cal.existsCompany("testcompany")).thenReturn(true);
        when(cal.getUserByEmail("testcompany", "user@example.com")).thenReturn(mock(User.class));
        
        String[] credentials = authStrategy.extractCredentials(headers);
        
        assertThat(credentials).hasSize(4);
        assertThat(credentials[0]).isEqualTo("testcompany");
        assertThat(credentials[1]).isEqualTo("testuser");
        assertThat(credentials[2]).isNull(); // No password for header auth
        assertThat(credentials[3]).isEqualTo("fr");
    }

    @Test
    void constants_shouldHaveExpectedValues() {
        assertThat(HeaderAuthStrategy.OIDC_NAME).isEqualTo("cysec_header_username");
        assertThat(HeaderAuthStrategy.OIDC_MAIL).isEqualTo("cysec_header_email");
        assertThat(HeaderAuthStrategy.OIDC_COMPANY).isEqualTo("cysec_header_company");
        assertThat(HeaderAuthStrategy.OIDC_FIRSTNAME).isEqualTo("cysec_header_firstname");
        assertThat(HeaderAuthStrategy.OIDC_LASTNAME).isEqualTo("cysec_header_lastname");
        assertThat(HeaderAuthStrategy.OIDC_LOCALE).isEqualTo("cysec_header_locale");
    }

    // Test resource class to simulate JAX-RS endpoints
    private static class TestResource {
        @GET
        public String get() {
            return "GET";
        }
    }
}
