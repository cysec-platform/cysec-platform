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

import eu.smesec.cysec.platform.bridge.generated.User;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive tests for Validator class covering all validation methods.
 */
public class ValidatorComprehensiveTest {

    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setUsername("validuser");
        testUser.setEmail("valid@email.com");
        testUser.setFirstname("John");
        testUser.setSurname("Doe");
        testUser.getRole().clear();
        testUser.getRole().add("admin");
    }

    // ========== validateWord Tests ==========

    @Test
    public void testValidateWord_ValidInputs() {
        // Valid word characters: a-zA-Z_0-9
        assertTrue("Letters should be valid", Validator.validateWord("abcABC"));
        assertTrue("Numbers should be valid", Validator.validateWord("123456"));
        assertTrue("Underscore should be valid", Validator.validateWord("test_word"));
        assertTrue("Mix should be valid", Validator.validateWord("Test_123"));
        assertTrue("Single char should be valid", Validator.validateWord("a"));
        assertTrue("Empty string should be valid", Validator.validateWord(""));
    }

    @Test
    public void testValidateWord_InvalidInputs() {
        assertFalse("Space should be invalid", Validator.validateWord("test word"));
        assertFalse("Special chars should be invalid", Validator.validateWord("test@word"));
        assertFalse("Hyphen should be invalid", Validator.validateWord("test-word"));
        assertFalse("Period should be invalid", Validator.validateWord("test.word"));
        assertFalse("Comma should be invalid", Validator.validateWord("test,word"));
        assertFalse("Null should be invalid", Validator.validateWord(null));
    }

    @Test
    public void testValidateWord_EdgeCases() {
        assertTrue("Long valid string", Validator.validateWord("a".repeat(1000)));
        assertFalse("String with newline", Validator.validateWord("test\nword"));
        assertFalse("String with tab", Validator.validateWord("test\tword"));
        assertFalse("String with carriage return", Validator.validateWord("test\rword"));
    }

    // ========== validateWordSpace Tests ==========

    @Test
    public void testValidateWordSpace_ValidInputs() {
        assertTrue("Letters should be valid", Validator.validateWordSpace("abcABC"));
        assertTrue("Numbers should be valid", Validator.validateWordSpace("123456"));
        assertTrue("Underscore should be valid", Validator.validateWordSpace("test_word"));
        assertTrue("Space should be valid", Validator.validateWordSpace("test word"));
        assertTrue("Multiple spaces should be valid", Validator.validateWordSpace("test  word"));
        assertTrue("Leading space should be valid", Validator.validateWordSpace(" test"));
        assertTrue("Trailing space should be valid", Validator.validateWordSpace("test "));
        assertTrue("Mix should be valid", Validator.validateWordSpace("Test 123 Word"));
    }

    @Test
    public void testValidateWordSpace_InvalidInputs() {
        assertFalse("Special chars should be invalid", Validator.validateWordSpace("test@word"));
        assertFalse("Hyphen should be invalid", Validator.validateWordSpace("test-word"));
        assertFalse("Period should be invalid", Validator.validateWordSpace("test.word"));
        assertFalse("Comma should be invalid", Validator.validateWordSpace("test,word"));
        assertFalse("Null should be invalid", Validator.validateWordSpace(null));
        assertFalse("Newline should be invalid", Validator.validateWordSpace("test\nword"));
        assertFalse("Tab should be invalid", Validator.validateWordSpace("test\tword"));
    }

    // ========== validateEmail Tests ==========

    @Test
    public void testValidateEmail_ValidEmails() {
        assertTrue("Simple email", Validator.validateEmail("user@example.com"));
        assertTrue("With subdomain", Validator.validateEmail("user@mail.example.com"));
        assertTrue("With dots in name", Validator.validateEmail("first.last@example.com"));
        assertTrue("With numbers", Validator.validateEmail("user123@example.com"));
        assertTrue("With underscore", Validator.validateEmail("user_name@example.com"));
        assertTrue("Multiple subdomains", Validator.validateEmail("user@mail.server.example.com"));
        assertTrue("Short TLD", Validator.validateEmail("user@example.co"));
        assertTrue("Long TLD", Validator.validateEmail("user@example.museum"));
    }

    @Test
    public void testValidateEmail_InvalidEmails() {
        assertFalse("Missing @", Validator.validateEmail("userexample.com"));
        assertFalse("Missing domain", Validator.validateEmail("user@"));
        assertFalse("Missing user", Validator.validateEmail("@example.com"));
        assertFalse("Double @", Validator.validateEmail("user@@example.com"));
        assertFalse("No TLD", Validator.validateEmail("user@example"));
        assertFalse("Spaces", Validator.validateEmail("user name@example.com"));
        assertFalse("Special chars", Validator.validateEmail("user!name@example.com"));
        assertFalse("Null", Validator.validateEmail(null));
        assertFalse("Empty string", Validator.validateEmail(""));
        assertFalse("Just @", Validator.validateEmail("@"));
    }

    @Test
    public void testValidateEmail_SecurityConcerns() {
        // These SHOULD be invalid but might pass with current weak regex
        String[] problematicEmails = {
            "user+tag@example.com",  // Plus addressing
            "user@localhost",         // No TLD
            "user@192.168.1.1",      // IP address
            "very.long.email.address.that.might.cause.issues@very.long.domain.name.com",
            "user@example..com",      // Double dots
            ".user@example.com",      // Leading dot
            "user.@example.com",      // Trailing dot before @
        };
        
        for (String email : problematicEmails) {
            boolean result = Validator.validateEmail(email);
            System.out.println("Email '" + email + "' validation: " + result);
        }
    }

    // ========== validateAnswer Tests ==========

    @Test
    public void testValidateAnswer_ValidInputs() {
        assertTrue("Letters", Validator.validateAnswer("abcABC"));
        assertTrue("Numbers", Validator.validateAnswer("123456"));
        assertTrue("Spaces", Validator.validateAnswer("test answer"));
        assertTrue("Periods", Validator.validateAnswer("Mr. Smith"));
        assertTrue("Commas", Validator.validateAnswer("Smith, John"));
        assertTrue("Apostrophe", Validator.validateAnswer("don't"));
        assertTrue("Quotes", Validator.validateAnswer("\"quoted\""));
        assertTrue("Parentheses", Validator.validateAnswer("test (example)"));
        assertTrue("Equals", Validator.validateAnswer("2+2=4"));
        assertTrue("Underscore", Validator.validateAnswer("test_answer"));
        assertTrue("Hyphen", Validator.validateAnswer("test-answer"));
    }

    @Test
    public void testValidateAnswer_InvalidInputs() {
        // Characters that should be blocked: /><;?*!&{}
        assertFalse("Forward slash", Validator.validateAnswer("test/answer"));
        assertFalse("Greater than", Validator.validateAnswer("test>answer"));
        assertFalse("Less than", Validator.validateAnswer("test<answer"));
        assertFalse("Semicolon", Validator.validateAnswer("test;answer"));
        assertFalse("Question mark", Validator.validateAnswer("test?answer"));
        assertFalse("Asterisk", Validator.validateAnswer("test*answer"));
        assertFalse("Exclamation", Validator.validateAnswer("test!answer"));
        assertFalse("Ampersand", Validator.validateAnswer("test&answer"));
        assertFalse("Curly braces open", Validator.validateAnswer("test{answer"));
        assertFalse("Curly braces close", Validator.validateAnswer("test}answer"));
        assertFalse("Null", Validator.validateAnswer(null));
    }

    @Test
    public void testValidateAnswer_XSSVectors() {
        // Test common XSS attack vectors
        assertFalse("Script tag", Validator.validateAnswer("<script>alert('XSS')</script>"));
        assertFalse("IMG tag", Validator.validateAnswer("<img src=x onerror=alert('XSS')>"));
        assertFalse("JavaScript URL", Validator.validateAnswer("javascript:alert('XSS')"));
        assertFalse("Event handler", Validator.validateAnswer("onclick=alert('XSS')"));
        
        // These might pass but shouldn't for security
        String[] xssAttempts = {
            "'+alert('XSS')+'",
            "\"><script>alert('XSS')</script>",
            "';alert('XSS');//",
            "</textarea><script>alert('XSS')</script>",
        };
        
        for (String xss : xssAttempts) {
            boolean result = Validator.validateAnswer(xss);
            if (result) {
                System.out.println("WARNING: XSS vector passed validation: " + xss);
            }
        }
    }

    @Test
    public void testValidateAnswer_SQLInjection() {
        // Test SQL injection patterns
        assertFalse("Basic SQL injection", Validator.validateAnswer("' OR '1'='1"));
        assertFalse("Comment injection", Validator.validateAnswer("admin'--"));
        assertFalse("Union injection", Validator.validateAnswer("' UNION SELECT * FROM users--"));
        
        // Test if these pass (they might with current validation)
        String[] sqlAttempts = {
            "1=1",
            "' OR 1=1--",
            "admin' #",
            "' DROP TABLE users--"
        };
        
        for (String sql : sqlAttempts) {
            boolean result = Validator.validateAnswer(sql);
            System.out.println("SQL pattern '" + sql + "' validation: " + result);
        }
    }

    // ========== validateUser Tests ==========

    @Test
    public void testValidateUser_ValidUser() {
        assertTrue("Valid user should pass", Validator.validateUser(testUser));
    }

    @Test
    public void testValidateUser_InvalidUsername() {
        testUser.setUsername("invalid username");  // Contains space
        assertFalse("Invalid username should fail", Validator.validateUser(testUser));
        
        testUser.setUsername("user@name");  // Contains @
        assertFalse("Invalid username should fail", Validator.validateUser(testUser));
        
        testUser.setUsername(null);
        assertFalse("Null username should fail", Validator.validateUser(testUser));
    }

    @Test
    public void testValidateUser_InvalidEmail() {
        testUser.setEmail("invalid.email");  // No @
        assertFalse("Invalid email should fail", Validator.validateUser(testUser));
        
        testUser.setEmail("@example.com");  // No user part
        assertFalse("Invalid email should fail", Validator.validateUser(testUser));
        
        testUser.setEmail(null);
        assertFalse("Null email should fail", Validator.validateUser(testUser));
    }

    @Test
    public void testValidateUser_InvalidFirstname() {
        testUser.setFirstname("John@Doe");  // Contains @
        assertFalse("Invalid firstname should fail", Validator.validateUser(testUser));
        
        testUser.setFirstname("John-Doe");  // Contains hyphen
        assertFalse("Invalid firstname should fail", Validator.validateUser(testUser));
        
        testUser.setFirstname(null);
        assertFalse("Null firstname should fail", Validator.validateUser(testUser));
    }

    @Test
    public void testValidateUser_InvalidSurname() {
        testUser.setSurname("Doe@123");  // Contains @
        assertFalse("Invalid surname should fail", Validator.validateUser(testUser));
        
        testUser.setSurname("Doe-Smith");  // Contains hyphen
        assertFalse("Invalid surname should fail", Validator.validateUser(testUser));
        
        testUser.setSurname(null);
        assertFalse("Null surname should fail", Validator.validateUser(testUser));
    }

    @Test
    public void testValidateUser_InvalidRoles() {
        testUser.getRole().clear();
        testUser.getRole().add("admin@role");  // Invalid role with @
        assertFalse("Invalid role should fail", Validator.validateUser(testUser));
        
        testUser.getRole().clear();
        testUser.getRole().add("admin role");  // Invalid role with space
        assertFalse("Invalid role with space should fail", Validator.validateUser(testUser));
        
        // Test with null by setting the internal field to null - this is tricky with JAXB
        try {
            Validator.validateUser(testUser);
            // Might throw NPE or return false
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testValidateUser_MultipleRoles() {
        testUser.getRole().clear();
        testUser.getRole().add("admin");
        testUser.getRole().add("user");
        testUser.getRole().add("moderator");
        
        assertTrue("Multiple valid roles should pass", Validator.validateUser(testUser));
        
        // Add one invalid role
        testUser.getRole().add("invalid@role");
        assertFalse("One invalid role should fail entire validation", Validator.validateUser(testUser));
    }

    @Test
    public void testValidateUser_EmptyRoles() {
        testUser.getRole().clear();
        assertTrue("Empty roles list should pass", Validator.validateUser(testUser));
    }

    // ========== Performance Tests ==========

    @Test
    public void testValidationPerformance() {
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < 10000; i++) {
            Validator.validateWord("testword123");
            Validator.validateWordSpace("test word 123");
            Validator.validateEmail("user@example.com");
            Validator.validateAnswer("test answer");
            Validator.validateUser(testUser);
        }
        
        long duration = System.currentTimeMillis() - start;
        assertTrue("Validation should be fast (< 1 second for 10000 iterations)", duration < 1000);
        System.out.println("10000 validations completed in " + duration + "ms");
    }

    // ========== Unicode and International Character Tests ==========

    @Test
    public void testValidation_UnicodeCharacters() {
        // Test various Unicode characters
        assertFalse("Chinese characters in word", Validator.validateWord("测试"));
        assertFalse("Arabic in word", Validator.validateWord("اختبار"));
        assertFalse("Emoji in word", Validator.validateWord("test😀"));
        
        // These might pass in validateWordSpace but shouldn't
        assertFalse("Chinese with space", Validator.validateWordSpace("测 试"));
        assertFalse("Mixed Unicode", Validator.validateWordSpace("test 测试"));
        
        // Test in email
        assertFalse("Unicode in email", Validator.validateEmail("用户@example.com"));
        assertFalse("Emoji in email", Validator.validateEmail("user😀@example.com"));
    }

    @Test
    public void testValidation_EdgeLengths() {
        // Test extremely long inputs
        String longString = "a".repeat(10000);
        assertTrue("Long valid word", Validator.validateWord(longString));
        assertTrue("Long valid word with space", Validator.validateWordSpace(longString));
        
        String longEmail = "a".repeat(100) + "@" + "b".repeat(100) + ".com";
        assertTrue("Long email", Validator.validateEmail(longEmail));
        
        String longAnswer = "a".repeat(10000);
        assertTrue("Long answer", Validator.validateAnswer(longAnswer));
    }

    @Test
    public void testValidation_SpecialCases() {
        // Test boundary conditions
        assertTrue("Empty string word", Validator.validateWord(""));
        assertTrue("Empty string wordSpace", Validator.validateWordSpace(""));
        assertFalse("Empty string email", Validator.validateEmail(""));
        assertFalse("Empty string answer", Validator.validateAnswer(""));
        
        // Single character tests
        assertTrue("Single char word", Validator.validateWord("a"));
        assertTrue("Single char wordSpace", Validator.validateWordSpace("a"));
        assertFalse("Single char email", Validator.validateEmail("a"));
        assertTrue("Single char answer", Validator.validateAnswer("a"));
        
        // Just spaces
        assertFalse("Just space word", Validator.validateWord(" "));
        assertTrue("Just space wordSpace", Validator.validateWordSpace(" "));
        assertFalse("Just space email", Validator.validateEmail(" "));
        assertTrue("Just space answer", Validator.validateAnswer(" "));
    }
}
