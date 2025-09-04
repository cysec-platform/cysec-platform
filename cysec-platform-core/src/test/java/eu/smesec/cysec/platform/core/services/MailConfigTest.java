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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MailConfigTest {

    private MailConfig mailConfig;

    @BeforeEach
    void setUp() {
        mailConfig = new MailConfig();
    }

    @Test
    void gettersAndSetters_shouldWorkCorrectly() {
        String host = "smtp.example.com";
        String port = "587";
        String senderName = "Test Sender";
        String senderAddress = "test@example.com";

        mailConfig.setMailSmtpHost(host);
        mailConfig.setMailSmtpPort(port);
        mailConfig.setMailSenderName(senderName);
        mailConfig.setMailSenderAddress(senderAddress);

        assertThat(mailConfig.getMailSmtpHost()).isEqualTo(host);
        assertThat(mailConfig.getMailSmtpPort()).isEqualTo(port);
        assertThat(mailConfig.getMailSenderName()).isEqualTo(senderName);
        assertThat(mailConfig.getMailSenderAddress()).isEqualTo(senderAddress);
    }

    @Test
    void constructor_shouldCreateEmptyConfig() {
        assertThat(mailConfig.getMailSmtpHost()).isNull();
        assertThat(mailConfig.getMailSmtpPort()).isNull();
        assertThat(mailConfig.getMailSenderName()).isNull();
        assertThat(mailConfig.getMailSenderAddress()).isNull();
    }

    @Test
    void setters_shouldAcceptNullValues() {
        mailConfig.setMailSmtpHost(null);
        mailConfig.setMailSmtpPort(null);
        mailConfig.setMailSenderName(null);
        mailConfig.setMailSenderAddress(null);

        assertThat(mailConfig.getMailSmtpHost()).isNull();
        assertThat(mailConfig.getMailSmtpPort()).isNull();
        assertThat(mailConfig.getMailSenderName()).isNull();
        assertThat(mailConfig.getMailSenderAddress()).isNull();
    }

    @Test
    void setters_shouldAcceptEmptyValues() {
        mailConfig.setMailSmtpHost("");
        mailConfig.setMailSmtpPort("");
        mailConfig.setMailSenderName("");
        mailConfig.setMailSenderAddress("");

        assertThat(mailConfig.getMailSmtpHost()).isEmpty();
        assertThat(mailConfig.getMailSmtpPort()).isEmpty();
        assertThat(mailConfig.getMailSenderName()).isEmpty();
        assertThat(mailConfig.getMailSenderAddress()).isEmpty();
    }

    @Test
    void setters_shouldHandleSpecialCharacters() {
        String specialHost = "smtp.täst.com";
        String specialName = "Müller, Hans";
        String specialAddress = "hans.müller@täst.com";

        mailConfig.setMailSmtpHost(specialHost);
        mailConfig.setMailSenderName(specialName);
        mailConfig.setMailSenderAddress(specialAddress);

        assertThat(mailConfig.getMailSmtpHost()).isEqualTo(specialHost);
        assertThat(mailConfig.getMailSenderName()).isEqualTo(specialName);
        assertThat(mailConfig.getMailSenderAddress()).isEqualTo(specialAddress);
    }
}
