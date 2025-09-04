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
 * Comprehensive tests for CryptPasswordStorage covering all functionality.
 */
public class CryptPasswordStorageComprehensiveTest {

    @Test
    public void testConstructorWithPasswordAndSalt() throws NoSuchAlgorithmException {
        String password = "myPassword123";
        String salt = "mySalt456";
        
        CryptPasswordStorage storage = new CryptPasswordStorage(password, salt);
        
        assertNotNull("Storage should be created", storage);
        assertEquals("Salt should be preserved", salt, storage.getSalt());
        assertTrue("Password should verify", storage.verify(password));
        assertFalse("Wrong password should not verify", storage.verify("wrongPassword"));
    }

    @Test
    public void testConstructorWithPasswordSaltAndType() throws NoSuchAlgorithmException {
        String password = "myPassword123";
        String salt = "mySalt456";
        
        for (CryptType type : CryptType.values()) {
            CryptPasswordStorage storage = new CryptPasswordStorage(password, salt, type);
            
            assertNotNull("Storage should be created for type " + type, storage);
            assertEquals("Type should match", type, storage.getType());
            assertEquals("Salt should be preserved", salt, storage.getSalt());
            assertTrue("Password should verify for type " + type, storage.verify(password));
        }
    }

    @Test
    public void testConstructorWithStorageString() throws NoSuchAlgorithmException {
        // Create original storage
        CryptPasswordStorage original = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
        String storageString = original.toString();
        
        // Reconstruct from string
        CryptPasswordStorage reconstructed = new CryptPasswordStorage(storageString);
        
        assertEquals("Type should match", original.getType(), reconstructed.getType());
        assertEquals("Salt should match", original.getSalt(), reconstructed.getSalt());
        assertEquals("Storage strings should match", storageString, reconstructed.toString());
    }

    @Test
    public void testSetPasswordWithNullSalt() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", null);
        
        assertNotNull("Storage should be created", storage);
        assertNotNull("Salt should be generated", storage.getSalt());
        assertEquals("Salt length should be 32", 32, storage.getSalt().length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPasswordWithEmptySalt() throws NoSuchAlgorithmException {
        new CryptPasswordStorage("password", "");
    }

    @Test
    public void testGetRandomHexStringDefaultLength() {
        String hex = CryptPasswordStorage.getRandomHexString();
        
        assertNotNull("Hex string should be generated", hex);
        assertEquals("Default length should be 32", 32, hex.length());
        assertTrue("Should be valid hex", hex.matches("[0-9a-f]+"));
    }

    @Test
    public void testGetRandomHexStringCustomLength() {
        int[] lengths = {8, 16, 24, 32, 64, 128};
        
        for (int length : lengths) {
            String hex = CryptPasswordStorage.getRandomHexString(length);
            
            assertNotNull("Hex string should be generated", hex);
            assertEquals("Length should be " + length, length, hex.length());
            assertTrue("Should be valid hex", hex.matches("[0-9a-f]+"));
        }
    }

    @Test
    public void testGetRandomHexStringUniqueness() {
        String hex1 = CryptPasswordStorage.getRandomHexString();
        String hex2 = CryptPasswordStorage.getRandomHexString();
        String hex3 = CryptPasswordStorage.getRandomHexString();
        
        assertNotEquals("Random strings should be unique", hex1, hex2);
        assertNotEquals("Random strings should be unique", hex2, hex3);
        assertNotEquals("Random strings should be unique", hex1, hex3);
    }

    @Test
    public void testEqualsMethod() throws NoSuchAlgorithmException {
        String password = "testPassword";
        String salt = "testSalt";
        
        CryptPasswordStorage storage1 = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        CryptPasswordStorage storage2 = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        CryptPasswordStorage storage3 = new CryptPasswordStorage("different", salt, CryptType.SHA512);
        CryptPasswordStorage storage4 = new CryptPasswordStorage(password, "diffSalt", CryptType.SHA512);
        CryptPasswordStorage storage5 = new CryptPasswordStorage(password, salt, CryptType.SHA256);
        
        // Test equality
        assertEquals("Same password/salt/type should be equal", storage1, storage2);
        assertEquals("Should be equal to itself", storage1, storage1);
        
        // Test inequality
        assertNotEquals("Different password should not be equal", storage1, storage3);
        assertNotEquals("Different salt should not be equal", storage1, storage4);
        assertNotEquals("Different type should not be equal", storage1, storage5);
        assertNotEquals("Should not equal null", storage1, null);
        assertNotEquals("Should not equal different type", storage1, "string");
    }

    @Test
    public void testHashCodeMethod() throws NoSuchAlgorithmException {
        String password = "testPassword";
        String salt = "testSalt";
        
        CryptPasswordStorage storage1 = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        CryptPasswordStorage storage2 = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        CryptPasswordStorage storage3 = new CryptPasswordStorage("different", salt, CryptType.SHA512);
        
        assertEquals("Same storage should have same hashCode", storage1.hashCode(), storage2.hashCode());
        assertNotEquals("Different storage should have different hashCode", storage1.hashCode(), storage3.hashCode());
    }

    @Test
    public void testToStringMethod() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
        String str = storage.toString();
        
        assertNotNull("toString should not return null", str);
        assertTrue("Should start with $", str.startsWith("$"));
        assertTrue("Should contain type ID", str.contains("$6$"));
        assertTrue("Should contain salt", str.contains("salt"));
        assertFalse("Should not contain plaintext password", str.contains("password"));
    }

