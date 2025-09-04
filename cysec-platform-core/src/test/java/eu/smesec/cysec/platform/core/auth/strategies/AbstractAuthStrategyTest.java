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
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import java.util.List;

public class AbstractAuthStrategyTest {

    @Mock
    private CacheAbstractionLayer cal;
    @Mock
    private Config config;
    @Mock
    private ServletContext context;

    private ConcreteAuthStrategy authStrategy;

    private static class ConcreteAuthStrategy extends AbstractAuthStrategy {
        public ConcreteAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context, boolean proxyAuth) {
            super(cal, config, context, proxyAuth);
        }

        @Override
        public List<String> getHeaderNames() {
            return List.of("test-header");
        }

        @Override
        public boolean authenticate(MultivaluedMap<String, String> headers, Method method) 
                throws CacheException, ClientErrorException {
            return true;
        }

        public void testSetupCompany(User user, String companyId) {
            setupCompany(user, companyId);
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authStrategy = new ConcreteAuthStrategy(cal, config, context, true);
    }

    @Test
    void constructor_shouldSetFields() {
        assertThat(authStrategy.cal).isEqualTo(cal);
        assertThat(authStrategy.config).isEqualTo(config);
        assertThat(authStrategy.context).isEqualTo(context);
        assertThat(authStrategy.isProxyAuth()).isTrue();
    }

    @Test
    void constructor_shouldSetProxyAuthFalse() {
        ConcreteAuthStrategy strategy = new ConcreteAuthStrategy(cal, config, context, false);
        
        assertThat(strategy.isProxyAuth()).isFalse();
    }

    @Test
    void setupCompany_shouldCreateNewCompanyAndUser() throws CacheException {
        String companyId = "testCompany";
        User user = createTestUser("testUser", "test@example.com");
        
        when(cal.existsCompany(companyId)).thenReturn(false);
        
        authStrategy.testSetupCompany(user, companyId);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(cal).existsCompany(companyId);
        verify(cal).createCompany(eq(companyId), eq(companyId), userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("testUser");
        assertThat(capturedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(capturedUser.getLock()).isEqualTo(Locks.NONE);
        assertThat(capturedUser.getRole()).contains("Admin");
    }

    @Test
    void setupCompany_shouldCreateUserInExistingCompany() throws CacheException {
        String companyId = "existingCompany";
        User user = createTestUser("newUser", "new@example.com");
        
        when(cal.existsCompany(companyId)).thenReturn(true);
        when(cal.getUserByName(companyId, "newUser")).thenReturn(null);
        
        authStrategy.testSetupCompany(user, companyId);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(cal).existsCompany(companyId);
        verify(cal).getUserByName(companyId, "newUser");
        verify(cal).createUser(eq(companyId), userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("newUser");
        assertThat(capturedUser.getLock()).isEqualTo(Locks.NONE);
        assertThat(capturedUser.getRole()).doesNotContain("Admin");
    }

    @Test
    void setupCompany_shouldNotCreateDuplicateUser() throws CacheException {
        String companyId = "existingCompany";
        User user = createTestUser("existingUser", "existing@example.com");
        User existingUser = createTestUser("existingUser", "existing@example.com");
        
        when(cal.existsCompany(companyId)).thenReturn(true);
        when(cal.getUserByName(companyId, "existingUser")).thenReturn(existingUser);
        
        authStrategy.testSetupCompany(user, companyId);
        
        verify(cal).existsCompany(companyId);
        verify(cal).getUserByName(companyId, "existingUser");
        verify(cal, never()).createUser(any(), any());
        verify(cal, never()).createCompany(any(), any(), any());
    }

    @Test
    void setupCompany_shouldHandleCacheException() throws CacheException {
        String companyId = "testCompany";
        User user = createTestUser("testUser", "test@example.com");
        
        when(cal.existsCompany(companyId)).thenThrow(new CacheException("Test exception"));
        
        assertThatCode(() -> authStrategy.testSetupCompany(user, companyId))
                .doesNotThrowAnyException();
        
        verify(cal).existsCompany(companyId);
    }

    @Test
    void getHeaderNames_shouldReturnTestHeader() {
        List<String> headerNames = authStrategy.getHeaderNames();
        
        assertThat(headerNames).containsExactly("test-header");
    }

    @Test
    void authenticate_shouldReturnTrue() throws CacheException {
        MultivaluedMap<String, String> headers = mock(MultivaluedMap.class);
        Method method = mock(Method.class);
        
        boolean result = authStrategy.authenticate(headers, method);
        
        assertThat(result).isTrue();
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
}
