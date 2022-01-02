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

import eu.smesec.cysec.platform.core.config.Config;
import eu.smesec.cysec.platform.core.config.CysecConfig;
import org.glassfish.jersey.logging.LoggingFeature;

import java.util.logging.Logger;

/**
 * Provides a {@link MailConfig} based on the {@link CysecConfig}
 *
 * @author matthiasluppi
 */
public class MailConfigProvider {

    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    // mail-specific configuration keys referenced in 'cysec.cfgresources'
    private static final String CONFIG_MAIL_SMTP_HOST = "cysec_mail_smtp_host";
    private static final String CONFIG_MAIL_SMTP_PORT = "cysec_mail_smtp_port";
    private static final String CONFIG_MAIL_SENDER_NAME = "cysec_mail_sender_name";
    private static final String CONFIG_MAIL_SENDER_ADDRESS = "cysec_mail_sender_address";
    private static final String DEFAULT_MAIL_SENDER_ADDRESS = "no-reply@example.com"; // must match default in 'cysec.cfgresources'

    public static MailConfig getMailConfig() {
        final Config config = CysecConfig.getDefault();
        final String mailSmtpHost = config.getStringValue(null, CONFIG_MAIL_SMTP_HOST);
        final String mailSmtpPort = config.getStringValue(null, CONFIG_MAIL_SMTP_PORT);
        final String mailSenderName = config.getStringValue(null, CONFIG_MAIL_SENDER_NAME);
        final String mailSenderAddress = config.getStringValue(null, CONFIG_MAIL_SENDER_ADDRESS);

        final MailConfig mailConfig = new MailConfig();
        mailConfig.setMailSmtpHost(mailSmtpHost);
        mailConfig.setMailSmtpPort(mailSmtpPort);

        if (mailSenderName != null && !mailSenderName.trim().isEmpty()) {
            mailConfig.setMailSenderName(mailSenderName);
        }

        if (DEFAULT_MAIL_SENDER_ADDRESS.equals(mailSenderAddress)) {
            logger.warning("No configuration for '" + CONFIG_MAIL_SENDER_ADDRESS + "' found");
            throw new IllegalStateException();
        } else {
            mailConfig.setMailSenderAddress(mailSenderAddress);
        }

        return mailConfig;
    }

    private MailConfigProvider() {
    }
}
