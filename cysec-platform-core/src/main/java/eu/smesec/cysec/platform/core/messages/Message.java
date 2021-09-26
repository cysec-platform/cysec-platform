package eu.smesec.cysec.platform.core.messages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * GNU gettext message class. Used to handle the message strings for *.jsp files. All classes in
 * this package will be scanned for {@link I18n#tr(String)} and {@link I18n#trn(String, String,
 * long)} calls.
 */
public abstract class Message {
  protected final I18n i18n;
  protected final Map<String, String> messages;

  public Message(Locale locale) {
    i18n = I18nFactory.getI18n(getClass(), "Messages", locale != null ? locale : Locale.ENGLISH);
    messages = new HashMap<>();
  }

  /**
   * Returns all messages for the *.jsp file.
   *
   * @return dictionary
   */
  public Map<String, String> getMessages() {
    return messages;
  }
}
