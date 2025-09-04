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
import static org.junit.Assert.*;

/**
 * Comprehensive tests for CryptType enum covering all functionality.
 */
public class CryptTypeComprehensiveTest {

    @Test
    public void testGetId() {
        assertEquals("MD5 should have ID 1", "1", CryptType.MD5.getId());
        assertEquals("SHA256 should have ID 5", "5", CryptType.SHA256.getId());
        assertEquals("SHA512 should have ID 6", "6", CryptType.SHA512.getId());
        assertEquals("PLAIN should have ID 99", "99", CryptType.PLAIN.getId());
    }

    @Test
    public void testGetDefault() {
        CryptType defaultType = CryptType.getDefault();
        assertNotNull("Default type should not be null", defaultType);
        assertEquals("Default should be SHA512", CryptType.SHA512, defaultType);
        
        // Verify default is not a weak algorithm
        assertNotEquals("Default should not be MD5", CryptType.MD5, defaultType);
        assertNotEquals("Default should not be PLAIN", CryptType.PLAIN, defaultType);
    }

    @Test
    public void testGetById_ValidIds() {
        assertEquals("ID 1 should return MD5", CryptType.MD5, CryptType.getById("1"));
        assertEquals("ID 5 should return SHA256", CryptType.SHA256, CryptType.getById("5"));
        assertEquals("ID 6 should return SHA512", CryptType.SHA512, CryptType.getById("6"));
        assertEquals("ID 99 should return PLAIN", CryptType.PLAIN, CryptType.getById("99"));
    }

    @Test
    public void testGetById_InvalidIds() {
        assertNull("Invalid ID should return null", CryptType.getById("0"));
        assertNull("Invalid ID should return null", CryptType.getById("2"));
        assertNull("Invalid ID should return null", CryptType.getById("100"));
        assertNull("Invalid ID should return null", CryptType.getById("abc"));
        assertNull("Empty string should return null", CryptType.getById(""));
        assertNull("Null should return null", CryptType.getById(null));
        assertNull("Negative ID should return null", CryptType.getById("-1"));
    }

    @Test
    public void testEnumValues() {
        CryptType[] types = CryptType.values();
        
        assertEquals("Should have 4 types", 4, types.length);
        
        // Verify all types are present
        boolean hasMD5 = false;
        boolean hasSHA256 = false;
        boolean hasSHA512 = false;
        boolean hasPLAIN = false;
        
        for (CryptType type : types) {
            if (type == CryptType.MD5) hasMD5 = true;
            if (type == CryptType.SHA256) hasSHA256 = true;
            if (type == CryptType.SHA512) hasSHA512 = true;
            if (type == CryptType.PLAIN) hasPLAIN = true;
        }
        
        assertTrue("Should have MD5", hasMD5);
        assertTrue("Should have SHA256", hasSHA256);
        assertTrue("Should have SHA512", hasSHA512);
        assertTrue("Should have PLAIN", hasPLAIN);
    }

