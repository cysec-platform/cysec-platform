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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class TestConfiguration implements BeforeEachCallback, AfterEachCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(TestConfiguration.class);
    private static final AtomicLong testCounter = new AtomicLong(0);
    
    public static final String TEST_TEMP_DIR = "target/test-temp";
    public static final String TEST_DATA_DIR = "src/test/resources/test-data";
    public static final String TEST_CONFIG_FILE = "src/test/resources/test.properties";
    
    public static final int DEFAULT_TEST_PORT = 8080;
    public static final String DEFAULT_TEST_HOST = "localhost";
    public static final String DEFAULT_TEST_CONTEXT = "/cysec";
    
    public static final String TEST_USER = "testuser";
    public static final String TEST_PASSWORD = "testpass123";
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    
    private static Properties testProperties;
    private static boolean initialized = false;
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        initializeTestEnvironment();
        logger.debug("Test starting: {}", context.getDisplayName());
    }
    
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        cleanupTestArtifacts();
        logger.debug("Test completed: {}", context.getDisplayName());
    }
    
    public static void initializeTestEnvironment() throws IOException {
        if (!initialized) {
            loadTestProperties();
            createTestDirectories();
            setupTestData();
            initialized = true;
            logger.info("Test environment initialized");
        }
    }
    
    private static void loadTestProperties() throws IOException {
        testProperties = new Properties();
        Path configPath = Paths.get(TEST_CONFIG_FILE);
        
        if (Files.exists(configPath)) {
            testProperties.load(Files.newInputStream(configPath));
            logger.debug("Loaded test properties from {}", TEST_CONFIG_FILE);
        } else {
            setupDefaultProperties();
            logger.debug("Using default test properties");
        }
    }
    
    private static void setupDefaultProperties() {
        testProperties.setProperty("test.host", DEFAULT_TEST_HOST);
        testProperties.setProperty("test.port", String.valueOf(DEFAULT_TEST_PORT));
        testProperties.setProperty("test.context", DEFAULT_TEST_CONTEXT);
        testProperties.setProperty("test.user", TEST_USER);
        testProperties.setProperty("test.password", TEST_PASSWORD);
        testProperties.setProperty("test.admin.user", ADMIN_USER);
        testProperties.setProperty("test.admin.password", ADMIN_PASSWORD);
        testProperties.setProperty("test.timeout.seconds", "30");
        testProperties.setProperty("test.retry.count", "3");
    }
    
    private static void createTestDirectories() throws IOException {
        Path tempDir = Paths.get(TEST_TEMP_DIR);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
            logger.debug("Created test temp directory: {}", tempDir);
        }
        
        Path dataDir = Paths.get(TEST_DATA_DIR);
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
            logger.debug("Created test data directory: {}", dataDir);
        }
    }
    
    private static void setupTestData() {
        // Initialize test data if needed
        logger.debug("Test data setup completed");
    }
    
    private void cleanupTestArtifacts() {
        try {
            Path tempDir = Paths.get(TEST_TEMP_DIR);
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                    .filter(path -> path.toString().contains("test-" + testCounter.get()))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete test artifact: {}", path, e);
                        }
                    });
            }
        } catch (IOException e) {
            logger.warn("Failed to cleanup test artifacts", e);
        }
    }
    
    public static String getTestProperty(String key) {
        return testProperties.getProperty(key);
    }
    
    public static String getTestProperty(String key, String defaultValue) {
        return testProperties.getProperty(key, defaultValue);
    }
    
    public static int getTestPort() {
        return Integer.parseInt(getTestProperty("test.port", String.valueOf(DEFAULT_TEST_PORT)));
    }
    
    public static String getTestHost() {
        return getTestProperty("test.host", DEFAULT_TEST_HOST);
    }
    
    public static String getTestContext() {
        return getTestProperty("test.context", DEFAULT_TEST_CONTEXT);
    }
    
    public static String getBaseUrl() {
        return String.format("http://%s:%d%s", getTestHost(), getTestPort(), getTestContext());
    }
    
    public static String getTestUser() {
        return getTestProperty("test.user", TEST_USER);
    }
    
    public static String getTestPassword() {
        return getTestProperty("test.password", TEST_PASSWORD);
    }
    
    public static String getAdminUser() {
        return getTestProperty("test.admin.user", ADMIN_USER);
    }
    
    public static String getAdminPassword() {
        return getTestProperty("test.admin.password", ADMIN_PASSWORD);
    }
    
    public static int getTestTimeout() {
        return Integer.parseInt(getTestProperty("test.timeout.seconds", "30"));
    }
    
    public static int getRetryCount() {
        return Integer.parseInt(getTestProperty("test.retry.count", "3"));
    }
    
    public static long getNextTestId() {
        return testCounter.incrementAndGet();
    }
    
    public static Path getTempTestFile(String prefix, String suffix) throws IOException {
        Path tempDir = Paths.get(TEST_TEMP_DIR);
        String fileName = String.format("%s-test-%d-%s", prefix, getNextTestId(), suffix);
        return Files.createFile(tempDir.resolve(fileName));
    }
}
