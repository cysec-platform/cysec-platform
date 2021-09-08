package eu.smesec.platform.services;

import eu.smesec.core.config.Config;
import eu.smesec.core.config.CysecConfig;
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
