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
