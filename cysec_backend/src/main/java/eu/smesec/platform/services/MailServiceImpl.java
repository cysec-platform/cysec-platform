package eu.smesec.platform.services;

import com.sun.net.ssl.internal.ssl.Provider;
import eu.smesec.bridge.generated.User;
import java.security.Security;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.glassfish.jersey.logging.LoggingFeature;

/**
 * An implementation of the MailService interface.
 * @see eu.smesec.platform.services.MailService
 */
@Singleton
public class MailServiceImpl implements MailService {
  static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  private static final String MAIL_SENDER = "SMESEC-Framework-platform";
  private static final String MAIL_SUBJECT = "New user signed up";
  private static final String MAIL_BODY = "You have sucessfully signed up";
  private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

  /**
   * Sends an email to the local SMTP server.
   * @param recipient The user to receive the email.
   * @param cc A Carbon Copy receiver.
   * @param bc A Blind Copy receiver.
   * @param subject The email subject line.
   * @param body The content of the email.
   */
  @Override
  public void sendMail(User recipient, String cc, String bc, String subject, String body) {
    Security.addProvider(new Provider());

    if (recipient == null || recipient.equals("")) {
      throw new IllegalArgumentException();
    }

    // Get a Properties object
    Properties props = System.getProperties();
    props.setProperty("mail.smtps.host", "localhost");
    props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
    //important line!
    props.setProperty("mail.smtp.socketFactory.fallback", "true");
    props.setProperty("mail.smtp.port", "25");
    props.setProperty("mail.smtp.starttls.enable", "true");
    props.setProperty("mail.smtp.debug", "true");
    //props.setProperty("mail.smtp.socketFactory.port", "587");
    props.put("mail.smtps.quitwait", "false");

    Session session = Session.getInstance(props, null);
    final MimeMessage msg = new MimeMessage(session);

    try {
      // -- Set FROM and Recipients (CC && BC) to new MailMessage
      msg.setFrom(new InternetAddress(MAIL_SENDER));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient.getEmail(),
              false));
      if (cc.length() > 0) {
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
      }
      // Set remaining fields
      msg.setSubject(subject);
      msg.setText(body, "utf-8");
      msg.setSentDate(new Date());
      // Handle session and send mail
      Transport t = session.getTransport("smtp");
      t.connect("localhost", 25, null, null);
      t.sendMessage(msg, msg.getAllRecipients());
      t.close();
      logger.info("Send mail to: " + recipient.getEmail());
    } catch (MessagingException e) {
      e.printStackTrace();
      logger.warning("Something went wrong sending the email");
    }
  }

  /**
   * Method overloading for sendMail, allows to send email to multiple receivers at once.
   * @param recipients A list of users who will receive the email.
   * @param cc A Carbon Copy receiver.
   * @param bc A Blind Copy receiver.
   * @param subject The email subject line.
   * @param body The content of the email.
   */
  @Override
  public void sendMail(List<User> recipients, String cc, String bc, String subject, String body) {
    for (User user : recipients) {
      sendMail(user, cc, bc, subject, body);
    }
  }
}
