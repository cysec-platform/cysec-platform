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

public class CoachMsgTest {

    @Test
    void constructor_shouldInitializeWithLocale() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        assertThat(msg).isNotNull();
    }

    @Test
    void getMessages_shouldContainAllRequiredKeys() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).containsKeys("readmore", "next", "summary", "unflagQuestion", "flagQuestion");
    }

    @Test
    void getMessages_shouldHaveCorrectSize() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).hasSize(5);
    }

    @Test
    void getMessages_shouldContainReadmore() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("readmore")).isNotNull();
    }

    @Test
    void getMessages_shouldContainNext() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("next")).isNotNull();
    }

    @Test
    void getMessages_shouldContainSummary() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("summary")).isNotNull();
    }

    @Test
    void getMessages_shouldContainUnflagQuestion() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("unflagQuestion")).isNotNull();
    }

    @Test
    void getMessages_shouldContainFlagQuestion() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("flagQuestion")).isNotNull();
    }

    @Test
    void constructor_shouldHandleNullLocale() {
        CoachMsg msg = new CoachMsg(null);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(5);
    }

    @Test
    void constructor_shouldHandleDifferentLocales() {
        CoachMsg msgEnglish = new CoachMsg(Locale.ENGLISH);
        CoachMsg msgGerman = new CoachMsg(Locale.GERMAN);
        CoachMsg msgFrench = new CoachMsg(Locale.FRENCH);
        
        assertThat(msgEnglish.getMessages()).isNotNull();
        assertThat(msgGerman.getMessages()).isNotNull();
        assertThat(msgFrench.getMessages()).isNotNull();
        
        assertThat(msgEnglish.getMessages()).hasSize(5);
        assertThat(msgGerman.getMessages()).hasSize(5);
        assertThat(msgFrench.getMessages()).hasSize(5);
    }

    @Test
    void getMessages_shouldReturnSameMapInstance() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages1 = msg.getMessages();
        Map<String, String> messages2 = msg.getMessages();
        
        assertThat(messages1).isSameAs(messages2);
    }

    @Test
    void getMessages_shouldAllowModification() {
        CoachMsg msg = new CoachMsg(Locale.ENGLISH);
        Map<String, String> messages = msg.getMessages();
        
        int originalSize = messages.size();
        messages.put("newKey", "newValue");
        
        assertThat(messages).hasSize(originalSize + 1);
        assertThat(messages.get("newKey")).isEqualTo("newValue");
    }
}
