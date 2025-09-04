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

public class BadgeMsgTest {

    @Test
    void constructor_shouldInitializeWithLocaleAndBadgeCount() {  // basic constructor
        BadgeMsg msg = new BadgeMsg(Locale.ENGLISH, 5);
        assertThat(msg).isNotNull();
    }

    @Test
    void getMessages_shouldContainTitle() {
        BadgeMsg msg = new BadgeMsg(Locale.ENGLISH, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).containsKey("title");
        assertThat(messages.get("title")).isNotNull();
    }

    @Test
    void getMessages_shouldContainUnlockedMessage() {
        BadgeMsg msg = new BadgeMsg(Locale.ENGLISH, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).containsKey("unlocked");
        assertThat(messages.get("unlocked")).isNotNull();
    }

    @Test
    void constructor_shouldHandleSingleBadge() {
        BadgeMsg msg = new BadgeMsg(Locale.ENGLISH, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("unlocked")).isNotNull();
    }

    @Test
    void constructor_shouldHandleMultipleBadges() { // plural handling
        BadgeMsg msg = new BadgeMsg(Locale.ENGLISH, 10);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages.get("unlocked")).isNotNull();
    }

    @Test
    void constructor_shouldHandleZeroBadges() {
        BadgeMsg msg = new BadgeMsg(Locale.ENGLISH, 0);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(2);  // should still have 2 messages
    }

    @Test
    void constructor_shouldHandleNullLocale() {
        BadgeMsg msg = new BadgeMsg(null, 5);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).containsKeys("title", "unlocked");
    }

    @Test
    void constructor_shouldHandleDifferentLocales() {
        BadgeMsg msgEnglish = new BadgeMsg(Locale.ENGLISH, 5);
        BadgeMsg msgGerman = new BadgeMsg(Locale.GERMAN, 5);
        
        assertThat(msgEnglish.getMessages()).isNotNull();
        assertThat(msgGerman.getMessages()).isNotNull();
    }

    @Test
    void getMessages_shouldReturnSameMapInstance() {
        BadgeMsg msg = new BadgeMsg(Locale.ENGLISH, 5);
        Map<String, String> messages1 = msg.getMessages();
        Map<String, String> messages2 = msg.getMessages();
        
        assertThat(messages1).isSameAs(messages2);
    }
}
