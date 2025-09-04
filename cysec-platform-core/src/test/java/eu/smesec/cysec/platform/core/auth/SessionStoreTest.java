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
package eu.smesec.cysec.platform.core.auth;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class SessionStoreTest {

    private SessionStore sessionStore;

    @BeforeEach
    void setUp() {
        sessionStore = new SessionStore();
    }

    @Test
    void constructor_shouldInitializeEmptySessionMap() {  // should be empty at start
        assertThat(sessionStore).isNotNull();
        assertThat(sessionStore.getSessions()).isNotNull();
        assertThat(sessionStore.getSessions()).isEmpty();
    }

    @Test
    void startSession_shouldAddSessionToStore() {
        String token = "test-token-123";  
        Session session = new Session("testuser");  // create test session
        
        sessionStore.startSession(token, session);
        
        assertThat(sessionStore.getSessions()).hasSize(1);
        assertThat(sessionStore.getSessions()).containsKey(token);
        assertThat(sessionStore.getSessions().get(token)).isSameAs(session); // verify session stored
    }

    @Test
    void startSession_shouldOverwriteExistingSession() {
        String token = "test-token-123";
        Session session1 = new Session("user1");
        Session session2 = new Session("user2");
        
        sessionStore.startSession(token, session1);
        sessionStore.startSession(token, session2);
        
        assertThat(sessionStore.getSessions()).hasSize(1);
        assertThat(sessionStore.getSessions().get(token)).isSameAs(session2);  // second session wins
    }

    @Test
    void endSession_shouldRemoveSessionFromStore() {
        String token = "test-token-123";
        Session session = new Session("testuser");
        
        sessionStore.startSession(token, session);
        sessionStore.endSession(token);
        
        assertThat(sessionStore.getSessions()).isEmpty();
        assertThat(sessionStore.getSessions()).doesNotContainKey(token);
    }

    @Test
    void endSession_shouldNotFailForNonExistentToken() {
        sessionStore.endSession("non-existent-token");
        assertThat(sessionStore.getSessions()).isEmpty();
    }

    @Test
    void isActiveSession_shouldReturnTrueForExistingSession() {
        String token = "test-token-123";
        Session session = new Session("testuser");
        
        sessionStore.startSession(token, session);
        
        assertThat(sessionStore.isActiveSession(token)).isTrue();
    }

    @Test
    void isActiveSession_shouldReturnFalseForNonExistentSession() {
        assertThat(sessionStore.isActiveSession("non-existent-token")).isFalse();
    }

    @Test
    void isActiveSession_shouldReturnFalseAfterEndSession() {
        String token = "test-token-123";
        Session session = new Session("testuser");
        
        sessionStore.startSession(token, session);
        sessionStore.endSession(token);
        
        assertThat(sessionStore.isActiveSession(token)).isFalse();
    }

    @Test
    void getSessions_shouldReturnSameMapInstance() {
        Map<String, Session> sessions1 = sessionStore.getSessions();
        Map<String, Session> sessions2 = sessionStore.getSessions();
        
        assertThat(sessions1).isSameAs(sessions2);
    }

    @Test
    void getSessions_shouldAllowDirectMapModification() {
        String token = "test-token-123";
        Session session = new Session("testuser");
        
        Map<String, Session> sessions = sessionStore.getSessions();
        sessions.put(token, session);
        
        assertThat(sessionStore.isActiveSession(token)).isTrue();
    }

    @Test
    void multipleSessionHandling_shouldWorkCorrectly() {
        String token1 = "token-1";
        String token2 = "token-2";
        String token3 = "token-3";
        Session session1 = new Session("user1");
        Session session2 = new Session("user2");
        Session session3 = new Session("user3");
        
        sessionStore.startSession(token1, session1);
        sessionStore.startSession(token2, session2);
        sessionStore.startSession(token3, session3);
        
        assertThat(sessionStore.getSessions()).hasSize(3);
        assertThat(sessionStore.isActiveSession(token1)).isTrue();
        assertThat(sessionStore.isActiveSession(token2)).isTrue();
        assertThat(sessionStore.isActiveSession(token3)).isTrue();
        
        sessionStore.endSession(token2);
        
        assertThat(sessionStore.getSessions()).hasSize(2);
        assertThat(sessionStore.isActiveSession(token1)).isTrue();
        assertThat(sessionStore.isActiveSession(token2)).isFalse();
        assertThat(sessionStore.isActiveSession(token3)).isTrue();
    }

    @Test
    void startSession_shouldHandleNullToken() {
        Session session = new Session("testuser");
        sessionStore.startSession(null, session);
        
        assertThat(sessionStore.getSessions()).hasSize(1);
        assertThat(sessionStore.getSessions()).containsKey(null);
        assertThat(sessionStore.isActiveSession(null)).isTrue();
    }

    @Test
    void startSession_shouldHandleNullSession() {
        String token = "test-token-123";
        sessionStore.startSession(token, null);
        
        assertThat(sessionStore.getSessions()).hasSize(1);
        assertThat(sessionStore.getSessions().get(token)).isNull();
    }
}
