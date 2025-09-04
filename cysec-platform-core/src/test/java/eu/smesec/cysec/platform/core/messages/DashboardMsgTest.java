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

public class DashboardMsgTest {

    @Test
    void constructor_shouldInitializeWithAllParameters() {
        DashboardMsg msg = new DashboardMsg(Locale.ENGLISH, 5, 3, 10);
        assertThat(msg).isNotNull();
    }

    @Test
    void getMessages_shouldContainAllRequiredKeys() {
        DashboardMsg msg = new DashboardMsg(Locale.ENGLISH, 1, 1, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).containsKeys(
            "recommendations", "recommendation", "noRecommendation", "noRecommendationInfo",
            "coaches", "noCoachesStartedInfo", "coachStart", "coachContinue", "coachReset",
            "remaining", "noRemaining", "skills", "strength", "knowHow", "fitness",
            "achievedLevels", "noLevels", "latestAchievements", "noAchievements",
            "showAll", "coachMore", "adminModalTitle", "adminModalExport", "adminModalImport",
            "coachMeta", "editMetadata", "addMetadataField", "metadataCancel", "metadataSave",
            "metadataDelete", "metadataVisible", "metadataKey", "metadataValue"
        );
    }

    @Test
    void getMessages_shouldHaveCorrectSize() {
        DashboardMsg msg = new DashboardMsg(Locale.ENGLISH, 1, 1, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).hasSize(33);
    }

    @Test
    void constructor_shouldHandleZeroValues() {
        DashboardMsg msg = new DashboardMsg(Locale.ENGLISH, 0, 0, 0);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(33);
    }

    @Test
    void constructor_shouldHandleMultipleValues() {
        DashboardMsg msg = new DashboardMsg(Locale.ENGLISH, 10, 5, 20);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages.get("coaches")).isNotNull();
        assertThat(messages.get("remaining")).isNotNull();
        assertThat(messages.get("latestAchievements")).isNotNull();
    }

    @Test
    void constructor_shouldHandleNullLocale() {
        DashboardMsg msg = new DashboardMsg(null, 5, 3, 10);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(33);
    }

    @Test
    void constructor_shouldHandleDifferentLocales() {
        DashboardMsg msgEnglish = new DashboardMsg(Locale.ENGLISH, 5, 3, 10);
        DashboardMsg msgGerman = new DashboardMsg(Locale.GERMAN, 5, 3, 10);
        
        assertThat(msgEnglish.getMessages()).isNotNull();
        assertThat(msgGerman.getMessages()).isNotNull();
    }

    @Test
    void getMessages_shouldReturnSameMapInstance() {
        DashboardMsg msg = new DashboardMsg(Locale.ENGLISH, 5, 3, 10);
        Map<String, String> messages1 = msg.getMessages();
        Map<String, String> messages2 = msg.getMessages();
        
        assertThat(messages1).isSameAs(messages2);
    }

    @Test
    void getMessages_shouldContainMetadataKeys() {
        DashboardMsg msg = new DashboardMsg(Locale.ENGLISH, 1, 1, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).containsKeys(
            "coachMeta", "editMetadata", "addMetadataField", 
            "metadataCancel", "metadataSave", "metadataDelete",
            "metadataVisible", "metadataKey", "metadataValue"
        );
    }

    @Test
    void getMessages_shouldContainAdminModalKeys() {
        DashboardMsg msg = new DashboardMsg(Locale.ENGLISH, 1, 1, 1);
        Map<String, String> messages = msg.getMessages();
        
        assertThat(messages).containsKeys(
            "adminModalTitle", "adminModalExport", "adminModalImport"
        );
    }
}
