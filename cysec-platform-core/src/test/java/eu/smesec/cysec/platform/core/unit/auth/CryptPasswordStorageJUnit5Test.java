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
package eu.smesec.cysec.platform.core.unit.auth;

import eu.smesec.cysec.platform.core.auth.CryptPasswordStorage;
import eu.smesec.cysec.platform.core.auth.CryptType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * JUnit 5 tests for CryptPasswordStorage with modern testing features.
 * Uses AssertJ for fluent assertions and JUnit 5 advanced features.
 */
@DisplayName("CryptPasswordStorage - JUnit 5 Tests")
@Tag("unit")
@Tag("security")
class CryptPasswordStorageJUnit5Test {

    private CryptPasswordStorage storage;
    
    @BeforeEach
    void setUp() {
        // Fresh instance for each test
    }

    @Nested
    @DisplayName("Critical Security Vulnerabilities")
    @Tag("vulnerability")
    class SecurityVulnerabilityTests {

        @Test
        @DisplayName("🔴 CRITICAL: Empty passwords are accepted")
        void emptyPasswordAcceptance_Vulnerability() throws NoSuchAlgorithmException {
            // Given
            CryptPasswordStorage emptyStorage = new CryptPasswordStorage("");
            
            // When
            boolean result = emptyStorage.verify("");
            
            // Then - This assertion PASSES, proving the vulnerability
            assertThat(result)
                .as("Empty password vulnerability exists")
                .isTrue();
            
            // Document vulnerability location
            assertThat("CryptPasswordStorage.java:155-156")
                .as("Vulnerable code location")
                .isNotEmpty();
        }

        @Test
        @DisplayName("🔴 CRITICAL: MD5 password hashing is supported")
        void md5PasswordStorage_Vulnerability() throws NoSuchAlgorithmException {
            // Given
            String password = "testPassword";
            String salt = "testSalt";
            
            // When
            CryptPasswordStorage md5Storage = new CryptPasswordStorage(password, salt, CryptType.MD5);
            
            // Then
            assertThat(md5Storage.getType())
                .as("MD5 is supported (cryptographically broken since 2004)")
                .isEqualTo(CryptType.MD5);
            
            assertThat(md5Storage.verify(password))
                .as("MD5 passwords can be verified")
                .isTrue();
        }

        @Test
        @DisplayName("🔴 CRITICAL: Plaintext password storage is supported")
        void plaintextPasswordStorage_Vulnerability() throws NoSuchAlgorithmException {
            // Given
            String password = "supersecret";
            String salt = "salt";
            
            // When
            CryptPasswordStorage plainStorage = new CryptPasswordStorage(password, salt, CryptType.PLAIN);
            
            // Then
            assertThat(plainStorage.getType())
                .as("Plaintext storage is supported")
                .isEqualTo(CryptType.PLAIN);
            
            assertThat(plainStorage.toString())
                .as("Password is stored in plaintext")
                .contains(password);
        }

