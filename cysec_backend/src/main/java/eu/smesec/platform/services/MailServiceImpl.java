package eu.smesec.platform.services;

import eu.smesec.bridge.generated.User;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import eu.smesec.platform.config.Config;
import eu.smesec.platform.config.CysecConfig;
import org.glassfish.jersey.logging.LoggingFeature;

/**
 * An implementation of the MailService interface.
 * @see eu.smesec.platform.services.MailService
 */
@Singleton
public class MailServiceImpl implements MailService {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private static final String CONFIG_MAIL_SENDER_ADDRESS = "cysec_mail_sender_address";
  private static final String DEFAULT_MAIL_SENDER_ADDRESS = "no-reply@example.com";

  /**
   * Sends an email to the local SMTP server.
   * @param recipient The user to receive the email.
   * @param cc A carbon copy receiver.
   * @param bcc A blind carbon copy receiver.
   * @param subject The email subject line.
   * @param body The content of the email.
   */
  @Override
  public void sendMail(User recipient, String cc, String bcc, String subject, String body) {
    final Config config = CysecConfig.getDefault();
    final String mailSender = config.getStringValue(null, CONFIG_MAIL_SENDER_ADDRESS);

    // skip sending mail when no sender address is configured
    if (DEFAULT_MAIL_SENDER_ADDRESS.equals(mailSender)) {
      logger.warning("No configuration for '" + CONFIG_MAIL_SENDER_ADDRESS + "' found");
      return;
    }

    if (recipient == null || recipient.getEmail() == null || recipient.getEmail().equals("")) {
      throw new IllegalArgumentException();
    }

    final Properties props = new Properties();
    props.put("mail.smtp.host", "localhost");
    props.put("mail.smtp.port", "25");
    props.put("mail.smtp.quitwait", "false");
    props.put("mail.debug", "true");
    final Session session = Session.getInstance(props, null);

    try {
      final MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(mailSender));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient.getEmail(), false));
      if (cc != null && !cc.isEmpty()) {
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
      }
      msg.setSubject(subject);
      msg.setText(body, "utf-8");
      msg.setSentDate(new Date());

      Transport.send(msg);
      logger.info("Send mail to: " + recipient.getEmail());
    } catch (MessagingException e) {
      logger.log(Level.WARNING, "Something went wrong sending the email", e);
    }
  }

  /**
   * Method overloading for sendMail, allows to send email to multiple receivers at once.
   * @param recipients A list of users who will receive the email.
   * @param cc A carbon copy receiver.
   * @param bcc A blind carbon copy receiver.
   * @param subject The email subject line.
   * @param body The content of the email.
   */
  @Override
  public void sendMail(List<User> recipients, String cc, String bcc, String subject, String body) {
    for (User user : recipients) {
      sendMail(user, cc, bcc, subject, body);
    }
  }
}
