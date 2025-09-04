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
package eu.smesec.cysec.platform.core.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

public class LocaleUtilsTest {

    @Test
    void fromString_shouldReturnEnglishForNull() {
        Locale result = LocaleUtils.fromString(null);
        assertThat(result).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void fromString_shouldReturnEnglishForEmptyString() {
        Locale result = LocaleUtils.fromString("");
        assertThat(result).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void fromString_shouldReturnEnglishForInvalidLanguage() {
        Locale result = LocaleUtils.fromString("xyz");
        assertThat(result).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void fromString_shouldParseValidLanguageCode() {
        Locale result = LocaleUtils.fromString("de");
        assertThat(result.getLanguage()).isEqualTo("de");
    }

    @Test
    void fromString_shouldParseLanguageWithCountry() {
        Locale result = LocaleUtils.fromString("en-US");
        assertThat(result.getLanguage()).isEqualTo("en");
        assertThat(result.getCountry()).isEqualTo("US");
    }

    @Test
    void fromString_shouldHandleUnderscoreSeparator() {
        Locale result = LocaleUtils.fromString("en_US");
        assertThat(result.getLanguage()).isEqualTo("en");
        assertThat(result.getCountry()).isEqualTo("US");
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "de", "fr", "es", "it", "ja", "ko", "zh"})
    void fromString_shouldAcceptValidLanguageCodes(String languageCode) {
        Locale result = LocaleUtils.fromString(languageCode);
        assertThat(result.getLanguage()).isEqualTo(languageCode);
        assertThat(result).isNotEqualTo(Locale.ENGLISH);
    }

    @Test
    void fromString_shouldHandleCaseInsensitivity() {
        Locale result1 = LocaleUtils.fromString("EN");
        Locale result2 = LocaleUtils.fromString("en");
        
        assertThat(result1.getLanguage()).isEqualTo("en");
        assertThat(result2.getLanguage()).isEqualTo("en");
    }

    @Test
    void isLanguage_shouldReturnFalseForNull() {
        assertThat(LocaleUtils.isLanguage(null)).isFalse();
    }

    @Test
    void isLanguage_shouldReturnFalseForEmpty() {
        assertThat(LocaleUtils.isLanguage("")).isFalse();
    }

    @Test
    void isLanguage_shouldReturnTrueForValidLanguages() {
        assertThat(LocaleUtils.isLanguage("en")).isTrue();
        assertThat(LocaleUtils.isLanguage("de")).isTrue();
        assertThat(LocaleUtils.isLanguage("fr")).isTrue();
        assertThat(LocaleUtils.isLanguage("es")).isTrue();
    }

    @Test
    void isLanguage_shouldReturnFalseForInvalidLanguages() {
        assertThat(LocaleUtils.isLanguage("xyz")).isFalse();
        assertThat(LocaleUtils.isLanguage("abc")).isFalse();
        assertThat(LocaleUtils.isLanguage("123")).isFalse();
    }

    @Test
    void isLanguage_shouldBeCaseInsensitive() {
        assertThat(LocaleUtils.isLanguage("EN")).isTrue();
        assertThat(LocaleUtils.isLanguage("De")).isTrue();
        assertThat(LocaleUtils.isLanguage("FR")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "de", "fr", "es", "it", "ja", "ko", "zh", "ar", "ru"})
    void isLanguage_shouldAcceptCommonLanguageCodes(String language) {
        assertThat(LocaleUtils.isLanguage(language)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"xyz", "abc", "123", "zz", "xx", "invalid"})
    void isLanguage_shouldRejectInvalidLanguageCodes(String language) {
        assertThat(LocaleUtils.isLanguage(language)).isFalse();
    }

    @Test
    void isCountry_shouldReturnFalseForNull() {
        assertThat(LocaleUtils.isCountry(null)).isFalse();
    }

    @Test
    void isCountry_shouldReturnFalseForEmpty() {
        assertThat(LocaleUtils.isCountry("")).isFalse();
    }

    @Test
    void isCountry_shouldReturnTrueForValidCountries() {
        assertThat(LocaleUtils.isCountry("US")).isTrue();
        assertThat(LocaleUtils.isCountry("DE")).isTrue();
        assertThat(LocaleUtils.isCountry("FR")).isTrue();
        assertThat(LocaleUtils.isCountry("CH")).isTrue();
    }

    @Test
    void isCountry_shouldReturnFalseForInvalidCountries() {
        assertThat(LocaleUtils.isCountry("XY")).isFalse();
        assertThat(LocaleUtils.isCountry("AB")).isFalse();
        assertThat(LocaleUtils.isCountry("12")).isFalse();
        assertThat(LocaleUtils.isCountry("ZZ")).isFalse();
    }

    @Test
    void isCountry_shouldBeCaseInsensitive() {
        assertThat(LocaleUtils.isCountry("us")).isTrue();
        assertThat(LocaleUtils.isCountry("de")).isTrue();
        assertThat(LocaleUtils.isCountry("ch")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"US", "DE", "FR", "CH", "IT", "ES", "GB", "CA", "AU", "JP"})
    void isCountry_shouldAcceptCommonCountryCodes(String country) {
        assertThat(LocaleUtils.isCountry(country)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"XY", "AB", "12", "ZZ", "QQ", "invalid"})
    void isCountry_shouldRejectInvalidCountryCodes(String country) {
        assertThat(LocaleUtils.isCountry(country)).isFalse();
    }

    @Test
    void isCountry_shouldHandleLowercase() {
        assertThat(LocaleUtils.isCountry("us")).isTrue();
        assertThat(LocaleUtils.isCountry("de")).isTrue();
        assertThat(LocaleUtils.isCountry("ch")).isTrue();
    }

    @Test
    void isCountry_shouldHandleMixedCase() {
        assertThat(LocaleUtils.isCountry("Us")).isTrue();
        assertThat(LocaleUtils.isCountry("dE")).isTrue();
        assertThat(LocaleUtils.isCountry("Ch")).isTrue();
    }
}
