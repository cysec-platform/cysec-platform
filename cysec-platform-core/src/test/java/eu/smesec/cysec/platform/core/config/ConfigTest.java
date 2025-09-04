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
package eu.smesec.cysec.platform.core.config;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigTest {

    private Config config;

    @BeforeEach
    void setUp() throws IOException {
        config = new Config(null);
    }

    @Test
    void copy_shouldCreateDeepCopy() {
        config.createStringConfigValue("test_key", "Test description", "default_value");
        config.setStringValue("section1", "test_key", "new_value", 1);

        Config copy = config.copy();

        assertThat(copy.getStringValue("section1", "test_key")).isEqualTo("new_value");
        assertThat(copy.getDefaultValue("test_key")).isEqualTo("default_value");

        // Modify original - copy should not be affected
        config.setStringValue("section1", "test_key", "modified_value", 2);
        assertThat(copy.getStringValue("section1", "test_key")).isEqualTo("new_value");
    }

    @Test
    void clear_shouldResetAllValuesToDefaults() {
        config.createStringConfigValue("test_key", "Test description", "default_value");
        config.setStringValue("section1", "test_key", "modified_value", 1);

        config.clear();

        assertThat(config.getStringValue("section1", "test_key")).isEqualTo("default_value");
    }

    @Test
    void createBooleanConfigValue_shouldCreateBooleanConfig() {
        config.createBooleanConfigValue("bool_key", "Boolean test", true);

        assertThat(config.getBooleanValue(null, "bool_key")).isTrue();
        assertThat(config.getDefaultValue("bool_key")).isEqualTo("true");
        assertThat(config.getDescription("bool_key")).isEqualTo("Boolean test");
    }

    @Test
    void createBooleanConfigValue_shouldThrowForDuplicateId() {
        config.createBooleanConfigValue("duplicate_key", "First", true);

        assertThatThrownBy(() -> config.createBooleanConfigValue("duplicate_key", "Second", false))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is already defined");
    }

    @Test
    void setBooleanValue_shouldUpdateValue() {
        config.createBooleanConfigValue("bool_key", "Test", false);

        boolean oldValue = config.setBooleanValue("section1", "bool_key", true, 1);

        assertThat(oldValue).isFalse();
        assertThat(config.getBooleanValue("section1", "bool_key")).isTrue();
    }

    @Test
    void getBooleanValue_shouldThrowForUnknownKey() {
        assertThatThrownBy(() -> config.getBooleanValue(null, "unknown_key"))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("is not known to the config subsystem");
    }

    @Test
    void getBooleanValue_shouldThrowForWrongType() {
        config.createStringConfigValue("string_key", "Test", "value");

        assertThatThrownBy(() -> config.getBooleanValue(null, "string_key"))
            .isInstanceOf(ClassCastException.class)
            .hasMessageContaining("expected: boolean");
    }

    @Test
    void createNumericConfigValue_shouldCreateNumericConfig() {
        config.createNumericConfigValue("num_key", "Numeric test", 42);

        assertThat(config.getNumericValue(null, "num_key")).isEqualTo(42);
        assertThat(config.getDefaultValue("num_key")).isEqualTo("42");
    }

    @Test
    void setNumericValue_shouldUpdateValue() {
        config.createNumericConfigValue("num_key", "Test", 10);

        int oldValue = config.setNumericValue("section1", "num_key", 20, 1);

        assertThat(oldValue).isEqualTo(10);
        assertThat(config.getNumericValue("section1", "num_key")).isEqualTo(20);
    }

    @Test
    void getNumericValue_shouldThrowForWrongType() {
        config.createStringConfigValue("string_key", "Test", "value");

        assertThatThrownBy(() -> config.getNumericValue(null, "string_key"))
            .isInstanceOf(ClassCastException.class)
            .hasMessageContaining("expected: numeric");
    }

    @Test
    void createStringConfigValue_shouldCreateStringConfig() {
        boolean created = config.createStringConfigValue("str_key", "String test", "default");

        assertThat(created).isTrue();
        assertThat(config.getStringValue(null, "str_key")).isEqualTo("default");
    }

    @Test
    void createStringConfigValue_shouldReturnFalseForDuplicate() {
        config.createStringConfigValue("str_key", "First", "value1");

        boolean created = config.createStringConfigValue("str_key", "Second", "value2");

        assertThat(created).isFalse();
    }

    @Test
    void setStringValue_shouldUpdateValue() {
        config.createStringConfigValue("str_key", "Test", "default");

        String oldValue = config.setStringValue("section1", "str_key", "new_value", 1);

        assertThat(oldValue).isEqualTo("default");
        assertThat(config.getStringValue("section1", "str_key")).isEqualTo("new_value");
    }

    @Test
    void setStringValue_shouldThrowForNullValue() {
        config.createStringConfigValue("str_key", "Test", "default");

        assertThatThrownBy(() -> config.setStringValue(null, "str_key", null, 1))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createSectionConfigValue_shouldCreateSectionConfig() {
        config.createSectionConfigValue("sec_key", "Section test", "default_section");

        assertThat(config.getSectionValue(null, "sec_key")).isEqualTo("default_section");
    }

    @Test
    void createSectionListConfigValue_shouldCreateSectionListConfig() {
        boolean created = config.createSectionListConfigValue("seclist_key", "Section list test", "sec1,sec2,sec3");

        assertThat(created).isTrue();
        String[] sections = config.getSectionListValue(null, "seclist_key");
        assertThat(sections).containsExactly("sec1", "sec2", "sec3");
    }

    @Test
    void getSectionListValue_shouldReturnEmptyArrayForNullValue() {
        config.createSectionListConfigValue("seclist_key", "Test", null);

        String[] sections = config.getSectionListValue(null, "seclist_key");

        assertThat(sections).isEmpty();
    }

    @Test
    void getSectionListValue_shouldSplitByComma() {
        config.createSectionListConfigValue("seclist_key", "Test", "a, b , c");

        String[] sections = config.getSectionListValue(null, "seclist_key");

        assertThat(sections).containsExactly("a", "b", "c");
    }

    @Test
    void removeConfigValue_shouldRemoveExistingValue() {
        config.createStringConfigValue("str_key", "Test", "value");

        boolean removed = config.removeConfigValue("str_key");

        assertThat(removed).isTrue();
        assertThatThrownBy(() -> config.getStringValue(null, "str_key"))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void removeConfigValue_shouldReturnFalseForNonExistentValue() {
        boolean removed = config.removeConfigValue("non_existent");

        assertThat(removed).isFalse();
    }

    @Test
    void sectionHandling_shouldSeparateValuesBySection() {
        config.createStringConfigValue("str_key", "Test", "default");

        config.setStringValue("section1", "str_key", "value1", 1);
        config.setStringValue("section2", "str_key", "value2", 2);

        assertThat(config.getStringValue("section1", "str_key")).isEqualTo("value1");
        assertThat(config.getStringValue("section2", "str_key")).isEqualTo("value2");
        assertThat(config.getStringValue(null, "str_key")).isEqualTo("default");
    }

    @Test
    void sectionHandling_shouldFallbackToDefaultSection() {
        config.createStringConfigValue("str_key", "Test", "default_value");
        config.setStringValue(null, "str_key", "default_section_value", 1);

        // Should get default section value when specific section doesn't exist
        assertThat(config.getStringValue("nonexistent_section", "str_key")).isEqualTo("default_section_value");
    }

    @Test
    void sectionHandling_shouldFallbackToDefaultValue() {
        config.createStringConfigValue("str_key", "Test", "default_value");

        // Should get default value when no sections are set
        assertThat(config.getStringValue("any_section", "str_key")).isEqualTo("default_value");
    }

    @Test
    void store_shouldGenerateConfigString() throws IOException {
        config.createStringConfigValue("str_key", "Test string", "default");
        config.createBooleanConfigValue("bool_key", "Test boolean", true);
        config.setStringValue("section1", "str_key", "section_value", 1);

        String configString = config.store();

        assertThat(configString).contains("[default]");
        assertThat(configString).contains("[section1]");
        assertThat(configString).contains("str_key = section_value");
        assertThat(configString).contains("// name: str_key");
        assertThat(configString).contains("// Test string");
    }

    @Test
    void load_shouldLoadConfigFromValidFile(@TempDir Path tempDir) throws IOException {
        config.createStringConfigValue("app_name", "Application name", "default");
        config.createNumericConfigValue("port", "Port number", 8080);
        config.createBooleanConfigValue("debug", "Debug mode", false);

        Path configFile = tempDir.resolve("test.conf");
        String configContent = 
            "// Test config file\n" +
            "[default]\n" +
            "app_name = TestApp\n" +
            "port = 9000\n" +
            "\n" +
            "[development]\n" +
            "debug = true\n" +
            "port = 3000\n";
        Files.writeString(configFile, configContent);

        config.load(configFile.toString());

        assertThat(config.getStringValue(null, "app_name")).isEqualTo("TestApp");
        assertThat(config.getNumericValue(null, "port")).isEqualTo(9000);
        assertThat(config.getBooleanValue("development", "debug")).isTrue();
        assertThat(config.getNumericValue("development", "port")).isEqualTo(3000);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "TRUE", "yes", "YES", "True", "Yes"})
    void booleanParsing_shouldAcceptTrueValues(String value) throws IOException {
        config.createBooleanConfigValue("test_bool", "Test", false);

        Path tempFile = Files.createTempFile("test", ".conf");
        Files.writeString(tempFile, "test_bool = " + value);
        
        config.load(tempFile.toString());

        assertThat(config.getBooleanValue(null, "test_bool")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "FALSE", "no", "NO", "anything", ""})
    void booleanParsing_shouldRejectFalseValues(String value) throws IOException {
        config.createBooleanConfigValue("test_bool", "Test", true);

        Path tempFile = Files.createTempFile("test", ".conf");
        Files.writeString(tempFile, "test_bool = " + value);
        
        config.load(tempFile.toString());

        assertThat(config.getBooleanValue(null, "test_bool")).isFalse();
    }
}
