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
package eu.smesec.cysec.platform.core.messages;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.Locale;
import java.util.Map;

public class AdminMsgTest {

    @Test
    void constructor_shouldInitializeWithLocaleAndCompanyCount() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 5);
        assertThat(msg).isNotNull();
    }

    @Test
    void getMessages_shouldContainAllRequiredKeys() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).containsKeys("title", "companies", "noCompanies");
    }

    @Test
    void getMessages_shouldContainTitle() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("title")).isNotNull();
    }

    @Test
    void getMessages_shouldContainCompaniesMessage() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("companies")).isNotNull();
    }

    @Test
    void getMessages_shouldContainNoCompaniesMessage() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 0);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("noCompanies")).isNotNull();
    }

    @Test
    void constructor_shouldHandleSingleCompany() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("companies")).isNotNull();
    }

    @Test
    void constructor_shouldHandleMultipleCompanies() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 10);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("companies")).isNotNull();
    }

    @Test
    void constructor_shouldHandleZeroCompanies() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 0);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(3);
    }

    @Test
    void constructor_shouldHandleNullLocale() {
        AdminMsg msg = new AdminMsg(null, 5);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).containsKeys("title", "companies", "noCompanies");
    }

    @Test
    void constructor_shouldHandleDifferentLocales() {
        AdminMsg msgEnglish = new AdminMsg(Locale.ENGLISH, 5);
        AdminMsg msgGerman = new AdminMsg(Locale.GERMAN, 5);
        
        assertThat(msgEnglish.getMessages()).isNotNull();
        assertThat(msgGerman.getMessages()).isNotNull();
    }

    @Test
    void getMessages_shouldReturnSameMapInstance() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, 5);
        Map<String, String> messages1 = msg.getMessages();
        Map<String, String> messages2 = msg.getMessages();
        
        assertThat(messages1).isSameAs(messages2);
    }

    @Test
    void constructor_shouldHandleNegativeCompanyCount() {
        AdminMsg msg = new AdminMsg(Locale.ENGLISH, -1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(3);
    }
}
