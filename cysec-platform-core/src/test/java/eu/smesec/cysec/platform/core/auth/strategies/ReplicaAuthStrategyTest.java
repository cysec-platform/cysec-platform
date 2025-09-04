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

public class ReplicaAuthStrategyTest {

    @Mock
    private CacheAbstractionLayer cal;
    @Mock
    private Config config;
    @Mock
    private ServletContext context;

    private ReplicaAuthStrategy authStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authStrategy = new ReplicaAuthStrategy(cal, config, context);
    }

    @Test
    void constructor_shouldSetProxyAuthToFalse() {
        assertThat(authStrategy.isProxyAuth()).isFalse();
    }

    @Test
    void getHeaderNames_shouldReturnReplicaTokenHeader() {
        List<String> headerNames = authStrategy.getHeaderNames();
        
        assertThat(headerNames).containsExactly("x-cysec-replica-token");
    }

    @Test
    void authenticate_shouldThrowForMissingReplicaHeader() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid auth header");
    }

    @Test
    void authenticate_shouldThrowForNullReplicaHeader() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, null);
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid auth header");
    }

    @Test
    void authenticate_shouldThrowForInvalidTokenPattern() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "invalidtoken");
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("company/token pattern does not match");
    }

    @Test
    void authenticate_shouldThrowForMissingCompanyInPattern() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "/token");
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("company/token pattern does not match");
    }

    @Test
    void authenticate_shouldThrowForMissingTokenInPattern() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "company/");
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("company/token pattern does not match");
    }

    @Test
    void authenticate_shouldReturnFalseForMissingCompanyToken() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "testcompany/validtoken");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.getCompanyReplicaToken("testcompany")).thenReturn(null);
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isFalse();
        verify(cal).getCompanyReplicaToken("testcompany");
    }

    @Test
    void authenticate_shouldReturnFalseForEmptyCompanyToken() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "testcompany/validtoken");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.getCompanyReplicaToken("testcompany")).thenReturn("");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isFalse();
        verify(cal).getCompanyReplicaToken("testcompany");
    }

    @Test
    void authenticate_shouldReturnFalseForMismatchedToken() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "testcompany/providedtoken");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.getCompanyReplicaToken("testcompany")).thenReturn("differenttoken");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isFalse();
        verify(cal).getCompanyReplicaToken("testcompany");
    }

    @Test
    void authenticate_shouldSucceedWithValidToken() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "testcompany/correcttoken");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.getCompanyReplicaToken("testcompany")).thenReturn("correcttoken");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
        verify(cal).getCompanyReplicaToken("testcompany");
        verify(context).setAttribute("company", "testcompany");
    }

    @Test
    void authenticate_shouldHandleComplexCompanyNames() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "company123/token456");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.getCompanyReplicaToken("company123")).thenReturn("token456");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
        verify(context).setAttribute("company", "company123");
    }

    @Test
    void authenticate_shouldHandleComplexTokens() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "testcompany/abc123-def456_ghi789");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.getCompanyReplicaToken("testcompany")).thenReturn("abc123-def456_ghi789");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
        verify(context).setAttribute("company", "testcompany");
    }

    @Test
    void authenticate_shouldHandleCacheException() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "testcompany/token");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.getCompanyReplicaToken("testcompany")).thenThrow(new CacheException("Cache error"));
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(CacheException.class)
                .hasMessage("Cache error");
    }

    @Test
    void authenticate_shouldValidateRegexPattern() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        // Test that the regex properly validates word characters for company name
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "test-company/token"); // hyphen should not match \w+
        Method method = TestResource.class.getMethod("get");
        
        assertThatThrownBy(() -> authStrategy.authenticate(headers, method))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("company/token pattern does not match");
    }

    @Test
    void authenticate_shouldHandleTokensWithSpecialCharacters() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "company/token-with-special_chars.123");
        Method method = TestResource.class.getMethod("get");
        
        when(cal.getCompanyReplicaToken("company")).thenReturn("token-with-special_chars.123");
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
    }

    @Test
    void constants_shouldHaveExpectedValues() {
        assertThat(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER).isEqualTo("x-cysec-replica-token");
    }

    // Test resource class to simulate JAX-RS endpoints
    private static class TestResource {
        @GET
        public String get() {
            return "GET";
        }
    }
}
