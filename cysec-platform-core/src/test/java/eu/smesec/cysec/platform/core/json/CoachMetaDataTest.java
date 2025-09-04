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
package eu.smesec.cysec.platform.core.json;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoachMetaDataTest {

    @Test
    void constants_shouldHaveExpectedValues() {
        assertThat(CoachMetaData.KEY_VISIBLE).isEqualTo("_cysec.coach-meta-visible");
        assertThat(CoachMetaData.KEY_HIDDEN).isEqualTo("_cysec.coach-meta-hidden");
    }

    @Test
    void defaultConstructor_shouldCreateEmptyObject() {
        CoachMetaData metadata = new CoachMetaData();
        
        assertThat(metadata).isNotNull();
        assertThat(metadata.getKey()).isNull();
        assertThat(metadata.getValue()).isNull();
        assertThat(metadata.isVisible()).isFalse();
    }

    @Test
    void parameterizedConstructor_shouldSetAllFields() {
        String key = "testKey";
        String value = "testValue";
        boolean visible = true;
        
        CoachMetaData metadata = new CoachMetaData(key, value, visible);
        
        assertThat(metadata.getKey()).isEqualTo(key);
        assertThat(metadata.getValue()).isEqualTo(value);
        assertThat(metadata.isVisible()).isEqualTo(visible);
    }

    @Test
    void setKey_shouldUpdateKey() {
        CoachMetaData metadata = new CoachMetaData();
        String newKey = "newKey";
        
        metadata.setKey(newKey);
        
        assertThat(metadata.getKey()).isEqualTo(newKey);
    }

    @Test
    void setValue_shouldUpdateValue() {
        CoachMetaData metadata = new CoachMetaData();
        String newValue = "newValue";
        
        metadata.setValue(newValue);
        
        assertThat(metadata.getValue()).isEqualTo(newValue);
    }

    @Test
    void setVisible_shouldUpdateVisibility() {
        CoachMetaData metadata = new CoachMetaData();
        
        metadata.setVisible(true);
        assertThat(metadata.isVisible()).isTrue();
        
        metadata.setVisible(false);
        assertThat(metadata.isVisible()).isFalse();
    }

    @Test
    void constructor_shouldHandleNullValues() {
        CoachMetaData metadata = new CoachMetaData(null, null, false);
        
        assertThat(metadata.getKey()).isNull();
        assertThat(metadata.getValue()).isNull();
        assertThat(metadata.isVisible()).isFalse();
    }

    @Test
    void gettersAndSetters_shouldWorkCorrectly() {
        CoachMetaData metadata = new CoachMetaData();
        
        String key = "myKey";
        String value = "myValue";
        
        metadata.setKey(key);
        metadata.setValue(value);
        metadata.setVisible(true);
        
        assertThat(metadata.getKey()).isEqualTo(key);
        assertThat(metadata.getValue()).isEqualTo(value);
        assertThat(metadata.isVisible()).isTrue();
    }
}
