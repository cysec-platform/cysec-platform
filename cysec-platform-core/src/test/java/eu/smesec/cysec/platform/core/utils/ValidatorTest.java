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
import static org.junit.jupiter.api.Assertions.*;

import eu.smesec.cysec.platform.bridge.generated.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class ValidatorTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstname("John");
        testUser.setSurname("Doe");
        testUser.getRole().clear();
        testUser.getRole().add("user");
    }

    @Test
    void validateWord_shouldReturnTrueForValidWords() {
        assertThat(Validator.validateWord("word")).isTrue();
        assertThat(Validator.validateWord("Word123")).isTrue();
        assertThat(Validator.validateWord("test_user")).isTrue();
        assertThat(Validator.validateWord("123")).isTrue();
        assertThat(Validator.validateWord("_underscore")).isTrue();
        assertThat(Validator.validateWord("")).isTrue(); // Empty string matches \w*
    }

    @Test
    void validateWord_shouldReturnFalseForInvalidWords() {
        assertThat(Validator.validateWord("word with space")).isFalse();
        assertThat(Validator.validateWord("word-dash")).isFalse();
        assertThat(Validator.validateWord("word.dot")).isFalse();
        assertThat(Validator.validateWord("word@symbol")).isFalse();
        assertThat(Validator.validateWord("word!")).isFalse();
    }

    @Test
    void validateWord_shouldReturnFalseForNull() {
        assertThat(Validator.validateWord(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"validword", "Word123", "test_user", "_start", "123numbers"})
    void validateWord_shouldAcceptValidInputs(String input) {
        assertThat(Validator.validateWord(input)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"word space", "word-dash", "word.dot", "word@email", "special!"})
    void validateWord_shouldRejectInvalidInputs(String input) {
        assertThat(Validator.validateWord(input)).isFalse();
    }

    @Test
    void validateWordSpace_shouldReturnTrueForValidWordsWithSpaces() {
        assertThat(Validator.validateWordSpace("word")).isTrue();
        assertThat(Validator.validateWordSpace("word with spaces")).isTrue();
        assertThat(Validator.validateWordSpace("Word123 Test")).isTrue();
        assertThat(Validator.validateWordSpace("test_user name")).isTrue();
        assertThat(Validator.validateWordSpace("123 456")).isTrue();
        assertThat(Validator.validateWordSpace("")).isTrue();
        assertThat(Validator.validateWordSpace(" ")).isTrue();
    }

    @Test
    void validateWordSpace_shouldReturnFalseForInvalidCharacters() {
        assertThat(Validator.validateWordSpace("word-dash")).isFalse();
        assertThat(Validator.validateWordSpace("word.dot")).isFalse();
        assertThat(Validator.validateWordSpace("word@symbol")).isFalse();
        assertThat(Validator.validateWordSpace("word!")).isFalse();
        assertThat(Validator.validateWordSpace("word\ttab")).isFalse();
        assertThat(Validator.validateWordSpace("word\nnewline")).isFalse();
    }

    @Test
    void validateWordSpace_shouldReturnFalseForNull() {
        assertThat(Validator.validateWordSpace(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"John Doe", "Mary Jane", "First Middle Last", "Test User 123", "_underscore name"})
    void validateWordSpace_shouldAcceptValidNames(String input) {
        assertThat(Validator.validateWordSpace(input)).isTrue();
    }

    @Test
    void validateEmail_shouldReturnTrueForValidEmails() {
        assertThat(Validator.validateEmail("user@example.com")).isTrue();
        assertThat(Validator.validateEmail("test.user@domain.org")).isTrue();
        assertThat(Validator.validateEmail("name123@test.co.uk")).isTrue();
        assertThat(Validator.validateEmail("simple@domain.io")).isTrue();
    }

    @Test
    void validateEmail_shouldReturnFalseForInvalidEmails() {
        assertThat(Validator.validateEmail("invalid.email")).isFalse();
        assertThat(Validator.validateEmail("@domain.com")).isFalse();
        assertThat(Validator.validateEmail("user@")).isFalse();
        assertThat(Validator.validateEmail("user@domain")).isFalse();
        assertThat(Validator.validateEmail("user name@domain.com")).isFalse();
        assertThat(Validator.validateEmail("user@domain .com")).isFalse();
        assertThat(Validator.validateEmail("")).isFalse();
    }

    @Test
    void validateEmail_shouldReturnFalseForNull() {
        assertThat(Validator.validateEmail(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "user@example.com",
        "test.user@domain.org", 
        "name123@test.co.uk",
        "a@b.co",
        "long.email.address@very.long.domain.name.com"
    })
    void validateEmail_shouldAcceptValidEmailFormats(String email) {
        assertThat(Validator.validateEmail(email)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid.email",
        "@domain.com",
        "user@",
        "user@domain",
        "user name@domain.com",
        "user@domain .com"
    })
    void validateEmail_shouldRejectInvalidEmailFormats(String email) {
        assertThat(Validator.validateEmail(email)).isFalse();
    }

    @Test
    void validateAnswer_shouldReturnTrueForValidAnswers() {
        assertThat(Validator.validateAnswer("This is a valid answer")).isTrue();
        assertThat(Validator.validateAnswer("Answer with numbers 123")).isTrue();
        assertThat(Validator.validateAnswer("Allowed chars: @#$%^()[]+=_-")).isTrue();
        assertThat(Validator.validateAnswer("Simple answer")).isTrue();
    }

    @Test
    void validateAnswer_shouldReturnFalseForForbiddenCharacters() {
        assertThat(Validator.validateAnswer("Answer with /")).isFalse();
        assertThat(Validator.validateAnswer("Answer with >")).isFalse();
        assertThat(Validator.validateAnswer("Answer with <")).isFalse();
        assertThat(Validator.validateAnswer("Answer with ;")).isFalse();
        assertThat(Validator.validateAnswer("Answer with ?")).isFalse();
        assertThat(Validator.validateAnswer("Answer with *")).isFalse();
        assertThat(Validator.validateAnswer("Answer with !")).isFalse();
        assertThat(Validator.validateAnswer("Answer with &")).isFalse();
        assertThat(Validator.validateAnswer("Answer with {")).isFalse();
        assertThat(Validator.validateAnswer("Answer with }")).isFalse();
    }

    @Test
    void validateAnswer_shouldReturnFalseForNull() {
        assertThat(Validator.validateAnswer(null)).isFalse();
    }

    @Test
    void validateAnswer_shouldReturnFalseForEmptyString() {
        assertThat(Validator.validateAnswer("")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", ">", "<", ";", "?", "*", "!", "&", "{", "}"})
    void validateAnswer_shouldRejectEachForbiddenCharacter(String forbiddenChar) {
        String answer = "Valid answer " + forbiddenChar;
        assertThat(Validator.validateAnswer(answer)).isFalse();
    }

    @Test
    void validateUser_shouldReturnTrueForValidUser() {
        assertThat(Validator.validateUser(testUser)).isTrue();
    }

    @Test
    void validateUser_shouldReturnFalseForInvalidUsername() {
        testUser.setUsername("invalid username");
        assertThat(Validator.validateUser(testUser)).isFalse();
    }

    @Test
    void validateUser_shouldReturnFalseForInvalidEmail() {
        testUser.setEmail("invalid.email");
        assertThat(Validator.validateUser(testUser)).isFalse();
    }

    @Test
    void validateUser_shouldReturnFalseForInvalidFirstname() {
        testUser.setFirstname("John@invalid");
        assertThat(Validator.validateUser(testUser)).isFalse();
    }

    @Test
    void validateUser_shouldReturnFalseForInvalidSurname() {
        testUser.setSurname("Doe!invalid");
        assertThat(Validator.validateUser(testUser)).isFalse();
    }

    @Test
    void validateUser_shouldReturnFalseForInvalidRole() {
        testUser.getRole().clear();
        testUser.getRole().add("invalid role");
        assertThat(Validator.validateUser(testUser)).isFalse();
    }

    @Test
    void validateUser_shouldHandleMultipleRoles() {
        testUser.getRole().clear();
        testUser.getRole().add("admin");
        testUser.getRole().add("user");
        testUser.getRole().add("moderator");
        
        assertThat(Validator.validateUser(testUser)).isTrue();
    }

    @Test
    void validateUser_shouldReturnFalseForMixedValidInvalidRoles() {
        testUser.getRole().clear();
        testUser.getRole().add("admin");
        testUser.getRole().add("invalid role");
        testUser.getRole().add("user");
        
        assertThat(Validator.validateUser(testUser)).isFalse();
    }

    @Test
    void validateUser_shouldHandleEdgeCases() {
        User edgeUser = new User();
        edgeUser.setUsername("a");
        edgeUser.setEmail("a@b.co");
        edgeUser.setFirstname("A");
        edgeUser.setSurname("B");
        edgeUser.getRole().clear();
        edgeUser.getRole().add("r");
        
        assertThat(Validator.validateUser(edgeUser)).isTrue();
    }

    @Test
    void validateUser_shouldAcceptEmptyNames() {
        testUser.setFirstname("");
        testUser.setSurname("");
        
        assertThat(Validator.validateUser(testUser)).isTrue();
    }
}
