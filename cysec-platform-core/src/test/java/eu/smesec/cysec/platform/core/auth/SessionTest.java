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

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

public class SessionTest {

    @Test
    void constructor_shouldInitializeWithUsername() {
        String username = "testuser";
        Session session = new Session(username);
        
        assertThat(session).isNotNull();
    }

    @Test
    void constructor_shouldStoreUsernameInPrivateField() throws Exception {
        String username = "testuser";
        Session session = new Session(username);
        
        Field usernameField = Session.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        String storedUsername = (String) usernameField.get(session);
        
        assertThat(storedUsername).isEqualTo(username);
    }

    @Test
    void constructor_shouldHandleNullUsername() throws Exception {
        Session session = new Session(null);
        
        assertThat(session).isNotNull();
        
        Field usernameField = Session.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        String storedUsername = (String) usernameField.get(session);
        
        assertThat(storedUsername).isNull();
    }

    @Test
    void constructor_shouldHandleEmptyUsername() throws Exception { // edge case for empty string
        String username = "";
        Session session = new Session(username);
        
        Field usernameField = Session.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        String storedUsername = (String) usernameField.get(session);
        
        assertThat(storedUsername).isEmpty();
    }

    @Test
    void constructor_shouldHandleSpecialCharactersInUsername() throws Exception {
        String username = "user@example.com!#$%";
        Session session = new Session(username);
        
        Field usernameField = Session.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        String storedUsername = (String) usernameField.get(session);
        
        assertThat(storedUsername).isEqualTo(username);
    }

    @Test
    void multipleInstances_shouldBeIndependent() throws Exception {
        Session session1 = new Session("user1");
        Session session2 = new Session("user2");
        
        Field usernameField = Session.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        
        String username1 = (String) usernameField.get(session1);
        String username2 = (String) usernameField.get(session2);
        
        assertThat(username1).isEqualTo("user1");
        assertThat(username2).isEqualTo("user2");
        assertThat(session1).isNotSameAs(session2);
    }

    @Test
    void class_shouldHaveUsernameField() {
        Field[] fields = Session.class.getDeclaredFields();
        
        boolean hasUsernameField = false;
        for (Field field : fields) {
            if ("username".equals(field.getName())) {
                hasUsernameField = true;
                assertThat(field.getType()).isEqualTo(String.class);
                break;
            }
        }
        
        assertThat(hasUsernameField).isTrue();
    }
}
