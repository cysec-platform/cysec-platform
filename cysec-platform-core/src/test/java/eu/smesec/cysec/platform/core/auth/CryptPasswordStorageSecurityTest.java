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
package eu.smesec.cysec.platform.core.auth;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.security.NoSuchAlgorithmException;

/**
 * Security-focused tests for CryptPasswordStorage.
 * These tests document and verify critical security vulnerabilities in password handling.
 */
public class CryptPasswordStorageSecurityTest {

    private CryptPasswordStorage storage;

    @Before
    public void setUp() {
        // Initialize for each test
    }

    // ========== VULNERABILITY TESTS - These PASS showing security issues ==========

    /**
     * CRITICAL VULNERABILITY: Empty passwords are accepted as valid
     * This test PASSES, demonstrating the vulnerability exists
     */
    @Test
    public void testEmptyPasswordAcceptance_VULNERABILITY() throws NoSuchAlgorithmException {
        // Create storage with empty password
        CryptPasswordStorage emptyStorage = new CryptPasswordStorage("");
        
        // VULNERABILITY: This returns true for empty password!
        boolean result = emptyStorage.verify("");
        
        // This assertion PASSES, proving the vulnerability exists
        assertTrue("VULNERABILITY: Empty passwords are accepted!", result);
        
        // Document the vulnerable code location
        System.out.println("VULNERABILITY FOUND: CryptPasswordStorage.java lines 155-156 accept empty passwords");
    }

    /**
     * CRITICAL VULNERABILITY: Blank/whitespace passwords might be accepted
     */
    @Test
    public void testBlankPasswordAcceptance_VULNERABILITY() throws NoSuchAlgorithmException {
        // Test various blank password scenarios
        String[] blankPasswords = {"", " ", "  ", "\t", "\n", "   \t\n   "};
        
        for (String blankPass : blankPasswords) {
            try {
                CryptPasswordStorage storage = new CryptPasswordStorage(blankPass.trim());
                boolean result = storage.verify(blankPass.trim());
                
                if (result && blankPass.trim().isEmpty()) {
                    System.out.println("VULNERABILITY: Blank password '" + blankPass + "' accepted!");
                }
            } catch (Exception e) {
                // Some blank passwords might cause exceptions, which is good
            }
        }
    }

    /**
     * CRITICAL VULNERABILITY: MD5 password hashing is supported
     * MD5 has been cryptographically broken since 2004
     */
    @Test
    public void testMD5PasswordStorage_VULNERABILITY() throws NoSuchAlgorithmException {
        String testPassword = "testPassword123";
        String salt = "randomsalt";
        
        // VULNERABILITY: MD5 is a valid option
        CryptPasswordStorage md5Storage = new CryptPasswordStorage(testPassword, salt, CryptType.MD5);
        
        // Verify MD5 is actually being used
        assertEquals("VULNERABILITY: MD5 hashing is supported!", CryptType.MD5, md5Storage.getType());
        
        // Verify MD5 password verification works
        assertTrue("MD5 passwords can be verified", md5Storage.verify(testPassword));
        
        System.out.println("VULNERABILITY FOUND: CryptType.java line 24 supports MD5 hashing");
    }

    /**
     * CRITICAL VULNERABILITY: Plaintext password storage is supported
     */
    @Test
    public void testPlainTextPasswordStorage_VULNERABILITY() throws NoSuchAlgorithmException {
        String testPassword = "supersecretpassword";
        String salt = "somesalt";
        
        // VULNERABILITY: PLAIN is a valid option
        CryptPasswordStorage plainStorage = new CryptPasswordStorage(testPassword, salt, CryptType.PLAIN);
        
        // Verify PLAIN is being used
        assertEquals("VULNERABILITY: Plaintext storage is supported!", CryptType.PLAIN, plainStorage.getType());
        
        // Check if password is actually stored in plaintext
        String storageString = plainStorage.toString();
        assertTrue("Password stored in plaintext!", storageString.contains(testPassword));
        
        System.out.println("VULNERABILITY FOUND: CryptType.java line 27 supports plaintext storage");
        System.out.println("Plaintext storage format: " + storageString);
    }

    /**
     * Test null password handling
     */
    @Test
    public void testNullPasswordHandling() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("validPassword", "salt");
        