    @Test
    public void testGetSaltWithInvalidStorage() {
        try {
            CryptPasswordStorage storage = new CryptPasswordStorage("$invalid$");
            storage.getSalt();
            fail("Should throw exception for invalid storage");
        } catch (IllegalArgumentException e) {
            assertTrue("Should indicate storage issue", e.getMessage().contains("storage"));
        } catch (NoSuchAlgorithmException e) {
            // Also acceptable
        }
    }

    @Test
    public void testGetTypeWithNullStorage() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt");
        // Manually set storage to null (using reflection would be better)
        storage.storage = null;
        
        assertNull("Type should be null for null storage", storage.getType());
    }

    @Test
    public void testGetTypeWithInvalidStorage() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt");
        storage.storage = "invalid";
        
        assertNull("Type should be null for invalid storage", storage.getType());
    }

    @Test
    public void testGetTypeWithShortStorage() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt");
        storage.storage = "$";
        
        assertNull("Type should be null for too short storage", storage.getType());
    }

    @Test
    public void testVerifyWithDifferentPasswords() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("correctPassword", "salt");
        
        // Test various incorrect passwords
        assertFalse("Should reject wrong password", storage.verify("wrongPassword"));
        assertFalse("Should reject similar password", storage.verify("correctPassword1"));
        assertFalse("Should reject substring", storage.verify("correct"));
        assertFalse("Should reject case difference", storage.verify("CORRECTPASSWORD"));
        assertFalse("Should reject with spaces", storage.verify(" correctPassword"));
        assertFalse("Should reject with spaces", storage.verify("correctPassword "));
    }

    @Test
    public void testVerifyWithSpecialCharacters() throws NoSuchAlgorithmException {
        String[] passwords = {
            "p@ssw0rd!",
            "päsśwörd",
            "パスワード",
            "🔒secure🔒",
            "tab\there",
            "new\nline",
            "quote\"test",
            "slash/test",
            "back\\slash"
        };
        
        for (String password : passwords) {
            CryptPasswordStorage storage = new CryptPasswordStorage(password, "salt");
            assertTrue("Should verify special password: " + password, storage.verify(password));
            assertFalse("Should reject wrong password", storage.verify("wrong"));
        }
    }

    @Test
    public void testVerifyWithLongPasswords() throws NoSuchAlgorithmException {
        // Test various password lengths
        StringBuilder longPassword = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPassword.append("a");
        }
        
        CryptPasswordStorage storage = new CryptPasswordStorage(longPassword.toString(), "salt");
        assertTrue("Should handle long password", storage.verify(longPassword.toString()));
        assertFalse("Should reject different long password", storage.verify(longPassword.toString() + "b"));
    }

    @Test
    public void testVerifyPerformance() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt", CryptType.SHA512);
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            storage.verify("password");
        }
        long duration = System.currentTimeMillis() - start;
        
        // Should complete 100 verifications reasonably quickly
        assertTrue("Verification should be reasonably fast", duration < 5000);
    }

    @Test
    public void testConstructorWithNullType() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt", null);
        
        // Should use default type
        assertEquals("Should use default type", CryptType.getDefault(), storage.getType());
    }

    @Test
    public void testVerifyWithNullPasswordException() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt");
        storage.storage = "$6$salt$hash";
        
        // Verify with null should handle gracefully
        try {
            boolean result = storage.verify(null);
            // Either false or exception is acceptable
            assertFalse("Should return false for null", result);
        } catch (NullPointerException e) {
            // This is also acceptable behavior
        }
    }

    @Test
    public void testStorageFormatForAllTypes() throws NoSuchAlgorithmException {
        String password = "testPassword";
        String salt = "testSalt";
        
        // Test each type has correct format
        CryptPasswordStorage md5 = new CryptPasswordStorage(password, salt, CryptType.MD5);
        assertTrue("MD5 should have correct format", md5.toString().contains("$1$"));
        
        CryptPasswordStorage sha256 = new CryptPasswordStorage(password, salt, CryptType.SHA256);
        assertTrue("SHA256 should have correct format", sha256.toString().contains("$5$"));
        
        CryptPasswordStorage sha512 = new CryptPasswordStorage(password, salt, CryptType.SHA512);
        assertTrue("SHA512 should have correct format", sha512.toString().contains("$6$"));
        
        CryptPasswordStorage plain = new CryptPasswordStorage(password, salt, CryptType.PLAIN);
        assertTrue("PLAIN should have correct format", plain.toString().contains("$99$"));
        assertTrue("PLAIN should contain password", plain.toString().contains(password));
    }

    @Test
    public void testMultiplePasswordChanges() throws NoSuchAlgorithmException {
        CryptPasswordStorage storage = new CryptPasswordStorage("initial", "salt");
        assertTrue("Should verify initial password", storage.verify("initial"));
        
        // Change password
        storage.setPassword("changed", "salt");
        assertFalse("Should not verify old password", storage.verify("initial"));
        assertTrue("Should verify new password", storage.verify("changed"));
        
        // Change again
        storage.setPassword("final", "newSalt");
        assertFalse("Should not verify previous password", storage.verify("changed"));
        assertTrue("Should verify final password", storage.verify("final"));
        assertEquals("Salt should be updated", "newSalt", storage.getSalt());
    }

    @Test
    public void testConcurrentPasswordVerification() throws Exception {
        final CryptPasswordStorage storage = new CryptPasswordStorage("password", "salt");
        final int threadCount = 10;
        final int iterationsPerThread = 100;
        
        Thread[] threads = new Thread[threadCount];
        final boolean[] results = new boolean[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    try {
                        results[index] = storage.verify("password");
                    } catch (Exception e) {
                        results[index] = false;
                    }
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }
        
        // All should succeed
        for (boolean result : results) {
            assertTrue("Concurrent verification should succeed", result);
        }
    }
}
