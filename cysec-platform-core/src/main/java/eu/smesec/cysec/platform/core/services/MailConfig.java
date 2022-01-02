/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

public class MailConfig {

    private String mailSmtpHost;
    private String mailSmtpPort;
    private String mailSenderName;
    private String mailSenderAddress;

    public String getMailSmtpHost() {
        return mailSmtpHost;
    }

    public void setMailSmtpHost(final String mailSmtpHost) {
        this.mailSmtpHost = mailSmtpHost;
    }

    public String getMailSmtpPort() {
        return mailSmtpPort;
    }

    public void setMailSmtpPort(final String mailSmtpPort) {
        this.mailSmtpPort = mailSmtpPort;
    }

    public String getMailSenderName() {
        return mailSenderName;
    }

    public void setMailSenderName(final String mailSenderName) {
        this.mailSenderName = mailSenderName;
    }

    public String getMailSenderAddress() {
        return mailSenderAddress;
    }

    public void setMailSenderAddress(final String mailSenderAddress) {
        this.mailSenderAddress = mailSenderAddress;
    }
}
