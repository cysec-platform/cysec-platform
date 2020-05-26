package eu.smesec.platform.messages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.jersey.logging.LoggingFeature;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * <p>GNU gettext message class.</p>
 * <p>Used to handle the message strings for *.jsp files.</p>
 * <p>All classes in this package will be scanned for {@link I18n#tr(String)}
 * and {@link I18n#trn(String, String, long)} calls.</p>
 */
public abstract class Message {
  protected final I18n i18n;
  protected final Map<String, String> messages;

  public Message(Locale locale) {
    i18n = I18nFactory.getI18n(getClass(), "Messages", locale != null ? locale : Locale.ENGLISH);
    messages = new HashMap<>();
  }

  /**
   * <p>Returns all messages for the *.jsp file.</p>
   *
   * @return dictionary
   */
  public Map<String, String> getMessages() {
    return messages;
  }
}