    @Test
    public void testValueOf() {
        assertEquals("valueOf MD5", CryptType.MD5, CryptType.valueOf("MD5"));
        assertEquals("valueOf SHA256", CryptType.SHA256, CryptType.valueOf("SHA256"));
        assertEquals("valueOf SHA512", CryptType.SHA512, CryptType.valueOf("SHA512"));
        assertEquals("valueOf PLAIN", CryptType.PLAIN, CryptType.valueOf("PLAIN"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_Invalid() {
        CryptType.valueOf("INVALID");
    }

    @Test
    public void testEnumOrdinal() {
        assertEquals("MD5 ordinal", 0, CryptType.MD5.ordinal());
        assertEquals("SHA256 ordinal", 1, CryptType.SHA256.ordinal());
        assertEquals("SHA512 ordinal", 2, CryptType.SHA512.ordinal());
        assertEquals("PLAIN ordinal", 3, CryptType.PLAIN.ordinal());
    }

    @Test
    public void testEnumName() {
        assertEquals("MD5 name", "MD5", CryptType.MD5.name());
        assertEquals("SHA256 name", "SHA256", CryptType.SHA256.name());
        assertEquals("SHA512 name", "SHA512", CryptType.SHA512.name());
        assertEquals("PLAIN name", "PLAIN", CryptType.PLAIN.name());
    }

    @Test
    public void testToString() {
        // toString() returns the enum name by default
        assertEquals("MD5 toString", "MD5", CryptType.MD5.toString());
        assertEquals("SHA256 toString", "SHA256", CryptType.SHA256.toString());
        assertEquals("SHA512 toString", "SHA512", CryptType.SHA512.toString());
        assertEquals("PLAIN toString", "PLAIN", CryptType.PLAIN.toString());
    }

    @Test
    public void testEnumComparison() {
        assertTrue("Same enum should be equal", CryptType.SHA512 == CryptType.SHA512);
        assertFalse("Different enums should not be equal", CryptType.SHA512 == CryptType.SHA256);
        
        assertTrue("equals should work", CryptType.SHA512.equals(CryptType.SHA512));
        assertFalse("equals should detect difference", CryptType.SHA512.equals(CryptType.MD5));
        assertFalse("equals with null", CryptType.SHA512.equals(null));
        assertFalse("equals with different type", CryptType.SHA512.equals("SHA512"));
    }

    @Test
    public void testHashCode() {
        CryptType type1 = CryptType.SHA512;
        CryptType type2 = CryptType.SHA512;
        CryptType type3 = CryptType.MD5;
        
        assertEquals("Same enum should have same hashCode", type1.hashCode(), type2.hashCode());
        assertNotEquals("Different enums should have different hashCode", type1.hashCode(), type3.hashCode());
    }

    @Test
    public void testIdUniqueness() {
        String[] ids = new String[CryptType.values().length];
        int index = 0;
        
        for (CryptType type : CryptType.values()) {
            String id = type.getId();
            assertNotNull("ID should not be null for " + type, id);
            
            // Check for duplicates
            for (int i = 0; i < index; i++) {
                assertNotEquals("IDs should be unique: " + type + " has duplicate ID", ids[i], id);
            }
            ids[index++] = id;
        }
    }

    @Test
    public void testSecurityClassification() {
        // Document which algorithms are secure vs insecure
        for (CryptType type : CryptType.values()) {
            switch (type) {
                case SHA512:
                case SHA256:
                    System.out.println(type + " (ID: " + type.getId() + ") - SECURE");
                    break;
                case MD5:
                    System.out.println(type + " (ID: " + type.getId() + ") - INSECURE (broken since 2004)");
                    break;
                case PLAIN:
                    System.out.println(type + " (ID: " + type.getId() + ") - INSECURE (no encryption)");
                    break;
            }
        }
    }

    @Test
    public void testGetByIdPerformance() {
        // Test performance of getById method
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < 100000; i++) {
            CryptType.getById("1");
            CryptType.getById("5");
            CryptType.getById("6");
            CryptType.getById("99");
            CryptType.getById("invalid");
        }
        
        long duration = System.currentTimeMillis() - start;
        assertTrue("getById should be fast (< 1 second for 100000 calls)", duration < 1000);
        System.out.println("100000 getById calls completed in " + duration + "ms");
    }

    @Test
    public void testCryptTypeUsageInContext() {
        // Test how CryptType would be used in actual password storage
        for (CryptType type : CryptType.values()) {
            try {
                // This would be used in CryptPasswordStorage
                String cryptId = "$" + type.getId() + "$";
                assertTrue("Crypt ID should start with $", cryptId.startsWith("$"));
                assertTrue("Crypt ID should end with $", cryptId.endsWith("$"));
                assertTrue("Crypt ID should contain the type ID", cryptId.contains(type.getId()));
            } catch (Exception e) {
                fail("CryptType " + type + " should work in context: " + e.getMessage());
            }
        }
    }

    @Test
    public void testDefaultNotWeak() {
        CryptType defaultType = CryptType.getDefault();
        
        // Ensure default is not a weak algorithm
        assertNotEquals("Default should not be MD5 (weak)", CryptType.MD5, defaultType);
        assertNotEquals("Default should not be PLAIN (no encryption)", CryptType.PLAIN, defaultType);
        
        // Default should be one of the secure options
        assertTrue("Default should be a secure algorithm",
            defaultType == CryptType.SHA256 || defaultType == CryptType.SHA512);
    }

    @Test
    public void testEnumIteration() {
        int count = 0;
        for (CryptType type : CryptType.values()) {
            assertNotNull("Type should not be null", type);
            assertNotNull("Type ID should not be null", type.getId());
            count++;
        }
        assertEquals("Should iterate over all 4 types", 4, count);
    }

    @Test
    public void testCaseInsensitiveGetById() {
        // Current implementation is case-sensitive, but test current behavior
        assertNull("Uppercase should not match", CryptType.getById("1".toUpperCase()));
        
        // Test with leading/trailing spaces
        assertNull("Leading space should not match", CryptType.getById(" 1"));
        assertNull("Trailing space should not match", CryptType.getById("1 "));
        assertNull("Both spaces should not match", CryptType.getById(" 1 "));
    }
}
