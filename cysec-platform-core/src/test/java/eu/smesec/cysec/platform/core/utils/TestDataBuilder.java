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

import eu.smesec.cysec.platform.core.auth.CryptPasswordStorage;
import eu.smesec.cysec.platform.core.auth.CryptType;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataBuilder {
    
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", 
        "Grace", "Henry", "Iris", "Jack", "Kate", "Liam", "Mia", "Noah"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
        "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson"
    };
    
    private static final String[] DOMAINS = {
        "test.com", "example.org", "demo.net", "sample.edu", "mock.gov"
    };
    
    private static final String[] COMPANY_NAMES = {
        "TechCorp", "InnoSoft", "DataSys", "CyberSec", "CloudTech", "DevOps Inc",
        "SecureNet", "InfoTech", "SysAdmin LLC", "NetGuard"
    };
    
    public static class UserBuilder {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String password;
        private boolean isActive = true;
        private boolean isAdmin = false;
        private LocalDateTime createdDate = LocalDateTime.now();
        
        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder randomUsername() {
            this.username = generateRandomUsername();
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder randomEmail() {
            this.email = generateRandomEmail();
            return this;
        }
        
        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public UserBuilder randomName() {
            this.firstName = randomElement(FIRST_NAMES);
            this.lastName = randomElement(LAST_NAMES);
            return this;
        }
        
        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UserBuilder randomPassword() {
            this.password = generateSecurePassword();
            return this;
        }
        
        public UserBuilder active(boolean active) {
            this.isActive = active;
            return this;
        }
        
        public UserBuilder admin(boolean admin) {
            this.isAdmin = admin;
            return this;
        }
        
        public UserBuilder createdDate(LocalDateTime date) {
            this.createdDate = date;
            return this;
        }
        
        public TestUser build() {
            if (username == null) username = generateRandomUsername();
            if (email == null) email = generateRandomEmail();
            if (firstName == null) firstName = randomElement(FIRST_NAMES);
            if (lastName == null) lastName = randomElement(LAST_NAMES);
            if (password == null) password = generateSecurePassword();
            
            return new TestUser(username, email, firstName, lastName, password, 
                              isActive, isAdmin, createdDate);
        }
    }
    
    public static class CompanyBuilder {
        private String name;
        private String domain;
        private String industry;
        private int employeeCount = 100;
        private String country = "Switzerland";
        
        public CompanyBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public CompanyBuilder randomName() {
            this.name = randomElement(COMPANY_NAMES);
            return this;
        }
        
        public CompanyBuilder domain(String domain) {
            this.domain = domain;
            return this;
        }
        
        public CompanyBuilder randomDomain() {
            this.domain = randomElement(DOMAINS);
            return this;
        }
        
        public CompanyBuilder industry(String industry) {
            this.industry = industry;
            return this;
        }
        
        public CompanyBuilder employeeCount(int count) {
            this.employeeCount = count;
            return this;
        }
        
        public CompanyBuilder country(String country) {
            this.country = country;
            return this;
        }
        
        public TestCompany build() {
            if (name == null) name = randomElement(COMPANY_NAMES);
            if (domain == null) domain = randomElement(DOMAINS);
            if (industry == null) industry = "Technology";
            
            return new TestCompany(name, domain, industry, employeeCount, country);
        }
    }
    
    public static UserBuilder user() {
        return new UserBuilder();
    }
    
    public static CompanyBuilder company() {
        return new CompanyBuilder();
    }
    
    public static List<TestUser> createMultipleUsers(int count) {
        List<TestUser> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(user().randomUsername().randomEmail().randomName().randomPassword().build());
        }
        return users;
    }
    
    public static TestUser createAdminUser() {
        return user()
            .username("admin")
            .email("admin@test.com")
            .firstName("Admin")
            .lastName("User")
            .password("admin123")
            .admin(true)
            .build();
    }
    
    public static TestUser createTestUser() {
        return user()
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .password("testpass123")
            .build();
    }
    
    public static CryptPasswordStorage createPasswordStorage(String password) throws NoSuchAlgorithmException {
        return new CryptPasswordStorage(password, generateRandomSalt(), CryptType.SHA512);
    }
    
    public static CryptPasswordStorage createWeakPasswordStorage(String password) throws NoSuchAlgorithmException {
        return new CryptPasswordStorage(password, "salt", CryptType.MD5);
    }
    
    public static Map<String, String> createFormData(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even");
        }
        
        Map<String, String> formData = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            formData.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return formData;
    }
    
    public static String generateCsrfToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    public static String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    public static String generateApiKey() {
        return "api-" + UUID.randomUUID().toString();
    }
    
    private static String generateRandomUsername() {
        return (randomElement(FIRST_NAMES) + randomElement(LAST_NAMES) + 
                ThreadLocalRandom.current().nextInt(1000, 9999)).toLowerCase();
    }
    
    private static String generateRandomEmail() {
        return generateRandomUsername() + "@" + randomElement(DOMAINS);
    }
    
    private static String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = ThreadLocalRandom.current();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    
    private static String generateRandomSalt() {
        return CryptPasswordStorage.getRandomHexString(16);
    }
    
    private static <T> T randomElement(T[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }
    
    public static class TestUser {
        public final String username;
        public final String email;
        public final String firstName;
        public final String lastName;
        public final String password;
        public final boolean isActive;
        public final boolean isAdmin;
        public final LocalDateTime createdDate;
        
        public TestUser(String username, String email, String firstName, String lastName,
                       String password, boolean isActive, boolean isAdmin, LocalDateTime createdDate) {
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.password = password;
            this.isActive = isActive;
            this.isAdmin = isAdmin;
            this.createdDate = createdDate;
        }
        
        public String getFullName() {
            return firstName + " " + lastName;
        }
        
        public String toJson() {
            return String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"isActive\":%b,\"isAdmin\":%b,\"createdDate\":\"%s\"}",
                username, email, firstName, lastName, isActive, isAdmin, 
                createdDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
        
        @Override
        public String toString() {
            return String.format("TestUser{username='%s', email='%s', fullName='%s', admin=%b}", 
                               username, email, getFullName(), isAdmin);
        }
    }
    
    public static class TestCompany {
        public final String name;
        public final String domain;
        public final String industry;
        public final int employeeCount;
        public final String country;
        
        public TestCompany(String name, String domain, String industry, int employeeCount, String country) {
            this.name = name;
            this.domain = domain;
            this.industry = industry;
            this.employeeCount = employeeCount;
            this.country = country;
        }
        
        @Override
        public String toString() {
            return String.format("TestCompany{name='%s', domain='%s', industry='%s', employees=%d, country='%s'}", 
                               name, domain, industry, employeeCount, country);
        }
    }
}
