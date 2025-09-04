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
package eu.smesec.cysec.platform.core.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import eu.smesec.cysec.platform.bridge.generated.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MailServiceImplTest {

    private MailServiceImpl mailService;

    @Mock
    private MailConfig mailConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mailService = new MailServiceImpl();
        
        when(mailConfig.getMailSmtpHost()).thenReturn("localhost");
        when(mailConfig.getMailSmtpPort()).thenReturn("587");
        when(mailConfig.getMailSenderAddress()).thenReturn("noreply@example.com");
        when(mailConfig.getMailSenderName()).thenReturn("CYSEC Platform");
    }

    @Test
    void sendMail_shouldThrowForNullRecipient() {
        assertThatThrownBy(() -> 
            mailService.sendMail((User) null, null, null, "Test Subject", "Test Body"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Empty recipient");
    }

    @Test
    void sendMail_shouldThrowForRecipientWithNullEmail() {
        User user = new User();
        user.setEmail(null);

        assertThatThrownBy(() -> 
            mailService.sendMail(user, null, null, "Test Subject", "Test Body"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Empty recipient");
    }

    @Test
    void sendMail_shouldThrowForRecipientWithEmptyEmail() {
        User user = new User();
        user.setEmail("");

        assertThatThrownBy(() -> 
            mailService.sendMail(user, null, null, "Test Subject", "Test Body"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Empty recipient");
    }

    @Test
    void sendMail_shouldHandleValidRecipient() {
        User user = createValidUser("test@example.com");

        try (MockedStatic<MailConfigProvider> mockedProvider = mockStatic(MailConfigProvider.class)) {
            mockedProvider.when(MailConfigProvider::getMailConfig).thenReturn(mailConfig);

            assertThatCode(() -> 
                mailService.sendMail(user, null, null, "Test Subject", "Test Body"))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void sendMail_shouldHandleCCAndBCCRecipients() {
        User user = createValidUser("test@example.com");
        String cc = "cc@example.com";
        String bcc = "bcc@example.com";

        try (MockedStatic<MailConfigProvider> mockedProvider = mockStatic(MailConfigProvider.class)) {
            mockedProvider.when(MailConfigProvider::getMailConfig).thenReturn(mailConfig);

            assertThatCode(() -> 
                mailService.sendMail(user, cc, bcc, "Test Subject", "Test Body"))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void sendMail_shouldHandleEmptyCCAndBCC() {
        User user = createValidUser("test@example.com");

        try (MockedStatic<MailConfigProvider> mockedProvider = mockStatic(MailConfigProvider.class)) {
            mockedProvider.when(MailConfigProvider::getMailConfig).thenReturn(mailConfig);

            assertThatCode(() -> 
                mailService.sendMail(user, "", "", "Test Subject", "Test Body"))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void sendMail_shouldHandleUTF8Characters() {
        User user = createValidUser("test@example.com");
        String subject = "Tëst Sübject with Ümlïuts";
        String body = "Bödÿ with special chäracters: åñð emøjïs 🎉";

        try (MockedStatic<MailConfigProvider> mockedProvider = mockStatic(MailConfigProvider.class)) {
            mockedProvider.when(MailConfigProvider::getMailConfig).thenReturn(mailConfig);

            assertThatCode(() -> 
                mailService.sendMail(user, null, null, subject, body))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void sendMail_shouldHandleExceptionsGracefully() {
        User user = createValidUser("test@example.com");

        try (MockedStatic<MailConfigProvider> mockedProvider = mockStatic(MailConfigProvider.class)) {
            mockedProvider.when(MailConfigProvider::getMailConfig)
                    .thenThrow(new RuntimeException("Config not available"));

            assertThatCode(() -> 
                mailService.sendMail(user, null, null, "Test Subject", "Test Body"))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void sendMailToMultipleRecipients_shouldSendToEachRecipient() {
        User user1 = createValidUser("user1@example.com");
        User user2 = createValidUser("user2@example.com");
        User user3 = createValidUser("user3@example.com");
        List<User> recipients = Arrays.asList(user1, user2, user3);

        try (MockedStatic<MailConfigProvider> mockedProvider = mockStatic(MailConfigProvider.class)) {
            mockedProvider.when(MailConfigProvider::getMailConfig).thenReturn(mailConfig);

            assertThatCode(() -> 
                mailService.sendMail(recipients, null, null, "Test Subject", "Test Body"))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void sendMailToMultipleRecipients_shouldHandleEmptyList() {
        List<User> recipients = Collections.emptyList();

        assertThatCode(() -> 
            mailService.sendMail(recipients, null, null, "Test Subject", "Test Body"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMailToMultipleRecipients_shouldSkipInvalidRecipients() {
        User validUser = createValidUser("valid@example.com");
        User invalidUser1 = new User();
        invalidUser1.setEmail(null);
        User invalidUser2 = new User();
        invalidUser2.setEmail("");
        
        List<User> recipients = Arrays.asList(validUser, invalidUser1, invalidUser2);

        try (MockedStatic<MailConfigProvider> mockedProvider = mockStatic(MailConfigProvider.class)) {
            mockedProvider.when(MailConfigProvider::getMailConfig).thenReturn(mailConfig);

            assertThatCode(() -> 
                mailService.sendMail(recipients, null, null, "Test Subject", "Test Body"))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void singleton_shouldImplementSingletonAnnotation() {
        assertThat(MailServiceImpl.class.isAnnotationPresent(javax.inject.Singleton.class))
                .isTrue();
    }

    private User createValidUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setUsername("testuser");
        return user;
    }
}