        try {
            boolean result = storage.verify(null);
            assertFalse("Null password should not verify", result);
        } catch (NullPointerException e) {
            // NPE is acceptable for null input
        }
    }

    /**
     * Test weak passwords are accepted (no complexity requirements)
     */
    @Test
    public void testWeakPasswordsAccepted_VULNERABILITY() throws NoSuchAlgorithmException {
        String[] weakPasswords = {
            "a",           // Single character
            "12",          // Two digits
            "123",         // Sequential numbers  
            "abc",         // Sequential letters
            "password",    // Common word
            "12345678",    // Sequential digits
            "qwerty",      // Keyboard pattern
            "        "     // Only spaces
        };
        
        for (String weakPass : weakPasswords) {
            try {
                CryptPasswordStorage storage = new CryptPasswordStorage(weakPass, "salt");
                assertTrue("Weak password '" + weakPass + "' is accepted", storage.verify(weakPass));
                System.out.println("VULNERABILITY: Weak password accepted: " + weakPass);
            } catch (Exception e) {
                // Some might fail, which is good
            }
        }
    }

    // ========== EXPECTED SECURE BEHAVIOR TESTS - These should FAIL initially ==========

    /**
     * Test that empty passwords SHOULD be rejected (will fail showing vulnerability)
     */
    @Test
    public void testEmptyPasswordRejection_EXPECTED() {
        try {
            CryptPasswordStorage storage = new CryptPasswordStorage("");
            boolean result = storage.verify("");
            
            // This SHOULD be false but will be true due to vulnerability
            assertFalse("Empty passwords SHOULD be rejected", result);
        } catch (Exception e) {
            // Exception is acceptable for invalid input
        }
    }

    /**
     * Test that short passwords SHOULD be rejected
     */
    @Test
    public void testShortPasswordRejection_EXPECTED() {
        String[] shortPasswords = {"a", "ab", "abc", "1234", "12345", "123456", "1234567"};
        
        for (String shortPass : shortPasswords) {
            try {
                CryptPasswordStorage storage = new CryptPasswordStorage(shortPass, "salt");
                // In secure implementation, this should throw exception or return false
                fail("Short password '" + shortPass + "' SHOULD have been rejected");
            } catch (IllegalArgumentException e) {
                // Expected behavior - short passwords rejected
            } catch (Exception e) {
                // Other exceptions acceptable
            }
        }
    }

    /**
     * Test password storage string format and salt extraction
     */
    @Test
    public void testPasswordStorageFormat() throws NoSuchAlgorithmException {
        String password = "testPassword";
        String salt = "customSalt123";
        
        CryptPasswordStorage storage = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        
        // Test salt extraction
        assertEquals("Salt should be preserved", salt, storage.getSalt());
        
        // Test storage format
        String storageString = storage.toString();
        assertTrue("Storage should contain type identifier", storageString.contains("$6$"));
        assertTrue("Storage should contain salt", storageString.contains(salt));
        assertFalse("Storage should NOT contain plaintext password", storageString.contains(password));
    }

    /**
     * Test all supported CryptTypes
     */
    @Test  
    public void testAllCryptTypes() throws NoSuchAlgorithmException {
        String password = "testPassword123";
        String salt = "testSalt";
        
        CryptType[] types = CryptType.values();
        
        for (CryptType type : types) {
            CryptPasswordStorage storage = new CryptPasswordStorage(password, salt, type);
            assertEquals("Type should be set correctly", type, storage.getType());
            assertTrue("Password should verify with correct type", storage.verify(password));
            assertFalse("Wrong password should not verify", storage.verify("wrongPassword"));
            
            System.out.println("CryptType " + type + " with ID " + type.getId() + " is functional");
        }
    }

    /**
     * Test random salt generation
     */
    @Test
    public void testRandomSaltGeneration() throws NoSuchAlgorithmException {
        String password = "testPassword";
        
        // Create storage with null salt (should generate random)
        CryptPasswordStorage storage1 = new CryptPasswordStorage(password, null);
        CryptPasswordStorage storage2 = new CryptPasswordStorage(password, null);
        
        // Salts should be different
        assertNotEquals("Random salts should be unique", storage1.getSalt(), storage2.getSalt());
        
        // Both should still verify the password
        assertTrue("Password should verify with random salt", storage1.verify(password));
        assertTrue("Password should verify with different random salt", storage2.verify(password));
    }

    /**
     * Test empty salt rejection
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEmptySaltRejection() throws NoSuchAlgorithmException {
        // Empty salt should be rejected
        new CryptPasswordStorage("password", "");
    }

    /**
     * Test password comparison between different storage instances
     */
    @Test
    public void testPasswordComparison() throws NoSuchAlgorithmException {
        String password = "samePassword";
        String salt = "sameSalt";
        
        CryptPasswordStorage storage1 = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        CryptPasswordStorage storage2 = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        
        // Same password and salt should produce same storage
        assertEquals("Same password/salt should produce equal storage", storage1, storage2);
        
        // Different passwords should not match
        CryptPasswordStorage storage3 = new CryptPasswordStorage("differentPassword", salt, CryptType.SHA512);
        assertNotEquals("Different passwords should not match", storage1, storage3);
    }

    /**
     * Test timing attack resistance (basic check)
     */
    @Test
    public void testTimingAttackResistance() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("correctPassword", "salt");
        
        // Time verification of correct vs incorrect passwords
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
        
        // Times should be similar (not a perfect test but basic check)
        double ratio = (double) timeCorrect / timeIncorrect;
        assertTrue("Timing should be similar to prevent timing attacks", ratio > 0.5 && ratio < 2.0);
    }

    /**
     * Test CryptType default selection
     */
    @Test
    public void testDefaultCryptType() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt", null);
        
        // Default should be SHA512 (most secure option)
        assertEquals("Default should be SHA512", CryptType.SHA512, storage.getType());
        assertEquals("Default from enum should match", CryptType.getDefault(), storage.getType());
    }

    /**
     * Test invalid storage string handling
     */
    @Test
    public void testInvalidStorageString() {
        try {
            CryptPasswordStorage storage = new CryptPasswordStorage("$invalid$format$");
            storage.verify("anyPassword");
            fail("Invalid storage format should cause error");
        } catch (Exception e) {
            // Expected - invalid format should fail
        }
    }

    /**
     * Test storage string reconstruction
     */
    @Test
    public void testStorageStringReconstruction() throws NoSuchAlgorithmException {
        String password = "testPassword";
        String salt = "testSalt";
        
        // Create initial storage
        CryptPasswordStorage original = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        String storageString = original.toString();
        
        // Reconstruct from string
        CryptPasswordStorage reconstructed = new CryptPasswordStorage(storageString);
        
        // Should verify same password
        assertTrue("Reconstructed storage should verify original password", reconstructed.verify(password));
        assertFalse("Reconstructed storage should reject wrong password", reconstructed.verify("wrongPassword"));
        assertEquals("Types should match", original.getType(), reconstructed.getType());
        assertEquals("Salts should match", original.getSalt(), reconstructed.getSalt());
    }
}
