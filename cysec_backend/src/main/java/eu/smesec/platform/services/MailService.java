package eu.smesec.platform.services;

import eu.smesec.bridge.generated.User;
import java.util.List;

/**
 * An abstraction layer for MailService to decouple concrete implementation.
 * It offers methods to send emails to the local MailService of a CySec instance.
 */
public interface MailService {

  void sendMail(User recipient, String cc, String bc, String subject, String body);

  void sendMail(List<User> recipient, String cc, String bc, String subject, String body);
}
