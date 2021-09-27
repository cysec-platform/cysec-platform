/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.bridge.generated.User;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.glassfish.jersey.logging.LoggingFeature;

/**
 * An implementation of the MailService interface.
 * @see MailService
 */
@Singleton
public class MailServiceImpl implements MailService {

  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  /**
   * Sends an email to the local SMTP server.
   * @param recipient The user to receive the email.
   * @param cc Carbon copy recipients as comma separated address strings.
   * @param bcc Blind carbon copy recipients as comma separated address strings.
   * @param subject The email subject line.
   * @param body The content of the email.
   */
  @Override
  public void sendMail(User recipient, String cc, String bcc, String subject, String body) {

    if (recipient == null || recipient.getEmail() == null || recipient.getEmail().equals("")) {
      throw new IllegalArgumentException("Empty recipient");
    }

    try {
      final MailConfig mailConfig = MailConfigProvider.getMailConfig();

      final Properties props = new Properties();
      props.put("mail.smtp.host", mailConfig.getMailSmtpHost());
      props.put("mail.smtp.port", mailConfig.getMailSmtpPort());
      props.put("mail.smtp.quitwait", "false");
      props.put("mail.debug", "true");
      final Session session = Session.getInstance(props, null);

      final MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(mailConfig.getMailSenderAddress(), mailConfig.getMailSenderName()));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient.getEmail(), false));
      if (cc != null && !cc.isEmpty()) {
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
      }
      if (bcc != null && !bcc.isEmpty()) {
        msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
      }
      msg.setSubject(subject);
      msg.setText(body, "utf-8");
      msg.setSentDate(new Date());

      Transport.send(msg);
      logger.info("Send mail to: " + recipient.getEmail());
    } catch (Exception e) {
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