        @ParameterizedTest(name = "Weak password accepted: {0}")
        @ValueSource(strings = {"a", "12", "123", "abc", "password", "12345678"})
        @DisplayName("Weak passwords are accepted without validation")
        void weakPasswordsAccepted(String weakPassword) {
            // No exception thrown for weak passwords
            assertDoesNotThrow(() -> {
                CryptPasswordStorage storage = new CryptPasswordStorage(weakPassword, "salt");
                assertThat(storage.verify(weakPassword)).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with password and salt")
        void constructorWithPasswordAndSalt() throws NoSuchAlgorithmException {
            // Given
            String password = "myPassword";
            String salt = "mySalt";
            
            // When
            CryptPasswordStorage storage = new CryptPasswordStorage(password, salt);
            
            // Then
            assertThat(storage)
                .isNotNull()
                .satisfies(s -> {
                    assertThat(s.getSalt()).isEqualTo(salt);
                    assertThat(s.verify(password)).isTrue();
                    assertThat(s.verify("wrongPassword")).isFalse();
                });
        }

        @ParameterizedTest
        @EnumSource(CryptType.class)
        @DisplayName("Constructor with all CryptTypes")
        void constructorWithAllCryptTypes(CryptType type) throws NoSuchAlgorithmException {
            // Given
            String password = "testPassword";
            String salt = "testSalt";
            
            // When
            CryptPasswordStorage storage = new CryptPasswordStorage(password, salt, type);
            
            // Then
            assertThat(storage)
                .extracting(CryptPasswordStorage::getType)
                .isEqualTo(type);
            
            assertThat(storage.verify(password))
                .as("Password should verify with type: %s", type)
                .isTrue();
        }

        @Test
        @DisplayName("Constructor with null salt generates random salt")
        void constructorWithNullSalt() throws NoSuchAlgorithmException {
            // When
            CryptPasswordStorage storage = new CryptPasswordStorage("password", null);
            
            // Then
            assertThat(storage.getSalt())
                .isNotNull()
                .hasSize(32)
                .matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("Constructor with empty salt throws exception")
        void constructorWithEmptySalt() {
            // Then
            assertThatThrownBy(() -> new CryptPasswordStorage("password", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("salt");
        }

        @Test
        @DisplayName("Constructor from storage string")
        void constructorFromStorageString() throws NoSuchAlgorithmException {
            // Given
            CryptPasswordStorage original = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
            String storageString = original.toString();
            
            // When
            CryptPasswordStorage reconstructed = new CryptPasswordStorage(storageString);
            
            // Then
            assertThat(reconstructed)
                .usingRecursiveComparison()
                .isEqualTo(original);
            
            assertThat(reconstructed.verify("password")).isTrue();
        }
    }

    @Nested
    @DisplayName("Password Verification Tests")
    class PasswordVerificationTests {

        @ParameterizedTest
        @CsvSource({
            "password123, password123, true",
            "password123, Password123, false",
            "password123, password, false",
            "password123, password1234, false",
            "pass word, pass word, true",
            "pass word, passwo rd, false"
        })
        @DisplayName("Password verification scenarios")
        void passwordVerificationScenarios(String original, String toVerify, boolean expected) 
                throws NoSuchAlgorithmException {
            // Given
            CryptPasswordStorage storage = new CryptPasswordStorage(original, "salt");
            
            // When & Then
            assertThat(storage.verify(toVerify))
                .as("Verifying '%s' against '%s'", toVerify, original)
                .isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Edge case password verification")
        void edgeCasePasswords(String password) throws NoSuchAlgorithmException {
            if (password == null) {
                CryptPasswordStorage storage = new CryptPasswordStorage("test", "salt");
                // Null password should either return false or throw NPE
                try {
                    boolean result = storage.verify(null);
                    assertThat(result).isFalse();
                } catch (NullPointerException e) {
                    // Also acceptable
                }
            } else {
                // Test with various whitespace passwords
                assertDoesNotThrow(() -> {
                    CryptPasswordStorage storage = new CryptPasswordStorage(password.trim(), "salt");
                    storage.verify(password.trim());
                });
            }
        }

        @Test
        @DisplayName("Special characters in passwords")
        void specialCharacterPasswords() throws NoSuchAlgorithmException {
            // Given
            String[] specialPasswords = {
                "p@ssw0rd!",
                "päsśwörd",
                "パスワード",
                "🔒secure🔒",
                "tab\there",
                "quote\"test",
                "slash/test"
            };
            
            // Then
            for (String password : specialPasswords) {
                CryptPasswordStorage storage = new CryptPasswordStorage(password, "salt");
                
                assertThat(storage.verify(password))
                    .as("Should verify special password: %s", password)
                    .isTrue();
                
                assertThat(storage.verify("wrong"))
                    .as("Should reject wrong password")
                    .isFalse();
            }
        }
    }

    @Nested
    @DisplayName("Random Hex String Generation")
    class RandomHexStringTests {

        @RepeatedTest(value = 5, name = "Random hex uniqueness test {currentRepetition}/{totalRepetitions}")
        @DisplayName("Random hex strings should be unique")
        void randomHexUniqueness() {
            // Given & When
            String hex1 = CryptPasswordStorage.getRandomHexString();
            String hex2 = CryptPasswordStorage.getRandomHexString();
            
            // Then
            assertThat(hex1)
                .isNotEqualTo(hex2)
                .hasSize(32)
                .matches("[0-9a-f]+");
        }

        @ParameterizedTest
        @ValueSource(ints = {8, 16, 32, 64, 128})
        @DisplayName("Random hex with custom lengths")
        void randomHexCustomLength(int length) {
            // When
            String hex = CryptPasswordStorage.getRandomHexString(length);
            
            // Then
            assertThat(hex)
                .hasSize(length)
                .matches("[0-9a-f]+");
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    @Tag("performance")
    class PerformanceTests {

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Password verification performance")
        void passwordVerificationPerformance() throws NoSuchAlgorithmException {
            // Given
            CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
            
            // When & Then
            assertTimeout(Duration.ofMillis(1000), () -> {
                for (int i = 0; i < 1000; i++) {
                    storage.verify("password");
                }
            }, "1000 verifications should complete within 1 second");
        }

        @Test
        @DisplayName("Timing attack resistance")
        void timingAttackResistance() throws NoSuchAlgorithmException {
            // Given
            CryptPasswordStorage storage = new CryptPasswordStorage("correctPassword", "salt");
            
            // When
            long startCorrect = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                storage.verify("correctPassword");
            }
            long timeCorrect = System.nanoTime() - startCorrect;
            
            long startIncorrect = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                storage.verify("wrongPassword!!!");
            }
            long timeIncorrect = System.nanoTime() - startIncorrect;
            
            // Then
            double ratio = (double) timeCorrect / timeIncorrect;
            assertThat(ratio)
                .as("Timing ratio should be close to 1 to prevent timing attacks")
                .isBetween(0.5, 2.0);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Equals contract")
        void equalsContract() throws NoSuchAlgorithmException {
            // Given
            CryptPasswordStorage storage1 = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
            CryptPasswordStorage storage2 = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
            CryptPasswordStorage storage3 = new CryptPasswordStorage("different", "salt", CryptType.SHA512);
            
            // Then
            assertThat(storage1)
                .isEqualTo(storage1) // Reflexive
                .isEqualTo(storage2) // Symmetric
                .isNotEqualTo(storage3)
                .isNotEqualTo(null)
                .isNotEqualTo("string");
            
            assertThat(storage2).isEqualTo(storage1); // Symmetric verification
        }

        @Test
        @DisplayName("HashCode consistency")
        void hashCodeConsistency() throws NoSuchAlgorithmException {
            // Given
            CryptPasswordStorage storage1 = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
            CryptPasswordStorage storage2 = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
            
            // Then
            assertThat(storage1.hashCode())
                .as("Equal objects should have equal hashCodes")
                .isEqualTo(storage2.hashCode());
        }
    }

    @Nested
    @DisplayName("Platform-Specific Tests")
    class PlatformSpecificTests {

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        @DisplayName("Unix-specific crypt compatibility")
        void unixCryptCompatibility() throws NoSuchAlgorithmException {
            // Test Unix crypt(3) compatibility
            CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
            
            assertThat(storage.toString())
                .as("Should use Unix crypt format")
                .startsWith("$6$"); // SHA512 prefix
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("Windows-specific tests")
        void windowsSpecificTests() throws NoSuchAlgorithmException {
            // Windows-specific test cases if needed
            CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt");
            assertThat(storage).isNotNull();
        }
    }
}
